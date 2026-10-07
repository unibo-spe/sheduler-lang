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

public class ShedulerInterpreter {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Aborting: no path to .shed file provided!");
            return;
        }
        Injector injector = new ShedulerStandaloneSetup().createInjectorAndDoEMFRegistration();
        ShedulerInterpreter interpreter = injector.getInstance(ShedulerInterpreter.class);
        interpreter.runFile(args[0]);
    }

    @Inject private Provider<ResourceSet> resourceSetProvider;
    @Inject private IResourceValidator validator;

    protected void runFile(String string) {
        ResourceSet set = resourceSetProvider.get();
        Resource resource = set.getResource(URI.createFileURI(string), true);

        List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        issues.forEach(System.err::println);
        if (issues.stream().anyMatch(i -> i.getSeverity() == Severity.ERROR)) {
            return;
        }

        TaskPoolSet taskPools = (TaskPoolSet) resource.getContents().get(0);
        ShedulerRuntime runtime = new ShedulerRuntime(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
        for (TaskPool pool : taskPools.getPools()) {
            for (Task task : pool.getTasks()) {
                runtime.schedule(toShedulerTask(task));
            }
        }
        // executor threads are non-daemon: the JVM stays alive until killed (Ctrl+C)
    }

    static ShedulerTask toShedulerTask(Task task) {
        ShedulerTask result;
        if (task.getRelative() != null) {
            result = ShedulerTask.in(task.getName(), task.getCommand(), task.getEntrypoint(), TimeUtils.toDuration(task.getRelative()));
        } else if (task.getAbsolute() != null) {
            result = ShedulerTask.at(task.getName(), task.getCommand(), task.getEntrypoint(), TimeUtils.toLocalDateTime(task.getAbsolute()));
        } else {
            throw new UnsupportedOperationException("before/after tasks are not supported yet"); // replaced in Ex 5
        }
        if (task.getPeriod() != null) {
            result.setPeriod(TimeUtils.toDuration(task.getPeriod()));
        }
        return result;
    }
}
