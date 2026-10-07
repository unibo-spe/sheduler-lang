package it.unibo.spe.mdd.sheduler.tests;

import it.unibo.spe.mdd.sheduler.generator.ShedulerGenerator;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeTemplatesSyncTest {
    private static final Path RUNTIME_DIR = Path.of("src/main/java/it/unibo/spe/mdd/sheduler/runtime"); // Gradle/Eclipse test cwd = sub-project dir

    private static String normalize(String s) {
        return s.replace("\r\n", "\n").strip();
    }

    private static void assertTemplateMatchesRealClass(String name) throws Exception {
        String real = Files.readString(RUNTIME_DIR.resolve(name + ".java"));
        try (InputStream in = ShedulerGenerator.class.getResourceAsStream(name + ".java.template")) {
            String template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(normalize(real), normalize(template),
                    "RES/" + name + ".java.template must be an exact copy of runtime/" + name + ".java");
        }
    }

    @Test
    void taskTemplateMatchesRealClass() throws Exception {
        assertTemplateMatchesRealClass("ShedulerTask");
    }

    @Test
    void runtimeTemplateMatchesRealClass() throws Exception {
        assertTemplateMatchesRealClass("ShedulerRuntime");
    }
}
