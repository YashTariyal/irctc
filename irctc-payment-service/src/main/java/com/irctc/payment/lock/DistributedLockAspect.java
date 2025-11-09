package com.irctc.payment.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

/**
 * AOP Aspect for Distributed Locking
 * 
 * Intercepts methods annotated with @DistributedLock and
 * automatically acquires/releases locks.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Aspect
@Component
public class DistributedLockAspect {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockAspect.class);
    private static final ExpressionParser parser = new SpelExpressionParser();
    
    @Autowired(required = false)
    private DistributedLockService lockService;
    
    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        if (lockService == null) {
            logger.warn("DistributedLockService not available, executing without lock");
            return joinPoint.proceed();
        }
        
        // Resolve lock key using SpEL
        String lockKey = resolveLockKey(distributedLock.key(), joinPoint);
        
        // Acquire lock
        DistributedLockService.LockHandle lockHandle = lockService.acquireLock(
            lockKey,
            distributedLock.timeout(),
            distributedLock.waitTime()
        );
        
        if (lockHandle == null) {
            String errorMsg = distributedLock.errorMessage() + ": " + lockKey;
            if (distributedLock.throwOnFailure()) {
                throw new RuntimeException(errorMsg);
            } else {
                logger.warn("⚠️  {}", errorMsg);
                return null; // or throw exception based on return type
            }
        }
        
        try {
            logger.debug("🔒 Executing with lock: {}", lockKey);
            return joinPoint.proceed();
        } finally {
            // Always release lock
            boolean released = lockService.releaseLock(lockHandle);
            if (!released) {
                logger.warn("⚠️  Failed to release lock: {}", lockKey);
            }
        }
    }
    
    private String resolveLockKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        try {
            EvaluationContext context = new StandardEvaluationContext();
            
            // Add method arguments
            String[] paramNames = getParameterNames(joinPoint);
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            // Parse and evaluate expression
            Expression expression = parser.parseExpression(keyExpression);
            return expression.getValue(context, String.class);
            
        } catch (Exception e) {
            logger.error("Error resolving lock key expression: {}", keyExpression, e);
            return keyExpression; // Return as-is if parsing fails
        }
    }
    
    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        // Get parameter names from method signature
        org.aspectj.lang.reflect.MethodSignature signature = 
            (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
        return signature.getParameterNames();
    }
}

