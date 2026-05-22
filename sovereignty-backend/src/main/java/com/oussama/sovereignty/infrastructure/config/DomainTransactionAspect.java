package com.oussama.sovereignty.infrastructure.config;

import com.oussama.sovereignty.application.common.DomainTransactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Aspect
@Component
@RequiredArgsConstructor
public class DomainTransactionAspect {

    private final PlatformTransactionManager transactionManager;

    @Around("@annotation(domainTransactional) || @within(domainTransactional)")
    public Object manageTransaction(ProceedingJoinPoint joinPoint, DomainTransactional domainTransactional) throws Throwable {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(joinPoint.getSignature().toShortString());

        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            Object result = joinPoint.proceed();
            transactionManager.commit(status);
            return result;
        } catch (Throwable ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }
}
