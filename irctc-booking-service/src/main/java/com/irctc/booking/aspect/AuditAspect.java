package com.irctc.booking.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.annotation.Auditable;
import com.irctc.booking.entity.AuditLog;
import com.irctc.booking.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Around("@annotation(com.irctc.booking.annotation.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        if (auditable == null) {
            return joinPoint.proceed();
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(auditable.entityType());
        auditLog.setAction(auditable.action());
        auditLog.setTimestamp(LocalDateTime.now());

        // Extract request information
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setHttpMethod(request.getMethod());
                auditLog.setRequestPath(request.getRequestURI());
                auditLog.setIpAddress(getClientIpAddress(request));
                
                // Extract user info from request (if available)
                String userId = request.getHeader("X-User-Id");
                String username = request.getHeader("X-Username");
                if (userId != null) {
                    auditLog.setUserId(userId);
                }
                if (username != null) {
                    auditLog.setUsername(username);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract request information for audit", e);
        }

        // Extract entity ID from method arguments
        Object[] args = joinPoint.getArgs();
        Long entityId = extractEntityId(args);
        if (entityId != null) {
            auditLog.setEntityId(entityId);
        }

        // Log request body if enabled
        if (auditable.logRequestBody() && args.length > 0) {
            try {
                Object requestBody = args[args.length - 1]; // Usually the last parameter
                if (requestBody != null && !isPrimitive(requestBody)) {
                    String bodyJson = serializeObject(requestBody);
                    auditLog.setRequestBody(bodyJson);
                }
            } catch (Exception e) {
                logger.warn("Failed to serialize request body for audit", e);
            }
        }

        // Execute the method and capture result/error
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            // Extract entity ID from result if not found in arguments
            if (auditLog.getEntityId() == null && result != null) {
                Long resultEntityId = extractEntityIdFromResult(result);
                if (resultEntityId != null) {
                    auditLog.setEntityId(resultEntityId);
                }
            }
            
            // Log response if enabled
            if (auditable.logResponseBody() && result != null) {
                try {
                    String responseJson = serializeObject(result);
                    auditLog.setResponseBody(responseJson);
                    auditLog.setResponseStatus(200); // Success
                } catch (Exception e) {
                    logger.warn("Failed to serialize response body for audit", e);
                }
            } else {
                auditLog.setResponseStatus(200);
            }
            
            // Store new values if this is an update
            if ("UPDATE".equals(auditable.action()) && result != null) {
                try {
                    String newValues = serializeObject(result);
                    auditLog.setNewValues(newValues);
                } catch (Exception e) {
                    logger.warn("Failed to serialize new values for audit", e);
                }
            }
            
            return result;
            
        } catch (Throwable throwable) {
            auditLog.setResponseStatus(500);
            auditLog.setErrorMessage(throwable.getMessage());
            auditLog.setAdditionalInfo("Exception: " + throwable.getClass().getName());
            
            throw throwable; // Re-throw the exception
        } finally {
            // Save audit log asynchronously to avoid blocking
            try {
                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                logger.error("Failed to save audit log", e);
            }
        }
    }

    private Long extractEntityId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            } else if (arg instanceof Number) {
                return ((Number) arg).longValue();
            } else if (arg != null) {
                // Try to find getId() method
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(arg);
                    if (id instanceof Long) {
                        return (Long) id;
                    } else if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                } catch (Exception ignored) {
                    // Not an entity with getId()
                }
            }
        }
        return null;
    }

    private Long extractEntityIdFromResult(Object result) {
        if (result instanceof Optional) {
            Optional<?> optional = (Optional<?>) result;
            if (optional.isPresent()) {
                return extractEntityIdFromResult(optional.get());
            }
        } else if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                } else if (id instanceof Number) {
                    return ((Number) id).longValue();
                }
            } catch (Exception ignored) {
                // Not an entity with getId()
            }
        }
        return null;
    }

    private String serializeObject(Object obj) {
        try {
            if (objectMapper != null) {
                return objectMapper.writeValueAsString(obj);
            } else {
                return obj.toString();
            }
        } catch (Exception e) {
            logger.warn("Failed to serialize object", e);
            return obj.toString();
        }
    }

    private boolean isPrimitive(Object obj) {
        return obj instanceof String ||
               obj instanceof Number ||
               obj instanceof Boolean ||
               obj.getClass().isPrimitive();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

