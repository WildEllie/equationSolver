package com.ellieportugali.equationsolver.test;

import com.ellieportugali.equationsolver.test.Expressions;

public class Main {
    private static final String fileName = "N://temp/test_expressions.txt";

    public static void main(String[] args) {
        Expressions myEx = new Expressions(fileName);

        System.out.println(myEx.outputString());
    }
}
