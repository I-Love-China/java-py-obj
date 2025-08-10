package com.github.typist;

import com.github.typist.lexer.Lexer;
import com.github.typist.lexer.Token;
import com.github.typist.parser.Parser;
import com.github.typist.parser.PythonValue;
import com.github.typist.visitor.JsonNodeVisitor;
import com.github.typist.visitor.JavaObjectVisitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Python对象解析器 - 编译器系统的顶层门面类
 * 
 * 这是整个Python对象到JSON转换系统的主入口，集成了完整的编译流水线：
 * 词法分析 → 语法分析 → 语义分析 → 代码生成 → 输出格式化
 * 
 * 系统架构：
 * ┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌─────────────┐
 * │ 输入字符串   │ -> │  词法分析器   │ -> │  语法分析器   │ -> │ JSON转换器  │
 * │ Python语法  │    │   (Lexer)    │    │  (Parser)   │    │ (Converter) │
 * └─────────────┘    └──────────────┘    └──────────────┘    └─────────────┘
 *         ↓                   ↓                   ↓                   ↓
 * "'hello world'"     Token序列        PythonValue        JSON字符串
 * [1, 2, 3]          NUMBER, COMMA...   ListValue         "[1,2,3]"
 * {'key': 'value'}   LBRACE, STRING...  DictValue         {"key":"value"}
 * 
 * 核心特性：
 * 1. **完整的编译流程**：实现了从源码到目标格式的完整转换
 * 2. **多格式输出**：支持JSON字符串和Java对象两种输出格式
 * 3. **错误处理与报告**：提供详细的错误信息和位置定位
 * 4. **类型安全转换**：确保Python类型到JSON类型的正确映射
 * 5. **嵌套结构支持**：处理任意深度的嵌套数据结构
 * 
 * 支持的Python数据类型：
 * ┌─────────────┬─────────────────┬──────────────────┬─────────────────┐
 * │ Python类型  │     示例        │   中间表示       │   JSON输出      │
 * ├─────────────┼─────────────────┼──────────────────┼─────────────────┤
 * │ int         │ 42, -10         │ PrimitiveValue   │ 42, -10         │
 * │ float       │ 3.14, -2.5      │ PrimitiveValue   │ 3.14, -2.5      │
 * │ bool        │ True, False     │ PrimitiveValue   │ true, false     │
 * │ None        │ None            │ PrimitiveValue   │ null            │
 * │ str         │ 'hello', "world"│ PrimitiveValue   │ "hello", "world"│
 * │ list        │ [1, 2, 3]       │ ListValue        │ [1,2,3]         │
 * │ tuple       │ (1, 2, 3)       │ TupleValue       │ [1,2,3]         │
 * │ set         │ {1, 2, 3}       │ SetValue         │ [1,2,3]         │
 * │ dict        │ {'a': 1}        │ DictValue        │ {"a":1}         │
 * └─────────────┴─────────────────┴──────────────────┴─────────────────┘
 * 
 * 使用示例：
 * ```java
 * PythonObjectParser parser = new PythonObjectParser();
 * 
 * // 转换为JSON字符串
 * String json = parser.parseToJson("{'name': 'Alice', 'age': 25}");
 * // 输出: {"name":"Alice","age":25}
 * 
 * // 转换为Java对象
 * Object obj = parser.parseToObject("[1, 2, 3]");
 * // 输出: Object[] {1, 2, 3}
 * ```
 * 
 * 设计模式应用：
 * - **门面模式 (Facade Pattern)**：隐藏复杂的编译流程细节
 * - **管道模式 (Pipeline Pattern)**：数据在各个阶段间流转
 * - **访问者模式 (Visitor Pattern)**：类型转换逻辑与数据结构分离
 * - **策略模式 (Strategy Pattern)**：支持多种输出格式
 * - **模板方法模式**：标准化的解析流程
 * 
 * 性能特征：
 * - 时间复杂度：O(n)，其中n为输入字符串长度
 * - 空间复杂度：O(d + m)，其中d为嵌套深度，m为节点数量
 * - 内存使用：临时对象较多，适合短生命周期使用
 * 
 * 线程安全性：
 * - 实例变量只有ObjectMapper，且是线程安全的
 * - 每次解析都创建新的词法和语法分析器
 * - 可以安全地在多线程环境中使用同一个实例
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
public class PythonObjectParser {
    
    // ========================= 核心依赖 =========================
    
    /**
     * Jackson JSON处理器
     * 
     * 用于JSON字符串的序列化，线程安全。
     * 在访问者模式重构后，主要用于最终的JSON字符串生成。
     */
    private final ObjectMapper objectMapper;
    
    /**
     * JSON节点转换访问者
     * 
     * 使用访问者模式实现PythonValue到JsonNode的转换。
     * 访问者模式的优势：
     * - 符合开放封闭原则：新增类型时无需修改现有代码
     * - 职责分离：转换逻辑与业务流程分离
     * - 易于扩展：可以轻松添加新的输出格式
     * - 提高可测试性：访问者可以独立测试
     */
    private final JsonNodeVisitor jsonNodeVisitor;
    
    /**
     * Java对象转换访问者
     * 
     * 使用访问者模式实现PythonValue到Java原生对象的转换。
     * 直接输出Java对象，跳过JSON序列化步骤，提高性能。
     */
    private final JavaObjectVisitor javaObjectVisitor;

    // ========================= 构造函数 =========================
    
    /**
     * 构造Python对象解析器
     * 
     * 初始化JSON处理器和访问者对象，准备开始解析工作。
     * 使用访问者模式重构后，转换逻辑委托给专门的访问者处理。
     * 
     * 组件初始化：
     * - ObjectMapper：用于JSON序列化
     * - JsonNodeVisitor：处理到JsonNode的转换
     * - JavaObjectVisitor：处理到Java对象的转换
     * 
     * 设计优势：
     * - 职责分离：解析器专注于流程编排
     * - 可扩展性：可以轻松添加新的访问者
     * - 可测试性：每个组件都可以独立测试
     */
    public PythonObjectParser() {
        this.objectMapper = new ObjectMapper();
        this.jsonNodeVisitor = new JsonNodeVisitor(objectMapper);
        this.javaObjectVisitor = new JavaObjectVisitor();
    }

    // ========================= 公共API方法 =========================
    
    /**
     * 将Python对象字符串解析为JSON字符串
     * 
     * 这是最常用的API方法，实现了从Python语法到JSON字符串的直接转换。
     * 整个转换过程经历了编译器的主要阶段，最终输出标准的JSON格式。
     * 
     * 转换流程：
     * 1. **词法分析阶段**：将输入字符串分解为记号序列
     *    输入: "{'name': 'John'}" 
     *    输出: [LBRACE, STRING, COLON, STRING, RBRACE, EOF]
     * 
     * 2. **语法分析阶段**：将记号序列解析为抽象语法树
     *    输入: Token序列
     *    输出: DictValue{PrimitiveValue("name") -> PrimitiveValue("John")}
     * 
     * 3. **语义分析阶段**：将AST转换为JsonNode树
     *    输入: PythonValue对象
     *    输出: ObjectNode{"name" -> TextNode("John")}
     * 
     * 4. **代码生成阶段**：将JsonNode序列化为JSON字符串
     *    输入: JsonNode树
     *    输出: "{\"name\":\"John\"}"
     * 
     * 支持的输入格式：
     * - 基本类型：42, 3.14, True, False, None, 'string'
     * - 容器类型：[1,2,3], (1,2,3), {1,2,3}, {'key': 'value'}
     * - 嵌套结构：{'users': [{'name': 'Alice'}]}
     * - 混合类型：[1, 'hello', True, None]
     * 
     * 输出特性：
     * - 标准JSON格式，符合RFC 7159规范
     * - 紧凑输出，无多余空格
     * - UTF-8编码，支持Unicode字符
     * - 数字类型保持精度
     * 
     * 错误处理：
     * - 词法错误：无效字符、未闭合字符串等
     * - 语法错误：缺失分隔符、括号不匹配等
     * - 类型错误：不支持的Python类型等
     * - 所有错误都包装为RuntimeException，包含详细错误信息
     * 
     * @param pythonString Python对象的字符串表示，不能为null
     * @return JSON格式的字符串，保证格式正确
     * @throws RuntimeException 如果解析过程中发生任何错误
     * @throws IllegalArgumentException 如果输入为null或格式无效
     * 
     * @see #parseToObject(String) 如需Java对象输出
     */
    public String parseToJson(String pythonString) {
        try {
            // 第1阶段：词法分析 - 字符流 → 记号流
            Lexer lexer = new Lexer(pythonString);
            List<Token> tokens = lexer.tokenize();
            
            // 第2阶段：语法分析 - 记号流 → 抽象语法树
            Parser parser = new Parser(tokens);
            PythonValue pythonValue = parser.parse();
            
            // 第3阶段：使用访问者模式转换 - AST → JsonNode树
            JsonNode jsonNode = pythonValue.accept(jsonNodeVisitor);
            
            // 第4阶段：代码生成 - JsonNode树 → JSON字符串
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            // 统一错误处理：包装所有异常为运行时异常
            throw new RuntimeException("Python对象解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将Python对象字符串解析为Java对象
     * 
     * 提供了另一种输出格式，直接转换为Java原生对象，跳过JSON序列化步骤。
     * 适合需要在Java代码中直接操作数据的场景，避免JSON字符串的开销。
     * 
     * 转换流程：
     * 1-2. 词法和语法分析（与parseToJson相同）
     * 3. 直接调用PythonValue.toJavaObject()进行转换
     * 
     * 输出类型映射：
     * - Python int/float → Java Integer/Long/Double
     * - Python str → Java String
     * - Python bool → Java Boolean
     * - Python None → Java null
     * - Python list/tuple/set → Java Object[]
     * - Python dict → Java Map<String, Object>
     * 
     * 使用场景：
     * - 需要在Java中直接处理数据
     * - 避免JSON字符串解析开销
     * - 与现有Java API集成
     * - 数据验证和类型检查
     * 
     * 注意事项：
     * - 返回的Object需要根据实际类型进行类型转换
     * - 嵌套结构返回Object[]和Map<String,Object>
     * - 不保证类型的深度不可变性
     * 
     * @param pythonString Python对象的字符串表示
     * @return 对应的Java对象，类型取决于输入的Python类型
     * @throws RuntimeException 如果解析过程中发生任何错误
     * 
     * @see #parseToJson(String) 如需JSON字符串输出
     */
    public Object parseToObject(String pythonString) {
        try {
            // 第1-2阶段：词法和语法分析（复用相同逻辑）
            Lexer lexer = new Lexer(pythonString);
            List<Token> tokens = lexer.tokenize();
            
            Parser parser = new Parser(tokens);
            PythonValue pythonValue = parser.parse();
            
            // 第3阶段：使用访问者模式直接转换为Java对象
            return pythonValue.accept(javaObjectVisitor);
            
        } catch (Exception e) {
            // 统一错误处理
            throw new RuntimeException("Python对象解析失败: " + e.getMessage(), e);
        }
    }


    // ========================= 演示和测试方法 =========================
    
    /**
     * 主方法：演示Python对象解析器的功能
     * 
     * 这个主方法提供了完整的功能演示，展示解析器处理各种Python数据类型的能力。
     * 通过一系列测试用例，验证了从简单的基本类型到复杂的嵌套结构的转换功能。
     * 
     * 测试用例覆盖范围：
     * 1. **基本类型测试**：
     *    - 整数：42
     *    - 浮点数：3.14
     *    - 布尔值：True, False
     *    - 空值：None
     *    - 字符串：'hello world'
     * 
     * 2. **容器类型测试**：
     *    - 列表：[1, 2, 3]
     *    - 元组：(1, 2, 3)
     *    - 集合：{1, 2, 3}
     *    - 字典：{'name': 'John', 'age': 30}
     * 
     * 3. **混合类型测试**：
     *    - 异构列表：[1, 'hello', True, None]
     *    - 嵌套结构：{'users': [{'name': 'Alice', 'active': True}, {'name': 'Bob', 'active': False}]}
     * 
     * 4. **复杂场景测试**：
     *    - 多层嵌套：包含null值、数组、对象的复合结构
     *    - Unicode支持：包含中文字符的字符串处理
     * 
     * 输出格式：
     * ```
     * Python Object to JSON Conversion Examples:
     * ==========================================
     * Python: 42                                      -> JSON: 42
     * Python: 'hello world'                           -> JSON: "hello world"
     * Python: [1, 2, 3]                              -> JSON: [1,2,3]
     * Python: {'name': 'John'}                       -> JSON: {"name":"John"}
     * ```
     * 
     * 错误处理演示：
     * - 如果某个测试用例解析失败，会显示错误信息而不是中断程序
     * - 每个测试用例独立执行，一个失败不影响其他测试
     * 
     * 实际应用指导：
     * - 开发者可以参考这些示例了解支持的语法格式
     * - 测试用例可以作为功能验证的基准
     * - 演示了错误处理的最佳实践
     * 
     * 性能观察：
     * - 可以通过运行时间观察不同复杂度输入的处理性能
     * - 嵌套层次越深，解析时间相应增加
     * - 适合进行基准测试和性能调优
     * 
     * @param args 命令行参数（当前版本未使用）
     */
    public static void main(String[] args) {
        PythonObjectParser parser = new PythonObjectParser();
        
        String[] testCases = {
            "42",
            "3.14",
            "True",
            "False",
            "None",
            "'hello world'",
            "[1, 2, 3]",
            "(1, 2, 3)",
            "{1, 2, 3}",
            "{'name': 'John', 'age': 30}",
            "[1, 'hello', True, None]",
            "{'users': [{'name': 'Alice', 'active': True}, {'name': 'Bob', 'active': False}]}",
                "{'personalTags': None, 'contentTags': [{'taxonomy1Tag': '汽车', 'taxonomy2Tags': ['汽车评测', '用车攻略']}], 'featureTags': None}"
        };
        
        System.out.println("Python Object to JSON Conversion Examples:");
        System.out.println("==========================================");
        
        for (String testCase : testCases) {
            try {
                String json = parser.parseToJson(testCase);
                System.out.printf("Python: %-60s -> JSON: %s%n", testCase, json);
            } catch (Exception e) {
                System.out.printf("Python: %-60s -> ERROR: %s%n", testCase, e.getMessage());
            }
        }
    }
}