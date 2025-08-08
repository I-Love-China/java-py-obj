package com.github.typist;

/**
 * 词法分析中的记号（Token）类
 * 
 * 在编译原理中，记号是词法分析的基本单位，包含以下要素：
 * 1. 记号类型（TokenType）：标识记号的种类
 * 2. 记号值（value）：具体的词素内容或解析后的值
 * 3. 位置信息（position）：用于错误报告和调试
 * 
 * 记号是连接词法分析和语法分析的桥梁：
 * 词法分析器产生记号序列 → 语法分析器消费记号序列
 * 
 * 示例记号：
 * - 数字 "42" → Token(NUMBER, 42, position)
 * - 字符串 "'hello'" → Token(STRING, "hello", position)  
 * - 布尔 "True" → Token(BOOLEAN, true, position)
 * - 分隔符 "[" → Token(LEFT_BRACKET, '[', position)
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
public class Token {
    
    // ========================= 核心属性 =========================
    
    /**
     * 记号类型
     * 
     * 决定了这个记号在语法中的作用和含义。
     * 语法分析器主要根据记号类型来做决策。
     * 
     * @see TokenType 支持的所有记号类型
     */
    private final TokenType type;
    
    /**
     * 记号的值
     * 
     * 存储记号的具体内容，类型根据记号类型而定：
     * - NUMBER: Integer, Long, 或 Double
     * - STRING: String（不包含引号）
     * - BOOLEAN: Boolean (true/false)
     * - NULL: null
     * - 分隔符: Character ('[', ']', '{', '}', '(', ')', ',', ':')
     * - EOF: null
     * 
     * 使用Object类型以支持多种数据类型，体现了多态性。
     */
    private final Object value;
    
    /**
     * 记号在源代码中的位置
     * 
     * 用于：
     * 1. 错误报告：准确指出错误位置
     * 2. 调试信息：帮助开发者定位问题
     * 3. IDE支持：语法高亮、错误标记等
     * 
     * 位置从0开始计算，表示字符在输入字符串中的索引。
     */
    private final int position;

    // ========================= 构造函数 =========================
    
    /**
     * 构造一个记号对象
     * 
     * @param type 记号类型，决定记号的语法作用
     * @param value 记号值，包含具体的语义信息
     * @param position 记号在源码中的位置，用于错误报告
     * 
     * @throws IllegalArgumentException 如果type为null
     */
    public Token(TokenType type, Object value, int position) {
        if (type == null) {
            throw new IllegalArgumentException("Token type cannot be null");
        }
        
        this.type = type;
        this.value = value;
        this.position = position;
    }

    // ========================= 访问器方法 =========================
    
    /**
     * 获取记号类型
     * 
     * 这是语法分析器最常用的方法，用于判断当前记号的种类，
     * 从而决定应该采用哪种语法规则进行分析。
     * 
     * @return 记号类型，永远不会为null
     */
    public TokenType getType() {
        return type;
    }

    /**
     * 获取记号值
     * 
     * 返回记号的具体内容。调用者需要根据记号类型来判断
     * 返回值的实际类型并进行相应的类型转换。
     * 
     * 类型对应关系：
     * - TokenType.NUMBER → Integer/Long/Double
     * - TokenType.STRING → String
     * - TokenType.BOOLEAN → Boolean
     * - TokenType.NULL → null
     * - 其他分隔符 → Character
     * 
     * @return 记号的值，可能为null（如NULL类型和EOF类型）
     */
    public Object getValue() {
        return value;
    }

    /**
     * 获取记号位置
     * 
     * 返回记号在源代码中的字符位置，主要用于：
     * - 生成有意义的错误消息
     * - 调试和开发工具支持
     * - 源代码映射（source mapping）
     * 
     * @return 字符位置（从0开始的索引）
     */
    public int getPosition() {
        return position;
    }

    // ========================= 辅助方法 =========================
    
    /**
     * 返回记号的字符串表示
     * 
     * 主要用于：
     * 1. 调试输出：查看词法分析的结果
     * 2. 日志记录：记录编译过程信息
     * 3. 测试验证：检查词法分析是否正确
     * 
     * 格式：Token{type=类型, value=值, pos=位置}
     * 示例：Token{type=NUMBER, value=42, pos=10}
     * 
     * @return 易于阅读的字符串表示
     */
    @Override
    public String toString() {
        return String.format("Token{type=%s, value=%s, pos=%d}", 
                           type, value, position);
    }
    
    /**
     * 检查两个记号是否相等
     * 
     * 记号相等的条件：
     * 1. 记号类型相同
     * 2. 记号值相同（使用Objects.equals处理null值）
     * 3. 位置信息相同
     * 
     * 注意：在语法分析中，通常只比较类型和值，位置信息主要用于错误报告。
     * 
     * @param obj 要比较的对象
     * @return 如果记号相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Token token = (Token) obj;
        return position == token.position &&
               type == token.type &&
               java.util.Objects.equals(value, token.value);
    }
    
    /**
     * 返回记号的哈希码
     * 
     * 基于记号类型、值和位置计算哈希码，确保相等的记号
     * 具有相同的哈希码，支持在哈希集合中使用。
     * 
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, value, position);
    }
}