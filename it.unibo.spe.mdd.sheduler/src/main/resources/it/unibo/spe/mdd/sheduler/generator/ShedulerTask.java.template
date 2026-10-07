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
 * This class is the run-time counterpart of the {@code Task} model element: the generator emits code that builds
 * {@code ShedulerTask}s, while the interpreter builds them directly from the parsed model.
 * <p>
 * NOTE: a copy of this file is shipped to generated projects via {@code ShedulerTask.java.template}
 * (see {@code RuntimeTemplatesSyncTest}): if you edit this class, copy it over the template too.
 * <p>
 * A task is either:
 * <ul>
 *     <li><em>timed</em>: it has a {@link #getDelay() delay} (and maybe a {@link #getPeriod() period}),
 *     and is meant to be passed to {@link ShedulerRuntime#schedule(ShedulerTask)};</li>
 *     <li><em>dependent</em> (Ex 5): it has no delay, and runs right before/after some other task (its <em>anchor</em>),
 *     which it is attached to via {@link #addPredecessor(ShedulerTask)} / {@link #addSuccessor(ShedulerTask)}.</li>
 * </ul>
 */
public class ShedulerTask {
    /** Used when the DSL omits {@code entry point "..."}: the command is interpreted by the POSIX shell. */
    public static final String DEFAULT_ENTRYPOINT = "/bin/sh -c";

    private static int instanceCount = 0; // used to give a name to anonymous tasks

    private final String name;
    private final String command;
    private final String entrypoint;
    private final Duration delay; // null iff dependent
    private Duration period;
    private final List<ShedulerTask> predecessors = new ArrayList<>(); // tasks to run right BEFORE this one
    private final List<ShedulerTask> successors = new ArrayList<>();   // tasks to run right AFTER this one

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

    /** DSL: {@code schedule task name { ... before <task> }} or {@code ... after <task> } (Ex 5). */
    public static ShedulerTask dependent(String name, String command, String entrypoint) {
        return new ShedulerTask(name, command, entrypoint, null);
    }

    public String getName() { return name; }
    public String getCommand() { return command; }
    public String getEntrypoint() { return entrypoint; }
    public Duration getPeriod() { return period; }
    public boolean isPeriodic() { return period != null; }
    public ShedulerTask setPeriod(Duration period) { this.period = period; return this; }
    public Duration getDelay() { return delay; } // null for dependent tasks

    public boolean isDependent() { return delay == null; }

    /** {@code task} will run right before {@code this} one (DSL: {@code task before this}). */
    public ShedulerTask addPredecessor(ShedulerTask task) {
        predecessors.add(Objects.requireNonNull(task));
        return this;
    }

    /** {@code task} will run right after {@code this} one (DSL: {@code task after this}). */
    public ShedulerTask addSuccessor(ShedulerTask task) {
        successors.add(Objects.requireNonNull(task));
        return this;
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

    /**
     * Runs all predecessors, then this task, then all successors, each one waiting for the previous one to terminate.
     * The recursion takes care of dependents of dependents (e.g. {@code c after b}, {@code b after a}).
     */
    void runChain() throws IOException, InterruptedException {
        for (ShedulerTask p : predecessors) p.runChain();
        executeAsync().waitFor();
        for (ShedulerTask s : successors) s.runChain();
    }

    /** Adapts this task to what {@link java.util.concurrent.ScheduledExecutorService} expects: a {@link Runnable}. */
    public Runnable asRunnable() {
        return () -> {
            try {
                runChain();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // the executor is shutting down: restore the flag and stop
            }
        };
    }

    @Override
    public String toString() {
        return "ShedulerTask{name='" + name + "', command='" + command + "', entrypoint='" + entrypoint
                + "', delay=" + delay + ", period=" + period + '}';
    }
}
