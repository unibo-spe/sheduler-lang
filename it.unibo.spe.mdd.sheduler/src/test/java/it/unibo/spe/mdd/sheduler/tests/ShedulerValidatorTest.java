package it.unibo.spe.mdd.sheduler.tests;

import com.google.inject.Inject;
import it.unibo.spe.mdd.sheduler.sheduler.TaskPoolSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static it.unibo.spe.mdd.sheduler.sheduler.ShedulerPackage.Literals.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(ShedulerInjectorProvider.class)
class ShedulerValidatorTest {
    @Inject
    private ParseHelper<TaskPoolSet> parseHelper;

    @Inject
    private ValidationTestHelper helper;

    private static String t(String name) {
        return "schedule task " + name + " { command \"c\" in 1 s } ";
    }

    private TaskPoolSet task(String timing) throws Exception {
        return parseHelper.parse("pool { schedule task x { command \"c\" " + timing + " } }");
    }

    @Test
    void relativeTimeNotRepresentable() throws Exception {
        helper.assertWarning(task("in 2147483647 years"), RELATIVE_TIME, null, "not representable");
    }

    @Test
    void absoluteTimeNotRepresentable() throws Exception {
        helper.assertWarning(task("at 2030/02/31 10:00"), ABSOLUTE_TIME, null, "not representable");
    }

    @Test
    void absoluteTimeInThePast() throws Exception {
        helper.assertWarning(task("at 2000/01/01 10:00"), ABSOLUTE_TIME, null, "future");
    }

    @Test
    void invalidDateDoesNotBreakValidation() throws Exception {
        helper.assertError(task("at 2030/13/01 10:00"), DATE, null, "Month");
    }

    @Test
    void invalidClockTime() throws Exception {
        helper.assertError(task("at 2099/01/01 25:00"), CLOCK_TIME, null, "Hour");
    }

    @Test
    void zeroDuration() throws Exception {
        helper.assertError(task("in 0 s"), TIME_SPAN, null, "strictly positive");
    }

    @Test
    void longDurationsAreAllowed() throws Exception {
        helper.assertNoErrors(task("in 1 s repeat every 48 hours"));
    }

    @Test
    void repeatedTaskName() throws Exception {
        helper.assertError(parseHelper.parse("pool { " + t("x") + t("x") + "}"), TASK, null, "Repeated task ID");
    }

    @Test
    void repeatedPoolName() throws Exception {
        helper.assertError(parseHelper.parse("pool p { " + t("x") + "} pool p { " + t("other") + "}"), TASK_POOL, null, "Repeated pool ID");
    }

    @Test
    void dependentTaskCannotBePeriodic() throws Exception {
        var model = parseHelper.parse("pool { " + t("a") + "schedule task b { command \"c\" after a repeat every 1 h } }");
        helper.assertError(model, TASK, null, "cannot be periodic");
    }

    @Test
    void periodTooShort() throws Exception {
        helper.assertError(task("in 1 s repeat every 500 ns"), TASK, null, "at least 1 millisecond");
    }

    @Test
    void cyclicDependency() throws Exception {
        var model = parseHelper.parse("pool { schedule task a { command \"c\" after b } schedule task b { command \"c\" before a } }");
        helper.assertError(model, TASK, null, "Cyclic");
    }

    @Test
    void acyclicDependencyChain() throws Exception {
        var model = parseHelper.parse("pool { " + t("a") + "schedule task b { command \"c\" after a } schedule task c { command \"c\" before b } }");
        helper.assertNoErrors(model);
    }
}
