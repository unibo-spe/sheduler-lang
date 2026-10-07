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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Alternative to code generation: rather than producing Java code which builds {@link ShedulerTask}s,
 * the interpreter builds them directly from the parsed model, and schedules them right away.
 * <p>
 * Usage: {@code ./gradlew runInterpreter --args=/absolute/path/to/file.shed}
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
            Map<Task, ShedulerTask> tasks = new LinkedHashMap<>(); // EObjects use identity equality
            // first pass: create all tasks, so that the second pass can find every anchor in the map
            for (Task task : pool.getTasks()) {
                tasks.put(task, toShedulerTask(task));
            }
            // second pass: attach each dependent task to its anchor (same semantics as in the generator)
            for (Map.Entry<Task, ShedulerTask> entry : tasks.entrySet()) {
                Task task = entry.getKey();
                if (task.getAfter() != null) {
                    tasks.get(task.getAfter()).addSuccessor(entry.getValue());
                } else if (task.getBefore() != null) {
                    tasks.get(task.getBefore()).addPredecessor(entry.getValue());
                }
            }
            // third pass: schedule timed tasks only, dependent ones will be run by their anchors
            for (ShedulerTask t : tasks.values()) {
                if (!t.isDependent()) {
                    runtime.schedule(t);
                }
            }
        }
        // executor threads are non-daemon: the JVM stays alive until killed (Ctrl+C)
    }

    // the same case analysis as ShedulerGenerator.generateTask, but producing objects instead of code
    static ShedulerTask toShedulerTask(Task task) {
        ShedulerTask result;
        if (task.getRelative() != null) {
            result = ShedulerTask.in(task.getName(), task.getCommand(), task.getEntrypoint(), TimeUtils.toDuration(task.getRelative()));
        } else if (task.getAbsolute() != null) {
            result = ShedulerTask.at(task.getName(), task.getCommand(), task.getEntrypoint(), TimeUtils.toLocalDateTime(task.getAbsolute()));
        } else {
            result = ShedulerTask.dependent(task.getName(), task.getCommand(), task.getEntrypoint());
        }
        if (task.getPeriod() != null) {
            result.setPeriod(TimeUtils.toDuration(task.getPeriod()));
        }
        return result;
    }
}
