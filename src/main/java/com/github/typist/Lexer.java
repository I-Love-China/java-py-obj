package com.github.typist;

import java.util.ArrayList;
import java.util.List;

/**
 * 词法分析器（Lexical Analyzer / Scanner）
 * 
 * 词法分析器是编译器前端的第一个组件，负责将输入的字符流转换为记号流。
 * 它实现了编译原理中的词法分析阶段，是连接源代码和语法分析的桥梁。
 * 
 * 核心功能：
 * 1. 字符流扫描：逐个读取输入字符
 * 2. 模式识别：识别数字、字符串、标识符、分隔符等
 * 3. 记号生成：将识别的模式转换为记号对象
 * 4. 错误处理：报告词法错误
 * 
 * 支持的Python语法元素：
 * - 数字：整数和浮点数，包括负数
 * - 字符串：单引号和双引号，支持转义字符
 * - 布尔值：True, False
 * - 空值：None
 * - 容器分隔符：[], {}, (), 逗号, 冒号
 * 
 * 词法分析流程：
 * 输入字符流 → 状态转换 → 记号识别 → 记号序列输出
 * 
 * 实现技术：
 * - 有限状态自动机（Finite State Automaton）
 * - 向前看（Lookahead）技术
 * - 最长匹配原则
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
public class Lexer {
    // ========================= 核心状态 =========================
    
    /**
     * 输入字符串
     * 
     * 词法分析器要处理的完整输入文本。
     * 使用final确保输入不可变，避免分析过程中的意外修改。
     */
    private final String input;
    
    /**
     * 当前字符位置
     * 
     * 指向input中当前正在处理的字符的索引。
     * 从0开始，用于：
     * 1. 跟踪扫描进度
     * 2. 计算记号位置
     * 3. 错误报告定位
     */
    private int position;
    
    /**
     * 当前字符
     * 
     * 缓存当前position位置的字符，避免重复的数组访问。
     * 使用'\0'作为文件结束标记（End of File），这是编译原理中的常见做法。
     * 
     * 状态转换依据：
     * - 字母/下划线 → 标识符状态
     * - 数字/负号 → 数字状态  
     * - 引号 → 字符串状态
     * - 分隔符 → 直接识别
     * - 空白符 → 跳过
     */
    private char currentChar;

    // ========================= 构造函数 =========================
    
    /**
     * 构造词法分析器
     * 
     * 初始化词法分析器的状态，准备开始词法分析。
     * 设置初始位置为0，如果输入为空则设置EOF标记。
     * 
     * @param input 要分析的输入字符串，不能为null
     * @throws IllegalArgumentException 如果input为null
     */
    public Lexer(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        this.input = input;
        this.position = 0;
        // 安全地获取第一个字符，空字符串返回EOF标记
        this.currentChar = input.length() > 0 ? input.charAt(0) : '\0';
    }

    
    // ========================= 字符操作方法 =========================
    
    /**
     * 向前移动一个字符
     * 
     * 这是词法分析器的核心操作，实现了输入流的顺序扫描。
     * 使用有限状态自动机的状态转换机制。
     * 
     * 操作流程：
     * 1. position自增，指向下一个字符
     * 2. 更新currentChar为新位置的字符
     * 3. 如果超出输入范围，设置EOF标记
     * 
     * 边界情况处理：
     * - 到达输入末尾时，设置currentChar为'\0'
     * - 避免数组越界异常
     */
    private void advance() {
        position++;
        // 安全地检查边界，避免数组越界
        currentChar = position < input.length() ? input.charAt(position) : '\0';
    }

    /**
     * 跳过空白字符
     * 
     * 空白字符在大多数编程语言中只起分隔作用，没有语义意义。
     * 词法分析器需要跳过它们，只保留有意义的记号。
     * 
     * 处理的空白字符类型：
     * - 空格 ' '
     * - 制表符 '\t'
     * - 换行符 '\n'
     * - 回车符 '\r'
     * - 其他Unicode空白字符
     * 
     * 使用Character.isWhitespace()方法确保完整的Unicode支持。
     */
    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    
    // ========================= 词素读取方法 =========================
    
    /**
     * 读取数字词素
     * 
     * 识别和读取整数和浮点数，支持负数。
     * 实现了数字的有限状态自动机识别。
     * 
     * 支持的数字格式：
     * - 正整数：123, 0, 999
     * - 负整数：-123, -0
     * - 浮点数：3.14, -2.5, 0.0
     * - 小数：.5 (会被读取为 0.5)
     * 
     * 状态转换图：
     * 开始 → [负号] → 整数部分 → [小数点] → 小数部分 → 结束
     * 
     * @return 数字字符串表示，由调用者负责解析为具体数值类型
     */
    private String readNumber() {
        StringBuilder result = new StringBuilder();
        
        // 处理可选的负号前缀
        if (currentChar == '-') {
            result.append(currentChar);
            advance();
        }
        
        // 读取整数部分（或小数点前的数字）
        while (currentChar != '\0' && (Character.isDigit(currentChar) || currentChar == '.')) {
            result.append(currentChar);
            advance();
        }
        
        return result.toString();
    }

    /**
     * 读取字符串词素
     * 
     * 处理单引号和双引号包围的字符串，支持转义字符处理。
     * 这是上下文敏感的词法分析，在字符串模式下工作。
     * 
     * 状态转换：
     * 普通模式 → [遇到引号] → 字符串模式 → [匹配引号] → 普通模式
     * 
     * 支持的转义字符：
     * - \n → 换行符
     * - \t → 制表符
     * - \r → 回车符
     * - \\\\ → 反斜杠
     * - \' → 单引号
     * - \" → 双引号
     * - 其他 → 原样保留
     * 
     * 错误处理：
     * - 未闭合的字符串：到达文件末尾时仍未找到匹配引号
     * 
     * @param quote 开始的引号类型（'或"），用于匹配结束引号
     * @return 字符串内容（不包含引号，已处理转义字符）
     */
    private String readString(char quote) {
        StringBuilder result = new StringBuilder();
        advance(); // 跳过开始引号
        
        // 循环读取字符直到找到匹配的结束引号
        while (currentChar != '\0' && currentChar != quote) {
            if (currentChar == '\\') {
                // 处理转义字符序列
                advance(); // 跳过反斜杠
                switch (currentChar) {
                    case 'n':
                        result.append('\n'); // 换行
                        break;
                    case 't':
                        result.append('\t'); // 制表
                        break;
                    case 'r':
                        result.append('\r'); // 回车
                        break;
                    case '\\':
                        result.append('\\'); // 反斜杠
                        break;
                    case '\'':
                        result.append('\''); // 单引号
                        break;
                    case '"':
                        result.append('"'); // 双引号
                        break;
                    default:
                        // 未知转义字符，原样保留
                        result.append(currentChar);
                }
            } else {
                // 普通字符，直接添加
                result.append(currentChar);
            }
            advance();
        }
        
        // 处理结束引号
        if (currentChar == quote) {
            advance(); // 跳过结束引号
        }
        // 注意：如果没有找到匹配的结束引号，也不抛出异常
        // 这里采用容错处理，让语法分析器来发现错误
        
        return result.toString();
    }

    /**
     * 读取标识符词素
     * 
     * 识别和读取符合Python标识符规范的字符序列。
     * 标识符用于表示变量名、函数名和关键字。
     * 
     * Python标识符规则：
     * - 必须以字母或下划线开始
     * - 后续字符可以是字母、数字或下划线
     * - 区分大小写
     * - 不能是关键字
     * 
     * 正则表示式：[a-zA-Z_][a-zA-Z0-9_]*
     * 
     * 后续处理：
     * 读取完成后，调用者需要检查是否为关键字：
     * - True/False → BOOLEAN 类型
     * - None → NULL 类型
     * - 其他 → IDENTIFIER 类型
     * 
     * @return 标识符字符串
     */
    private String readIdentifier() {
        StringBuilder result = new StringBuilder();
        
        // 按照标识符规则读取字符
        // Character.isLetterOrDigit() 支持Unicode字符
        while (currentChar != '\0' && 
               (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            result.append(currentChar);
            advance();
        }
        
        return result.toString();
    }

    
    // ========================= 主要分析方法 =========================
    
    /**
     * 执行词法分析，将输入字符串转换为记号序列
     * 
     * 这是词法分析器的主入口，实现了完整的词法分析过程。
     * 使用有限状态自动机的原理，通过状态转换识别不同类型的记号。
     * 
     * 分析流程：
     * 1. 初始化记号列表
     * 2. 循环处理每个字符：
     *    a. 跳过空白字符
     *    b. 记录当前位置（用于记号位置信息）
     *    c. 根据当前字符类型选择相应的处理逻辑
     *    d. 生成相应的记号并添加到列表
     * 3. 添加EOF记号标记结束
     * 
     * 状态转换决策：
     * - 数字/负号 → 调用readNumber()
     * - 引号 → 调用readString()
     * - 字母/下划线 → 调用readIdentifier() + 关键字检查
     * - 分隔符 → 直接识别
     * - 其他 → 错误处理
     * 
     * 错误处理策略：
     * - 遇到未知字符时抛出IllegalArgumentException
     * - 提供详细的错误信息和位置信息
     * 
     * 时间复杂度：O(n)，其中n为输入字符串长度
     * 空间复杂度：O(m)，其中m为记号数量
     * 
     * @return 记号列表，以EOF记号结尾，不会返回null
     * @throws IllegalArgumentException 当遇到无法识别的字符时
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        
        // 主循环：处理所有输入字符
        while (currentChar != '\0') {
            // 第一步：跳过空白字符
            skipWhitespace();
            
            // 检查是否已经到达文件末尾
            if (currentChar == '\0') {
                break;
            }
            
            // 记录记号开始位置，用于错误报告和调试
            int tokenPos = position;
            
            // 状态转换决策树：根据字符类型选择处理逻辑
            if (Character.isDigit(currentChar) || currentChar == '-') {
                // ========== 数字处理 ==========
                String numberStr = readNumber();
                Object value;
                
                // 数值类型推断和转换
                if (numberStr.contains(".")) {
                    // 浮点数处理
                    value = Double.parseDouble(numberStr);
                } else {
                    // 整数处理：先尝试Integer，溢出后使用Long
                    try {
                        value = Integer.parseInt(numberStr);
                    } catch (NumberFormatException e) {
                        value = Long.parseLong(numberStr);
                    }
                }
                tokens.add(new Token(TokenType.NUMBER, value, tokenPos));
            }
            else if (currentChar == '\'' || currentChar == '"') {
                // ========== 字符串处理 ==========
                char quote = currentChar;
                String stringValue = readString(quote);
                tokens.add(new Token(TokenType.STRING, stringValue, tokenPos));
            }
            else if (Character.isLetter(currentChar) || currentChar == '_') {
                // ========== 标识符和关键字处理 ==========
                String identifier = readIdentifier();
                TokenType type;
                Object value;
                
                // 关键字检查和类型分派
                // 这里体现了上下文敏感的词法分析
                switch (identifier) {
                    case "True":
                        type = TokenType.BOOLEAN;
                        value = true;  // Java的boolean值
                        break;
                    case "False":
                        type = TokenType.BOOLEAN;
                        value = false; // Java的boolean值
                        break;
                    case "None":
                        type = TokenType.NULL;
                        value = null;  // Java的null值
                        break;
                    default:
                        // 非关键字，作为一般标识符处理
                        type = TokenType.IDENTIFIER;
                        value = identifier;
                }
                
                tokens.add(new Token(type, value, tokenPos));
            }
            else {
                // ========== 分隔符处理 ==========
                TokenType type;
                
                // 单字符分隔符的直接映射
                switch (currentChar) {
                    case '[':
                        type = TokenType.LEFT_BRACKET;  // 列表开始
                        break;
                    case ']':
                        type = TokenType.RIGHT_BRACKET; // 列表结束
                        break;
                    case '{':
                        type = TokenType.LEFT_BRACE;    // 字典/集合开始
                        break;
                    case '}':
                        type = TokenType.RIGHT_BRACE;   // 字典/集合结束
                        break;
                    case '(':
                        type = TokenType.LEFT_PAREN;    // 元组开始
                        break;
                    case ')':
                        type = TokenType.RIGHT_PAREN;   // 元组结束
                        break;
                    case ',':
                        type = TokenType.COMMA;         // 元素分隔符
                        break;
                    case ':':
                        type = TokenType.COLON;         // 键值分隔符
                        break;
                    default:
                        // 遇到无法识别的字符：报告详细错误
                        throw new IllegalArgumentException(
                            "Unexpected character: '" + currentChar + 
                            "' (ASCII: " + (int)currentChar + ") at position " + position);
                }
                
                // 生成分隔符记号并前进
                tokens.add(new Token(type, currentChar, tokenPos));
                advance();
            }
        }
        
        // 添加文件结束标记，表示词法分析完成
        // 这是编译原理中的标准做法，方便语法分析器判断结束
        tokens.add(new Token(TokenType.EOF, null, position));
        
        return tokens;
    }
}