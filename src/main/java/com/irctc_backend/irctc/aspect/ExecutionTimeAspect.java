package com.irctc_backend.irctc.aspect;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP Aspect for measuring execution time of methods annotated with @ExecutionTime.
 * This aspect provides detailed timing information including method name, parameters,
 * and execution duration.
 */
@Aspect
@Component
public class ExecutionTimeAspect {
    
    private static final Logger logger = LogManager.getLogger(ExecutionTimeAspect.class);
    
    /**
     * Around advice that measures execution time of methods annotated with @ExecutionTime.
     * 
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@annotation(com.irctc_backend.irctc.annotation.ExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Get method information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        
        // Get annotation value if provided
        ExecutionTime executionTimeAnnotation = method.getAnnotation(ExecutionTime.class);
        String description = executionTimeAnnotation.value();
        String operationName = description.isEmpty() ? methodName : description;
        
        // Log start of execution
        logger.info("🚀 Starting execution of: {} in class: {}", operationName, className);
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Calculate execution time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Log successful completion with timing
            logger.info("✅ Completed: {} in class: {} - Execution time: {}ms", 
                       operationName, className, executionTime);
            
            // Log warning if execution time is high (more than 1 second)
            if (executionTime > 1000) {
                logger.warn("⚠️  Slow execution detected: {} in class: {} took {}ms", 
                           operationName, className, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            // Calculate execution time even for failed executions
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Log error with timing information
            logger.error("❌ Failed: {} in class: {} - Execution time: {}ms - Error: {}", 
                        operationName, className, executionTime, e.getMessage(), e);
            
            throw e;
        }
    }
    
    /**
     * Around advice that measures execution time of all REST controller methods.
     * This provides automatic timing for all API endpoints without requiring annotations.
     * 
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("execution(* com.irctc_backend.irctc.controller.*.*(..))")
    public Object measureControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Get method information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        
        // Get HTTP method and path if available
        String httpMethod = "";
        String path = "";
        
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)) {
            httpMethod = "GET";
            org.springframework.web.bind.annotation.GetMapping mapping = 
                method.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class);
            if (mapping.value().length > 0) {
                path = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)) {
            httpMethod = "POST";
            org.springframework.web.bind.annotation.PostMapping mapping = 
                method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);
            if (mapping.value().length > 0) {
                path = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)) {
            httpMethod = "PUT";
            org.springframework.web.bind.annotation.PutMapping mapping = 
                method.getAnnotation(org.springframework.web.bind.annotation.PutMapping.class);
            if (mapping.value().length > 0) {
                path = mapping.value()[0];
            }
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class)) {
            httpMethod = "DELETE";
            org.springframework.web.bind.annotation.DeleteMapping mapping = 
                method.getAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class);
            if (mapping.value().length > 0) {
                path = mapping.value()[0];
            }
        }
        
        String apiEndpoint = httpMethod.isEmpty() ? methodName : httpMethod + " " + path;
        
        // Log start of API execution
        logger.info("🌐 API Request: {} - {} in class: {}", apiEndpoint, methodName, className);
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Calculate execution time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Log successful completion with timing
            logger.info("✅ API Response: {} - {} in class: {} - Execution time: {}ms", 
                       apiEndpoint, methodName, className, executionTime);
            
            // Log warning if execution time is high (more than 2 seconds for APIs)
            if (executionTime > 2000) {
                logger.warn("⚠️  Slow API detected: {} - {} in class: {} took {}ms", 
                           apiEndpoint, methodName, className, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            // Calculate execution time even for failed executions
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Log error with timing information
            logger.error("❌ API Error: {} - {} in class: {} - Execution time: {}ms - Error: {}", 
                        apiEndpoint, methodName, className, executionTime, e.getMessage(), e);
            
            throw e;
        }
    }
}
