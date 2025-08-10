package com.github.typist;

import com.github.typist.lexer.Lexer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
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
     * 用于JSON数据的序列化和反序列化，以及JsonNode对象的创建。
     * Jackson是业界标准的JSON处理库，提供了：
     * - 高性能的JSON解析和生成
     * - 灵活的数据绑定
     * - JsonNode树模型API
     * - 线程安全的操作
     * 
     * 在本系统中的作用：
     * 1. 创建各种类型的JsonNode（ObjectNode、ArrayNode等）
     * 2. 将JsonNode序列化为JSON字符串
     * 3. 提供类型安全的JSON操作API
     */
    private final ObjectMapper objectMapper;

    // ========================= 构造函数 =========================
    
    /**
     * 构造Python对象解析器
     * 
     * 初始化JSON处理器，准备开始解析工作。
     * 使用默认配置的ObjectMapper，适合大多数使用场景。
     * 
     * ObjectMapper配置：
     * - 使用默认的序列化配置
     * - 支持所有标准JSON数据类型
     * - 紧凑输出格式（无多余空格）
     * 
     * 设计考虑：
     * - 使用final确保线程安全
     * - 延迟初始化避免不必要的开销
     * - 可扩展：未来可支持自定义配置
     */
    public PythonObjectParser() {
        this.objectMapper = new ObjectMapper();
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
            
            // 第3阶段：语义分析 - AST → JsonNode树
            JsonNode jsonNode = convertToJsonNode(pythonValue);
            
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
            
            // 第3阶段：直接转换为Java对象（跳过JsonNode）
            return pythonValue.toJavaObject();
            
        } catch (Exception e) {
            // 统一错误处理
            throw new RuntimeException("Python对象解析失败: " + e.getMessage(), e);
        }
    }

    // ========================= 内部转换逻辑 =========================
    
    /**
     * 将PythonValue对象转换为Jackson JsonNode对象
     * 
     * 这是整个编译流程中的核心转换方法，实现了从抽象语法树到JSON节点树的映射。
     * 该方法使用访问者模式的变体，通过instanceof检查来处理不同类型的Python值。
     * 
     * 转换映射规则：
     * ┌─────────────────┬──────────────────┬──────────────────────────────┐
     * │ Python类型      │ PythonValue类型  │ JsonNode类型                 │
     * ├─────────────────┼──────────────────┼──────────────────────────────┤
     * │ None            │ PrimitiveValue   │ NullNode                     │
     * │ bool            │ PrimitiveValue   │ BooleanNode                  │
     * │ int             │ PrimitiveValue   │ IntNode/LongNode             │
     * │ float           │ PrimitiveValue   │ DoubleNode                   │
     * │ str             │ PrimitiveValue   │ TextNode                     │
     * │ list            │ ListValue        │ ArrayNode                    │
     * │ tuple           │ TupleValue       │ ArrayNode                    │
     * │ set             │ SetValue         │ ArrayNode                    │
     * │ dict            │ DictValue        │ ObjectNode                   │
     * └─────────────────┴──────────────────┴──────────────────────────────┘
     * 
     * 递归处理策略：
     * 1. **基本类型**：直接创建对应的JsonNode
     * 2. **容器类型**：递归转换每个子元素
     * 3. **字典类型**：将键转换为字符串，递归转换值
     * 
     * 类型安全性：
     * - 所有instanceof检查确保类型匹配
     * - 使用Jackson的类型安全API创建节点
     * - 异常处理确保转换失败时提供清晰的错误信息
     * 
     * 性能考虑：
     * - 时间复杂度：O(n)，n为节点总数（包括嵌套节点）
     * - 空间复杂度：O(h + n)，h为嵌套深度，n为节点数
     * - 使用Jackson的高效节点创建API
     * 
     * 设计模式：
     * - **访问者模式**：通过类型检查分派到不同处理逻辑
     * - **递归组合**：处理嵌套的数据结构
     * - **工厂方法**：使用ObjectMapper创建不同类型的节点
     * 
     * @param pythonValue 要转换的Python值对象，不能为null
     * @return 对应的JsonNode对象，保证类型正确
     * @throws IllegalArgumentException 如果遇到不支持的Python值类型
     * @throws RuntimeException 如果转换过程中发生意外错误
     */
    private JsonNode convertToJsonNode(PythonValue pythonValue) {
        if (pythonValue instanceof PythonValue.PrimitiveValue) {
            Object value = pythonValue.toJavaObject();
            if (value == null) {
                return NullNode.getInstance();
            } else if (value instanceof Boolean) {
                return BooleanNode.valueOf((Boolean) value);
            } else if (value instanceof Integer) {
                return IntNode.valueOf((Integer) value);
            } else if (value instanceof Long) {
                return LongNode.valueOf((Long) value);
            } else if (value instanceof Double) {
                return DoubleNode.valueOf((Double) value);
            } else if (value instanceof String) {
                return TextNode.valueOf((String) value);
            }
        } else if (pythonValue instanceof PythonValue.ListValue) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (PythonValue element : ((PythonValue.ListValue) pythonValue).getElements()) {
                arrayNode.add(convertToJsonNode(element));
            }
            return arrayNode;
        } else if (pythonValue instanceof PythonValue.TupleValue) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (PythonValue element : ((PythonValue.TupleValue) pythonValue).getElements()) {
                arrayNode.add(convertToJsonNode(element));
            }
            return arrayNode;
        } else if (pythonValue instanceof PythonValue.SetValue) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (PythonValue element : ((PythonValue.SetValue) pythonValue).getElements()) {
                arrayNode.add(convertToJsonNode(element));
            }
            return arrayNode;
        } else if (pythonValue instanceof PythonValue.DictValue) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (java.util.Map.Entry<PythonValue, PythonValue> entry : 
                 ((PythonValue.DictValue) pythonValue).getEntries().entrySet()) {
                String key = String.valueOf(entry.getKey().toJavaObject());
                JsonNode value = convertToJsonNode(entry.getValue());
                objectNode.set(key, value);
            }
            return objectNode;
        }
        
        throw new IllegalArgumentException("Unsupported Python value type: " + pythonValue.getClass());
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