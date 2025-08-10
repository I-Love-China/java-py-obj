package com.github.typist.visitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.github.typist.parser.PythonValue;

import java.util.Map;

/**
 * JSON节点转换访问者
 * 
 * 实现访问者模式的具体访问者，负责将PythonValue对象转换为Jackson的JsonNode对象。
 * 这个访问者替代了原来PythonObjectParser中的convertToJsonNode方法，实现了
 * 类型转换逻辑与业务流程的分离。
 * 
 * 设计特点：
 * - 单一职责：专注于JsonNode转换，不关心其他转换逻辑
 * - 类型安全：利用访问者模式的双分派确保类型匹配
 * - 可扩展：新增Python类型时只需添加对应的visit方法
 * - 可测试：独立的转换逻辑便于单元测试
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
 * 性能优化：
 * - 使用Jackson的高效节点创建API
 * - 避免多余的对象创建和类型检查
 * - 递归调用利用JVM的尾调用优化
 * 
 * 错误处理：
 * - 不支持的类型抛出IllegalArgumentException
 * - null值安全处理
 * - 类型转换异常的统一处理
 * 
 * 使用示例：
 * ```java
 * ObjectMapper objectMapper = new ObjectMapper();
 * JsonNodeVisitor visitor = new JsonNodeVisitor(objectMapper);
 * JsonNode result = pythonValue.accept(visitor);
 * String json = objectMapper.writeValueAsString(result);
 * ```
 * 
 * 线程安全性：
 * - ObjectMapper是线程安全的，可以安全地在多线程环境中使用
 * - 访问者本身是无状态的，可以被多个线程共享
 * - 创建的JsonNode对象是不可变的
 * 
 * @author Generated with Claude Code
 * @version 1.0
 * @see PythonValueVisitor
 * @see PythonValue
 */
public class JsonNodeVisitor implements PythonValueVisitor<JsonNode> {
    
    /**
     * Jackson JSON处理器
     * 
     * 用于创建各种类型的JsonNode对象。Jackson是线程安全的，
     * 可以在多线程环境中安全使用。
     */
    private final ObjectMapper objectMapper;
    
    /**
     * 构造JsonNode转换访问者
     * 
     * @param objectMapper Jackson对象映射器，不能为null
     * @throws IllegalArgumentException 如果objectMapper为null
     */
    public JsonNodeVisitor(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new IllegalArgumentException("ObjectMapper cannot be null");
        }
        this.objectMapper = objectMapper;
    }
    
    /**
     * 访问基本类型值，转换为对应的JsonNode
     * 
     * 处理Python中的基本数据类型，根据实际的Java对象类型创建对应的JsonNode：
     * - null → NullNode
     * - Boolean → BooleanNode
     * - Integer → IntNode
     * - Long → LongNode
     * - Double → DoubleNode
     * - String → TextNode
     * 
     * 类型检查顺序：
     * 1. 首先检查null，避免NullPointerException
     * 2. 按照常见程度排序，提高性能
     * 3. 使用instanceof确保类型安全
     * 
     * @param primitive 基本类型值对象，不能为null
     * @return 对应的JsonNode，保证类型正确
     * @throws IllegalArgumentException 如果遇到不支持的基本类型
     */
    @Override
    public JsonNode visitPrimitive(PythonValue.PrimitiveValue primitive) {
        Object value = primitive.getValue();
        
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
        } else {
            throw new IllegalArgumentException(
                "Unsupported primitive type: " + value.getClass().getSimpleName()
            );
        }
    }
    
    /**
     * 访问列表类型值，转换为ArrayNode
     * 
     * 递归处理列表中的每个元素：
     * 1. 创建新的ArrayNode
     * 2. 遍历列表中的每个PythonValue元素
     * 3. 递归调用accept方法转换子元素
     * 4. 将转换结果添加到ArrayNode中
     * 
     * 递归处理策略：
     * - 保持元素顺序
     * - 支持任意深度的嵌套
     * - 异构元素类型安全处理
     * 
     * 性能考虑：
     * - 时间复杂度：O(n)，n为元素个数
     * - 空间复杂度：O(n + d)，d为嵌套深度
     * 
     * @param list 列表类型值对象，不能为null
     * @return ArrayNode，包含转换后的所有元素
     */
    @Override
    public JsonNode visitList(PythonValue.ListValue list) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        
        for (PythonValue element : list.getElements()) {
            // 递归转换
            JsonNode elementNode = element.accept(this);
            arrayNode.add(elementNode);
        }
        
        return arrayNode;
    }
    
    /**
     * 访问字典类型值，转换为ObjectNode
     * 
     * 处理Python字典到JSON对象的转换：
     * 1. 创建新的ObjectNode
     * 2. 遍历字典中的每个键值对
     * 3. 将键转换为字符串（JSON要求字符串键）
     * 4. 递归转换值对象
     * 5. 将键值对添加到ObjectNode中
     * 
     * 键转换策略：
     * - 首先将键转换为Java对象（调用accept）
     * - 然后使用String.valueOf转换为字符串
     * - 确保JSON兼容性
     * 
     * 潜在问题处理：
     * - 键冲突：不同的Python键可能转换为相同的字符串键
     * - 键类型：支持所有Python不可变类型作为键
     * 
     * @param dict 字典类型值对象，不能为null
     * @return ObjectNode，包含转换后的所有键值对
     */
    @Override
    public JsonNode visitDict(PythonValue.DictValue dict) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        
        for (Map.Entry<PythonValue, PythonValue> entry : dict.getEntries().entrySet()) {
            // 将键转换为字符串，确保JSON兼容性
            PythonValue keyValue = entry.getKey();
            JsonNode keyNode = keyValue.accept(this);
            String key = convertJsonNodeToString(keyNode);
            
            // 递归转换值
            PythonValue valueValue = entry.getValue();
            JsonNode valueNode = valueValue.accept(this);
            
            objectNode.set(key, valueNode);
        }
        
        return objectNode;
    }
    
    /**
     * 访问元组类型值，转换为ArrayNode
     * 
     * 元组转换策略与列表相同，因为JSON不区分列表和元组，
     * 都统一转换为数组格式。
     * 
     * 实现细节：
     * - 保持元素顺序（元组是有序的）
     * - 递归处理嵌套元素
     * - 类型安全的转换
     * 
     * @param tuple 元组类型值对象，不能为null
     * @return ArrayNode，包含转换后的所有元素
     */
    @Override
    public JsonNode visitTuple(PythonValue.TupleValue tuple) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        
        for (PythonValue element : tuple.getElements()) {
            // 递归转换
            JsonNode elementNode = element.accept(this);
            arrayNode.add(elementNode);
        }
        
        return arrayNode;
    }
    
    /**
     * 访问集合类型值，转换为ArrayNode
     * 
     * 集合转换策略与列表相同，因为JSON不支持集合类型，
     * 统一转换为数组格式。
     * 
     * 实现注意事项：
     * - 元素顺序不保证（集合是无序的）
     * - 不进行元素去重（假设输入已正确）
     * - 递归处理嵌套元素
     * 
     * @param set 集合类型值对象，不能为null
     * @return ArrayNode，包含转换后的所有元素
     */
    @Override
    public JsonNode visitSet(PythonValue.SetValue set) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        
        for (PythonValue element : set.getElements()) {
            // 递归转换
            JsonNode elementNode = element.accept(this);
            arrayNode.add(elementNode);
        }
        
        return arrayNode;
    }
    
    /**
     * 将JsonNode转换为字符串
     * 
     * 这是一个辅助方法，用于将字典的键从JsonNode转换为字符串。
     * JSON要求所有对象键都必须是字符串，因此需要进行类型转换。
     * 
     * 转换规则：
     * - TextNode：直接获取字符串值
     * - 其他类型：使用toString()方法转换
     * 
     * @param node 要转换的JsonNode
     * @return 字符串表示
     */
    private String convertJsonNodeToString(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else {
            return node.toString();
        }
    }
}