package com.github.typist.parser;

/**
 * 语法规则接口
 * 
 * 定义了语法分析器中语法规则的统一接口。每个具体的语法规则（如列表、字典、基本类型等）
 * 都实现此接口来处理对应的Python语法结构。
 * 
 * 语法规则层次：
 * - ValueRule：根规则，处理所有Python值类型
 * - PrimitiveRule：处理基本类型（数字、字符串、布尔值、None）
 * - ListRule：处理列表语法 [元素1, 元素2, ...]
 * - DictOrSetRule：处理字典 {key: value, ...} 和集合 {元素1, 元素2, ...}
 * - TupleRule：处理元组语法 (元素1, 元素2, ...)
 * 
 * 解析策略：
 * 1. 根据当前token类型选择合适的语法规则
 * 2. 递归解析嵌套结构
 * 3. 构建对应的PythonValue对象
 * 4. 处理语法错误并提供准确的错误位置
 * 
 * @author typist
 */
interface GrammarRule {

    /**
     * 解析语法规则
     * 
     * 每个具体的语法规则解释器实现此方法，
     * 根据当前的 token 流状态解析对应的语法结构。
     * 
     * @param context 解析上下文，包含 token 流和共享状态
     * @return 解析得到的 Python 值对象
     * @throws IllegalArgumentException 如果遇到语法错误
     */
    PythonValue parse(ParseContext context);
}