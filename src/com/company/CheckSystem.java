package com.company;

public class CheckSystem {
    public static String check() {

        String osName = System.getProperty("os.name");

        System.out.println(osName); //Windows 10

        if (osName.startsWith("Windows")) {
            System.out.println("Windows");
            return "Windows";
        } else {
            System.out.println("Mac");
            return "Mac";
        }

    }
}
