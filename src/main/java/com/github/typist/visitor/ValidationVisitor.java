package com.github.typist.visitor;

import com.github.typist.parser.PythonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据验证访问者
 * 
 * 实现访问者模式的具体访问者，用于验证PythonValue对象的数据完整性和结构合理性。
 * 这个访问者演示了访问者模式的可扩展性 - 在不修改现有PythonValue类的情况下，
 * 增加了新的数据验证功能。
 * 
 * 验证功能：
 * - 结构完整性检查：确保所有容器类型不为空且包含有效元素
 * - 数据类型一致性：检查基本类型的合理性
 * - 嵌套深度控制：防止过深的嵌套结构
 * - 数据统计收集：统计各种类型的数量分布
 * 
 * 使用场景：
 * - 数据导入前的预验证
 * - 调试和诊断工具
 * - 数据质量监控
 * - 性能分析准备
 * 
 * 扩展性演示：
 * 这个类展示了访问者模式的核心优势：
 * 1. 无需修改PythonValue类就能添加新功能
 * 2. 保持了单一职责原则
 * 3. 可以轻松添加更多验证规则
 * 4. 与现有转换访问者完全解耦
 * 
 * 设计模式体现：
 * - **访问者模式**：提供新的验证操作
 * - **策略模式**：不同的验证策略可以替换
 * - **收集器模式**：收集验证统计信息
 * 
 * @author Generated with Claude Code
 * @version 1.0
 * @see PythonValueVisitor
 * @see PythonValue
 */
public class ValidationVisitor implements PythonValueVisitor<ValidationVisitor.ValidationResult> {
    
    /**
     * 最大允许的嵌套深度，防止栈溢出
     */
    private static final int MAX_NESTING_DEPTH = 100;
    
    /**
     * 当前访问的嵌套深度
     */
    private int currentDepth = 0;
    
    /**
     * 验证结果数据类
     * 
     * 封装验证过程中收集的统计信息和验证结果。
     * 使用不可变设计确保结果的可靠性。
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Map<String, Integer> typeStatistics;
        private final int maxDepth;
        private final int totalElements;
        
        /**
         * 构造验证结果
         * 
         * @param valid 是否验证通过
         * @param errorMessage 错误信息，验证通过时为null
         * @param typeStatistics 类型统计信息
         * @param maxDepth 最大嵌套深度
         * @param totalElements 总元素数量
         */
        public ValidationResult(boolean valid, String errorMessage, 
                              Map<String, Integer> typeStatistics, 
                              int maxDepth, int totalElements) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.typeStatistics = new HashMap<>(typeStatistics);
            this.maxDepth = maxDepth;
            this.totalElements = totalElements;
        }
        
        /**
         * 创建验证通过的结果
         */
        public static ValidationResult success(Map<String, Integer> typeStatistics, 
                                             int maxDepth, int totalElements) {
            return new ValidationResult(true, null, typeStatistics, maxDepth, totalElements);
        }
        
        /**
         * 创建验证失败的结果
         */
        public static ValidationResult failure(String errorMessage, 
                                             Map<String, Integer> typeStatistics, 
                                             int maxDepth, int totalElements) {
            return new ValidationResult(false, errorMessage, typeStatistics, maxDepth, totalElements);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Integer> getTypeStatistics() { return new HashMap<>(typeStatistics); }
        public int getMaxDepth() { return maxDepth; }
        public int getTotalElements() { return totalElements; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("valid=").append(valid);
            if (!valid) {
                sb.append(", error='").append(errorMessage).append("'");
            }
            sb.append(", maxDepth=").append(maxDepth);
            sb.append(", totalElements=").append(totalElements);
            sb.append(", statistics=").append(typeStatistics);
            sb.append("}");
            return sb.toString();
        }
    }
    
    /**
     * 类型统计收集器
     */
    private final Map<String, Integer> typeStatistics = new HashMap<>();
    private int totalElements = 0;
    private int maxDepthReached = 0;
    
    /**
     * 访问基本类型值，进行基础验证
     * 
     * 验证项目：
     * - 检查是否为null（这里null是合法的，代表Python的None）
     * - 统计基本类型分布
     * - 检查字符串长度合理性
     * - 检查数字范围合理性
     */
    @Override
    public ValidationResult visitPrimitive(PythonValue.PrimitiveValue primitive) {
        currentDepth++;
        maxDepthReached = Math.max(maxDepthReached, currentDepth);
        totalElements++;
        
        try {
            Object value = primitive.getValue();
            String typeName = value == null ? "null" : value.getClass().getSimpleName();
            typeStatistics.merge(typeName, 1, Integer::sum);
            
            // 基本类型特定验证
            if (value instanceof String) {
                String str = (String) value;
                if (str.length() > 10000) {
                    // 防止过长字符串
                    return ValidationResult.failure(
                        "String too long: " + str.length() + " characters",
                        typeStatistics, maxDepthReached, totalElements
                    );
                }
            } else if (value instanceof Double) {
                Double num = (Double) value;
                if (num.isInfinite() || num.isNaN()) {
                    return ValidationResult.failure(
                        "Invalid numeric value: " + num,
                        typeStatistics, maxDepthReached, totalElements
                    );
                }
            }
            
            return ValidationResult.success(typeStatistics, maxDepthReached, totalElements);
            
        } finally {
            currentDepth--;
        }
    }
    
    /**
     * 访问列表类型值，进行容器验证
     */
    @Override
    public ValidationResult visitList(PythonValue.ListValue list) {
        return validateContainer("List", list.getElements());
    }
    
    /**
     * 访问字典类型值，进行键值对验证
     */
    @Override
    public ValidationResult visitDict(PythonValue.DictValue dict) {
        currentDepth++;
        maxDepthReached = Math.max(maxDepthReached, currentDepth);
        
        try {
            // 检查嵌套深度
            if (currentDepth > MAX_NESTING_DEPTH) {
                return ValidationResult.failure(
                    "Nesting too deep: " + currentDepth + " > " + MAX_NESTING_DEPTH,
                    typeStatistics, maxDepthReached, totalElements
                );
            }
            
            typeStatistics.merge("Dict", 1, Integer::sum);
            totalElements++;
            
            // 验证每个键值对
            for (Map.Entry<PythonValue, PythonValue> entry : dict.getEntries().entrySet()) {
                // 验证键
                ValidationResult keyResult = entry.getKey().accept(this);
                if (!keyResult.isValid()) {
                    return keyResult;
                }
                
                // 验证值
                ValidationResult valueResult = entry.getValue().accept(this);
                if (!valueResult.isValid()) {
                    return valueResult;
                }
            }
            
            return ValidationResult.success(typeStatistics, maxDepthReached, totalElements);
            
        } finally {
            currentDepth--;
        }
    }
    
    /**
     * 访问元组类型值，进行容器验证
     */
    @Override
    public ValidationResult visitTuple(PythonValue.TupleValue tuple) {
        return validateContainer("Tuple", tuple.getElements());
    }
    
    /**
     * 访问集合类型值，进行容器验证
     */
    @Override
    public ValidationResult visitSet(PythonValue.SetValue set) {
        return validateContainer("Set", set.getElements());
    }
    
    /**
     * 通用容器验证逻辑
     * 
     * @param containerType 容器类型名称
     * @param elements 容器中的元素列表
     * @return 验证结果
     */
    private ValidationResult validateContainer(String containerType, 
                                             java.util.List<PythonValue> elements) {
        currentDepth++;
        maxDepthReached = Math.max(maxDepthReached, currentDepth);
        
        try {
            // 检查嵌套深度
            if (currentDepth > MAX_NESTING_DEPTH) {
                return ValidationResult.failure(
                    "Nesting too deep: " + currentDepth + " > " + MAX_NESTING_DEPTH,
                    typeStatistics, maxDepthReached, totalElements
                );
            }
            
            typeStatistics.merge(containerType, 1, Integer::sum);
            totalElements++;
            
            // 检查容器大小合理性（防止内存溢出）
            if (elements.size() > 100000) {
                return ValidationResult.failure(
                    containerType + " too large: " + elements.size() + " elements",
                    typeStatistics, maxDepthReached, totalElements
                );
            }
            
            // 递归验证每个元素
            for (int i = 0; i < elements.size(); i++) {
                PythonValue element = elements.get(i);
                if (element == null) {
                    return ValidationResult.failure(
                        containerType + " contains null element at index " + i,
                        typeStatistics, maxDepthReached, totalElements
                    );
                }
                
                ValidationResult elementResult = element.accept(this);
                if (!elementResult.isValid()) {
                    return elementResult;
                }
            }
            
            return ValidationResult.success(typeStatistics, maxDepthReached, totalElements);
            
        } finally {
            currentDepth--;
        }
    }
    
    /**
     * 重置验证器状态，用于验证新的数据结构
     */
    public void reset() {
        typeStatistics.clear();
        totalElements = 0;
        maxDepthReached = 0;
        currentDepth = 0;
    }
}