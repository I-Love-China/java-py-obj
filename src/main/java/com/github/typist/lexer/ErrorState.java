package com.github.typist.lexer;

/**
 * 错误处理状态
 * <p>
 * 专门负责处理词法分析过程中遇到的错误情况。
 * 提供详细的错误信息和位置定位。
 * <p>
 *
 * @author typist
 * @version 1.1
 */
class ErrorState implements LexerState {

    @Override
    public boolean process(LexerContext context) {
        char errorChar = context.getCurrentChar();
        int errorPos = context.getPosition();

        // 构造详细的错误信息
        String errorMessage = String.format(
                "Unexpected character: '%c' (ASCII: %d) at position %d",
                errorChar, (int) errorChar, errorPos
        );

        throw new IllegalArgumentException(errorMessage);
    }
}