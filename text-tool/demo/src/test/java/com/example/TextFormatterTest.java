package com.example;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for simple App.
 */
public class TextFormatterTest 
{
    @Test
    void shouldConvertNormalTextToUppercase()
    {
        String output = TextFormatter.shout("hello maven");
        assertEquals ("HELLO MAVEN", output);
    }

    @Test
    void shouldTrimSurroundingWhitespace()
    {
        String output = TextFormatter.shout(" HeLLo Maven  ");
        assertEquals("HELLO MAVEN", output);
    }
    
    @Test
    void shouldThrowExceptionWhenInputIsNull()
    {
        assertThrows (IllegalArgumentException.class, () -> TextFormatter.shout(null));
    }
}
