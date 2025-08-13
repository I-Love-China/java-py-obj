package com.github.typist;

import org.junit.Test;
import static org.junit.Assert.*;

public class PythonObjectParserTest {

    private final PythonObjectParser parser = new PythonObjectParser();

    // ==================== 基础数据类型测试 ====================
    
    @Test
    public void testPrimitiveTypes() {
        assertEquals("42", parser.parseToJson("42"));
        assertEquals(42, parser.parseToObject("42"));
        
        assertEquals("3.14", parser.parseToJson("3.14"));
        assertEquals(3.14, parser.parseToObject("3.14"));
        
        assertEquals("true", parser.parseToJson("True"));
        assertEquals(true, parser.parseToObject("True"));
        
        assertEquals("false", parser.parseToJson("False"));
        assertEquals(false, parser.parseToObject("False"));
        
        assertEquals("null", parser.parseToJson("None"));
        assertNull(parser.parseToObject("None"));
        
        assertEquals("\"hello\"", parser.parseToJson("'hello'"));
        assertEquals("hello", parser.parseToObject("'hello'"));
        
        assertEquals("\"world\"", parser.parseToJson("\"world\""));
        assertEquals("world", parser.parseToObject("\"world\""));
    }

    @Test
    public void testNegativeNumbers() {
        assertEquals("-42", parser.parseToJson("-42"));
        assertEquals(-42, parser.parseToObject("-42"));
        
        assertEquals("-3.14", parser.parseToJson("-3.14"));
        assertEquals(-3.14, parser.parseToObject("-3.14"));
    }

    @Test
    public void testBoundaryValues() {
        // 空容器
        assertEquals("[]", parser.parseToJson("[]"));
        assertArrayEquals(new Object[0], (Object[])parser.parseToObject("[]"));
        
        assertEquals("[]", parser.parseToJson("()"));
        assertArrayEquals(new Object[0], (Object[])parser.parseToObject("()"));
        
        assertEquals("{}", parser.parseToJson("{}"));
        assertTrue(((java.util.Map)parser.parseToObject("{}")).isEmpty());
        
        // 极大和极小整数
        // Integer.MAX_VALUE
        assertEquals("2147483647", parser.parseToJson("2147483647"));
        assertEquals(2147483647, parser.parseToObject("2147483647"));
        
        // Integer.MIN_VALUE
        assertEquals("-2147483648", parser.parseToJson("-2147483648"));
        assertEquals(-2147483648, parser.parseToObject("-2147483648"));

        // 极大和极小浮点数
//        assertEquals("1.7976931348623157E308", parser.parseToJson("1.7976931348623157E308"));
//        assertEquals("4.9E-324", parser.parseToJson("4.9E-324"));
        
        // 零值
        assertEquals("0", parser.parseToJson("0"));
        assertEquals(0, parser.parseToObject("0"));
        
        assertEquals("0.0", parser.parseToJson("0.0"));
        assertEquals(0.0, parser.parseToObject("0.0"));

        //        assertEquals("-0", parser.parseToJson("-0"));

        // 单元素容器
        assertEquals("[1]", parser.parseToJson("[1]"));
        assertArrayEquals(new Object[]{1}, (Object[])parser.parseToObject("[1]"));
        
        assertEquals("[1]", parser.parseToJson("(1,)"));
        assertArrayEquals(new Object[]{1}, (Object[])parser.parseToObject("(1,)"));
        
        assertEquals("{\"key\":\"value\"}", parser.parseToJson("{'key': 'value'}"));
        java.util.Map<String, Object> singleKeyValue = (java.util.Map<String, Object>)parser.parseToObject("{'key': 'value'}");
        assertEquals("value", singleKeyValue.get("key"));
    }

    // ==================== 集合类型测试 ====================
    
    @Test
    public void testList() {
        assertEquals("[1,2,3]", parser.parseToJson("[1, 2, 3]"));
        assertArrayEquals(new Object[]{1, 2, 3}, (Object[])parser.parseToObject("[1, 2, 3]"));
        
        assertEquals("[\"a\",\"b\",\"c\"]", parser.parseToJson("['a', 'b', 'c']"));
        assertArrayEquals(new Object[]{"a", "b", "c"}, (Object[])parser.parseToObject("['a', 'b', 'c']"));
        
        assertEquals("[1,\"hello\",true,null]", parser.parseToJson("[1, 'hello', True, None]"));
        assertArrayEquals(new Object[]{1, "hello", true, null}, (Object[])parser.parseToObject("[1, 'hello', True, None]"));
        
        assertEquals("[]", parser.parseToJson("[]"));
        assertArrayEquals(new Object[0], (Object[])parser.parseToObject("[]"));
    }

    @Test
    public void testTuple() {
        assertEquals("[1,2,3]", parser.parseToJson("(1, 2, 3)"));
        assertArrayEquals(new Object[]{1, 2, 3}, (Object[])parser.parseToObject("(1, 2, 3)"));
        
        assertEquals("[\"a\",\"b\"]", parser.parseToJson("('a', 'b')"));
        assertArrayEquals(new Object[]{"a", "b"}, (Object[])parser.parseToObject("('a', 'b')"));
        
        assertEquals("[]", parser.parseToJson("()"));
        assertArrayEquals(new Object[0], (Object[])parser.parseToObject("()"));
    }

    @Test
    public void testSet() {
        String result = parser.parseToJson("{1, 2, 3}");
        assertTrue(result.contains("1") && result.contains("2") && result.contains("3"));
        
        Object[] setResult = (Object[])parser.parseToObject("{1, 2, 3}");
        assertTrue(java.util.Arrays.asList(setResult).contains(1));
        assertTrue(java.util.Arrays.asList(setResult).contains(2));
        assertTrue(java.util.Arrays.asList(setResult).contains(3));
    }

    @Test
    public void testDict() {
        String result1 = parser.parseToJson("{'name': 'John', 'age': 30}");
        assertTrue(result1.contains("\"name\":\"John\"") && result1.contains("\"age\":30"));
        
        java.util.Map<String, Object> dict1 = (java.util.Map<String, Object>)parser.parseToObject("{'name': 'John', 'age': 30}");
        assertEquals("John", dict1.get("name"));
        assertEquals(30, dict1.get("age"));

        assertEquals("{\"active\":true}", parser.parseToJson("{'active': True}"));
        java.util.Map<String, Object> dict2 = (java.util.Map<String, Object>)parser.parseToObject("{'active': True}");
        assertEquals(true, dict2.get("active"));
        
        assertEquals("{}", parser.parseToJson("{}"));
        java.util.Map<String, Object> dict3 = (java.util.Map<String, Object>)parser.parseToObject("{}");
        assertTrue(dict3.isEmpty());
    }

    // ==================== 嵌套结构测试 ====================
    
    @Test
    public void testNestedStructures() {
        String input = "{'users': [{'name': 'Alice', 'active': True}, {'name': 'Bob', 'active': False}]}";
        String result = parser.parseToJson(input);
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Bob"));
        assertTrue(result.contains("users"));
        
        java.util.Map<String, Object> nestedResult = (java.util.Map<String, Object>)parser.parseToObject(input);
        Object[] users = (Object[])nestedResult.get("users");
        assertEquals(2, users.length);
        
        java.util.Map<String, Object> alice = (java.util.Map<String, Object>)users[0];
        assertEquals("Alice", alice.get("name"));
        assertEquals(true, alice.get("active"));
        
        java.util.Map<String, Object> bob = (java.util.Map<String, Object>)users[1];
        assertEquals("Bob", bob.get("name"));
        assertEquals(false, bob.get("active"));
    }

    @Test
    public void testComplexNesting() {
        String input = "[{'data': [1, 2, {'nested': True}]}, (3, 4), {5, 6}]";
        String result = parser.parseToJson(input);
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        
        Object[] complexResult = (Object[])parser.parseToObject(input);
        assertEquals(3, complexResult.length);
        
        java.util.Map<String, Object> firstElement = (java.util.Map<String, Object>)complexResult[0];
        Object[] dataArray = (Object[])firstElement.get("data");
        assertEquals(3, dataArray.length);
        java.util.Map<String, Object> nestedMap = (java.util.Map<String, Object>)dataArray[2];
        assertEquals(true, nestedMap.get("nested"));
        
        Object[] tupleElement = (Object[])complexResult[1];
        assertArrayEquals(new Object[]{3, 4}, tupleElement);
    }

    @Test
    public void testDeepNestedStructures() {
        // 5层嵌套字典
        String deepDict = "{'l1': {'l2': {'l3': {'l4': {'l5': 'deep'}}}}}";
        String result = parser.parseToJson(deepDict);
        assertTrue(result.contains("\"l5\":\"deep\""));
        
        java.util.Map<String, Object> deepDictResult = (java.util.Map<String, Object>)parser.parseToObject(deepDict);
        java.util.Map<String, Object> l1 = (java.util.Map<String, Object>)deepDictResult.get("l1");
        java.util.Map<String, Object> l2 = (java.util.Map<String, Object>)l1.get("l2");
        java.util.Map<String, Object> l3 = (java.util.Map<String, Object>)l2.get("l3");
        java.util.Map<String, Object> l4 = (java.util.Map<String, Object>)l3.get("l4");
        assertEquals("deep", l4.get("l5"));
        
        // 深度嵌套列表
        String deepList = "[[[[[1]]]]]";
        assertEquals("[[[[[1]]]]]", parser.parseToJson(deepList));
        
        Object[] deepListResult = (Object[])parser.parseToObject(deepList);
        Object[] level1 = (Object[])deepListResult[0];
        Object[] level2 = (Object[])level1[0];
        Object[] level3 = (Object[])level2[0];
        Object[] level4 = (Object[])level3[0];
        assertEquals(1, level4[0]);
        
        // 混合深度嵌套
        String mixed = "{'users': [{'profile': {'contacts': [{'type': 'email', 'value': 'test@example.com'}]}}]}";
        String mixedResult = parser.parseToJson(mixed);
        assertTrue(mixedResult.contains("\"email\""));
        assertTrue(mixedResult.contains("\"test@example.com\""));
        
        java.util.Map<String, Object> mixedResult2 = (java.util.Map<String, Object>)parser.parseToObject(mixed);
        Object[] mixedUsers = (Object[])mixedResult2.get("users");
        java.util.Map<String, Object> userProfile = (java.util.Map<String, Object>)((java.util.Map<String, Object>)mixedUsers[0]).get("profile");
        Object[] contacts = (Object[])userProfile.get("contacts");
        java.util.Map<String, Object> contact = (java.util.Map<String, Object>)contacts[0];
        assertEquals("email", contact.get("type"));
        assertEquals("test@example.com", contact.get("value"));
        
        // 复杂嵌套场景
        String complex = "[{'data': [1, {'nested': [True, False, None]}, (2, 3)]}, {4, 5}]";
        String complexResult = parser.parseToJson(complex);
        assertTrue(complexResult.contains("true"));
        assertTrue(complexResult.contains("false"));
        assertTrue(complexResult.contains("null"));
        
        Object[] complexObjResult = (Object[])parser.parseToObject(complex);
        java.util.Map<String, Object> firstMap = (java.util.Map<String, Object>)complexObjResult[0];
        Object[] dataArray = (Object[])firstMap.get("data");
        java.util.Map<String, Object> nestedMap = (java.util.Map<String, Object>)dataArray[1];
        Object[] nestedArray = (Object[])nestedMap.get("nested");
        assertArrayEquals(new Object[]{true, false, null}, nestedArray);
    }

    // ==================== 字符串测试 ====================
    
    @Test
    public void testStringEscaping() {
        assertEquals("\"hello\\nworld\"", parser.parseToJson("'hello\\nworld'"));
        assertEquals("hello\nworld", parser.parseToObject("'hello\\nworld'"));
        
        assertEquals("\"tab\\there\"", parser.parseToJson("'tab\\there'"));
        assertEquals("tab\there", parser.parseToObject("'tab\\there'"));
        
        assertEquals("\"quote\\\"test\"", parser.parseToJson("'quote\\\"test'"));
        assertEquals("quote\"test", parser.parseToObject("'quote\\\"test'"));
    }

    @Test
    public void testMoreStringEscaping() {
        // 各种转义字符
        assertEquals("\"\\r\"", parser.parseToJson("'\\r'"));
        assertEquals("\r", parser.parseToObject("'\\r'"));

        //        assertEquals("\"\\f\"", parser.parseToJson("'\\f'"));
        //        assertEquals("\"\\b\"", parser.parseToJson("'\\b'"));
        //        assertEquals("\"\\v\"", parser.parseToJson("'\\v'"));
        
        // 单引号在双引号字符串中
        assertEquals("\"single'quote\"", parser.parseToJson("\"single'quote\""));
        assertEquals("single'quote", parser.parseToObject("\"single'quote\""));
        
        // 双引号在单引号字符串中
        assertEquals("\"double\\\"quote\"", parser.parseToJson("'double\\\"quote'"));
        assertEquals("double\"quote", parser.parseToObject("'double\\\"quote'"));
        
        // 混合转义
        assertEquals("\"line1\\nline2\\ttab\"", parser.parseToJson("'line1\\nline2\\ttab'"));
        assertEquals("line1\nline2\ttab", parser.parseToObject("'line1\\nline2\\ttab'"));
        
        // 反斜杠本身
        assertEquals("\"\\\\\"", parser.parseToJson("'\\\\'"));
        assertEquals("\\", parser.parseToObject("'\\\\'"));
        
        assertEquals("\"path\\\\to\\\\file\"", parser.parseToJson("'path\\\\to\\\\file'"));
        assertEquals("path\\to\\file", parser.parseToObject("'path\\\\to\\\\file'"));

        // Unicode转义
        //        assertEquals("\"\\u4e2d\\u6587\"", parser.parseToJson("'\\u4e2d\\u6587'"));
    }

    @Test
    public void testKeywordsInStrings() {
        // 关键字作为字符串内容
        assertEquals("\"True\"", parser.parseToJson("'True'"));
        assertEquals("True", parser.parseToObject("'True'"));
        
        assertEquals("\"False\"", parser.parseToJson("'False'"));
        assertEquals("False", parser.parseToObject("'False'"));
        
        assertEquals("\"None\"", parser.parseToJson("'None'"));
        assertEquals("None", parser.parseToObject("'None'"));
        
        // 字符串中包含关键字
        assertEquals("\"This is True statement\"", parser.parseToJson("'This is True statement'"));
        assertEquals("This is True statement", parser.parseToObject("'This is True statement'"));
        
        assertEquals("\"False alarm\"", parser.parseToJson("'False alarm'"));
        assertEquals("False alarm", parser.parseToObject("'False alarm'"));
        
        assertEquals("\"Nothing is None here\"", parser.parseToJson("'Nothing is None here'"));
        assertEquals("Nothing is None here", parser.parseToObject("'Nothing is None here'"));
        
        // 混合情况：字符串关键字 vs 实际关键字
        String result1 = parser.parseToJson("{'boolean_true': True, 'string_true': 'True'}");
        assertTrue(result1.contains("\"boolean_true\":true"));
        assertTrue(result1.contains("\"string_true\":\"True\""));
        
        java.util.Map<String, Object> mixedResult1 = (java.util.Map<String, Object>)parser.parseToObject("{'boolean_true': True, 'string_true': 'True'}");
        assertEquals(true, mixedResult1.get("boolean_true"));
        assertEquals("True", mixedResult1.get("string_true"));
        
        String result2 = parser.parseToJson("{'status': 'False', 'valid': False}");
        assertTrue(result2.contains("\"status\":\"False\""));
        assertTrue(result2.contains("\"valid\":false"));
        
        java.util.Map<String, Object> mixedResult2 = (java.util.Map<String, Object>)parser.parseToObject("{'status': 'False', 'valid': False}");
        assertEquals("False", mixedResult2.get("status"));
        assertEquals(false, mixedResult2.get("valid"));
    }

    @Test
    public void testUnicodeStrings() {
        // 中文字符
        assertEquals("\"你好世界\"", parser.parseToJson("'你好世界'"));
        assertEquals("你好世界", parser.parseToObject("'你好世界'"));
        
        assertEquals("\"汽车\"", parser.parseToJson("'汽车'"));
        assertEquals("汽车", parser.parseToObject("'汽车'"));
        
        // 混合中英文
        assertEquals("\"Hello世界\"", parser.parseToJson("'Hello世界'"));
        assertEquals("Hello世界", parser.parseToObject("'Hello世界'"));
        
        assertEquals("\"测试Test\"", parser.parseToJson("'测试Test'"));
        assertEquals("测试Test", parser.parseToObject("'测试Test'"));
        
        // 包含中文的复杂结构
        String result1 = parser.parseToJson("{'name': '张三', 'city': '北京'}");
        assertTrue(result1.contains("\"name\":\"张三\""));
        assertTrue(result1.contains("\"city\":\"北京\""));
        
        java.util.Map<String, Object> chineseResult = (java.util.Map<String, Object>)parser.parseToObject("{'name': '张三', 'city': '北京'}");
        assertEquals("张三", chineseResult.get("name"));
        assertEquals("北京", chineseResult.get("city"));
        
        // 数组中的中文
        assertEquals("[\"苹果\",\"香蕉\",\"橙子\"]", parser.parseToJson("['苹果', '香蕉', '橙子']"));
        assertArrayEquals(new Object[]{"苹果", "香蕉", "橙子"}, (Object[])parser.parseToObject("['苹果', '香蕉', '橙子']"));
        
        // 特殊Unicode字符
        // 心形符号
        assertEquals("\"❤\"", parser.parseToJson("'❤'"));
        assertEquals("❤", parser.parseToObject("'❤'"));
        
        // 版权符号
        assertEquals("\"©\"", parser.parseToJson("'©'"));
        assertEquals("©", parser.parseToObject("'©'"));
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