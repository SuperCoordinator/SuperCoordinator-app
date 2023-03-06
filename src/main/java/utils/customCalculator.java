package utils;

import java.util.Scanner;
import java.util.Stack;


/**
 * Adapted from http://www2.lawrence.edu/fast/GREGGJ/CMSC150/073Calculator/Calculator.html
 */
public class customCalculator {

    private final Stack<Character> operatorStack;
    private final Stack<Double> valueStack;

    private double result;
    private boolean error;
    private String errorMsg;

    public customCalculator() {
        this.operatorStack = new Stack<>();
        this.valueStack = new Stack<>();
        this.error = false;
        this.result = Double.NaN;
        this.errorMsg = "";
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private boolean isVariable(char ch) {
        return ch == 'n' || ch == 'a' || ch == 'm';
    }

    private int getPrecedence(char ch) {
        if (ch == '+' || ch == '-') {
            return 1;
        }
        if (ch == '*' || ch == '/') {
            return 2;
        }
        return 0;
    }

    private void processOperator(char t) {
        double a, b;
        if (valueStack.empty()) {
            error = true;
            errorMsg = "Expression error.";
            return;
        } else {
            b = valueStack.peek();
            valueStack.pop();
        }
        if (valueStack.empty()) {
            error = true;
            errorMsg = "Expression error.";
            return;
        } else {
            a = valueStack.peek();
            valueStack.pop();
        }
        double r = 0;
        if (t == '+') {
            r = a + b;
        } else if (t == '-') {
            r = a - b;
        } else if (t == '*') {
            r = a * b;
        } else if (t == '/') {
            r = a / b;
        } else {
            error = true;
            errorMsg = "Operator error.";
            return;

        }
        valueStack.push(r);
    }

    private void processInput(String input, double n, double a, double m) {
        // The tokens that make up the input
        String[] tokens = input.split(" ");

        // Main loop - process all input tokens
        for (String nextToken : tokens) {
            char ch = nextToken.charAt(0);
            if (ch >= '0' && ch <= '9') {
                double value = Double.parseDouble(nextToken);
                valueStack.push(value);
            } else if (isOperator(ch)) {
                if (operatorStack.empty() || getPrecedence(ch) > getPrecedence(operatorStack.peek())) {
                    operatorStack.push(ch);
                } else {
                    while (!operatorStack.empty() && getPrecedence(ch) <= getPrecedence(operatorStack.peek())) {
                        char toProcess = operatorStack.peek();
                        operatorStack.pop();
                        processOperator(toProcess);
                    }
                    operatorStack.push(ch);
                }
            } else if (ch == '(') {
                operatorStack.push(ch);
            } else if (ch == ')') {
                while (!operatorStack.empty() && isOperator(operatorStack.peek())) {
                    char toProcess = operatorStack.peek();
                    operatorStack.pop();
                    processOperator(toProcess);
                }
                if (!operatorStack.empty() && operatorStack.peek() == '(') {
                    operatorStack.pop();
                } else {
                    error = true;
                    errorMsg = "Error: unbalanced parenthesis.";
                }
            } else if (isVariable(ch)) {
                switch (ch) {
                    case 'n' -> {
                        valueStack.push(n);
                    }
                    case 'a' -> {
                        valueStack.push(a);
                    }
                    case 'm' -> {
                        valueStack.push(m);
                    }
                }
            } else {
                error = true;
                errorMsg = "Unknown variable.";
                return;
            }
        }

        // Empty out the operator stack at the end of the input
        while (!operatorStack.empty() && isOperator(operatorStack.peek())) {
            char toProcess = operatorStack.peek();
            operatorStack.pop();
            processOperator(toProcess);
        }
        // Print the result if no error has been seen.
        if (!error) {
            double res = valueStack.peek();
            valueStack.pop();
            if (!operatorStack.empty() || !valueStack.empty()) {
                errorMsg = "Expression error.";
            } else {
                result = res;
            }
        }

    }

    public boolean evalExpression(String userInput) {
        processInput(userInput, 1, 1, 1);
        return error;
    }

    public String errorMsg (String userInput){
        processInput(userInput, 1, 1, 1);
        return errorMsg;
    }

    public double calcExpression(String userInput, double nPieces, double itemAgeMonth, double lastMaintenceDays){
        processInput(userInput, nPieces, itemAgeMonth, lastMaintenceDays);
        return result;
    }

}
