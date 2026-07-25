package com.endofdays_re.utils.tools;

import com.endofdays_re.event.data.AllSyncValue;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluatorTool {
    private final Map<String, Object> variables = new HashMap<>(
            Map.ofEntries(
                    Map.entry("day", AllSyncValue.Instance.day),
                    Map.entry("time", AllSyncValue.Instance.time)
            )
    );

    // 内置函数注册
    private final Map<String, Function> functions = new HashMap<>();

    public ExpressionEvaluatorTool() {
        registerBuiltinFunctions();
    }

    // 注册内置函数
    private void registerBuiltinFunctions() {
        // 数学函数
        functions.put("sqrt", args -> {
            checkArgs("sqrt", args, 1);
            return Math.sqrt(args.get(0));
        });
        functions.put("pow", args -> {
            checkArgs("pow", args, 2);
            return Math.pow(args.get(0), args.get(1));
        });
        functions.put("abs", args -> {
            checkArgs("abs", args, 1);
            return Math.abs(args.get(0));
        });
        functions.put("max", args -> {
            checkArgs("max", args, 1, Integer.MAX_VALUE);
            return args.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        });
        functions.put("min", args -> {
            checkArgs("min", args, 1, Integer.MAX_VALUE);
            return args.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        });
        functions.put("sum", args -> {
            checkArgs("sum", args, 1, Integer.MAX_VALUE);
            return args.stream().mapToDouble(Double::doubleValue).sum();
        });
        functions.put("avg", args -> {
            checkArgs("avg", args, 1, Integer.MAX_VALUE);
            return args.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        });
        functions.put("round", args -> {
            checkArgs("round", args, 1);
            return Math.round(args.get(0));
        });
        functions.put("ceil", args -> {
            checkArgs("ceil", args, 1);
            return Math.ceil(args.get(0));
        });
        functions.put("floor", args -> {
            checkArgs("floor", args, 1);
            return Math.floor(args.get(0));
        });

        // 三角函数
        functions.put("sin", args -> {
            checkArgs("sin", args, 1);
            return Math.sin(args.get(0));
        });
        functions.put("cos", args -> {
            checkArgs("cos", args, 1);
            return Math.cos(args.get(0));
        });
        functions.put("tan", args -> {
            checkArgs("tan", args, 1);
            return Math.tan(args.get(0));
        });

        // 常量
        functions.put("pi", args -> Math.PI);
        functions.put("e", args -> Math.E);
    }

    // 检查参数数量
    private void checkArgs(String funcName, List<Double> args, int expected) {
        if (args.size() != expected) {
            throw new IllegalArgumentException("函数 " + funcName + " 需要 " + expected + " 个参数，但提供了 " + args.size() + " 个");
        }
    }

    private void checkArgs(String funcName, List<Double> args, int min, int max) {
        if (args.size() < min || args.size() > max) {
            throw new IllegalArgumentException("函数 " + funcName + " 需要 " + min + " 到 " + max + " 个参数，但提供了 " + args.size() + " 个");
        }
    }

    // 添加自定义函数
    public ExpressionEvaluatorTool addFunction(String name, Function function) {
        functions.put(name, function);
        return this;
    }

    public boolean containsKey(String name) {
        return functions.containsKey(name);
    }

    // 添加变量
    public ExpressionEvaluatorTool setVariable(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    // 添加变量
    public Object getVariable(String name) {
        return variables.get(name);
    }

    // 批量添加变量
    public ExpressionEvaluatorTool setVariables(Map<String, Object> vars) {
        variables.putAll(vars);
        return this;
    }

    // 计算表达式
    public double evaluate(String expr) {
        try {
            if (expr == null || expr.trim().isEmpty()) {
                throw new IllegalArgumentException("表达式不能为空");
            }

            expr = expr.trim();

            // 如果是数字字面量（支持f/d后缀）
            if (expr.matches("-?\\d+(\\.\\d+)?[fFdD]?")) {
                return parseNumber(expr);
            }

            // 替换变量
            expr = replaceVariables(expr);

            // 使用改进的tokenize方法
            List<String> tokens = tokenize(expr);

            // 处理一元运算符
            tokens = processUnaryOperators(tokens);

            // 使用栈处理表达式
            Stack<Object> values = new Stack<>(); // 改为Object，支持函数调用
            Stack<String> operators = new Stack<>();

            for (String token : tokens) {
                // 数字
                if (token.matches("-?\\d+(\\.\\d+)?[fFdD]?")) {
                    values.push(parseNumber(token));
                }
                // 函数调用
                else if (functions.containsKey(token)) {
                    operators.push(token);
                }
                // 逗号 - 函数参数分隔符
                else if (token.equals(",")) {
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processOperator(values, operators.pop());
                    }
                    if (operators.isEmpty()) {
                        throw new IllegalArgumentException("逗号位置错误");
                    }
                }
                // 标识符（未定义的函数或变量）
                else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    // 这应该是未替换的变量或未定义的函数，默认为0
                    values.push(0.0);
                }
                // 运算符
                else if (isOperator(token)) {
                    while (!operators.isEmpty() &&
                            !operators.peek().equals("(") &&
                            (precedence(operators.peek()) > precedence(token) ||
                                    (precedence(operators.peek()) == precedence(token) && isLeftAssociative(token)))) {
                        processOperator(values, operators.pop());
                    }
                    operators.push(token);
                }
                // 左括号
                else if (token.equals("(")) {
                    operators.push(token);
                }
                // 右括号
                else if (token.equals(")")) {
                    // 处理运算符直到左括号
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processOperator(values, operators.pop());
                    }
                    if (operators.isEmpty()) {
                        throw new IllegalArgumentException("括号不匹配: " + expr);
                    }
                    operators.pop(); // 弹出左括号

                    // 检查是否是函数调用
                    if (!operators.isEmpty() && functions.containsKey(operators.peek())) {
                        processFunction(values, operators.pop());
                    }
                }
            }

            // 执行剩余运算
            while (!operators.isEmpty()) {
                String operator = operators.pop();
                if (operator.equals("(")) {
                    throw new IllegalArgumentException("括号不匹配: " + expr);
                }
                processOperator(values, operator);
            }

            if (values.size() != 1) {
                throw new IllegalArgumentException("表达式错误: " + expr + ", 剩余值: " + values);
            }

            Object result = values.pop();
            if (result instanceof Double) {
                return (Double) result;
            } else {
                throw new IllegalArgumentException("表达式结果类型错误: " + result.getClass());
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof ArithmeticException) {
                throw e;
            }
            throw new IllegalArgumentException("无法计算表达式: " + expr, e);
        }
    }

    // 处理函数调用
    private void processFunction(Stack<Object> values, String functionName) {
        Function function = functions.get(functionName);
        if (function == null) {
            throw new IllegalArgumentException("未知函数: " + functionName);
        }

        // 收集参数
        List<Double> args = new ArrayList<>();
        while (!values.isEmpty()) {
            Object arg = values.pop();
            if (arg instanceof Double) {
                args.add(0, (Double) arg); // 反转顺序
            } else {
                // 如果遇到非数字，停止收集（可能是其他操作的结果）
                values.push(arg);
                break;
            }
        }

        double result = function.apply(args);
        values.push(result);
    }

    // 改进的tokenize方法
    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();

        // 匹配数字、运算符、括号、标识符、逗号
        Pattern pattern = Pattern.compile(
                "-?\\d+(\\.\\d+)?[fFdD]?|" +  // 数字
                        "[a-zA-Z_][a-zA-Z0-9_]*|" +   // 标识符
                        "[+\\-*/^(),]"                 // 运算符、括号和逗号
        );

        Matcher matcher = pattern.matcher(expr);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        return tokens;
    }

    // 处理一元运算符
    private List<String> processUnaryOperators(List<String> tokens) {
        List<String> result = new ArrayList<>();
        String prevToken = null;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if ("-".equals(token) && (prevToken == null ||
                    "(".equals(prevToken) || isOperator(prevToken) || ",".equals(prevToken))) {
                // 这是一元负号，转换为特殊的标记或处理方式
                result.add("0");
                result.add("-");
            } else if ("+".equals(token) && (prevToken == null ||
                    "(".equals(prevToken) || isOperator(prevToken) || ",".equals(prevToken))) {
                // 一元正号，可以忽略
                continue;
            } else {
                result.add(token);
            }
            prevToken = token;
        }

        return result;
    }

    // 解析数字
    private double parseNumber(String numberStr) {
        char last = numberStr.charAt(numberStr.length() - 1);
        if (last == 'f' || last == 'F') {
            return Float.parseFloat(numberStr.substring(0, numberStr.length() - 1));
        } else if (last == 'd' || last == 'D') {
            return Double.parseDouble(numberStr.substring(0, numberStr.length() - 1));
        } else {
            return Double.parseDouble(numberStr);
        }
    }

    // 替换变量，未定义变量默认为0
    private String replaceVariables(String expr) {
        // 先按变量名长度降序排序，避免部分匹配问题
        List<String> varNames = new ArrayList<>(variables.keySet());
        varNames.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (String varName : varNames) {
            Object value = variables.get(varName);
            String valueStr;
            if (value instanceof Number) {
                valueStr = value.toString();
            } else {
                // 非数字类型尝试转换
                try {
                    valueStr = String.valueOf(Double.parseDouble(value.toString()));
                } catch (NumberFormatException e) {
                    valueStr = "0"; // 无法转换则设为0
                }
            }
            // 使用单词边界匹配，避免部分匹配
            expr = expr.replaceAll("\\b" + Pattern.quote(varName) + "\\b", valueStr);
        }

        return expr;
    }

    // 判断是否为运算符
    private boolean isOperator(String token) {
        return token.matches("[+\\-*/^]");
    }

    // 判断运算符是否左结合
    private boolean isLeftAssociative(String operator) {
        return !operator.equals("^"); // 指数运算是右结合，其他都是左结合
    }

    // 运算操作
    private void processOperator(Stack<Object> values, String operator) {
        if (values.size() < 2) {
            throw new IllegalArgumentException("运算符 " + operator + " 缺少操作数");
        }

        Object bObj = values.pop();
        Object aObj = values.pop();

        if (!(aObj instanceof Double) || !(bObj instanceof Double)) {
            throw new IllegalArgumentException("运算符 " + operator + " 的操作数类型错误");
        }

        double b = (Double) bObj;
        double a = (Double) aObj;
        double result;

        switch (operator) {
            case "+":
                result = a + b;
                break;
            case "-":
                result = a - b;
                break;
            case "*":
                result = a * b;
                break;
            case "/":
                if (b == 0) throw new ArithmeticException("除零错误: " + a + " / " + b);
                result = a / b;
                break;
            case "^":
                result = Math.pow(a, b);
                break;
            default:
                throw new IllegalArgumentException("未知运算符: " + operator);
        }

        values.push(result);
    }

    // 运算符优先级
    private int precedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            default -> 0;
        };
    }

    // 函数接口
    @FunctionalInterface
    public interface Function {
        double apply(List<Double> args);
    }

}