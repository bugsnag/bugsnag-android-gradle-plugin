package com.example.bugsnag.mylib;

public class Foo {

    static {
        System.loadLibrary("native-lib");
    }

    public native String bar();

    public static boolean isEven(int n) {
        return n % 2 == 0;
    }
}
