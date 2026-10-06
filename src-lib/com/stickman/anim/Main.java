package com.stickman.anim;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point and demonstration of the simplified Stickman animation model.
 *
 * Demonstrates:
 * 1. Instantiating the stickman model.
 * 2. Opening the built-in simple window.
 * 3. Composing an animation from a basic list of commands.
 * 4. Playing the animation.
 */
public class Main {

    public static void main(String[] args) {
        boolean isCli = (args.length > 0 && "--cli".equalsIgnoreCase(args[0]))
                || GraphicsEnvironment.isHeadless()
                || Boolean.getBoolean("java.awt.headless");

        if (isCli) {
            runCliDemo();
            return;
        }

        System.out.println("Starting Stickman Animation...");

        // 1. Instantiate the stickman model
        Stickman stickman = new Stickman(300, 280, 1.3);

        // 2. Open the simple window (no frills)
        stickman.showWindow(600, 600, "Stickman Animation Demo");

        // 3. Define a list of basic commands to play an animation (e.g. friendly wave + jump)
        List<Command> animation = Arrays.asList(
                // Phase 1: Raise right arm up to wave
                Command.parallel(
                        Command.rotate("right_shoulder", -145, 400),
                        Command.rotate("right_elbow", -55, 400),
                        Command.rotate("neck", -8, 400)
                ),
                // Phase 2: Wave hand back and forth
                Command.rotate("right_elbow", -15, 200),
                Command.rotate("right_elbow", -75, 200),
                Command.rotate("right_elbow", -15, 200),
                Command.rotate("right_elbow", -75, 200),
                // Phase 3: Lower arm to neutral
                Command.parallel(
                        Command.rotate("right_shoulder", 0, 350),
                        Command.rotate("right_elbow", 0, 350),
                        Command.rotate("neck", 0, 350)
                ),
                Command.pause(200),
                // Phase 4: Crouch down
                Command.parallel(
                        Command.rotate("left_hip", 30, 250),
                        Command.rotate("left_knee", -50, 250),
                        Command.rotate("right_hip", 30, 250),
                        Command.rotate("right_knee", -50, 250),
                        Command.moveBy(0, 20, 250)
                ),
                // Phase 5: Jump up!
                Command.parallel(
                        Command.rotate("left_hip", -10, 200),
                        Command.rotate("left_knee", 0, 200),
                        Command.rotate("right_hip", -10, 200),
                        Command.rotate("right_knee", 0, 200),
                        Command.rotate("left_shoulder", -120, 200),
                        Command.rotate("right_shoulder", -120, 200),
                        Command.moveBy(0, -60, 200)
                ),
                // Phase 6: Land back on ground
                Command.parallel(
                        Command.rotate("left_hip", 0, 250),
                        Command.rotate("left_knee", 0, 250),
                        Command.rotate("right_hip", 0, 250),
                        Command.rotate("right_knee", 0, 250),
                        Command.rotate("left_shoulder", 0, 250),
                        Command.rotate("right_shoulder", 0, 250),
                        Command.moveBy(0, 40, 250)
                ),
                Command.pause(400)
        );

        // 4. Play the animation in a continuous loop
        stickman.play(animation, true);
    }

    private static void runCliDemo() {
        System.out.println("=== STICKMAN PROGRAMMATIC CLI DEMO ===");

        Stickman stickman = new Stickman(200, 300, 1.0);
        System.out.printf("1. Instantiated Stickman at (%.1f, %.1f)%n", stickman.getX(), stickman.getY());
        System.out.printf("   Joint count: %d%n", stickman.getJoints().size());

        // Direct rotation
        stickman.rotate("right_shoulder", -90);
        stickman.rotate("right_elbow", 45);
        Stickman.Joint arm = stickman.getJoint("right_shoulder");
        System.out.printf("2. Direct Joint Rotation:%n   right_shoulder angle: %.1f° (end: %.1f, %.1f)%n",
                arm.getAngle(), arm.getWorldEndX(), arm.getWorldEndY());

        // Command execution test
        System.out.println("3. Executing RotateCommand test (simulating progress):");
        Command cmd = new RotateCommand("right_shoulder", 0, 300);
        cmd.start(stickman);
        cmd.update(stickman, 0.5);
        System.out.printf("   At 50%% progress: right_shoulder angle = %.1f°%n", stickman.getJoint("right_shoulder").getAngle());
        cmd.finish(stickman);
        System.out.printf("   At finish: right_shoulder angle = %.1f°%n", stickman.getJoint("right_shoulder").getAngle());

        System.out.println("\n=== DEMO PASSED SUCCESSFULLY ===");
    }
}
