package com.chenerge.save_after_read_consistent.city.common.web;

import com.chenerge.save_after_read_consistent.city.common.session.CurrentUser;
import com.chenerge.save_after_read_consistent.city.common.session.SessionContext;
import com.chenerge.save_after_read_consistent.city.common.session.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@ConditionalOnClass(ServletException.class)
@WebFilter(urlPatterns = "/*", filterName = "AuthFilter")
public class AuthFilter extends OncePerRequestFilter {
    @Autowired
    private SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("token");
        if(token == null){
            writeFailMessage(response, "token is null");
            return;
        }

        String userId = sessionService.getUserByToken(token);
        // 认证token失败, 返回fail
        if (userId == null) {
            writeFailMessage(response, "token invalid");
            return;
        }

        /*
            认证成功将session设置到当前线程
            后续通过 SessionContext.getSession() 在任何地方获取当前登录用户id
         */
        SessionContext.setSession(new CurrentUser(token, userId));

        chain.doFilter(request, response);
    }

    private void writeFailMessage(ServletResponse response, String msg) throws IOException {
        HttpServletResponse httpResponse = getAuthFailResponse(response);
        httpResponse.getWriter()
                .write(
                        new ObjectMapper()
                                .writeValueAsString(
                                        msg
                                ));
    }

    public HttpServletResponse getAuthFailResponse(ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("application/json; charset=utf-8");
        return httpResponse;
    }

}
