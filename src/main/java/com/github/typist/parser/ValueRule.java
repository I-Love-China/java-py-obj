package com.github.typist.parser;

import com.github.typist.lexer.TokenType;
import java.util.HashMap;
import java.util.Map;

/**
 * 值规则解释器
 * 
 * 实现语法规则：Value := Primitive | List | Dict | Set | Tuple
 * 
 * 这是解释器层次结构的根节点，负责根据当前 token 类型
 * 分发给对应的具体解释器。这体现了解释器模式的组合特性。
 * 
 * @author typist
 */
class ValueRule implements GrammarRule {
    
    private final Map<TokenType, GrammarRule> ruleMap;
    
    public ValueRule() {
        this.ruleMap = new HashMap<>();
        initializeRules();
    }
    
    private void initializeRules() {
        PrimitiveRule primitiveRule = new PrimitiveRule();
        
        ruleMap.put(TokenType.NUMBER, primitiveRule);
        ruleMap.put(TokenType.STRING, primitiveRule);
        ruleMap.put(TokenType.BOOLEAN, primitiveRule);
        ruleMap.put(TokenType.NULL, primitiveRule);
        ruleMap.put(TokenType.LEFT_BRACKET, new ListRule(this));
        ruleMap.put(TokenType.LEFT_BRACE, new DictOrSetRule(this));
        ruleMap.put(TokenType.LEFT_PAREN, new TupleRule(this));
    }
    
    @Override
    public PythonValue parse(ParseContext context) {
        TokenType currentType = context.currentType();
        GrammarRule rule = ruleMap.get(currentType);
        
        if (rule == null) {
            throw new IllegalArgumentException(
                "语法错误：在位置 " + context.current().getPosition() + 
                " 处遇到意外的记号 " + currentType
            );
        }
        
        return rule.parse(context);
    }
}