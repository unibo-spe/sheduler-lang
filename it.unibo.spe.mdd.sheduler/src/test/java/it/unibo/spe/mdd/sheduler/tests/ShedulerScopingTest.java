package it.unibo.spe.mdd.sheduler.tests;

import com.google.inject.Inject;
import it.unibo.spe.mdd.sheduler.sheduler.ShedulerPackage;
import it.unibo.spe.mdd.sheduler.sheduler.Task;
import it.unibo.spe.mdd.sheduler.sheduler.TaskPoolSet;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(ShedulerInjectorProvider.class)
class ShedulerScopingTest {
    @Inject
    private ParseHelper<TaskPoolSet> parseHelper;

    @Inject
    private ValidationTestHelper helper;

    @Inject
    private IScopeProvider scopeProvider;

    private static String t(String name) {
        return "schedule task " + name + " { command \"c\" in 1 s } ";
    }

    @Disabled("TODO Ex 2: remove this line once done")
    @Test
    void scopeContainsOnlyNamedSiblingTasks() throws Exception {
        TaskPoolSet model = parseHelper.parse("pool p { " + t("a") + "schedule { command \"anon\" in 1 s } "
                + "schedule task b { command \"c\" after a } } pool q { " + t("z") + "}");
        Task b = model.getPools().get(0).getTasks().get(2);
        List<String> names = StreamSupport.stream(scopeProvider.getScope(b, ShedulerPackage.Literals.TASK__AFTER).getAllElements().spliterator(), false)
                .map(IEObjectDescription::getName)
                .map(Object::toString)
                .toList();
        assertEquals(List.of("a"), names);
    }

    @Disabled("TODO Ex 2: remove this line once done")
    @Test
    void crossPoolReferenceFails() throws Exception {
        TaskPoolSet model = parseHelper.parse("pool p { " + t("x") + "} pool q { schedule task other { command \"c\" after x } }");
        helper.assertError(model, ShedulerPackage.Literals.TASK, Diagnostic.LINKING_DIAGNOSTIC);
    }

    @Disabled("TODO Ex 2: remove this line once done")
    @Test
    void selfReferenceFails() throws Exception {
        TaskPoolSet model = parseHelper.parse("pool { " + t("a") + "schedule task x { command \"c\" after x } }");
        helper.assertError(model, ShedulerPackage.Literals.TASK, Diagnostic.LINKING_DIAGNOSTIC);
    }
}
