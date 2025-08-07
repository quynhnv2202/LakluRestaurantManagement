package com.laklu.pos.uiltis;

import java.time.LocalTime;

public class Ultis {
    public static void throwIf(boolean condition, Exception e) throws Exception {
        if (condition) {
            throw e;
        }
    }

    public  static void throwUnless(boolean condition, Exception e) throws Exception {
        if (!condition) {
            throw e;
        }
    }

    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }
}
