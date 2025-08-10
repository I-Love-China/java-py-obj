package com.github.typist.parser;

import com.github.typist.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * 元组解释器
 * 
 * 实现语法规则：Tuple := '(' (Value (',' Value)*)? ')'
 * 
 * 解析流程与列表类似，但使用圆括号。
 * 
 * @author typist
 */
class TupleRule implements GrammarRule {
    
    private final GrammarRule valueRule;
    
    public TupleRule(GrammarRule valueRule) {
        this.valueRule = valueRule;
    }
    
    @Override
    public PythonValue parse(ParseContext context) {
        context.consume(TokenType.LEFT_PAREN);
        List<PythonValue> elements = parseElementList(context);
        context.consume(TokenType.RIGHT_PAREN);
        return new PythonValue.TupleValue(elements);
    }
    
    private List<PythonValue> parseElementList(ParseContext context) {
        List<PythonValue> elements = new ArrayList<>();
        
        if (!context.is(TokenType.RIGHT_PAREN)) {
            elements.add(valueRule.parse(context));
            
            while (context.is(TokenType.COMMA)) {
                context.advance();
                if (context.is(TokenType.RIGHT_PAREN)) break;
                elements.add(valueRule.parse(context));
            }
        }
        
        return elements;
    }
}