package pt.isep.sem4pi.services;

import org.junit.Test;

import static org.junit.Assert.*;

public class SomeNumberTest {
    @Test
    public void testSomeSomeNumber() {
        int  num1 = 1;
        int num2 = 3;

        SomeNumber someNumber = new SomeNumber();

        Integer result = someNumber.someSomeNumber(num1, num2);
        Integer resultExpected = 4;

        assertEquals(resultExpected, result);
    }
}