package com.github.typist.parser;

import com.github.typist.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * 列表解释器
 * 
 * 实现语法规则：List := '[' (Value (',' Value)*)? ']'
 * 
 * 解析流程：
 * 1. 消费 '['
 * 2. 如果不是 ']'，则解析元素列表
 * 3. 消费 ']'
 * 
 * @author typist
 */
class ListRule implements GrammarRule {
    
    private final GrammarRule valueRule;
    
    public ListRule(GrammarRule valueRule) {
        this.valueRule = valueRule;
    }
    
    @Override
    public PythonValue parse(ParseContext context) {
        context.consume(TokenType.LEFT_BRACKET);
        List<PythonValue> elements = parseElementList(context);
        context.consume(TokenType.RIGHT_BRACKET);
        return new PythonValue.ListValue(elements);
    }
    
    private List<PythonValue> parseElementList(ParseContext context) {
        List<PythonValue> elements = new ArrayList<>();
        
        if (!context.is(TokenType.RIGHT_BRACKET)) {
            elements.add(valueRule.parse(context));
            
            while (context.is(TokenType.COMMA)) {
                context.advance();
                if (context.is(TokenType.RIGHT_BRACKET)) break;
                elements.add(valueRule.parse(context));
            }
        }
        
        return elements;
    }
}