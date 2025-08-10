package com.github.typist.lexer;

import com.github.typist.Token;
import com.github.typist.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 * 词法分析器上下文类
 * 
 * 实现状态模式中的Context角色，管理状态转换和提供状态操作接口。
 * 重构后的词法分析器核心，将原有的单一职责分离为状态管理和字符操作。
 * 
 * 包内类，不直接对外暴露，通过Lexer类提供服务。
 * 
 * @author Generated with Claude Code
 * @version 2.0
 */
class LexerContext {
    
    // ========================= 核心状态 =========================
    
    private final String input;
    private int position;
    private char currentChar;
    private LexerState currentState;
    private List<Token> tokens;
    
    // 状态实例 - 单例模式避免重复创建
    private final LexerState dispatchState;
    private final LexerState numberState;
    private final LexerState stringState;
    private final LexerState identifierState;
    private final LexerState delimiterState;
    private final LexerState errorState;
    
    // ========================= 构造函数 =========================
    
    LexerContext(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        this.input = input;
        this.position = 0;
        this.currentChar = input.length() > 0 ? input.charAt(0) : '\0';
        this.tokens = new ArrayList<>();
        
        // 初始化所有状态实例
        this.dispatchState = new DispatchState();
        this.numberState = new NumberState();
        this.stringState = new StringState();
        this.identifierState = new IdentifierState();
        this.delimiterState = new DelimiterState();
        this.errorState = new ErrorState();
        
        // 设置初始状态为分发状态
        this.currentState = dispatchState;
    }
    
    // ========================= 公共接口 =========================
    
    /**
     * 执行词法分析
     */
    List<Token> tokenize() {
        tokens.clear();
        
        while (currentChar != '\0') {
            if (!currentState.process(this)) {
                break; // 状态处理失败，停止分析
            }
        }
        
        // 添加EOF标记
        tokens.add(new Token(TokenType.EOF, null, position));
        return tokens;
    }
    
    // ========================= 状态管理 =========================
    
    void transitionToDispatch() { this.currentState = dispatchState; }
    void transitionToNumber() { this.currentState = numberState; }
    void transitionToString() { this.currentState = stringState; }
    void transitionToIdentifier() { this.currentState = identifierState; }
    void transitionToDelimiter() { this.currentState = delimiterState; }
    void transitionToError() { this.currentState = errorState; }
    
    // ========================= 字符操作 =========================
    
    void advance() {
        position++;
        currentChar = position < input.length() ? input.charAt(position) : '\0';
    }
    
    char getCurrentChar() {
        return currentChar;
    }
    
    int getPosition() {
        return position;
    }
    
    char peekChar(int offset) {
        int peekPos = position + offset;
        return peekPos < input.length() ? input.charAt(peekPos) : '\0';
    }
    
    // ========================= Token管理 =========================
    
    void addToken(TokenType type, Object value, int tokenPos) {
        tokens.add(new Token(type, value, tokenPos));
    }
}