package com.github.typist.lexer;

import java.util.List;

/**
 * 词法分析器（Lexical Analyzer / Scanner）
 *
 * 词法分析器是编译器前端的核心组件，将输入字符流转换为有意义的记号序列。
 * 它识别程序源码中的词汇单元（如数字、字符串、标识符、运算符等），
 * 为后续的语法分析阶段提供结构化的输入。
 *
 * 核心功能：
 * - 词法扫描：逐字符读取输入，识别词汇边界
 * - 记号生成：将识别的词素转换为记号对象
 * - 错误检测：发现并报告词法错误（如非法字符）
 * - 位置跟踪：记录每个记号在源码中的位置信息
 *
 * 支持的 Python 语法元素：
 * - 数字：整数和浮点数，包括负数（如：42, 3.14, -10）
 * - 字符串：单引号和双引号字符串，支持转义字符（如：'hello', "world"）
 * - 布尔值：Python 布尔常量（True, False）
 * - 空值：Python 空值常量（None）
 * - 容器分隔符：列表[]、字典{}、元组()、逗号、冒号
 *
 * 使用示例：
 * <pre>
 * Lexer lexer = new Lexer("[1, 'hello', True]");
 * List&lt;Token&gt; tokens = lexer.tokenize();
 * // 生成：LEFT_BRACKET, NUMBER(1), COMMA, STRING("hello"), COMMA, BOOLEAN(true), RIGHT_BRACKET, EOF
 * </pre>
 *
 * 注意：内部采用状态模式实现，具有良好的扩展性和维护性，
 * 但这些实现细节对使用者透明。
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
     * 该方法扫描整个输入字符串，识别其中的词法单元并生成对应的记号。
     * 分析过程会跳过空白字符，识别数字、字符串、布尔值、空值和各种分隔符。
     *
     * 处理流程：
     * 1. 从头开始扫描输入字符串
     * 2. 跳过空白字符（空格、制表符、换行符等）
     * 3. 识别词法单元类型并提取对应的词素
     * 4. 生成记号对象并记录位置信息
     * 5. 继续扫描直到字符串结束
     * 6. 在记号序列末尾添加EOF标记
     *
     * @return 记号列表，按源码出现顺序排列，以EOF记号结尾，保证不为null
     * @throws IllegalArgumentException 当遇到无法识别的字符时，提供详细错误位置
     */
    public List<Token> tokenize() {
        return new LexerContext(input).tokenize();
    }
}