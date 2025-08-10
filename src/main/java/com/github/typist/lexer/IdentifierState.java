package com.github.typist.lexer;

import com.github.typist.TokenType;

/**
 * 标识符识别状态
 * 
 * 专门负责识别标识符和关键字，包括变量名、函数名以及Python关键字。
 * 实现标识符的词法规则和关键字识别。
 * 
 * 包内类，实现细节不对外暴露。
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
class IdentifierState implements LexerState {
    
    @Override
    public boolean process(LexerContext context) {
        int tokenPos = context.getPosition();
        
        // 验证首字符是否符合标识符规则
        char firstChar = context.getCurrentChar();
        if (!Character.isLetter(firstChar) && firstChar != '_') {
            context.transitionToError();
            return true;
        }
        
        String identifier = readIdentifier(context);
        
        // 解析标识符类型和值
        TokenInfo tokenInfo = parseIdentifier(identifier);
        context.addToken(tokenInfo.type, tokenInfo.value, tokenPos);
        
        // 返回分发状态
        context.transitionToDispatch();
        return true;
    }
    
    /**
     * 读取标识符字符序列
     */
    private String readIdentifier(LexerContext context) {
        StringBuilder result = new StringBuilder();
        while (context.getCurrentChar() != '\0' && 
               (Character.isLetterOrDigit(context.getCurrentChar()) || context.getCurrentChar() == '_')) {
            result.append(context.getCurrentChar());
            context.advance();
        }
        return result.toString();
    }
    
    /**
     * 解析标识符，区分关键字和普通标识符
     */
    private TokenInfo parseIdentifier(String identifier) {
        switch (identifier) {
            case "True":
                return new TokenInfo(TokenType.BOOLEAN, true);
            case "False":
                return new TokenInfo(TokenType.BOOLEAN, false);
            case "None":
                return new TokenInfo(TokenType.NULL, null);
            default:
                return new TokenInfo(TokenType.IDENTIFIER, identifier);
        }
    }
    
    /**
     * Token信息封装类
     */
    private static class TokenInfo {
        final TokenType type;
        final Object value;
        
        TokenInfo(TokenType type, Object value) {
            this.type = type;
            this.value = value;
        }
    }
}