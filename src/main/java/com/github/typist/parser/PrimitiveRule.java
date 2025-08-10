package com.github.typist.parser;

import com.github.typist.lexer.TokenType;

/**
 * 基本类型解释器
 * 
 * 实现语法规则：Primitive := NUMBER | STRING | BOOLEAN | NULL
 * 
 * 这是最简单的解释器，直接将 token 的值包装为 PrimitiveValue。
 * 
 * @author Generated with Claude Code
 */
class PrimitiveRule implements GrammarRule {
    
    @Override
    public PythonValue parse(ParseContext context) {
        Object value = context.current().getValue();
        context.advance();
        return new PythonValue.PrimitiveValue(value);
    }
}