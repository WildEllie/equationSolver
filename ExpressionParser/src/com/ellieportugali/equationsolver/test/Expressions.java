package com.ellieportugali.equationsolver.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Expressions {
    private static HashMap<String, Double> variables;

    public Expressions(String fileName) {
        List<String> equationList = new LinkedList<String>();
        variables = new HashMap<>();

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(equationList::add);
        } catch (IOException e) {
            e.printStackTrace();
            printErr(Errors.PROBLEM_READING_FILE);
            return;
        }

        if (equationList.size() > 0) {
            evaluate(equationList);
        } else {
            printErr(Errors.NO_EQUATIONS_FOUND);
        }
    }

    private void evaluate(List<String> listOfEquations){
        listOfEquations.forEach(eq -> {
            String[] equationParts = eq.split("(?<![-+])=");
            if (validateEquation(equationParts)) {
                variables.put(equationParts[0].trim(), solveExpression(padOperators(equationParts[1])));
            }

            String[] addEquationParts = eq.split("\\+=");
            if (validateEquation(addEquationParts)) {
                String varName = addEquationParts[0].trim();
                variables.put(varName, solveExpression(padOperators(varName + " + " + addEquationParts[1])));
            }

            String[] subtractEquationParts = eq.split("-=");
            if (validateEquation(subtractEquationParts)) {
                String varName = subtractEquationParts[0].trim();
                variables.put(varName, solveExpression(padOperators(varName + " - " + subtractEquationParts[1])));
            }
        });
    }

    private static String padOperators(String expression) {
        return String.join(" / ",
                String.join(" - ",
                 String.join(" + ",
                  String.join(" * ",
                        expression.split("\\*"))
                        .split("(?<!\\+)\\+(?![\\+=])"))
                         .split("(?<!-)-(?!-)"))
                          .split("/"));
    }

    private static boolean validateEquation(String[] eqParts)  {
        if (eqParts.length != 2) {
            return false;
        }

        if (!eqParts[0].matches("^[A-Za-z].*$")){
            printErr(Errors.MALFORMED_FILE);
            return false;
        }

        return true;
    }

    private static boolean validateBinaryOperation(String[] expParts)  {
        if (expParts.length != 2) {
            return false;
        }
        if (expParts[0] == null || expParts[1] == null) {
            return false;
        }

        return true;
    }

    private static Double solveExpression(String expression){
        // process addition +
        String[] addParts = expression.split("(?<!\\+)\\+(?![\\+=])", 2);
        if (validateBinaryOperation(addParts)) {
            return solveExpression(addParts[0]) + solveExpression(addParts[1]);
        }

        // process subtraction -
        // to maintain order of execution of left to right - take the last sign
        int lastIndexOfMinus = expression.lastIndexOf('-');
        String[] subtractParts = new String[2];
        if (lastIndexOfMinus > 0 && expression.charAt(lastIndexOfMinus - 1) != '-') {
            subtractParts[0] = expression.substring(0, lastIndexOfMinus);
            subtractParts[1] = expression.substring(lastIndexOfMinus + 1);
        }

        if (validateBinaryOperation(subtractParts)) {
            return solveExpression(subtractParts[0]) - solveExpression(subtractParts[1]);
        }

        // process multiplication *
        String[] multiplyParts = expression.split("\\*", 2);
        if (validateBinaryOperation(multiplyParts)) {
            return solveExpression(multiplyParts[0]) * solveExpression(multiplyParts[1]);
        }

        // process division /
        // to maintain order of execution of left to right - take the last sign
        int lastIndexOfDivide = expression.lastIndexOf('/');
        String[] divideParts = new String[2];
        if (lastIndexOfDivide > 0) {
            divideParts[0] = expression.substring(0, lastIndexOfDivide);
            divideParts[1] = expression.substring(lastIndexOfDivide + 1);
        }

        if (validateBinaryOperation(divideParts)) {
            return solveExpression(divideParts[0]) / solveExpression(divideParts[1]);
        }

        // process unary ++X
        if (expression.trim().startsWith("++")){
            String varName = expression.trim().substring(2);
            Double currentVarValue = variables.get(varName);
            variables.put(varName,  currentVarValue + 1D);
            return 1D + currentVarValue;
        }

        // process unary X++
        if (expression.trim().endsWith("++")){
            String varName = expression.trim().substring(0, expression.trim().length() - 2);
            Double currentVarValue = variables.get(varName);
            variables.put(varName,  currentVarValue + 1D);
            return currentVarValue;
        }

        // process unary --X
        if (expression.trim().startsWith("--")){
            String varName = expression.trim().substring(2);
            Double currentVarValue = variables.get(varName);
            variables.put(varName,  currentVarValue - 1D);
            return currentVarValue - 1;
        }

        // process unary X--
        if (expression.trim().endsWith("--")){
            String varName = expression.trim().substring(0, expression.trim().length() - 2);
            Double currentVarValue = variables.get(varName);
            variables.put(varName,  currentVarValue - 1D);
            return currentVarValue;
        }

        // process variables
        String varName = expression.trim();
        if (variables.containsKey(varName)) {
            return variables.get(varName);
        }

        // process rest (numbers?)
        return Double.parseDouble(expression);
    }

    public String outputString(){
        return "(" +
                variables
                        .keySet()
                        .stream()
                        .sorted()
                        .map(k -> "" + k + "=" + prettyPrintDouble(variables.get(k)))
                        .collect(Collectors.joining(","))
                + ")";
    }

    private String prettyPrintDouble(Double d) {
        if (Math.floor(d) == d) return String.valueOf(Math.round(d));

        return d.toString();
    }

    private static void printErr(Errors err) {
        if (err == Errors.NO_EQUATIONS_FOUND) {
            System.out.println("Did not find any equations to evaluate!");
        }

        if (err == Errors.PROBLEM_READING_FILE) {
            System.out.println("Could not read file, please check file exists and has read permissions.");
        }

        if (err == Errors.MALFORMED_FILE) {
            System.out.println("Something went wrong, file structure is malformed.");
        }
    }

    public Double getVarValue(String varName){
        return variables.get(varName);
    }

    public Set<String> getVariableNames(){
        return variables.keySet();
    }
}

enum Errors {
    NO_EQUATIONS_FOUND,
    PROBLEM_READING_FILE,
    MALFORMED_FILE
}