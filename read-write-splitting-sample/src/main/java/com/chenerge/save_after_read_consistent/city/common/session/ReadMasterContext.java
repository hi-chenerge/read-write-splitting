package com.chenerge.save_after_read_consistent.city.common.session;

/**
 * 当前线程后续操作是否启用强制走主库
 */
public class ReadMasterContext {
    private static ThreadLocal<Boolean> tl = new ThreadLocal<>();

    public static <T> void setOn() {
        tl.set(true);
    }

    public static boolean isOn() {
        return tl.get() != null && tl.get();
    }

    public static void clear() {
        tl.remove();
    }
}
