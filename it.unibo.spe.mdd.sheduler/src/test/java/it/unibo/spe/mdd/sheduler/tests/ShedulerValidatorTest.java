package it.unibo.spe.mdd.sheduler.tests;

import com.google.inject.Inject;
import it.unibo.spe.mdd.sheduler.sheduler.TaskPoolSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Disabled;
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

    @Disabled("TODO Ex 1.1: remove this line once done")
    @Test
    void relativeTimeNotRepresentable() throws Exception {
        helper.assertWarning(task("in 2147483647 years"), RELATIVE_TIME, null, "not representable");
    }

    @Disabled("TODO Ex 1.2: remove this line once done")
    @Test
    void absoluteTimeNotRepresentable() throws Exception {
        helper.assertWarning(task("at 2030/02/31 10:00"), ABSOLUTE_TIME, null, "not representable");
    }

    @Disabled("TODO Ex 1.3: remove this line once done")
    @Test
    void absoluteTimeInThePast() throws Exception {
        helper.assertWarning(task("at 2000/01/01 10:00"), ABSOLUTE_TIME, null, "future");
    }

    @Disabled("TODO Ex 1.3 (handle DateTimeException): remove this line once done")
    @Test
    void invalidDateDoesNotBreakValidation() throws Exception {
        helper.assertError(task("at 2030/13/01 10:00"), DATE, null, "Month");
    }

    @Disabled("TODO Ex 1.4: remove this line once done")
    @Test
    void invalidClockTime() throws Exception {
        helper.assertError(task("at 2099/01/01 25:00"), CLOCK_TIME, null, "Hour");
    }

    @Disabled("TODO Ex 1.5: remove this line once done")
    @Test
    void zeroDuration() throws Exception {
        helper.assertError(task("in 0 s"), TIME_SPAN, null, "strictly positive");
    }

    @Test
    void longDurationsAreAllowed() throws Exception {
        helper.assertNoErrors(task("in 1 s repeat every 48 hours"));
    }

    @Disabled("TODO Ex 1.6: remove this line once done")
    @Test
    void repeatedTaskName() throws Exception {
        helper.assertError(parseHelper.parse("pool { " + t("x") + t("x") + "}"), TASK, null, "Repeated task ID");
    }

    @Disabled("TODO Ex 1.7: remove this line once done")
    @Test
    void repeatedPoolName() throws Exception {
        helper.assertError(parseHelper.parse("pool p { " + t("x") + "} pool p { " + t("other") + "}"), TASK_POOL, null, "Repeated pool ID");
    }

    @Disabled("TODO Ex 1.8: remove this line once done")
    @Test
    void dependentTaskCannotBePeriodic() throws Exception {
        var model = parseHelper.parse("pool { " + t("a") + "schedule task b { command \"c\" after a repeat every 1 h } }");
        helper.assertError(model, TASK, null, "cannot be periodic");
    }

    @Disabled("TODO Ex 1.9: remove this line once done")
    @Test
    void periodTooShort() throws Exception {
        helper.assertError(task("in 1 s repeat every 500 ns"), TASK, null, "at least 1 millisecond");
    }

    @Disabled("TODO Ex 5.17: remove this line once done")
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
