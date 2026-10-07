package it.unibo.spe.mdd.sheduler.runtime;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thin wrapper around a {@link ScheduledExecutorService}, which does the actual job of running tasks at the right time,
 * on a pool of background threads.
 * <p>
 * NOTE: a copy of this file is shipped to generated projects via {@code ShedulerRuntime.java.template}
 * (see {@code RuntimeTemplatesSyncTest}): if you edit this class, copy it over the template too.
 */
public class ShedulerRuntime {
    private final ScheduledExecutorService delegate;

    public ShedulerRuntime(ScheduledExecutorService delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public void schedule(ShedulerTask task) {
        // dependent tasks have no delay of their own: they are run by their anchor (see ShedulerTask.runChain)
        if (task.isDependent()) {
            throw new IllegalArgumentException("Task " + task.getName() + " is dependent: it is triggered by its anchor task");
        }
        // executors work with a numeric amount + a time unit, so durations are converted into milliseconds
        // (this is why the validator warns about durations that overflow toMillis(), and forbids periods < 1 ms)
        if (task.isPeriodic()) {
            delegate.scheduleAtFixedRate(task.asRunnable(), task.getDelay().toMillis(), task.getPeriod().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            delegate.schedule(task.asRunnable(), task.getDelay().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
