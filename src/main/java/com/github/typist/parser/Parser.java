package com.github.typist.parser;

import com.github.typist.lexer.Token;

import java.util.Collections;
import java.util.List;

/**
 * Python 对象语法分析器
 * <p>
 * 采用解释器模式（Interpreter Pattern）重新设计的语法分析器。
 * 每个语法规则对应一个独立的解释器类，形成清晰的解释器层次结构。
 * <p>
 * 解释器模式优势：
 * 1. 符合编译原理本质：每个文法规则都是独立的解释器
 * 2. 易于扩展：新增语法规则只需添加新的解释器类
 * 3. 职责清晰：每个解释器只负责一种语法结构
 * 4. 符合开闭原则：扩展无需修改现有代码
 * <p>
 * 解释器层次结构：
 * ValueRule (根解释器)
 * ├── PrimitiveRule (基本类型)
 * ├── ListRule (列表)
 * ├── TupleRule (元组)
 * └── DictOrSetRule (字典/集合)
 * <p>
 * 核心组件：
 * - ParseContext: 解析上下文，管理 token 流
 * - GrammarRule: 解释器接口，定义解析契约
 * - 具体解释器: 实现各种语法规则
 * <p>
 * 使用方式：
 * ```java
 * Parser parser = new Parser(tokens);
 * PythonValue result = parser.parse();
 * ```
 *
 * @author Generated with Claude Code
 * @version 3.0
 */
public class Parser {
    private final List<Token> tokens;

    /**
     * 构造语法分析器
     *
     * @param tokens 词法分析器产生的 token 序列
     * @throws IllegalArgumentException 如果 token 序列无效
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * 解析 Python 对象
     * <p>
     * 使用解释器模式将 token 序列转换为抽象语法树（AST）
     *
     * @return 解析得到的 Python 值对象（AST 根节点）
     * @throws IllegalArgumentException 如果遇到语法错误
     */
    public PythonValue parse() {
        GrammarRule rootRule = new ValueRule();
        ParseContext context = new ParseContext(tokens);
        return rootRule.parse(context);
    }
}