package com.github.typist.lexer;

/**
 * 分发状态 - 词法分析器的入口状态
 *
 * 负责根据当前字符类型决定转换到哪个专门的识别状态。
 * 相当于词法分析器的"交通指挥员"，将不同类型的字符分发到对应的处理状态。
 *
 * 包内类，实现细节不对外暴露。
 *
 * @author Generated with Claude Code
 * @version 1.0
 */
class DispatchState implements LexerState {

    @Override
    public boolean process(LexerContext context) {
        // 跳过空白字符
        skipWhitespace(context);

        // 检查是否到达文件末尾
        if (context.getCurrentChar() == '\0') {
            return false;
        }

        char currentChar = context.getCurrentChar();

        // 根据字符类型分发到相应状态
        // 数字或负号 -> 数字状态
        if (Character.isDigit(currentChar) || currentChar == '-') {
            context.transitionToNumber();
            return true;
        }
        // 引号 -> 字符串状态
        else if (currentChar == '\'' || currentChar == '\"') {
            context.transitionToString();
            return true;
        }
        // 字母或下划线 -> 标识符状态
        else if (Character.isLetter(currentChar) || currentChar == '_') {
            context.transitionToIdentifier();
            return true;
        }
        // 可能是分隔符 -> 分隔符状态
        else {
            context.transitionToDelimiter();
            return true;
        }
    }

    /**
     * 跳过空白字符
     */
    private void skipWhitespace(LexerContext context) {
        while (context.getCurrentChar() != '\0' && Character.isWhitespace(context.getCurrentChar())) {
            context.advance();
        }
    }
}