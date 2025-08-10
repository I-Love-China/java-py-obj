package com.github.typist.lexer;

/**
 * 词法分析器状态接口
 * 
 * 实现状态模式的核心接口，每个具体状态负责处理特定类型的字符序列。
 * 状态之间通过LexerContext进行通信和协调。
 * 
 * 包内接口，不对外暴露具体的状态实现细节。
 * 
 * @author typist
 * @version 1.1
 */
interface LexerState {
    
    /**
     * 处理当前字符
     * 
     * 每个状态实现自己的字符处理逻辑，可能包括：
     * 1. 读取和识别字符序列
     * 2. 生成相应的Token
     * 3. 转换到其他状态
     * 4. 处理错误情况
     * 
     * @param context 词法分析器上下文，提供字符操作和状态管理功能
     * @return true表示处理完成可以继续，false表示需要特殊处理
     */
    boolean process(LexerContext context);
}