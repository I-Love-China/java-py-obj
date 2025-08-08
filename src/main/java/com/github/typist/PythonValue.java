package com.github.typist;

import java.util.List;
import java.util.Map;

/**
 * Python值对象的抽象表示
 * 
 * 这个类层次结构实现了Python对象到Java对象的映射，是语义分析阶段的核心数据结构。
 * 它使用了访问者模式（Visitor Pattern）的变体，通过抽象方法定义了统一的转换接口。
 * 
 * 设计模式应用：
 * - 抽象工厂模式：不同类型的Python值有不同的具体实现
 * - 访问者模式：toJavaObject()方法实现了对不同类型值的统一处理
 * - 组合模式：容器类型（List、Dict、Tuple、Set）可以包含其他PythonValue
 * 
 * 类型层次结构：
 * PythonValue (抽象基类)
 * ├── PrimitiveValue (基本类型：数字、字符串、布尔、null)
 * ├── ListValue (列表类型：[1, 2, 3])
 * ├── DictValue (字典类型：{'key': 'value'})
 * ├── TupleValue (元组类型：(1, 2, 3))
 * └── SetValue (集合类型：{1, 2, 3})
 * 
 * 转换映射关系：
 * Python类型 → Java类型
 * - int/float → Integer/Long/Double
 * - str → String
 * - bool → Boolean
 * - None → null
 * - list/tuple/set → Object[]
 * - dict → Map<String, Object>
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
public abstract class PythonValue {
    
    /**
     * 将Python值转换为对应的Java对象
     * 
     * 这是访问者模式的核心方法，每个具体的Python值类型都必须实现
     * 自己的转换逻辑。这种设计使得添加新的操作变得容易，而不需要
     * 修改现有的类型定义。
     * 
     * 转换原则：
     * - 保持数据的语义一致性
     * - 选择合适的Java类型表示
     * - 递归处理嵌套结构
     * - 处理类型转换异常
     * 
     * @return 转换后的Java对象，类型取决于具体的Python值类型
     */
    public abstract Object toJavaObject();

    // ========================= 基本类型实现 =========================
    
    /**
     * Python基本类型值的实现
     * 
     * 包装Python中的基本数据类型：整数、浮点数、字符串、布尔值、None。
     * 这些类型在Python中是不可变的（immutable），因此使用final字段。
     * 
     * 支持的基本类型：
     * - 数字类型：int, long, float → Integer, Long, Double
     * - 字符串类型：str → String
     * - 布尔类型：bool → Boolean
     * - 空值类型：None → null
     * 
     * 设计考虑：
     * - 使用Object类型存储以支持多种数据类型（多态）
     * - 不进行额外的类型检查，信任词法和语法分析的结果
     * - 保持不可变性以确保线程安全
     */
    public static class PrimitiveValue extends PythonValue {
        /**
         * 基本类型的值
         * 可能的类型：Integer, Long, Double, String, Boolean, null
         */
        private final Object value;

        /**
         * 构造基本类型值
         * @param value 基本类型的值，可以为null（表示Python的None）
         */
        public PrimitiveValue(Object value) {
            this.value = value;
        }

        /**
         * 基本类型的转换：直接返回存储的Java对象
         * 因为在词法分析阶段已经完成了类型转换
         */
        @Override
        public Object toJavaObject() {
            return value;
        }

        /**
         * 返回值的字符串表示，用于调试和日志
         */
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    // ========================= 容器类型实现 =========================
    
    /**
     * Python列表类型的实现
     * 
     * 对应Python的list类型，如：[1, 2, 3], ['a', 'b'], [1, 'mixed', True]
     * 列表是有序的、可变的、允许重复元素的容器类型。
     * 
     * 实现特点：
     * - 使用List<PythonValue>存储元素，支持异构类型
     * - 保持元素顺序
     * - 递归转换嵌套结构
     * - 转换为Java数组以保持与JSON的兼容性
     * 
     * 转换策略：
     * Python list → Java Object[] → JSON Array
     */
    public static class ListValue extends PythonValue {
        /**
         * 列表元素，使用List保持插入顺序
         */
        private final List<PythonValue> elements;

        /**
         * 构造列表值
         * @param elements 列表元素，不能为null但可以为空列表
         */
        public ListValue(List<PythonValue> elements) {
            if (elements == null) {
                throw new IllegalArgumentException("List elements cannot be null");
            }
            this.elements = elements;
        }

        /**
         * 获取列表元素，主要供Parser使用
         * @return 元素列表的只读视图
         */
        public List<PythonValue> getElements() {
            return elements;
        }

        /**
         * 将Python列表转换为Java对象数组
         * 使用流式API递归转换每个元素
         */
        @Override
        public Object toJavaObject() {
            return elements.stream()
                    .map(PythonValue::toJavaObject)  // 递归转换每个元素
                    .toArray();  // 转换为Object[]
        }

        /**
         * 返回列表的字符串表示，格式类似Python的list
         */
        @Override
        public String toString() {
            return elements.toString();
        }
    }

    /**
     * Python字典类型的实现
     * 
     * 对应Python的dict类型，如：{'name': 'John', 'age': 30}, {1: 'one', 2: 'two'}
     * 字典是无序的（Python 3.7+保持插入顺序）、可变的、键值对容器类型。
     * 
     * 实现特点：
     * - 使用Map<PythonValue, PythonValue>存储键值对
     * - 键可以是任何不可变类型（数字、字符串、布尔值等）
     * - 值可以是任何类型，包括嵌套的容器类型
     * - 转换时将所有键转换为字符串以符合JSON规范
     * 
     * 转换策略：
     * Python dict → Java Map<String, Object> → JSON Object
     * 
     * 注意事项：
     * - JSON要求所有键都是字符串，因此进行键类型转换
     * - 转换可能导致键冲突（如数字1和字符串"1"）
     */
    public static class DictValue extends PythonValue {
        /**
         * 字典的键值对，使用Map保持关联关系
         */
        private final Map<PythonValue, PythonValue> entries;

        /**
         * 构造字典值
         * @param entries 字典的键值对映射，不能为null但可以为空字典
         */
        public DictValue(Map<PythonValue, PythonValue> entries) {
            if (entries == null) {
                throw new IllegalArgumentException("Dict entries cannot be null");
            }
            this.entries = entries;
        }

        /**
         * 获取字典条目，主要供Parser使用
         * @return 键值对映射的视图
         */
        public Map<PythonValue, PythonValue> getEntries() {
            return entries;
        }

        /**
         * 将Python字典转换为Java Map
         * 
         * 转换过程：
         * 1. 创建新的HashMap<String, Object>
         * 2. 遍历所有键值对
         * 3. 将键转换为字符串（调用toJavaObject()然后String.valueOf）
         * 4. 递归转换值对象
         * 5. 存入结果映射
         * 
         * 潜在问题：键冲突处理
         * 如果多个键转换为相同的字符串，后面的会覆盖前面的。
         */
        @Override
        public Object toJavaObject() {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            for (Map.Entry<PythonValue, PythonValue> entry : entries.entrySet()) {
                // 将键转换为字符串，确保JSON兼容性
                String key = String.valueOf(entry.getKey().toJavaObject());
                // 递归转换值
                Object value = entry.getValue().toJavaObject();
                result.put(key, value);
            }
            return result;
        }

        /**
         * 返回字典的字符串表示，使用Java Map的默认格式
         */
        @Override
        public String toString() {
            return entries.toString();
        }
    }

    /**
     * Python元组类型的实现
     * 
     * 对应Python的tuple类型，如：(1, 2, 3), ('a', 'b'), (1, 'mixed', True)
     * 元组是有序的、不可变的、允许重复元素的容器类型。
     * 
     * 实现特点：
     * - 使用List<PythonValue>存储元素（虽然元组不可变，但Java List便于处理）
     * - 保持元素顺序
     * - 递归转换嵌套结构
     * - 转换为Java数组，与列表转换结果相同
     * 
     * 转换策略：
     * Python tuple → Java Object[] → JSON Array
     * 
     * 注意：JSON中没有区分list和tuple，都转换为数组
     */
    public static class TupleValue extends PythonValue {
        /**
         * 元组元素，使用List保持顺序
         */
        private final List<PythonValue> elements;

        /**
         * 构造元组值
         * @param elements 元组元素，不能为null但可以为空元组
         */
        public TupleValue(List<PythonValue> elements) {
            if (elements == null) {
                throw new IllegalArgumentException("Tuple elements cannot be null");
            }
            this.elements = elements;
        }

        /**
         * 获取元组元素，主要供Parser使用
         * @return 元素列表的只读视图
         */
        public List<PythonValue> getElements() {
            return elements;
        }

        /**
         * 将Python元组转换为Java对象数组
         * 转换逻辑与ListValue相同，因为JSON不区分列表和元组
         */
        @Override
        public Object toJavaObject() {
            return elements.stream()
                    .map(PythonValue::toJavaObject)  // 递归转换每个元素
                    .toArray();  // 转换为Object[]
        }

        /**
         * 返回元组的字符串表示，使用Python风格的圆括号格式
         * 格式：(元素1, 元素2, 元素3)
         */
        @Override
        public String toString() {
            return "(" + String.join(", ", elements.stream()
                    .map(Object::toString)
                    .toArray(String[]::new)) + ")";
        }
    }

    /**
     * Python集合类型的实现
     * 
     * 对应Python的set类型，如：{1, 2, 3}, {'a', 'b'}, {1, 'mixed', True}
     * 集合是无序的、可变的、不允许重复元素的容器类型。
     * 
     * 实现特点：
     * - 使用List<PythonValue>存储元素（简化实现，不进行去重）
     * - 元素顺序可能与Python中的不同
     * - 递归转换嵌套结构
     * - 转换为Java数组，与列表转换结果相同
     * 
     * 转换策略：
     * Python set → Java Object[] → JSON Array
     * 
     * 注意事项：
     * - JSON中没有集合类型，转换为数组
     * - 当前实现不进行元素去重，依赖Python源码的正确性
     * - 元素顺序不保证与Python一致
     */
    public static class SetValue extends PythonValue {
        /**
         * 集合元素，使用List简化实现
         * 注意：这里没有实现真正的Set语义（去重），假设输入已经正确
         */
        private final List<PythonValue> elements;

        /**
         * 构造集合值
         * @param elements 集合元素，不能为null但可以为空集合
         */
        public SetValue(List<PythonValue> elements) {
            if (elements == null) {
                throw new IllegalArgumentException("Set elements cannot be null");
            }
            this.elements = elements;
        }

        /**
         * 获取集合元素，主要供Parser使用
         * @return 元素列表的视图
         */
        public List<PythonValue> getElements() {
            return elements;
        }

        /**
         * 将Python集合转换为Java对象数组
         * 转换逻辑与ListValue相同，因为JSON不支持集合类型
         */
        @Override
        public Object toJavaObject() {
            return elements.stream()
                    .map(PythonValue::toJavaObject)  // 递归转换每个元素
                    .toArray();  // 转换为Object[]
        }

        /**
         * 返回集合的字符串表示，使用Python风格的花括号格式
         * 格式：{元素1, 元素2, 元素3}
         */
        @Override
        public String toString() {
            return "{" + String.join(", ", elements.stream()
                    .map(Object::toString)
                    .toArray(String[]::new)) + "}";
        }
    }
}