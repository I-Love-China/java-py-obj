package com.github.typist.parser;

import com.github.typist.visitor.PythonValueVisitor;

/**
 * 可访问接口
 * 
 * 访问者模式(Visitor Pattern)中的Element接口，定义了AST节点接受访问者的能力。
 * 实现此接口的AST节点可以被各种访问者访问，实现数据结构与操作的解耦。
 * 
 * 编译原理中的作用：
 * - 为AST节点提供统一的访问接口
 * - 支持多种语义分析和代码生成操作
 * - 实现编译器后端的模块化设计
 * 
 * 设计理念：
 * - 双分派机制：通过accept方法实现运行时的双重分派
 * - 类型安全：使用泛型确保访问者和被访问对象的类型匹配
 * - 开放封闭：新增访问者不需要修改现有的数据结构
 * 
 * 工作原理：
 * 1. 客户端调用element.accept(visitor)
 * 2. element将自己传递给visitor的相应方法
 * 3. visitor根据element的具体类型执行对应的操作
 * 4. 返回操作结果
 * 
 * 双分派示例：
 * ```java
 * // 第一次分派：根据元素类型选择accept实现
 * PythonValue element = new ListValue(...);
 * // 第二次分派：在accept中根据访问者类型选择visit方法
 * JsonNode result = element.accept(new JsonNodeVisitor());
 * ```
 * 
 * 类型安全保证：
 * - 泛型T确保返回值类型的一致性
 * - 编译期检查避免类型转换错误
 * - 访问者接口定义了所有支持的操作类型
 * 
 * 性能考虑：
 * - 避免了大量的instanceof检查
 * - 利用JVM的方法分派优化
 * - 减少了条件分支的开销
 * 
 * 扩展性：
 * - 添加新操作：实现新的Visitor，无需修改现有类
 * - 添加新类型：实现Visitable接口，添加对应的visit方法
 * - 组合访问者：可以创建复合访问者处理复杂场景
 * 
 * @author Generated with Claude Code
 * @version 1.0
 */
public interface Visitable {
    
    /**
     * 接受访问者的访问
     * 
     * 这是访问者模式的核心方法，实现了双分派机制：
     * 1. 首次分派：根据当前对象的运行时类型选择具体的accept实现
     * 2. 二次分派：在accept实现中调用visitor的相应方法，传入this
     * 
     * 实现规范：
     * - 每个具体类型应该调用visitor对应的visit方法
     * - 传入的参数必须是this，确保类型信息正确
     * - 返回visitor方法的执行结果
     * 
     * 典型实现模式：
     * ```java
     * @Override
     * public <T> T accept(PythonValueVisitor<T> visitor) {
     *     return visitor.visitXxx(this);  // Xxx为具体类型名
     * }
     * ```
     * 
     * 泛型使用：
     * - T是访问操作的返回类型，由具体访问者决定
     * - 同一个数据结构可以被不同返回类型的访问者访问
     * - 编译器确保类型安全，避免运行时类型错误
     * 
     * 错误处理：
     * - 如果访问者不支持某种类型，应该抛出适当的异常
     * - 可以使用默认访问者方法提供基础行为
     * - 建议在访问者中处理null值情况
     * 
     * @param <T> 访问操作的返回值类型，由访问者决定
     * @param visitor 要接受的访问者对象，不能为null
     * @return 访问者操作的结果，类型为T
     * @throws IllegalArgumentException 如果visitor为null
     * @throws UnsupportedOperationException 如果访问者不支持当前类型
     */
    <T> T accept(PythonValueVisitor<T> visitor);
}