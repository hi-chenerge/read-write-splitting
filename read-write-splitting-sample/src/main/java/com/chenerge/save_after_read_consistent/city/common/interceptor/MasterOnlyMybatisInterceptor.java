package com.chenerge.save_after_read_consistent.city.common.interceptor;

import com.chenerge.save_after_read_consistent.city.common.session.CurrentUser;
import com.chenerge.save_after_read_consistent.city.common.session.ReadMasterContext;
import com.chenerge.save_after_read_consistent.city.common.session.SessionContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.shardingsphere.api.hint.HintManager;

import java.util.Properties;


@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
public class MasterOnlyMybatisInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        CurrentUser session = SessionContext.getSession();
        // 有会话且启用强制走主库
        if (session != null && ReadMasterContext.isOn()) {
            Object result;
            try {
                // 后续sql强制走主库
                HintManager.getInstance().setMasterRouteOnly();
                result = invocation.proceed();
            } finally {
                // 和 HintManager.getInstance().setMasterRouteOnly(); 必须成对出现
                HintManager.clear();
            }

            return result;
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
