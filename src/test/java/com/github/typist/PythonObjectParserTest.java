package com.github.typist;

import org.junit.Test;
import static org.junit.Assert.*;

public class PythonObjectParserTest {

    private final PythonObjectParser parser = new PythonObjectParser();

    // ==================== 基础数据类型测试 ====================
    
    @Test
    public void testPrimitiveTypes() {
        assertEquals("42", parser.parseToJson("42"));
        assertEquals("3.14", parser.parseToJson("3.14"));
        assertEquals("true", parser.parseToJson("True"));
        assertEquals("false", parser.parseToJson("False"));
        assertEquals("null", parser.parseToJson("None"));
        assertEquals("\"hello\"", parser.parseToJson("'hello'"));
        assertEquals("\"world\"", parser.parseToJson("\"world\""));
    }

    @Test
    public void testNegativeNumbers() {
        assertEquals("-42", parser.parseToJson("-42"));
        assertEquals("-3.14", parser.parseToJson("-3.14"));
    }

    @Test
    public void testBoundaryValues() {
        // 空容器
        assertEquals("[]", parser.parseToJson("[]"));
        assertEquals("[]", parser.parseToJson("()"));
        assertEquals("{}", parser.parseToJson("{}"));
        
        // 极大和极小整数
        // Integer.MAX_VALUE
        assertEquals("2147483647", parser.parseToJson("2147483647"));
        // Integer.MIN_VALUE
        assertEquals("-2147483648", parser.parseToJson("-2147483648"));
        
        // 极大和极小浮点数
//        assertEquals("1.7976931348623157E308", parser.parseToJson("1.7976931348623157E308"));
//        assertEquals("4.9E-324", parser.parseToJson("4.9E-324"));
        
        // 零值
        assertEquals("0", parser.parseToJson("0"));
        assertEquals("0.0", parser.parseToJson("0.0"));
//        assertEquals("-0", parser.parseToJson("-0"));
        
        // 单元素容器
        assertEquals("[1]", parser.parseToJson("[1]"));
        assertEquals("[1]", parser.parseToJson("(1,)"));
        assertEquals("{\"key\":\"value\"}", parser.parseToJson("{'key': 'value'}"));
    }

    // ==================== 集合类型测试 ====================
    
    @Test
    public void testList() {
        assertEquals("[1,2,3]", parser.parseToJson("[1, 2, 3]"));
        assertEquals("[\"a\",\"b\",\"c\"]", parser.parseToJson("['a', 'b', 'c']"));
        assertEquals("[1,\"hello\",true,null]", parser.parseToJson("[1, 'hello', True, None]"));
        assertEquals("[]", parser.parseToJson("[]"));
    }

    @Test
    public void testTuple() {
        assertEquals("[1,2,3]", parser.parseToJson("(1, 2, 3)"));
        assertEquals("[\"a\",\"b\"]", parser.parseToJson("('a', 'b')"));
        assertEquals("[]", parser.parseToJson("()"));
    }

    @Test
    public void testSet() {
        String result = parser.parseToJson("{1, 2, 3}");
        assertTrue(result.contains("1") && result.contains("2") && result.contains("3"));
    }

    @Test
    public void testDict() {
        String result1 = parser.parseToJson("{'name': 'John', 'age': 30}");
        assertTrue(result1.contains("\"name\":\"John\"") && result1.contains("\"age\":30"));

        assertEquals("{\"active\":true}", parser.parseToJson("{'active': True}"));
        assertEquals("{}", parser.parseToJson("{}"));
    }

    // ==================== 嵌套结构测试 ====================
    
    @Test
    public void testNestedStructures() {
        String input = "{'users': [{'name': 'Alice', 'active': True}, {'name': 'Bob', 'active': False}]}";
        String result = parser.parseToJson(input);
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Bob"));
        assertTrue(result.contains("users"));
    }

    @Test
    public void testComplexNesting() {
        String input = "[{'data': [1, 2, {'nested': True}]}, (3, 4), {5, 6}]";
        String result = parser.parseToJson(input);
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    public void testDeepNestedStructures() {
        // 5层嵌套字典
        String deepDict = "{'l1': {'l2': {'l3': {'l4': {'l5': 'deep'}}}}}";
        String result = parser.parseToJson(deepDict);
        assertTrue(result.contains("\"l5\":\"deep\""));
        
        // 深度嵌套列表
        String deepList = "[[[[[1]]]]]";
        assertEquals("[[[[[1]]]]]", parser.parseToJson(deepList));
        
        // 混合深度嵌套
        String mixed = "{'users': [{'profile': {'contacts': [{'type': 'email', 'value': 'test@example.com'}]}}]}";
        String mixedResult = parser.parseToJson(mixed);
        assertTrue(mixedResult.contains("\"email\""));
        assertTrue(mixedResult.contains("\"test@example.com\""));
        
        // 复杂嵌套场景
        String complex = "[{'data': [1, {'nested': [True, False, None]}, (2, 3)]}, {4, 5}]";
        String complexResult = parser.parseToJson(complex);
        assertTrue(complexResult.contains("true"));
        assertTrue(complexResult.contains("false"));
        assertTrue(complexResult.contains("null"));
    }

    // ==================== 字符串测试 ====================
    
    @Test
    public void testStringEscaping() {
        assertEquals("\"hello\\nworld\"", parser.parseToJson("'hello\\nworld'"));
        assertEquals("\"tab\\there\"", parser.parseToJson("'tab\\there'"));
        assertEquals("\"quote\\\"test\"", parser.parseToJson("'quote\\\"test'"));
    }

    @Test
    public void testMoreStringEscaping() {
        // 各种转义字符
        assertEquals("\"\\r\"", parser.parseToJson("'\\r'"));
//        assertEquals("\"\\f\"", parser.parseToJson("'\\f'"));
//        assertEquals("\"\\b\"", parser.parseToJson("'\\b'"));
//        assertEquals("\"\\v\"", parser.parseToJson("'\\v'"));
        
        // 单引号在双引号字符串中
        assertEquals("\"single'quote\"", parser.parseToJson("\"single'quote\""));
        
        // 双引号在单引号字符串中
        assertEquals("\"double\\\"quote\"", parser.parseToJson("'double\\\"quote'"));
        
        // 混合转义
        assertEquals("\"line1\\nline2\\ttab\"", parser.parseToJson("'line1\\nline2\\ttab'"));
        
        // 反斜杠本身
        assertEquals("\"\\\\\"", parser.parseToJson("'\\\\'"));
        assertEquals("\"path\\\\to\\\\file\"", parser.parseToJson("'path\\\\to\\\\file'"));
        
        // Unicode转义
//        assertEquals("\"\\u4e2d\\u6587\"", parser.parseToJson("'\\u4e2d\\u6587'"));
    }

    @Test
    public void testKeywordsInStrings() {
        // 关键字作为字符串内容
        assertEquals("\"True\"", parser.parseToJson("'True'"));
        assertEquals("\"False\"", parser.parseToJson("'False'"));
        assertEquals("\"None\"", parser.parseToJson("'None'"));
        
        // 字符串中包含关键字
        assertEquals("\"This is True statement\"", parser.parseToJson("'This is True statement'"));
        assertEquals("\"False alarm\"", parser.parseToJson("'False alarm'"));
        assertEquals("\"Nothing is None here\"", parser.parseToJson("'Nothing is None here'"));
        
        // 混合情况：字符串关键字 vs 实际关键字
        String result1 = parser.parseToJson("{'boolean_true': True, 'string_true': 'True'}");
        assertTrue(result1.contains("\"boolean_true\":true"));
        assertTrue(result1.contains("\"string_true\":\"True\""));
        
        String result2 = parser.parseToJson("{'status': 'False', 'valid': False}");
        assertTrue(result2.contains("\"status\":\"False\""));
        assertTrue(result2.contains("\"valid\":false"));
    }

    @Test
    public void testUnicodeStrings() {
        // 中文字符
        assertEquals("\"你好世界\"", parser.parseToJson("'你好世界'"));
        assertEquals("\"汽车\"", parser.parseToJson("'汽车'"));
        
        // 混合中英文
        assertEquals("\"Hello世界\"", parser.parseToJson("'Hello世界'"));
        assertEquals("\"测试Test\"", parser.parseToJson("'测试Test'"));
        
        // 包含中文的复杂结构
        String result1 = parser.parseToJson("{'name': '张三', 'city': '北京'}");
        assertTrue(result1.contains("\"name\":\"张三\""));
        assertTrue(result1.contains("\"city\":\"北京\""));
        
        // 数组中的中文
        assertEquals("[\"苹果\",\"香蕉\",\"橙子\"]", parser.parseToJson("['苹果', '香蕉', '橙子']"));
        
        // 特殊Unicode字符
        // 心形符号
        assertEquals("\"❤\"", parser.parseToJson("'❤'"));
        // 版权符号
        assertEquals("\"©\"", parser.parseToJson("'©'"));
    }

    // ==================== 错误处理测试 ====================
    
    @Test(expected = RuntimeException.class)
    public void testInvalidSyntax() {
        parser.parseToJson("[1, 2,");
    }

    @Test(expected = RuntimeException.class)
    public void testUnexpectedCharacter() {
        parser.parseToJson("@invalid");
    }

    @Test
    public void testMoreErrorHandling() {
        // 不匹配的括号
        assertThrows(RuntimeException.class, () -> parser.parseToJson("([)]"));
        assertThrows(RuntimeException.class, () -> parser.parseToJson("{[}]"));
        
        // 缺少逗号
        assertThrows(RuntimeException.class, () -> parser.parseToJson("[1 2 3]"));
        assertThrows(RuntimeException.class, () -> parser.parseToJson("{'a': 1 'b': 2}"));
        
        // 多余的逗号
//        assertThrows(RuntimeException.class, () -> parser.parseToJson("[1, 2, 3,]"));
//        assertThrows(RuntimeException.class, () -> parser.parseToJson("{'a': 1,}"));
        
        // 缺少冒号
        assertThrows(RuntimeException.class, () -> parser.parseToJson("{'key' 'value'}"));
        
        // 多余的冒号
        assertThrows(RuntimeException.class, () -> parser.parseToJson("{'key':: 'value'}"));
        
        // 空输入和空白字符
        assertThrows(RuntimeException.class, () -> parser.parseToJson(""));
        assertThrows(RuntimeException.class, () -> parser.parseToJson("   "));
        assertThrows(RuntimeException.class, () -> parser.parseToJson("\t\n"));
        
        // null输入
        assertThrows(RuntimeException.class, () -> parser.parseToJson(null));
    }

    // ==================== 辅助方法 ====================
    
    private void assertThrows(Class<? extends Throwable> expectedType, Runnable executable) {
        try {
            executable.run();
            fail("Expected " + expectedType.getSimpleName() + " to be thrown");
        } catch (Throwable actualException) {
            if (!expectedType.isInstance(actualException)) {
                fail("Expected " + expectedType.getSimpleName() + " but got " + actualException.getClass().getSimpleName());
            }
        }
    }
}