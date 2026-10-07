package it.unibo.spe.mdd.sheduler.interpreter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import it.unibo.spe.mdd.sheduler.ShedulerStandaloneSetup;
import it.unibo.spe.mdd.sheduler.TimeUtils;
import it.unibo.spe.mdd.sheduler.runtime.ShedulerRuntime;
import it.unibo.spe.mdd.sheduler.runtime.ShedulerTask;
import it.unibo.spe.mdd.sheduler.sheduler.Task;
import it.unibo.spe.mdd.sheduler.sheduler.TaskPool;
import it.unibo.spe.mdd.sheduler.sheduler.TaskPoolSet;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Alternative to code generation: rather than producing Java code which builds {@link ShedulerTask}s,
 * the interpreter builds them directly from the parsed model, and schedules them right away.
 * <p>
 * Usage: {@code ./gradlew :it.unibo.spe.mdd.sheduler:runInterpreter --args=/absolute/path/to/file.shed}
 * <p>
 * Everything is already in place, except {@link #toShedulerTask(Task)} (Ex 4) and the support for dependencies (Ex 5).
 */
public class ShedulerInterpreter {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Aborting: no path to .shed file provided!");
            return;
        }
        // Xtext components are wired via Guice: the standalone setup registers the language and creates the injector,
        // which is then used to instantiate this class (filling in its @Inject fields)
        Injector injector = new ShedulerStandaloneSetup().createInjectorAndDoEMFRegistration();
        ShedulerInterpreter interpreter = injector.getInstance(ShedulerInterpreter.class);
        interpreter.runFile(args[0]);
    }

    @Inject private Provider<ResourceSet> resourceSetProvider;
    @Inject private IResourceValidator validator;

    protected void runFile(String string) {
        // 1. parse: load the file as an EMF resource, whose root is the TaskPoolSet
        ResourceSet set = resourceSetProvider.get();
        Resource resource = set.getResource(URI.createFileURI(string), true);

        // 2. validate: run syntax/linking checks plus our ShedulerValidator rules; warnings are printed but do not stop us
        List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        issues.forEach(System.err::println);
        if (issues.stream().anyMatch(i -> i.getSeverity() == Severity.ERROR)) {
            return;
        }

        // 3. execute: turn each Task (model) into a ShedulerTask (runtime), then schedule it
        TaskPoolSet taskPools = (TaskPoolSet) resource.getContents().get(0);
        ShedulerRuntime runtime = new ShedulerRuntime(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
        for (TaskPool pool : taskPools.getPools()) {
            // TODO Ex 5.13: dependent tasks need their anchor's ShedulerTask: first convert all tasks of the pool
            //               (hint: a LinkedHashMap<Task, ShedulerTask> keeps the order, and EObjects use identity equality)
            // TODO Ex 5.14: then attach each dependent task to its anchor (`x after y` -> y.addSuccessor(x),
            //               `x before y` -> y.addPredecessor(x))
            // TODO Ex 5.15: finally, schedule non-dependent tasks only
            for (Task task : pool.getTasks()) {
                runtime.schedule(toShedulerTask(task));
            }
        }
        // executor threads are non-daemon: the JVM stays alive until killed (Ctrl+C)
    }

    // TODO Ex 4.1: `in <relative time>` -> ShedulerTask.in(...), hint: TimeUtils.toDuration
    // TODO Ex 4.2: `at <absolute time>` -> ShedulerTask.at(...), hint: TimeUtils.toLocalDateTime
    // TODO Ex 4.3: `repeat every <relative time>` -> setPeriod(...)
    // TODO Ex 4.4: name and entry point may be null: ShedulerTask already handles that
    // TODO Ex 5.16: `before`/`after` -> ShedulerTask.dependent(...)
    static ShedulerTask toShedulerTask(Task task) {
        throw new UnsupportedOperationException("TODO Ex 4.1: convert a Task (model) into a ShedulerTask (runtime)");
    }
}
