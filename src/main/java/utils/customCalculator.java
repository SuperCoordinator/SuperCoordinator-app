package utils;

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
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%';
    }

    private boolean isVariable(char ch) {
        return ch == 'n' || ch == 'a' || ch == 'm';
    }

    private int getPrecedence(char ch) {
        if (ch == '+' || ch == '-') {
            return 1;
        }
        if (ch == '*' || ch == '/' || ch == '%') {
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
        } else if (t == '%')
            r = a % b;
        else {
            error = true;
            errorMsg = "Operator error.";
            return;

        }
        valueStack.push(r);
    }

    private void processInput(String input, double n, double a, double m) {

        if (input.equalsIgnoreCase("no"))
            return;

        if (input.contains("prob")) {
            return;
        }

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
        error = false;
        processInput(userInput, 1, 1, 1);
        return error;
    }

    public String errorMsg(String userInput) {
/*        errorMsg = "";
        processInput(userInput, 1, 1, 1);*/
        return errorMsg;
    }

    public double calcExpression(String userInput, double nPieces, double itemAgeDays, double lastMaintenceDays) {
        processInput(userInput, nPieces, itemAgeDays, lastMaintenceDays);
        return result;
    }

    private final String[] elements = new String[3];

    public boolean evalStochasticTimeExpression(String formula) {

        boolean error = false;

        if(formula.contains("no"))
            return error;

        if (formula.indexOf(';') == -1) {
            // Is random or linear

            if (!(formula.indexOf('[') != -1 && formula.indexOf(']') != -1)) {
                errorMsg = "Invalid formula, missing some [ ] ";
                return true;
            }

            elements[0] = formula.substring(0, formula.indexOf('['));
            elements[1] = formula.substring(formula.indexOf('[') + 1, formula.indexOf(']'));

        } // Is gaussian
        else {
            if (!(formula.indexOf('[') != -1 && formula.indexOf(';') != -1 && formula.indexOf(']') != -1)) {
                errorMsg = "Invalid formula, missing some ; or [ ] ";
                return true;
            }
            elements[0] = formula.substring(0, formula.indexOf('['));
            elements[1] = formula.substring(formula.indexOf('[') + 1, formula.indexOf(';'));
            elements[2] = formula.substring(formula.indexOf(';') + 1, formula.indexOf(']'));
        }

        // remove possible blank space in the beginning of each String
        if (elements[0].charAt(0) == ' ')
            elements[0] = elements[0].substring(1);
        if (elements[1].charAt(0) == ' ')
            elements[1] = elements[1].substring(1);
        if (elements[2] != null)
            if (elements[2].charAt(0) == ' ')
                elements[2] = elements[2].substring(1);

        // and for the type, remove possible blank space
        if (elements[0].charAt(elements[0].length() - 1) == ' ')
            elements[0] = elements[0].substring(0, elements[0].length() - 1);

/*        for (String str : elements) {
            System.out.print(str + "/");
        }
        System.out.println("");*/

        // gaussian
        if (elements[0].equalsIgnoreCase("gauss")) {
            boolean b = evalExpression(elements[1]);
            if (b)
                return true;
            b = evalExpression(elements[2]);
            if (b)
                return true;
        } // linear
        else if (elements[0].equalsIgnoreCase("linear")) {
            boolean b = evalExpression(elements[1]);
            if (b)
                return true;
        } else {
            errorMsg = "Unknown prefix (only gauss and linear)";
            return true;
        }

        // At this point all went well, so save the expression
        return error;
    }

    public String[] getStochasticTimeFormulaElements() {
        return elements;
    }

    public boolean evalFailureFormula(String formula) {
        error = false;

        String[] members;
        if (formula.contains(" > ")) {
            members = formula.split("> ");
        } else if (formula.contains(" < ")) {
            members = formula.split("< ");
        } else if (formula.contains(" = ")) {
            members = formula.split("= ");
        } else if (formula.contains(" <= ")) {
            members = formula.split("<= ");
        } else if (formula.contains(" >= ")) {
            members = formula.split(">= ");
        } else {
            return false;
        }

        processInput(members[0], 1, 1, 1);
        processInput(members[1], 1, 1, 1);

        return error;
    }


}
