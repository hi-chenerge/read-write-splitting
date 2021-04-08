package com.chenerge.save_after_read_consistent.city.common.session;

public class SessionContext {
    private static ThreadLocal<CurrentUser> sessionTheadLocal = new ThreadLocal<>();

    public static <T> void setSession(CurrentUser currentUser) {
        sessionTheadLocal.set(currentUser);
    }

    public static CurrentUser getSession() {
        return sessionTheadLocal.get();
    }

    public static void clear() {
        sessionTheadLocal.remove();
    }

    /**
     * 模拟登陆用户
     * @param currentUser
     */
    public static void mockSession(CurrentUser currentUser){
        setSession(currentUser);
    }
}
