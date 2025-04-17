package com.cgi.privsense.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StringUtils utility methods.
 */
public class StringUtilsTest {

    @Test
    void testEscapeSqlIdentifier() {
        assertEquals("\"table\"", StringUtils.escapeSqlIdentifier("table"), 
                "Should properly escape simple identifier");
        assertEquals("\"column_name\"", StringUtils.escapeSqlIdentifier("column_name"), 
                "Should properly escape identifier with underscore");
        assertEquals("\"table\"\"name\"", StringUtils.escapeSqlIdentifier("table\"name"), 
                "Should properly escape identifier with double quote");
    }
    
    @Test
    void testEscapeSqlIdentifierWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.escapeSqlIdentifier(null);
        }, "Should throw IllegalArgumentException for null identifier");
    }
    
    @Test
    void testEscapeSqlLiteral() {
        assertEquals("'value'", StringUtils.escapeSqlLiteral("value"), 
                "Should properly escape simple literal");
        assertEquals("'O''Reilly'", StringUtils.escapeSqlLiteral("O'Reilly"), 
                "Should properly escape literal with single quote");
        assertEquals("'Multiple''quotes''test'", StringUtils.escapeSqlLiteral("Multiple'quotes'test"), 
                "Should properly escape literal with multiple quotes");
    }
    
    @Test
    void testEscapeSqlLiteralWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.escapeSqlLiteral(null);
        }, "Should throw IllegalArgumentException for null literal");
    }
    
    @Test
    void testContainsSqlInjection() {
        assertTrue(StringUtils.containsSqlInjection("SELECT * FROM users"), 
                "Should detect SELECT statement");
        assertTrue(StringUtils.containsSqlInjection("name'; DROP TABLE users; --"), 
                "Should detect SQL injection with comment");
        assertTrue(StringUtils.containsSqlInjection("value OR 1=1"), 
                "Should detect common injection pattern");
        assertFalse(StringUtils.containsSqlInjection("normal text"), 
                "Should not flag normal text as SQL injection");
        assertFalse(StringUtils.containsSqlInjection("O'Reilly"), 
                "Should not flag text with apostrophe as SQL injection");
        assertFalse(StringUtils.containsSqlInjection(null), 
                "Should handle null input safely");
    }
    
    @Test
    void testNormalizeDbObjectName() {
        assertEquals("TABLE", StringUtils.normalizeDbObjectName("table"), 
                "Should normalize to uppercase");
        assertEquals("TABLENAME", StringUtils.normalizeDbObjectName("\"tablename\""), 
                "Should remove quotes and normalize");
        assertEquals("TABLE_NAME", StringUtils.normalizeDbObjectName("table_name"), 
                "Should preserve underscores while normalizing");
    }
    
    @Test
    void testNormalizeDbObjectNameWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.normalizeDbObjectName(null);
        }, "Should throw IllegalArgumentException for null object name");
    }
    
    @Test
    void testIsValidDbIdentifier() {
        assertTrue(StringUtils.isValidDbIdentifier("table"), 
                "Simple identifier should be valid");
        assertTrue(StringUtils.isValidDbIdentifier("table_name"), 
                "Identifier with underscore should be valid");
        assertTrue(StringUtils.isValidDbIdentifier("t123"), 
                "Identifier with numbers should be valid");
        assertTrue(StringUtils.isValidDbIdentifier("_table"), 
                "Identifier starting with underscore should be valid");
        assertTrue(StringUtils.isValidDbIdentifier("$table"), 
                "Identifier starting with dollar should be valid");
        
        assertFalse(StringUtils.isValidDbIdentifier(""), 
                "Empty identifier should be invalid");
        assertFalse(StringUtils.isValidDbIdentifier(null), 
                "Null identifier should be invalid");
        assertFalse(StringUtils.isValidDbIdentifier("123table"), 
                "Identifier starting with number should be invalid");
        assertFalse(StringUtils.isValidDbIdentifier("table-name"), 
                "Identifier with hyphen should be invalid");
        assertFalse(StringUtils.isValidDbIdentifier("table name"), 
                "Identifier with space should be invalid");
    }
}
