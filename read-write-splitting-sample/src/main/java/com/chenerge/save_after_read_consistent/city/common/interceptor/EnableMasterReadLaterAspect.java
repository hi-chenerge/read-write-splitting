package com.chenerge.save_after_read_consistent.city.common.interceptor;

import com.chenerge.save_after_read_consistent.city.common.session.CurrentUser;
import com.chenerge.save_after_read_consistent.city.common.session.CurrentUserReadMasterService;
import com.chenerge.save_after_read_consistent.city.common.session.SessionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class EnableMasterReadLaterAspect {
    @Autowired
    private CurrentUserReadMasterService currentUserReadMasterService;

    @Pointcut("@annotation(com.chenerge.save_after_read_consistent.city.common.interceptor.EnableMasterReadLater)")
    public void ann() {
    }

    @Around("ann() && @annotation(enableMasterReadLater)")
    public Object aroundRedisKey(ProceedingJoinPoint point, EnableMasterReadLater enableMasterReadLater) throws Throwable {
        CurrentUser session = SessionContext.getSession();
        if (session == null) {
            return point.proceed();
        }

        currentUserReadMasterService.setCnt(session.getToken(), enableMasterReadLater.readMasterCnt());

        return point.proceed();
    }
}
