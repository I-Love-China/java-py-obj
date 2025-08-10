package com.github.typist.visitor;

import com.github.typist.parser.PythonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Java对象转换访问者
 * 
 * 实现访问者模式的具体访问者，负责将PythonValue对象转换为Java原生对象。
 * 这个访问者提供了直接的Java对象转换，跳过JSON序列化步骤，适合需要在
 * Java代码中直接操作数据的场景。
 * 
 * 设计理念：
 * - 直接转换：避免JSON序列化/反序列化的开销
 * - 类型保持：尽可能保持原始的Java类型信息
 * - 性能优化：使用最适合的Java数据结构
 * - 易于扩展：支持多种输出格式的统一处理
 * 
 * 转换映射规则：
 * ┌─────────────────┬──────────────────┬──────────────────────────────┐
 * │ Python类型      │ PythonValue类型  │ Java类型                     │
 * ├─────────────────┼──────────────────┼──────────────────────────────┤
 * │ None            │ PrimitiveValue   │ null                         │
 * │ bool            │ PrimitiveValue   │ Boolean                      │
 * │ int             │ PrimitiveValue   │ Integer/Long                 │
 * │ float           │ PrimitiveValue   │ Double                       │
 * │ str             │ PrimitiveValue   │ String                       │
 * │ list            │ ListValue        │ Object[]                     │
 * │ tuple           │ TupleValue       │ Object[]                     │
 * │ set             │ SetValue         │ Object[]                     │
 * │ dict            │ DictValue        │ Map<String, Object>          │
 * └─────────────────┴──────────────────┴──────────────────────────────┘
 * 
 * 特殊处理：
 * - 数字类型：根据值的范围选择Integer或Long
 * - 字符串：保持UTF-8编码
 * - 容器类型：使用Object[]数组保持顺序
 * - 字典：使用HashMap<String, Object>提供高效查找
 * 
 * 使用场景：
 * - 需要在Java中直接处理数据
 * - 避免JSON字符串解析开销
 * - 与现有Java API集成
 * - 数据验证和类型检查
 * 
 * 性能特征：
 * - 时间复杂度：O(n)，n为节点总数
 * - 空间复杂度：O(n)，创建对应的Java对象
 * - 内存效率：比JSON字符串更节省内存
 * 
 * 线程安全性：
 * - 访问者本身是无状态的，线程安全
 * - 返回的对象可能是可变的，需要调用者处理并发
 * - 基本类型包装对象是不可变的，线程安全
 * 
 * @author typist
 * @version 1.1
 * @see PythonValueVisitor
 * @see PythonValue
 */
public class JavaObjectVisitor implements PythonValueVisitor<Object> {
    
    /**
     * 访问基本类型值，直接返回存储的Java对象
     * 
     * 基本类型值在词法分析阶段已经转换为对应的Java对象，
     * 因此可以直接返回存储的值，无需额外的类型转换。
     * 
     * 支持的类型：
     * - null (Python None)
     * - Boolean (Python bool)
     * - Integer/Long (Python int)
     * - Double (Python float)
     * - String (Python str)
     * 
     * 性能优势：
     * - 零拷贝：直接返回存储的对象
     * - 无类型检查：信任词法分析的结果
     * - 常量时间：O(1)操作
     * 
     * @param primitive 基本类型值对象，不能为null
     * @return 存储的Java对象，可能为null
     */
    @Override
    public Object visitPrimitive(PythonValue.PrimitiveValue primitive) {
        return primitive.getValue();
    }
    
    /**
     * 访问列表类型值，转换为Java对象数组
     * 
     * 将Python列表转换为Java Object[]数组：
     * 1. 获取列表中的所有元素
     * 2. 递归转换每个元素为Java对象
     * 3. 收集到Object[]数组中
     * 4. 保持元素顺序
     * 
     * 数组选择理由：
     * - 保持元素顺序
     * - 固定大小，内存效率高
     * - 支持异构元素类型
     * - 与JSON数组语义一致
     * 
     * 递归处理：
     * - 支持任意深度的嵌套
     * - 每个元素独立转换
     * - 类型安全保证
     * 
     * @param list 列表类型值对象，不能为null
     * @return Object[]数组，包含转换后的所有元素
     */
    @Override
    public Object visitList(PythonValue.ListValue list) {
        return list.getElements().stream()
                // 递归转换每个元素
                .map(element -> element.accept(this))
                // 转换为Object[]
                .toArray();
    }
    
    /**
     * 访问字典类型值，转换为Java Map
     * 
     * 将Python字典转换为Java HashMap<String, Object>：
     * 1. 创建新的HashMap
     * 2. 遍历字典中的每个键值对
     * 3. 将键转换为字符串（确保Map键的统一性）
     * 4. 递归转换值对象
     * 5. 存入HashMap中
     * 
     * HashMap选择理由：
     * - O(1)平均查找时间
     * - 支持null值
     * - 广泛的Java生态支持
     * - 可变性便于后续操作
     * 
     * 键转换策略：
     * - 先递归转换为Java对象
     * - 再使用String.valueOf转换为字符串
     * - 处理各种键类型（数字、字符串、布尔值等）
     * 
     * 潜在问题：
     * - 键冲突：不同类型的键可能转换为相同字符串
     * - 解决方案：依赖Python源码的正确性
     * 
     * @param dict 字典类型值对象，不能为null
     * @return HashMap<String, Object>，包含转换后的所有键值对
     */
    @Override
    public Object visitDict(PythonValue.DictValue dict) {
        Map<String, Object> result = new HashMap<>();
        
        for (Map.Entry<PythonValue, PythonValue> entry : dict.getEntries().entrySet()) {
            // 将键转换为字符串，确保Map键的类型一致性
            PythonValue keyValue = entry.getKey();
            // 递归转换键
            Object keyObject = keyValue.accept(this);
            String key = String.valueOf(keyObject);
            
            // 递归转换值
            PythonValue valueValue = entry.getValue();
            Object value = valueValue.accept(this);
            
            result.put(key, value);
        }
        
        return result;
    }
    
    /**
     * 访问元组类型值，转换为Java对象数组
     * 
     * 元组转换策略与列表相同，因为Java中没有专门的元组类型，
     * 统一使用Object[]数组表示有序序列。
     * 
     * 设计考虑：
     * - 保持元组的不可变语义（虽然Object[]本身可变）
     * - 保持元素顺序
     * - 支持异构元素类型
     * - 与JSON数组保持一致
     * 
     * 实现细节：
     * - 使用流式API进行转换
     * - 递归处理嵌套元素
     * - 类型安全保证
     * 
     * @param tuple 元组类型值对象，不能为null
     * @return Object[]数组，包含转换后的所有元素
     */
    @Override
    public Object visitTuple(PythonValue.TupleValue tuple) {
        return tuple.getElements().stream()
                // 递归转换每个元素
                .map(element -> element.accept(this))
                // 转换为Object[]
                .toArray();
    }
    
    /**
     * 访问集合类型值，转换为Java对象数组
     * 
     * 集合转换策略与列表相同，因为Java中的Set类型在序列化时
     * 通常也表示为数组形式，为保持一致性使用Object[]数组。
     * 
     * 设计权衡：
     * - 不使用Java Set<Object>：因为需要与JSON数组保持一致
     * - 不进行去重处理：信任Python源码的正确性
     * - 不保证元素顺序：集合本身是无序的
     * 
     * 替代方案考虑：
     * - 可以返回HashSet<Object>保持集合语义
     * - 当前选择数组是为了与其他容器类型保持一致
     * - 便于JSON序列化处理
     * 
     * @param set 集合类型值对象，不能为null
     * @return Object[]数组，包含转换后的所有元素
     */
    @Override
    public Object visitSet(PythonValue.SetValue set) {
        return set.getElements().stream()
                // 递归转换每个元素
                .map(element -> element.accept(this))
                // 转换为Object[]
                .toArray();
    }
}