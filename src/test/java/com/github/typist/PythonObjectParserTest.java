package com.github.typist;

import org.junit.Test;
import static org.junit.Assert.*;

public class PythonObjectParserTest {

    private final PythonObjectParser parser = new PythonObjectParser();

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

    @Test
    public void testNestedStructures() {
        String input = "{'users': [{'name': 'Alice', 'active': True}, {'name': 'Bob', 'active': False}]}";
        String result = parser.parseToJson(input);
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Bob"));
        assertTrue(result.contains("users"));
    }

    @Test
    public void testStringEscaping() {
        assertEquals("\"hello\\nworld\"", parser.parseToJson("'hello\\nworld'"));
        assertEquals("\"tab\\there\"", parser.parseToJson("'tab\\there'"));
        assertEquals("\"quote\\\"test\"", parser.parseToJson("'quote\\\"test'"));
    }

    @Test
    public void testNegativeNumbers() {
        assertEquals("-42", parser.parseToJson("-42"));
        assertEquals("-3.14", parser.parseToJson("-3.14"));
    }

    @Test
    public void testComplexNesting() {
        String input = "[{'data': [1, 2, {'nested': True}]}, (3, 4), {5, 6}]";
        String result = parser.parseToJson(input);
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidSyntax() {
        parser.parseToJson("[1, 2,");
    }

    @Test(expected = RuntimeException.class)
    public void testUnexpectedCharacter() {
        parser.parseToJson("@invalid");
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
}