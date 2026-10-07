package it.unibo.spe.mdd.sheduler.runtime;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A shell command to be executed at some point in time, possibly periodically.
 * <p>
 * This class is the run-time counterpart of the {@code Task} model element: the generator (Ex 3) emits code that
 * builds {@code ShedulerTask}s, while the interpreter (Ex 4) builds them directly from the parsed model.
 * <p>
 * NOTE: a copy of this file is shipped to generated projects via {@code ShedulerTask.java.template}
 * (see {@code RuntimeTemplatesSyncTest}): if you edit this class (Ex 5), copy it over the template too.
 * <p>
 * Tests: {@code ShedulerRuntimeTest} (remove {@code @Disabled} once Ex 5 is done).
 */
public class ShedulerTask {
    /** Used when the DSL omits {@code entry point "..."}: the command is interpreted by the POSIX shell. */
    public static final String DEFAULT_ENTRYPOINT = "/bin/sh -c";

    private static int instanceCount = 0; // used to give a name to anonymous tasks

    private final String name;
    private final String command;
    private final String entrypoint;
    private final Duration delay;
    private Duration period;
    // TODO Ex 5.1: a task must know which tasks to run right before it (predecessors) and right after it (successors)

    // private constructor: tasks are created via the static factory methods below, whose names mirror the DSL keywords
    private ShedulerTask(String name, String command, String entrypoint, Duration delay) {
        this.name = name == null ? "task" + instanceCount++ : name;
        this.command = Objects.requireNonNull(command);
        this.entrypoint = entrypoint == null ? DEFAULT_ENTRYPOINT : entrypoint;
        this.delay = delay;
    }

    /** DSL: {@code schedule task name { ... in <relative time> }}. */
    public static ShedulerTask in(String name, String command, String entrypoint, Duration delay) {
        return new ShedulerTask(name, command, entrypoint, delay == null ? Duration.ZERO : delay);
    }

    public static ShedulerTask in(String command, String entrypoint, Duration delay) {
        return in(null, command, entrypoint, delay);
    }

    /**
     * DSL: {@code schedule task name { ... at <absolute time> }}.
     * The absolute time is turned into a delay w.r.t. the moment the task is <em>created</em>.
     */
    public static ShedulerTask at(String name, String command, String entrypoint, LocalDateTime dateTime) {
        return in(name, command, entrypoint, Duration.between(LocalDateTime.now(), dateTime));
    }

    public static ShedulerTask at(String command, String entrypoint, LocalDateTime dateTime) {
        return at(null, command, entrypoint, dateTime);
    }

    /** DSL: {@code schedule task name { ... before <task> }} or {@code ... after <task> }. */
    public static ShedulerTask dependent(String name, String command, String entrypoint) {
        // TODO Ex 5.2: a dependent task has no timing of its own (e.g. no delay, i.e. delay == null)
        throw new UnsupportedOperationException("TODO Ex 5.2: create a task with no delay");
    }

    public String getName() { return name; }
    public String getCommand() { return command; }
    public String getEntrypoint() { return entrypoint; }
    public Duration getPeriod() { return period; }
    public boolean isPeriodic() { return period != null; }
    public ShedulerTask setPeriod(Duration period) { this.period = period; return this; }
    public Duration getDelay() { return delay; }

    public boolean isDependent() {
        return false; // TODO Ex 5.3: true iff this task was created via dependent(...)
    }

    /** {@code task} will run right before {@code this} one (DSL: {@code task before this}). */
    public ShedulerTask addPredecessor(ShedulerTask task) {
        throw new UnsupportedOperationException("TODO Ex 5.4: remember task as a predecessor of this one");
    }

    /** {@code task} will run right after {@code this} one (DSL: {@code task after this}). */
    public ShedulerTask addSuccessor(ShedulerTask task) {
        throw new UnsupportedOperationException("TODO Ex 5.5: remember task as a successor of this one");
    }

    /**
     * Starts the command as an OS process, and returns immediately (without waiting for it to terminate).
     * The entry point is split on whitespace because {@link ProcessBuilder} wants one argument per list element:
     * passing {@code "/bin/sh -c"} as a single element would look for an executable literally named {@code "sh -c"}.
     * {@code inheritIO()} makes the process print on the same console as the JVM.
     */
    public Process executeAsync() throws IOException {
        List<String> cmd = new ArrayList<>(List.of(entrypoint.trim().split("\\s+")));
        cmd.add(command); // e.g. ["/bin/sh", "-c", "echo hello"]
        return new ProcessBuilder(cmd).inheritIO().start();
    }

    /** Adapts this task to what {@link java.util.concurrent.ScheduledExecutorService} expects: a {@link Runnable}. */
    public Runnable asRunnable() {
        // TODO Ex 5.6: run all predecessors first, then this task, then all successors, in this order
        // TODO Ex 5.7: "in this order" means each process must terminate before the next one starts (Process.waitFor())
        // TODO Ex 5.8: predecessors/successors may have predecessors/successors of their own (hint: recursion)
        return () -> {
            try {
                executeAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    @Override
    public String toString() {
        return "ShedulerTask{name='" + name + "', command='" + command + "', entrypoint='" + entrypoint
                + "', delay=" + delay + ", period=" + period + '}';
    }
}
