package com.example.demo.test;

public class test4 {
    public static void main(String[] args) {
        try {
            int i = Integer.parseInt("李轩");
            System.out.println(i);
        }catch (NumberFormatException e){
            System.out.println(0);
        }

    }
}
