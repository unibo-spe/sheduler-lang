package it.unibo.spe.mdd.sheduler.tests;

import it.unibo.spe.mdd.sheduler.runtime.ShedulerRuntime;
import it.unibo.spe.mdd.sheduler.runtime.ShedulerTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class ShedulerRuntimeTest {
    private static final String SH = "/bin/sh -c";

    @TempDir
    Path tempDir;

    @Test
    void scheduleRejectsDependentTasks() {
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        try {
            assertThrows(IllegalArgumentException.class, () -> new ShedulerRuntime(ex).schedule(ShedulerTask.dependent("d", "true", null)));
        } finally {
            ex.shutdownNow();
        }
    }

    @Test
    void dependenciesRunInOrder() throws Exception {
        assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));
        Path log = tempDir.resolve("log.txt");
        ShedulerTask a = ShedulerTask.in("a", cmd("A", log), SH, Duration.ZERO);
        ShedulerTask b = ShedulerTask.dependent("b", cmd("B", log), SH);
        ShedulerTask c = ShedulerTask.dependent("c", cmd("C", log), SH);
        ShedulerTask d = ShedulerTask.dependent("d", cmd("D", log), SH);
        a.addPredecessor(b);
        a.addSuccessor(c);
        c.addSuccessor(d);
        a.asRunnable().run();
        assertEquals(List.of("B", "A", "C", "D"), Files.readAllLines(log));
    }

    private static String cmd(String x, Path log) {
        return "echo " + x + " >> '" + log + "'";
    }
}
