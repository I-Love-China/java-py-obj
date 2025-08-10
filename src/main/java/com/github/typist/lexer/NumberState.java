package com.github.typist.lexer;

/**
 * 数字识别状态
 * 
 * 专门负责识别和处理数字词素，包括整数、浮点数和负数。
 * 实现数字识别的有限状态自动机。
 * 
 * 包内类，实现细节不对外暴露。
 * 
 * @author typist
 * @version 1.1
 */
class NumberState implements LexerState {
    
    @Override
    public boolean process(LexerContext context) {
        int tokenPos = context.getPosition();
        String numberStr = readNumber(context);
        
        if (numberStr.isEmpty()) {
            // 读取失败，转到错误状态
            context.transitionToError();
            return true;
        }
        
        // 解析数值
        Object value = parseNumber(numberStr);
        context.addToken(TokenType.NUMBER, value, tokenPos);
        
        // 返回分发状态
        context.transitionToDispatch();
        return true;
    }
    
    /**
     * 读取数字字符序列
     */
    private String readNumber(LexerContext context) {
        StringBuilder result = new StringBuilder();
        
        // 处理可选的负号
        if (context.getCurrentChar() == '-') {
            result.append(context.getCurrentChar());
            context.advance();
        }
        
        // 读取数字部分
        boolean hasDigits = false;
        boolean hasDot = false;
        
        while (context.getCurrentChar() != '\0') {
            char ch = context.getCurrentChar();
            
            if (Character.isDigit(ch)) {
                result.append(ch);
                context.advance();
                hasDigits = true;
            }
            else if (ch == '.' && !hasDot) {
                result.append(ch);
                context.advance();
                hasDot = true;
            }
            else {
                // 遇到非数字字符，结束读取
                break;
            }
        }
        
        // 验证是否为有效数字
        if (!hasDigits || (result.length() == 1 && result.charAt(0) == '-')) {
            // 无效数字
            return "";
        }
        
        return result.toString();
    }
    
    /**
     * 解析数字字符串为相应的数值类型
     */
    private Object parseNumber(String numberStr) {
        try {
            if (numberStr.contains(".")) {
                return Double.parseDouble(numberStr);
            } else {
                try {
                    return Integer.parseInt(numberStr);
                } catch (NumberFormatException e) {
                    return Long.parseLong(numberStr);
                }
            }
        } catch (NumberFormatException e) {
            // 这里应该不会发生，因为我们已经验证了格式
            throw new IllegalStateException("Invalid number format: " + numberStr, e);
        }
    }
}