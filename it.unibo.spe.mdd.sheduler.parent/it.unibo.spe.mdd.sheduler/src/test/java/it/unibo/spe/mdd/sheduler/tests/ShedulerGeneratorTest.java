/*
 * generated by Xtext 2.32.0
 */
package it.unibo.spe.mdd.sheduler.tests;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(InjectionExtension.class)
@InjectWith(ShedulerInjectorProvider.class)
class ShedulerGeneratorTest extends AbstractTest {
	@Inject
	private GeneratorDelegate generator;

	@Inject
	private Provider<ResourceSet> resourceSetProvider;

	@Inject
	private IResourceValidator validator;

	@Inject
	private JavaIoFileSystemAccess fileAccess;

	private static File workingDir;
	private static File sourceFile;
	private static File outputDir;
	private static File generatedSystemFile;
	private static File generatedRuntimeFile;
	private static File generatedTaskFile;

	@BeforeAll
	public static void setUpAll() throws IOException {
		sourceFile = createTestFile("HelloWorld", loadResourceAsStringByName("HelloWorld.shed"));
		workingDir = sourceFile.getParentFile();
		outputDir = new File(workingDir, "src-gen");
		if (outputDir.exists()) {
			outputDir.delete();
		}
		generatedSystemFile = new File(outputDir, "ShedulerSystem_" + sourceFile.getName().split("\\.")[0] + ".java");
		generatedRuntimeFile = new File(outputDir, "ShedulerRuntime.java");
		generatedTaskFile = new File(outputDir, "ShedulerTask.java");
	}

	private void generate(File inputFile) {
		ResourceSet set = resourceSetProvider.get();
		Resource resource = set.getResource(URI.createFileURI(inputFile.getAbsolutePath()), true);

		// Validate the resource
		List<Issue> list = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
		if (!list.isEmpty()) {
			for (Issue issue : list) {
				Assertions.fail(issue.getMessage());
			}
			return;
		}

		// Configure and start the generator
		fileAccess.setOutputPath(outputDir.getAbsolutePath());
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(CancelIndicator.NullImpl);
		generator.generate(resource, fileAccess, context);
	}

	@Test
	public void generate() throws Exception {
		generate(sourceFile);
		System.out.println("Generated files in " + outputDir.getAbsolutePath());
		assertTrue(generatedSystemFile.exists());
		assertTrue(generatedRuntimeFile.exists());
		assertTrue(generatedTaskFile.exists());
	}
}
