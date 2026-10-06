package pt.up.fe.els2026;

import pt.up.fe.els2026.animation.Animation;
import pt.up.fe.els2026.parser.AnimationParseException;
import pt.up.fe.els2026.parser.AnimationParser;
import pt.up.fe.els2026.parser.AnimationProgram;
import pt.up.fe.els2026.parser.Diagnostics;
import pt.up.fe.els2026.runtime.AnimationPrinter;
import pt.up.fe.els2026.runtime.AnimationRunner;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point: parses a JSON animation file and plays its timeline on the skeleton.
 *
 * <pre>
 * els2026 &lt;file.json&gt; [--loop] [--verbose] [--dry-run]
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        String file = null;
        boolean loop = false;
        boolean verbose = false;
        boolean dryRun = false;

        for (String arg : args) {
            switch (arg) {
                case "--loop", "-l" -> loop = true;
                case "--verbose", "-v" -> verbose = true;
                case "--dry-run", "-n" -> dryRun = true;
                case "--help", "-h" -> {
                    printUsage();
                    return;
                }
                default -> {
                    if (arg.startsWith("-") || file != null) {
                        System.err.println("Unexpected argument: " + arg);
                        printUsage();
                        System.exit(2);
                    }
                    file = arg;
                }
            }
        }
        if (file == null) {
            printUsage();
            System.exit(2);
        }

        Diagnostics diagnostics = new Diagnostics(verbose);
        AnimationProgram program;
        try {
            program = new AnimationParser(diagnostics).parse(Path.of(file));
        } catch (AnimationParseException e) {
            diagnostics.printTo(System.err);
            System.err.println("Could not load '" + file + "': " + diagnostics.errors().size() + " error(s).");
            System.exit(1);
            return;
        } catch (IOException e) {
            System.err.println("Could not read '" + file + "': " + e.getMessage());
            System.exit(1);
            return;
        }
        diagnostics.printTo(System.out);

        Animation timeline = program.timeline();
        if (dryRun || GraphicsEnvironment.isHeadless()) {
            if (!dryRun) {
                System.out.println("No display available, printing the animation instead of playing it.");
            }
            for (Animation animation : program.animations().values()) {
                System.out.print(AnimationPrinter.print(animation));
            }
            System.out.print(AnimationPrinter.print(timeline));
            return;
        }

        AnimationRunner.withDefaultStickman().play(timeline, loop);
    }

    private static void printUsage() {
        System.out.println("""
                Usage: els2026 <file.json> [options]

                Options:
                  -l, --loop      play the timeline in a loop
                  -v, --verbose   print what the parser found (declared animations, comments, durations)
                  -n, --dry-run   validate and print the animation without opening a window
                  -h, --help      show this help""");
    }
}
