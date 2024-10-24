package it.unibo.spe.mdd.sheduler.generator;

import org.eclipse.xtext.generator.AbstractGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractShedulerGenerator extends AbstractGenerator {
    private static Map<String, String> templates = new HashMap<>();

    private static String templateFile(String name) throws IOException {
        if (!templates.containsKey(name)) {
            InputStream stream = AbstractShedulerGenerator.class.getResourceAsStream(name + ".template");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                var content = reader.lines().collect(Collectors.joining("\n"));
                templates.put(name, content);
            }
        }
        return templates.get(name);
    }

    protected static String javaTemplateFile(String name) throws IOException {
        return templateFile(name + ".java");
    }

    protected static String javaTemplateFile(String name, Map<String, String> replacements) throws IOException {
        return replace(javaTemplateFile(name), replacements);
    }

    protected static String replace(String template, Map<String, String> replacements) {
        for (var entry : replacements.entrySet()) {
            template = template.replace("__" + entry.getKey() + "__", entry.getValue());
        }
        return template;
    }
}
