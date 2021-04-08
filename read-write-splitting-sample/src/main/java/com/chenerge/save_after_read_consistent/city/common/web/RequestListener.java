package com.chenerge.save_after_read_consistent.city.common.web;

import com.chenerge.save_after_read_consistent.city.common.session.SessionContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

@ConditionalOnClass(ServletRequestListener.class)
@Component
public class RequestListener implements ServletRequestListener {
    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        // 当前request销毁后, 清空threadLocal信息, 防止内存泄露
        SessionContext.clear();
    }

    @SuppressWarnings("unused")
    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {

    }
}
