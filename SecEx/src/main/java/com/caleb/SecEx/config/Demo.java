package com.caleb.SecEx.config;

@FunctionalInterface 
interface A {
    void show(int i);
}

public class Demo{
    public static void main(String a[]){
        A obj = (i) -> System.out.println("in show " + i);

        obj.show(5);


    }
}