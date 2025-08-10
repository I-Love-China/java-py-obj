package com.github.typist.parser;

import com.github.typist.lexer.Token;
import com.github.typist.lexer.TokenType;
import java.util.List;

/**
 * 解析上下文
 * 
 * 解释器模式中的上下文类，维护解析过程中的共享状态。
 * 所有语法规则解释器共享同一个上下文，实现状态的统一管理。
 * 
 * 核心职责：
 * - Token 流的管理和遍历
 * - 当前解析位置的维护
 * - 解析状态的共享
 * - 错误信息的统一格式化
 * 
 * @author Generated with Claude Code
 */
class ParseContext {
    
    private final List<Token> tokens;
    private int position;
    private Token current;
    
    public ParseContext(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Token list cannot be null or empty");
        }
        this.tokens = tokens;
        this.position = 0;
        this.current = tokens.get(0);
    }
    
    public Token current() {
        return current;
    }
    
    public TokenType currentType() {
        return current.getType();
    }
    
    public void advance() {
        if (position < tokens.size() - 1) {
            position++;
            current = tokens.get(position);
        }
    }
    
    public void consume(TokenType expectedType) {
        if (current.getType() != expectedType) {
            throw new IllegalArgumentException(
                String.format("语法错误：期望 %s，但在位置 %d 处得到 %s", 
                    expectedType, current.getPosition(), current.getType())
            );
        }
        advance();
    }
    
    public boolean is(TokenType type) {
        return current.getType() == type;
    }
    
    public boolean hasMore() {
        return position < tokens.size() - 1;
    }
}