package it.unibo.spe.mdd.sheduler.runtime;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShedulerRuntime {
    private final ScheduledExecutorService delegate;

    public ShedulerRuntime(ScheduledExecutorService delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public void schedule(ShedulerTask task) {
        if (task.isDependent()) {
            throw new IllegalArgumentException("Task " + task.getName() + " is dependent: it is triggered by its anchor task");
        }
        if (task.isPeriodic()) {
            delegate.scheduleAtFixedRate(task.asRunnable(), task.getDelay().toMillis(), task.getPeriod().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            delegate.schedule(task.asRunnable(), task.getDelay().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
