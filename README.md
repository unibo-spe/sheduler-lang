# Sheduler DSL

An [Xtext](https://eclipse.dev/Xtext/) DSL for scheduling shell commands, used as the running example of the
_Model-Driven Development_ lecture of the Software Process Engineering course.

> **This is the `exercises` branch: the code contains placeholders for you to fill in.**
> Solutions are on the [`master`](https://github.com/unibo-spe/sheduler-lang/tree/master) branch (and in the slides).

Slides: <https://unibo-spe.github.io/11-mdd/>

```
pool myPool {
    schedule task greet { command "echo hello" entry point "/bin/sh -c" in 5 minutes repeat every 1 hours }
    schedule task bye   { command "echo bye" after greet }
}
```

## Exercises

| Ex  | Goal | Where (in `it.unibo.spe.mdd.sheduler/src/main/java/it/unibo/spe/mdd/sheduler/`) |
|-----|------|-------|
| 1   | Validation: representable times, valid clock times, positive durations, unique task/pool names, no periodic `before`/`after` tasks | `validation/ShedulerValidator.java` |
| 2   | Scoping: `before`/`after` may only reference named tasks of the same pool | `scoping/ShedulerScopeProvider.java` |
| 3   | Code generator producing a runnable Java program | `generator/ShedulerGenerator.java`, templates in `src/main/resources` |
| 4   | Interpreter scheduling tasks directly from the model | `interpreter/ShedulerInterpreter.java` |
| 5   | Task dependencies (`before`/`after`) in runtime, generator, interpreter, plus no cyclic dependencies | `runtime/`, plus the files above |

## Working on the exercises

1. Look for `TODO Ex N.M` comments (most IDEs list them in a _TODO_ view): they break each exercise into small steps.
   Unimplemented methods throw `UnsupportedOperationException("TODO Ex N.M: ...")`.
2. Each exercise has tests in `it.unibo.spe.mdd.sheduler/src/test/java`, marked `@Disabled("TODO Ex N.M ...")`:
   remove the annotation once you are done, and run `./gradlew build`.
3. Ex 5 changes `runtime/ShedulerTask.java` and `runtime/ShedulerRuntime.java`: copy them over their `.java.template`
   counterparts in `src/main/resources` too (`RuntimeTemplatesSyncTest` checks that).

## How to operate

Requires a JDK; Gradle provisions Java 21 automatically. Import the repository root as a Gradle project
in [Eclipse for DSL developers](https://eclipse.dev/Xtext/download.html) (full Xtext support) or IntelliJ.

| Command | What it does |
|---------|--------------|
| `./gradlew build` | Regenerates the Xtext infrastructure from `Sheduler.xtext`, compiles, runs all tests |
| `./gradlew jettyRun` | Starts the web playground on <http://localhost:8080> |
| `./gradlew :it.unibo.spe.mdd.sheduler:run --args=/abs/path/file.shed` | Generates Java code next to `file.shed` |
| `./gradlew :it.unibo.spe.mdd.sheduler:runInterpreter --args=/abs/path/file.shed` | Runs `file.shed` directly (stop with Ctrl+C) |
| `./gradlew shadowJar` | Packs the command-line compiler (`*-compiler.jar`) and the LSP server (`*-ls.jar`) |

