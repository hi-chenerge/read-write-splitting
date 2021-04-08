package com.chenerge.save_after_read_consistent.city.common.interceptor;

import com.chenerge.save_after_read_consistent.city.common.session.CurrentUser;
import com.chenerge.save_after_read_consistent.city.common.session.CurrentUserReadMasterService;
import com.chenerge.save_after_read_consistent.city.common.session.ReadMasterContext;
import com.chenerge.save_after_read_consistent.city.common.session.SessionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MasterReadIfNeededAspect {
    @Autowired
    private CurrentUserReadMasterService currentUserReadMasterService;

    @Pointcut("@annotation(com.chenerge.save_after_read_consistent.city.common.interceptor.MasterReadIfNeeded)")
    public void ann() {
    }

    @Around("ann() && @annotation(masterReadIfNeeded)")
    public Object aroundRedisKey(ProceedingJoinPoint point, MasterReadIfNeeded masterReadIfNeeded) throws Throwable {
        CurrentUser session = SessionContext.getSession();
        if (session == null) {
            return point.proceed();
        }

        int readMasterCnt = currentUserReadMasterService.getAndDecrementCnt(session.getToken());
        if(readMasterCnt >= 0){
            try {
                // 启用强制走主库, 后续 MasterOnlyMybatisInterceptor 会读这个标记
                ReadMasterContext.setOn();
                return point.proceed();
            } finally {
                // 防止线程泄露, 销毁TL变量
                ReadMasterContext.clear();
            }
        }

        return point.proceed();
    }
}
