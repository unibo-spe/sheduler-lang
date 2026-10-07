# Sheduler DSL

An [Xtext](https://eclipse.dev/Xtext/) DSL for scheduling shell commands, used as the running example of the
_Model-Driven Development_ lecture of the Software Process Engineering course.

> **This is the `master` branch, which contains the solutions.**
> If you want to do the exercises yourself, check out the [`exercises`](https://github.com/unibo-spe/sheduler-lang/tree/exercises) branch.

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

Tests (`it.unibo.spe.mdd.sheduler/src/test/java`) cover parsing, validation, scoping, generation (the generated code is
compiled) and the runtime.
