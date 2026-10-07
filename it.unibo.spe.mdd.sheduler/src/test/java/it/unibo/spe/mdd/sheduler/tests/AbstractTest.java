package it.unibo.spe.mdd.sheduler.tests;

import java.io.*;

public class AbstractTest {
    protected static String loadResourceAsStringByName(String name) {
        InputStream resource = AbstractTest.class.getResourceAsStream(name);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
            return reader.lines().reduce("", (a, b) -> a + b + "\n");
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot load resource " + name, e);
        }
    }
}
