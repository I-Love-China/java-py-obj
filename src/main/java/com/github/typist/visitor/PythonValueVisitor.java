package com.github.typist.visitor;

import com.github.typist.parser.PythonValue;

/**
 * Python 值访问者接口
 * 
 * 实现访问者模式 (Visitor Pattern) 的核心接口，用于对 PythonValue 对象进行各种操作。
 * 访问者模式将数据结构与作用于这些数据结构上的操作分离，使得可以在不修改
 * 数据结构的前提下定义新的操作。
 * 
 * 设计原则：
 * - 开放封闭原则：对扩展开放，对修改封闭
 * - 单一职责原则：每个访问者只负责一种特定的操作
 * - 依赖倒置原则：高层模块不依赖低层模块，都依赖抽象
 * 
 * 泛型设计：
 * - T 表示访问操作的返回值类型
 * - 不同的访问者可以返回不同类型的结果：
 *   * JsonNode (JSON转换)
 *   * Object (Java对象转换)
 *   * String (格式化输出)
 *   * Boolean (验证结果)
 *   * Void (无返回值操作)
 * 
 * 扩展性：
 * - 添加新的Python类型：在接口中增加对应的visit方法
 * - 添加新的操作：实现新的PythonValueVisitor子类
 * - 修改现有操作：只需修改对应的访问者实现
 * 
 * 使用示例：
 * ```java
 * // JSON转换访问者
 * PythonValueVisitor<JsonNode> jsonVisitor = new JsonNodeVisitor();
 * JsonNode result = pythonValue.accept(jsonVisitor);
 * 
 * // 数据验证访问者
 * PythonValueVisitor<Boolean> validator = new ValidationVisitor();
 * boolean isValid = pythonValue.accept(validator);
 * ```
 * 
 * @param <T> 访问操作的返回值类型
 * @author typist
 * @version 1.1
 */
public interface PythonValueVisitor<T> {

    /**
     * 访问基本类型值
     * 
     * 处理Python中的基本数据类型，包括：
     * - 数字类型：int, float
     * - 字符串类型：str
     * - 布尔类型：bool
     * - 空值类型：None
     * 
     * @param primitive 基本类型值对象
     * @return 访问操作的结果
     */
    T visitPrimitive(PythonValue.PrimitiveValue primitive);

    /**
     * 访问列表类型值
     * 
     * 处理Python的list类型，如：[1, 2, 3], ['a', 'b'], [1, 'mixed', True]
     * 列表是有序的、可变的、允许重复元素的容器类型。
     * 
     * 实现时需要考虑：
     * - 递归处理嵌套元素
     * - 保持元素顺序
     * - 处理异构类型
     * 
     * @param list 列表类型值对象
     * @return 访问操作的结果
     */
    T visitList(PythonValue.ListValue list);

    /**
     * 访问字典类型值
     * 
     * 处理Python的dict类型，如：{'name': 'John'}, {1: 'one', 2: 'two'}
     * 字典是键值对容器类型，键必须是不可变类型。
     * 
     * 实现时需要考虑：
     * - 键类型转换（特别是转为JSON时需要字符串键）
     * - 递归处理键和值
     * - 处理键冲突问题
     * 
     * @param dict 字典类型值对象
     * @return 访问操作的结果
     */
    T visitDict(PythonValue.DictValue dict);

    /**
     * 访问元组类型值
     * 
     * 处理Python的tuple类型，如：(1, 2, 3), ('a', 'b'), (1, 'mixed', True)
     * 元组是有序的、不可变的、允许重复元素的容器类型。
     * 
     * 实现时需要考虑：
     * - 与列表的区别处理（如果需要）
     * - 递归处理嵌套元素
     * - 保持元素顺序
     * 
     * @param tuple 元组类型值对象
     * @return 访问操作的结果
     */
    T visitTuple(PythonValue.TupleValue tuple);

    /**
     * 访问集合类型值
     * 
     * 处理Python的set类型，如：{1, 2, 3}, {'a', 'b'}, {1, 'mixed', True}
     * 集合是无序的、可变的、不允许重复元素的容器类型。
     * 
     * 实现时需要考虑：
     * - 元素去重（如果需要）
     * - 递归处理嵌套元素
     * - 顺序不保证
     * 
     * @param set 集合类型值对象
     * @return 访问操作的结果
     */
    T visitSet(PythonValue.SetValue set);
}