package com.github.typist.lexer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Token 类的单元测试
 * 
 * 测试 Token 类的核心功能：
 * 1. 构造函数和基本属性访问
 * 2. equals() 和 hashCode() 方法的正确性
 * 3. toString() 方法的格式化输出
 * 4. 边界条件和异常处理
 * 
 * @author typist
 * @version 1.1
 */
public class TokenTest {

    // ==================== 构造函数和基本属性测试 ====================
    
    @Test
    public void testTokenConstruction() {
        Token token = new Token(TokenType.NUMBER, 42, 10);
        
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals(42, token.getValue());
        assertEquals(10, token.getPosition());
    }
    
    @Test
    public void testTokenWithNullValue() {
        Token token = new Token(TokenType.NULL, null, 5);
        
        assertEquals(TokenType.NULL, token.getType());
        assertNull(token.getValue());
        assertEquals(5, token.getPosition());
    }
    
    @Test
    public void testTokenWithStringValue() {
        Token token = new Token(TokenType.STRING, "hello", 0);
        
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("hello", token.getValue());
        assertEquals(0, token.getPosition());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTokenWithNullType() {
        new Token(null, "value", 0);
    }

    // ==================== equals() 方法测试 ====================
    
    @Test
    public void testTokenEquality() {
        // 相同的记号应该相等
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.NUMBER, 42, 0);
        
        assertEquals(token1, token2);
        assertEquals(token2, token1); // 对称性
    }
    
    @Test
    public void testTokenEqualityWithSameReference() {
        Token token = new Token(TokenType.STRING, "test", 5);
        
        assertEquals(token, token); // 反射性
    }
    
    @Test
    public void testTokenInequalityDifferentType() {
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.STRING, "42", 0);
        
        assertNotEquals(token1, token2);
    }
    
    @Test
    public void testTokenInequalityDifferentValue() {
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.NUMBER, 43, 0);
        
        assertNotEquals(token1, token2);
    }
    
    @Test
    public void testTokenInequalityDifferentPosition() {
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.NUMBER, 42, 1);
        
        assertNotEquals(token1, token2);
    }
    
    @Test
    public void testTokenInequalityWithNull() {
        Token token = new Token(TokenType.NUMBER, 42, 0);
        
        assertNotEquals(token, null);
    }
    
    @Test
    public void testTokenInequalityWithDifferentClass() {
        Token token = new Token(TokenType.NUMBER, 42, 0);
        String notAToken = "not a token";
        
        assertNotEquals(token, notAToken);
    }
    
    @Test
    public void testTokenEqualityWithNullValues() {
        Token token1 = new Token(TokenType.NULL, null, 0);
        Token token2 = new Token(TokenType.NULL, null, 0);
        
        assertEquals(token1, token2);
    }
    
    @Test
    public void testTokenInequalityOneNullValue() {
        Token token1 = new Token(TokenType.STRING, "hello", 0);
        Token token2 = new Token(TokenType.STRING, null, 0);
        
        assertNotEquals(token1, token2);
    }

    // ==================== hashCode() 方法测试 ====================
    
    @Test
    public void testTokenHashCodeConsistency() {
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.NUMBER, 42, 0);
        
        // 相等的对象必须有相同的hashCode
        assertEquals(token1.hashCode(), token2.hashCode());
    }
    
    @Test
    public void testTokenHashCodeStability() {
        Token token = new Token(TokenType.STRING, "test", 5);
        
        // 同一个对象多次调用hashCode应该返回相同值
        int hash1 = token.hashCode();
        int hash2 = token.hashCode();
        assertEquals(hash1, hash2);
    }
    
    @Test
    public void testTokenHashCodeWithNullValue() {
        Token token1 = new Token(TokenType.NULL, null, 0);
        Token token2 = new Token(TokenType.NULL, null, 0);
        
        assertEquals(token1.hashCode(), token2.hashCode());
    }
    
    @Test
    public void testTokenHashCodeDifferentTokens() {
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.STRING, "42", 0);
        
        // 不同的记号通常应该有不同的hashCode（不保证，但概率很高）
        assertNotEquals(token1.hashCode(), token2.hashCode());
    }

    // ==================== toString() 方法测试 ====================
    
    @Test
    public void testTokenToString() {
        Token token = new Token(TokenType.NUMBER, 42, 10);
        String result = token.toString();
        
        assertTrue(result.contains("NUMBER"));
        assertTrue(result.contains("42"));
        assertTrue(result.contains("10"));
        assertTrue(result.startsWith("Token{"));
        assertTrue(result.endsWith("}"));
    }
    
    @Test
    public void testTokenToStringWithNullValue() {
        Token token = new Token(TokenType.NULL, null, 5);
        String result = token.toString();
        
        assertTrue(result.contains("NULL"));
        assertTrue(result.contains("null"));
        assertTrue(result.contains("5"));
    }
    
    @Test
    public void testTokenToStringWithStringValue() {
        Token token = new Token(TokenType.STRING, "hello world", 0);
        String result = token.toString();
        
        assertTrue(result.contains("STRING"));
        assertTrue(result.contains("hello world"));
        assertTrue(result.contains("0"));
    }

    // ==================== 各种TokenType的测试 ====================
    
    @Test
    public void testNumberTokens() {
        // 整数
        Token intToken = new Token(TokenType.NUMBER, 42, 0);
        assertEquals(TokenType.NUMBER, intToken.getType());
        assertEquals(42, intToken.getValue());
        
        // 浮点数
        Token doubleToken = new Token(TokenType.NUMBER, 3.14, 5);
        assertEquals(TokenType.NUMBER, doubleToken.getType());
        assertEquals(3.14, doubleToken.getValue());
        
        // 负数
        Token negativeToken = new Token(TokenType.NUMBER, -10, 8);
        assertEquals(TokenType.NUMBER, negativeToken.getType());
        assertEquals(-10, negativeToken.getValue());
    }
    
    @Test
    public void testBooleanTokens() {
        Token trueToken = new Token(TokenType.BOOLEAN, true, 0);
        assertEquals(TokenType.BOOLEAN, trueToken.getType());
        assertEquals(true, trueToken.getValue());
        
        Token falseToken = new Token(TokenType.BOOLEAN, false, 5);
        assertEquals(TokenType.BOOLEAN, falseToken.getType());
        assertEquals(false, falseToken.getValue());
    }
    
    @Test
    public void testDelimiterTokens() {
        Token leftBracket = new Token(TokenType.LEFT_BRACKET, '[', 0);
        assertEquals(TokenType.LEFT_BRACKET, leftBracket.getType());
        assertEquals('[', leftBracket.getValue());
        
        Token rightBrace = new Token(TokenType.RIGHT_BRACE, '}', 10);
        assertEquals(TokenType.RIGHT_BRACE, rightBrace.getType());
        assertEquals('}', rightBrace.getValue());
        
        Token comma = new Token(TokenType.COMMA, ',', 5);
        assertEquals(TokenType.COMMA, comma.getType());
        assertEquals(',', comma.getValue());
        
        Token colon = new Token(TokenType.COLON, ':', 8);
        assertEquals(TokenType.COLON, colon.getType());
        assertEquals(':', colon.getValue());
    }
    
    @Test
    public void testEofToken() {
        Token eof = new Token(TokenType.EOF, null, 100);
        assertEquals(TokenType.EOF, eof.getType());
        assertNull(eof.getValue());
        assertEquals(100, eof.getPosition());
    }

    // ==================== 边界条件测试 ====================
    
    @Test
    public void testTokenWithZeroPosition() {
        Token token = new Token(TokenType.STRING, "start", 0);
        assertEquals(0, token.getPosition());
    }
    
    @Test
    public void testTokenWithLargePosition() {
        Token token = new Token(TokenType.EOF, null, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, token.getPosition());
    }
    
    @Test
    public void testTokenWithEmptyString() {
        Token token = new Token(TokenType.STRING, "", 5);
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("", token.getValue());
        assertEquals(5, token.getPosition());
    }

    // ==================== 复杂场景测试 ====================
    
    @Test
    public void testTokenEqualityTransitivity() {
        // 传递性：如果a=b且b=c，则a=c
        Token token1 = new Token(TokenType.NUMBER, 42, 0);
        Token token2 = new Token(TokenType.NUMBER, 42, 0);
        Token token3 = new Token(TokenType.NUMBER, 42, 0);
        
        assertEquals(token1, token2);
        assertEquals(token2, token3);
        assertEquals(token1, token3); // 传递性
    }
    
    @Test
    public void testTokenHashCodeDistribution() {
        // 测试不同记号的hashCode分布（避免过多冲突）
        Token token1 = new Token(TokenType.NUMBER, 1, 0);
        Token token2 = new Token(TokenType.NUMBER, 2, 0);
        Token token3 = new Token(TokenType.STRING, "1", 0);
        Token token4 = new Token(TokenType.BOOLEAN, true, 0);
        
        // 这些不同的记号应该有不同的hashCode（概率性测试）
        int hash1 = token1.hashCode();
        int hash2 = token2.hashCode();
        int hash3 = token3.hashCode();
        int hash4 = token4.hashCode();
        
        // 至少应该有一些不同的hash值
        assertTrue("Hash codes should be different for different tokens",
                   hash1 != hash2 || hash1 != hash3 || hash1 != hash4);
    }
    
    @Test
    public void testTokenWithComplexStringValue() {
        String complexString = "hello\nworld\t\"quoted\"";
        Token token = new Token(TokenType.STRING, complexString, 15);
        
        assertEquals(TokenType.STRING, token.getType());
        assertEquals(complexString, token.getValue());
        assertEquals(15, token.getPosition());
        
        String toStringResult = token.toString();
        assertTrue(toStringResult.contains("STRING"));
        assertTrue(toStringResult.contains("15"));
    }
}