package it.unibo.spe.mdd.sheduler.runtime;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShedulerTask {
    public static final String DEFAULT_ENTRYPOINT = "/bin/sh -c";

    private static int instanceCount = 0;

    private final String name;
    private final String command;
    private final String entrypoint;
    private final Duration delay;
    private Duration period;

    private ShedulerTask(String name, String command, String entrypoint, Duration delay) {
        this.name = name == null ? "task" + instanceCount++ : name;
        this.command = Objects.requireNonNull(command);
        this.entrypoint = entrypoint == null ? DEFAULT_ENTRYPOINT : entrypoint;
        this.delay = delay;
    }

    public static ShedulerTask in(String name, String command, String entrypoint, Duration delay) {
        return new ShedulerTask(name, command, entrypoint, delay == null ? Duration.ZERO : delay);
    }

    public static ShedulerTask in(String command, String entrypoint, Duration delay) {
        return in(null, command, entrypoint, delay);
    }

    public static ShedulerTask at(String name, String command, String entrypoint, LocalDateTime dateTime) {
        return in(name, command, entrypoint, Duration.between(LocalDateTime.now(), dateTime));
    }

    public static ShedulerTask at(String command, String entrypoint, LocalDateTime dateTime) {
        return at(null, command, entrypoint, dateTime);
    }

    public String getName() { return name; }
    public String getCommand() { return command; }
    public String getEntrypoint() { return entrypoint; }
    public Duration getPeriod() { return period; }
    public boolean isPeriodic() { return period != null; }
    public ShedulerTask setPeriod(Duration period) { this.period = period; return this; }
    public Duration getDelay() { return delay; }

    public Process executeAsync() throws IOException {
        List<String> cmd = new ArrayList<>(List.of(entrypoint.trim().split("\\s+")));
        cmd.add(command); // e.g. ["/bin/sh", "-c", "echo hello"]
        return new ProcessBuilder(cmd).inheritIO().start();
    }

    public Runnable asRunnable() {
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
