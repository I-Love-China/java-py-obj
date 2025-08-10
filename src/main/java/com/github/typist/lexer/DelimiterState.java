package com.github.typist.lexer;

import com.github.typist.lexer.TokenType;

/**
 * 分隔符识别状态
 * <p>
 * 专门负责识别和处理分隔符词素，包括各种括号、逗号、冒号等。
 * 分隔符是单字符词素，处理相对简单但很重要。
 * <p>
 * 包内类，实现细节不对外暴露。
 *
 * @author Generated with Claude Code
 * @version 1.0
 */
class DelimiterState implements LexerState {

    @Override
    public boolean process(LexerContext context) {
        char currentChar = context.getCurrentChar();
        int tokenPos = context.getPosition();

        // 获取分隔符类型
        TokenType delimiterType = getDelimiterType(currentChar);
        if (delimiterType != null) {
            context.addToken(delimiterType, currentChar, tokenPos);
            context.advance();

            // 返回分发状态
            context.transitionToDispatch();
            return true;
        } else {
            // 不是分隔符，转到错误状态
            context.transitionToError();
            return true;
        }
    }

    /**
     * 获取分隔符类型
     */
    private TokenType getDelimiterType(char ch) {
        switch (ch) {
            case '[':
                return TokenType.LEFT_BRACKET;
            case ']':
                return TokenType.RIGHT_BRACKET;
            case '{':
                return TokenType.LEFT_BRACE;
            case '}':
                return TokenType.RIGHT_BRACE;
            case '(':
                return TokenType.LEFT_PAREN;
            case ')':
                return TokenType.RIGHT_PAREN;
            case ',':
                return TokenType.COMMA;
            case ':':
                return TokenType.COLON;
            default:
                return null;
        }
    }
}