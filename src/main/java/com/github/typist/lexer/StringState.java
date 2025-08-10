package com.github.typist.lexer;


/**
 * 字符串识别状态
 * <p>
 * 专门负责识别和处理字符串词素，支持单引号和双引号字符串。
 * 处理转义字符序列，实现上下文敏感的词法分析。
 * <p>
 * 包内类，实现细节不对外暴露。
 *
 * @author Generated with Claude Code
 * @version 1.0
 */
 class StringState implements LexerState {

    @Override
    public boolean process(LexerContext context) {
        char quote = context.getCurrentChar();
        int tokenPos = context.getPosition();

        // 验证是否为引号字符
        if (quote != '\'' && quote != '\"') {
            context.transitionToError();
            return true;
        }

        String stringValue = readString(context, quote);
        context.addToken(TokenType.STRING, stringValue, tokenPos);

        // 返回分发状态
        context.transitionToDispatch();
        return true;
    }

    /**
     * 读取字符串内容
     */
    private String readString(LexerContext context, char quote) {
        StringBuilder result = new StringBuilder();
        context.advance(); // 跳过开始引号

        while (context.getCurrentChar() != '\0' && context.getCurrentChar() != quote) {
            char ch = context.getCurrentChar();

            if (ch == '\\') {
                // 处理转义字符
                context.advance(); // 跳过反斜杠
                char escaped = context.getCurrentChar();

                switch (escaped) {
                    case 'n':
                        result.append('\n');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case '\\':
                        result.append('\\');
                        break;
                    case '\'':
                        result.append('\'');
                        break;
                    case '\"':
                        result.append('\"');
                        break;
                    case '\0':
                        // 到达文件末尾，未闭合字符串
                        return result.toString();
                    default:
                        // 未知转义字符，原样保留
                        result.append(escaped);
                }
            } else {
                result.append(ch);
            }

            context.advance();
        }

        // 跳过结束引号（如果存在）
        if (context.getCurrentChar() == quote) {
            context.advance();
        }

        return result.toString();
    }
}