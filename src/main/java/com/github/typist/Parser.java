package com.github.typist;

import java.util.*;

/**
 * 语法分析器（Syntax Parser）
 * 
 * 语法分析器是编译器前端的第二个重要组件，负责将词法分析器产生的记号序列
 * 转换为抽象语法树（Abstract Syntax Tree, AST）。它实现了Python对象语法的
 * 语法分析，使用递归下降分析法（Recursive Descent Parsing）。
 * 
 * 核心功能：
 * 1. 记号序列消费：按照语法规则有序消费记号
 * 2. 语法验证：检查记号序列是否符合Python对象的语法规则
 * 3. AST构建：将线性的记号序列转换为树状的语法结构
 * 4. 错误检测：发现并报告语法错误
 * 
 * 支持的Python语法结构：
 * - 基本类型：数字、字符串、布尔值、None
 * - 列表：[element1, element2, ...]
 * - 字典：{'key1': value1, 'key2': value2, ...}
 * - 元组：(element1, element2, ...)
 * - 集合：{element1, element2, ...}
 * - 嵌套结构：任意层次的嵌套组合
 * 
 * 语法规则（使用EBNF表示法）：
 * Value := Primitive | List | Dict | Set | Tuple
 * Primitive := NUMBER | STRING | BOOLEAN | NULL
 * List := '[' (Value (',' Value)*)? ']'
 * Dict := '{' (Value ':' Value (',' Value ':' Value)*)? '}'
 * Set := '{' (Value (',' Value)*)? '}'  // 与Dict的区别在于没有冒号
 * Tuple := '(' (Value (',' Value)*)? ')'
 * 
 * 实现技术：
 * - 递归下降分析：每个语法规则对应一个递归函数
 * - 向前看（Lookahead）：检查当前记号类型决定分析路径
 * - 左递归消除：避免无限递归的语法结构
 * - 错误恢复：提供有意义的错误信息
 * 
 * 算法复杂度：
 * - 时间复杂度：O(n)，其中n是记号数量
 * - 空间复杂度：O(d)，其中d是嵌套深度（递归调用栈）
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
public class Parser {
    
    // ========================= 解析器状态 =========================
    
    /**
     * 输入记号序列
     * 
     * 由词法分析器产生的记号列表，包含所有需要分析的语法元素。
     * 记号序列以EOF记号结尾，用于标记输入结束。
     */
    private final List<Token> tokens;
    
    /**
     * 当前记号位置指针
     * 
     * 指向tokens列表中当前正在分析的记号的索引。
     * 解析过程中会不断前进，直到达到EOF记号。
     */
    private int position;
    
    /**
     * 当前记号
     * 
     * 缓存当前位置的记号对象，避免重复的列表访问。
     * 这是向前看（lookahead）技术的基础，解析器根据当前记号
     * 的类型来决定使用哪个语法规则。
     */
    private Token currentToken;

    // ========================= 构造函数 =========================
    
    /**
     * 构造语法分析器
     * 
     * 初始化解析器状态，准备开始语法分析过程。
     * 设置位置指针为0，加载第一个记号。
     * 
     * @param tokens 词法分析器产生的记号序列，不能为null且至少包含EOF记号
     * @throws IllegalArgumentException 如果记号序列为空或null
     */
    public Parser(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Token list cannot be null or empty");
        }
        
        this.tokens = tokens;
        this.position = 0;
        this.currentToken = tokens.get(0);
    }

    // ========================= 记号操作方法 =========================
    
    /**
     * 前进到下一个记号
     * 
     * 这是解析器的基本操作，实现了记号流的顺序消费。
     * 使用边界检查避免越界访问，到达末尾时停留在最后一个记号（通常是EOF）。
     * 
     * 操作流程：
     * 1. 检查是否已经到达记号序列末尾
     * 2. 如果没有，将位置指针前进一位
     * 3. 更新currentToken为新位置的记号
     * 
     * 边界条件：
     * - 如果已经在最后一个记号，则不进行任何操作
     * - 确保currentToken始终指向有效记号
     */
    private void advance() {
        if (position < tokens.size() - 1) {
            position++;
            currentToken = tokens.get(position);
        }
    }
    
    /**
     * 期望特定类型的记号并消费它
     * 
     * 这是递归下降分析器的核心操作，用于验证语法规则的匹配。
     * 如果当前记号类型符合预期，则消费它（前进到下一个记号）；
     * 否则抛出语法错误异常。
     * 
     * 使用场景：
     * - 消费必需的分隔符（如 ',' ':' '[' ']' 等）
     * - 验证语法规则的固定部分
     * - 确保语法结构的完整性
     * 
     * 错误处理：
     * - 提供详细的错误信息，包括期望类型、实际类型和位置
     * - 抛出IllegalArgumentException，由上层调用者处理
     * 
     * @param expectedType 期望的记号类型
     * @throws IllegalArgumentException 如果当前记号类型不匹配
     */
    private void expect(TokenType expectedType) {
        if (currentToken.getType() != expectedType) {
            throw new IllegalArgumentException(
                String.format("语法错误：期望 %s，但在位置 %d 处得到 %s", 
                    expectedType, currentToken.getPosition(), currentToken.getType())
            );
        }
        advance();
    }

    // ========================= 主解析入口 =========================
    
    /**
     * 开始语法分析过程
     * 
     * 这是语法分析器的主入口点，开始将记号序列转换为抽象语法树。
     * 调用parseValue()方法来分析顶层的Python值。
     * 
     * 解析流程：
     * 1. 从第一个记号开始分析
     * 2. 根据记号类型选择相应的语法规则
     * 3. 递归构建语法树
     * 4. 返回根节点
     * 
     * @return 解析得到的Python值对象（AST的根节点）
     * @throws IllegalArgumentException 如果遇到语法错误
     */
    public PythonValue parse() {
        return parseValue();
    }

    // ========================= 语法规则实现 =========================
    
    /**
     * 解析Python值（语法规则：Value）
     * 
     * 这是语法分析的核心调度方法，根据当前记号类型决定使用哪个
     * 具体的语法规则。实现了LL(1)文法的向前看决策。
     * 
     * 决策表（记号类型 → 语法规则）：
     * - NUMBER/STRING/BOOLEAN/NULL → parsePrimitive()
     * - LEFT_BRACKET → parseList()
     * - LEFT_BRACE → parseDictOrSet()
     * - LEFT_PAREN → parseTuple()
     * 
     * 语法规则：
     * Value := Primitive | List | Dict | Set | Tuple
     * 
     * 错误处理：
     * 如果当前记号不能开始任何已知的语法结构，抛出语法错误。
     * 
     * @return 解析得到的Python值对象
     * @throws IllegalArgumentException 如果遇到意外的记号类型
     */
    private PythonValue parseValue() {
        switch (currentToken.getType()) {
            case NUMBER:
            case STRING:
            case BOOLEAN:
            case NULL:
                return parsePrimitive();
            case LEFT_BRACKET:
                return parseList();
            case LEFT_BRACE:
                return parseDictOrSet();
            case LEFT_PAREN:
                return parseTuple();
            default:
                throw new IllegalArgumentException(
                    "语法错误：在位置 " + currentToken.getPosition() + 
                    " 处遇到意外的记号 " + currentToken.getType()
                );
        }
    }
    
    /**
     * 解析基本类型值（语法规则：Primitive）
     * 
     * 处理Python的基本数据类型：数字、字符串、布尔值、None。
     * 这些类型在词法分析阶段已经完成了值的解析和类型转换，
     * 语法分析阶段只需要包装成PrimitiveValue对象。
     * 
     * 支持的类型：
     * - NUMBER：整数和浮点数
     * - STRING：字符串字面量
     * - BOOLEAN：True和False
     * - NULL：None值
     * 
     * 处理流程：
     * 1. 获取当前记号的值（已经是正确的Java类型）
     * 2. 消费当前记号
     * 3. 创建并返回PrimitiveValue包装对象
     * 
     * @return 包装了基本类型值的PythonValue对象
     */
    private PythonValue parsePrimitive() {
        Object value = currentToken.getValue();
        advance();
        return new PythonValue.PrimitiveValue(value);
    }
    
    /**
     * 解析列表（语法规则：List）
     * 
     * 处理Python的列表语法：[element1, element2, ...]
     * 列表是有序的、允许重复的元素集合，用方括号包围，逗号分隔。
     * 
     * 语法规则：
     * List := '[' (Value (',' Value)*)? ']'
     * 
     * 解析步骤：
     * 1. 消费开始方括号 '['
     * 2. 如果不是立即遇到 ']'，则解析第一个元素
     * 3. 循环处理逗号分隔的后续元素
     * 4. 消费结束方括号 ']'
     * 5. 创建并返回ListValue对象
     * 
     * 特殊情况处理：
     * - 空列表：[] （直接跳到步骤4）
     * - 尾随逗号：[1, 2, 3,] （在遇到 ']' 时停止解析元素）
     * 
     * 错误恢复：
     * 如果缺少结束方括号，expect()方法会抛出详细的错误信息。
     * 
     * @return 包含所有列表元素的ListValue对象
     * @throws IllegalArgumentException 如果语法不正确
     */
    private PythonValue parseList() {
        expect(TokenType.LEFT_BRACKET);  // 消费 '['
        List<PythonValue> elements = new ArrayList<>();
        
        // 处理非空列表
        if (currentToken.getType() != TokenType.RIGHT_BRACKET) {
            elements.add(parseValue());  // 递归解析第一个元素
            
            // 处理后续的逗号分隔元素
            while (currentToken.getType() == TokenType.COMMA) {
                advance();  // 消费逗号
                // 检查尾随逗号：[1, 2, 3,]
                if (currentToken.getType() != TokenType.RIGHT_BRACKET) {
                    elements.add(parseValue());  // 递归解析下一个元素
                }
            }
        }
        
        expect(TokenType.RIGHT_BRACKET);  // 消费 ']'
        return new PythonValue.ListValue(elements);
    }

    /**
     * 解析字典或集合（语法规则：Dict | Set）
     * 
     * 处理用花括号包围的Python语法结构，需要通过向前看技术
     * 来区分字典和集合，因为它们都以 '{' 开始。
     * 
     * 区分策略：
     * 1. 空结构 {} → 默认解释为空字典
     * 2. 遇到冒号 ':' → 字典结构 {'key': value}
     * 3. 没有冒号 → 集合结构 {element1, element2}
     * 4. 特殊情况：set() 构造器（目前未完全实现）
     * 
     * 语法规则：
     * Dict := '{' (Value ':' Value (',' Value ':' Value)*)? '}'
     * Set := '{' (Value (',' Value)*)? '}'
     * 
     * 解析流程：
     * 1. 消费开始花括号 '{'
     * 2. 检查空结构情况
     * 3. 处理特殊的 set() 构造器（保留功能）
     * 4. 解析第一个值
     * 5. 根据是否遇到冒号决定调用parseDict()还是parseSet()
     * 
     * 设计考虑：
     * - 使用延迟决策：先解析第一个元素，再根据后续记号决定类型
     * - 错误处理：如果语法不符合任何已知模式，会在后续方法中报错
     * 
     * @return DictValue或SetValue对象，取决于实际的语法结构
     * @throws IllegalArgumentException 如果语法不正确
     */
    private PythonValue parseDictOrSet() {
        expect(TokenType.LEFT_BRACE);  // 消费 '{'
        
        // 特殊情况1：空结构 {}，默认为空字典
        if (currentToken.getType() == TokenType.RIGHT_BRACE) {
            advance();
            return new PythonValue.DictValue(new HashMap<>());
        }
        
        // 特殊情况2：set() 构造器语法（保留功能，暂未完全使用）
        // 检查模式：set() 但这个逻辑可能需要调整，因为当前实现有问题
        if (currentToken.getType() == TokenType.IDENTIFIER && 
            "set".equals(currentToken.getValue()) && 
            position + 1 < tokens.size() && 
            tokens.get(position + 1).getType() == TokenType.LEFT_PAREN) {
            
            advance(); // 跳过 "set"
            expect(TokenType.LEFT_PAREN);   // 消费 '('
            expect(TokenType.RIGHT_PAREN);  // 消费 ')'
            expect(TokenType.RIGHT_BRACE);  // 消费 '}'
            return new PythonValue.SetValue(new ArrayList<>());
        }
        
        // 一般情况：解析第一个值，然后决定类型
        PythonValue firstValue = parseValue();
        
        // 决策点：检查下一个记号
        if (currentToken.getType() == TokenType.COLON) {
            // 发现冒号，这是字典结构
            return parseDict(firstValue);
        } else {
            // 没有冒号，这是集合结构
            return parseSet(firstValue);
        }
    }

    /**
     * 解析字典结构（语法规则：Dict）
     * 
     * 处理Python字典的键值对语法：{'key1': value1, 'key2': value2, ...}
     * 字典是无序的键值对映射，键必须是不可变类型。
     * 
     * 语法规则：
     * Dict := '{' (Value ':' Value (',' Value ':' Value)*)? '}'
     * 
     * 调用时机：
     * 此方法在parseDictOrSet()确定当前结构是字典后被调用，
     * 此时第一个键已经被解析，当前记号应该是冒号。
     * 
     * 解析步骤：
     * 1. 消费冒号 ':'
     * 2. 解析第一个值
     * 3. 将第一个键值对存入映射
     * 4. 循环处理后续的逗号分隔键值对
     * 5. 消费结束花括号 '}'
     * 
     * 特殊情况处理：
     * - 尾随逗号：{'a': 1, 'b': 2,} （在遇到 '}' 时停止）
     * - 键的类型：可以是任何Python值（数字、字符串等）
     * - 值的类型：可以是任何Python值，包括嵌套结构
     * 
     * 错误处理：
     * - 缺少冒号：expect()方法会报错
     * - 缺少结束花括号：expect()方法会报错
     * - 键值解析错误：parseValue()方法会递归报错
     * 
     * @param firstKey 已经解析的第一个键
     * @return 包含所有键值对的DictValue对象
     * @throws IllegalArgumentException 如果语法不正确
     */
    private PythonValue parseDict(PythonValue firstKey) {
        Map<PythonValue, PythonValue> entries = new HashMap<>();
        
        expect(TokenType.COLON);  // 消费冒号
        PythonValue firstValue = parseValue();  // 解析第一个值
        entries.put(firstKey, firstValue);  // 存储第一个键值对
        
        // 处理后续的逗号分隔键值对
        while (currentToken.getType() == TokenType.COMMA) {
            advance();  // 消费逗号
            // 检查尾随逗号：{'a': 1, 'b': 2,}
            if (currentToken.getType() != TokenType.RIGHT_BRACE) {
                PythonValue key = parseValue();    // 解析键
                expect(TokenType.COLON);           // 期望冒号
                PythonValue value = parseValue();  // 解析值
                entries.put(key, value);           // 存储键值对
            }
        }
        
        expect(TokenType.RIGHT_BRACE);  // 消费 '}'
        return new PythonValue.DictValue(entries);
    }

    /**
     * 解析集合结构（语法规则：Set）
     * 
     * 处理Python集合的语法：{element1, element2, element3, ...}
     * 集合是无序的、不重复的元素集合。
     * 
     * 语法规则：
     * Set := '{' (Value (',' Value)*)? '}'
     * 
     * 调用时机：
     * 此方法在parseDictOrSet()确定当前结构是集合后被调用，
     * 此时第一个元素已经被解析，当前记号不是冒号。
     * 
     * 解析步骤：
     * 1. 将已解析的第一个元素添加到列表
     * 2. 循环处理后续的逗号分隔元素
     * 3. 消费结束花括号 '}'
     * 
     * 特殊情况处理：
     * - 尾随逗号：{1, 2, 3,} （在遇到 '}' 时停止）
     * - 元素类型：可以是任何不可变的Python值
     * 
     * 实现注意：
     * 当前实现使用List存储元素，没有进行去重检查。
     * 假设输入的Python代码是正确的，不包含重复元素。
     * 在实际的Python中，集合会自动去重。
     * 
     * @param firstElement 已经解析的第一个元素
     * @return 包含所有集合元素的SetValue对象
     * @throws IllegalArgumentException 如果语法不正确
     */
    private PythonValue parseSet(PythonValue firstElement) {
        List<PythonValue> elements = new ArrayList<>();
        elements.add(firstElement);  // 添加第一个元素
        
        // 处理后续的逗号分隔元素
        while (currentToken.getType() == TokenType.COMMA) {
            advance();  // 消费逗号
            // 检查尾随逗号：{1, 2, 3,}
            if (currentToken.getType() != TokenType.RIGHT_BRACE) {
                elements.add(parseValue());  // 递归解析下一个元素
            }
        }
        
        expect(TokenType.RIGHT_BRACE);  // 消费 '}'
        return new PythonValue.SetValue(elements);
    }

    /**
     * 解析元组（语法规则：Tuple）
     * 
     * 处理Python的元组语法：(element1, element2, ...)
     * 元组是有序的、不可变的、允许重复的元素集合，用圆括号包围。
     * 
     * 语法规则：
     * Tuple := '(' (Value (',' Value)*)? ')'
     * 
     * 解析步骤：
     * 1. 消费开始圆括号 '('
     * 2. 如果不是立即遇到 ')'，则解析第一个元素
     * 3. 循环处理逗号分隔的后续元素
     * 4. 消费结束圆括号 ')'
     * 5. 创建并返回TupleValue对象
     * 
     * 特殊情况处理：
     * - 空元组：() （直接跳到步骤4）
     * - 单元素元组：(42,) 或 (42) （都被正确处理）
     * - 尾随逗号：(1, 2, 3,) （在遇到 ')' 时停止）
     * 
     * 注意事项：
     * Python中的单元素元组需要尾随逗号来区分，如 (42,) vs (42)，
     * 但在我们的解析器中，两种形式都被接受为元组。
     * 这简化了解析逻辑，因为在表达式上下文中，括号也用于分组。
     * 
     * 与列表的区别：
     * - 元组使用圆括号 ()，列表使用方括号 []
     * - 元组在Python中是不可变的，但JSON中都转换为数组
     * 
     * @return 包含所有元组元素的TupleValue对象
     * @throws IllegalArgumentException 如果语法不正确
     */
    private PythonValue parseTuple() {
        expect(TokenType.LEFT_PAREN);  // 消费 '('
        List<PythonValue> elements = new ArrayList<>();
        
        // 处理非空元组
        if (currentToken.getType() != TokenType.RIGHT_PAREN) {
            elements.add(parseValue());  // 递归解析第一个元素
            
            // 处理后续的逗号分隔元素
            while (currentToken.getType() == TokenType.COMMA) {
                advance();  // 消费逗号
                // 检查尾随逗号：(1, 2, 3,)
                if (currentToken.getType() != TokenType.RIGHT_PAREN) {
                    elements.add(parseValue());  // 递归解析下一个元素
                }
            }
        }
        
        expect(TokenType.RIGHT_PAREN);  // 消费 ')'
        return new PythonValue.TupleValue(elements);
    }
}