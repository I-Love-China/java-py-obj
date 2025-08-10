package com.github.typist.parser;

/**
 * 语法规则解释器接口
 * 
 * 解释器模式的核心抽象，每个语法规则都实现此接口。
 * 这是编译原理中递归下降分析器的面向对象实现。
 * 
 * 核心思想：
 * - 每个非终结符对应一个解释器类
 * - 每个解释器负责识别和处理一种语法结构
 * - 通过组合和递归调用实现复杂语法的解析
 * 
 * 解释器模式优势：
 * - 易于扩展：新增语法规则只需添加新的解释器
 * - 职责清晰：每个解释器只处理一种语法结构
 * - 符合开闭原则：扩展时无需修改现有代码
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