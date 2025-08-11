package com.github.typist.parser;

import com.github.typist.lexer.TokenType;

import java.util.*;

/**
 * 字典/集合解释器
 * <p>
 * 实现语法规则：
 * - Dict := '{' (Value ':' Value (',' Value ':' Value)*)? '}'
 * - Set := '{' (Value (',' Value)*)? '}'
 * <p>
 * 通过向前看技术区分字典和集合。
 *
 * @author typist
 */
class DictOrSetRule implements GrammarRule {

    private final GrammarRule valueRule;

    public DictOrSetRule(GrammarRule valueRule) {
        this.valueRule = valueRule;
    }

    @Override
    public PythonValue parse(ParseContext context) {
        context.consume(TokenType.LEFT_BRACE);

        if (context.is(TokenType.RIGHT_BRACE)) {
            context.advance();
            return new PythonValue.DictValue(new HashMap<>());
        }

        PythonValue firstValue = valueRule.parse(context);

        if (context.is(TokenType.COLON)) {
            return parseDict(context, firstValue);
        } else {
            return parseSet(context, firstValue);
        }
    }

    private PythonValue parseDict(ParseContext context, PythonValue firstKey) {
        Map<PythonValue, PythonValue> entries = new HashMap<>();

        context.consume(TokenType.COLON);
        PythonValue firstValue = valueRule.parse(context);
        entries.put(firstKey, firstValue);

        while (context.is(TokenType.COMMA)) {
            context.advance();
            if (context.is(TokenType.RIGHT_BRACE)) {
                break;
            }

            PythonValue key = valueRule.parse(context);
            context.consume(TokenType.COLON);
            PythonValue value = valueRule.parse(context);
            entries.put(key, value);
        }

        context.consume(TokenType.RIGHT_BRACE);
        return new PythonValue.DictValue(entries);
    }

    private PythonValue parseSet(ParseContext context, PythonValue firstElement) {
        List<PythonValue> elements = new ArrayList<>();
        elements.add(firstElement);

        while (context.is(TokenType.COMMA)) {
            context.advance();
            if (context.is(TokenType.RIGHT_BRACE)) {
                break;
            }
            elements.add(valueRule.parse(context));
        }

        context.consume(TokenType.RIGHT_BRACE);
        return new PythonValue.SetValue(elements);
    }
}