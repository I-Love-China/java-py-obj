package com.github.typist.lexer;

import java.util.List;

/**
 * 词法分析器（Lexical Analyzer / Scanner）- 状态模式版本
 * 
 * 词法分析器是编译器前端的第一个组件，负责将输入的字符流转换为记号流。
 * 重构后采用状态模式实现，提供更好的扩展性和维护性。
 * 
 * 设计模式：
 * - 状态模式（State Pattern）：将不同的词法识别逻辑分离到独立的状态类中
 * - 包级封装：所有实现细节隐藏在lexer包内，只暴露核心功能
 * 
 * 架构优势：
 * 1. 单一职责：每个状态类只处理特定类型的词素
 * 2. 易于扩展：添加新词素类型无需修改现有代码
 * 3. 状态清晰：词法分析的状态转换逻辑明确
 * 4. 易于测试：可以独立测试各个状态的处理逻辑
 * 5. 封装完善：实现细节完全隐藏，对外接口简洁
 * 
 * 支持的Python语法元素：
 * - 数字：整数和浮点数，包括负数
 * - 字符串：单引号和双引号，支持转义字符
 * - 布尔值：True, False
 * - 空值：None
 * - 容器分隔符：[], {}, (), 逗号, 冒号
 * 
 * @author typist
 * @version 1.1
 */
public class Lexer {
    
    /**
     * 词法分析器上下文
     * 
     * 使用组合模式，将具体的词法分析工作委托给LexerContext。
     * 现在所有组件都在同一个包内，可以直接访问。
     */
    private final String input;
    
    /**
     * 构造词法分析器
     * 
     * @param input 要分析的输入字符串，不能为null
     * @throws IllegalArgumentException 如果input为null
     */
    public Lexer(String input) {
        this.input = input;
    }
    
    /**
     * 执行词法分析，将输入字符串转换为记号序列
     * 
     * 使用状态模式实现的词法分析，具有更好的扩展性和维护性：
     * 
     * 状态流转：
     * DispatchState → 根据字符类型分发到专门状态
     * ├── NumberState → 处理数字 → 返回DispatchState
     * ├── StringState → 处理字符串 → 返回DispatchState  
     * ├── IdentifierState → 处理标识符 → 返回DispatchState
     * └── ErrorState → 处理错误情况
     * 
     * @return 记号列表，以EOF记号结尾，不会返回null
     * @throws IllegalArgumentException 当遇到无法识别的字符时
     */
    public List<Token> tokenize() {
        return new LexerContext(input).tokenize();
    }
}