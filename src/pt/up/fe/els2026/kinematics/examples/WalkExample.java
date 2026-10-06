package pt.up.fe.els2026.kinematics.examples;

import com.stickman.anim.Command;
import com.stickman.anim.Stickman;

import java.util.ArrayList;
import java.util.List;

/**
 * Example 2: Walking Gait Cycle Animation
 *
 * Demonstrates:
 * 1. Instantiating a Stickman at (x=300, y=280).
 * 2. Opening the built-in simple window.
 * 3. Defining a 4-phase walking gait cycle using parallel arm/leg commands and subtle vertical bobbing.
 * 4. Playing the walk cycle in a continuous loop.
 */
public class WalkExample {

    /**
     * Exposes a single walking cycle in place.
     */
    public static List<Command> createAnimation() {
        return createAnimation(1, 0.0);
    }

    /**
     * Exposes multiple walking cycles with optional forward displacement.
     *
     * @param cycles Number of full walk cycles to generate.
     * @param forwardPerPhase Horizontal distance moved forward per phase (0.0 for in-place).
     */
    public static List<Command> createAnimation(int cycles, double forwardPerPhase) {
        List<Command> walk = new ArrayList<>();
        int stepDuration = 220;

        for (int i = 0; i < cycles; i++) {
            // Phase 1: Left leg contact forward, Right leg pushes back; counter arm swing
            walk.add(Command.parallel(
                    Command.rotate("left_hip", -28, stepDuration),
                    Command.rotate("left_knee", 5, stepDuration),
                    Command.rotate("right_hip", 24, stepDuration),
                    Command.rotate("right_knee", 35, stepDuration),
                    Command.rotate("right_shoulder", -30, stepDuration),
                    Command.rotate("left_shoulder", 25, stepDuration),
                    Command.moveBy(forwardPerPhase, 3, stepDuration)
            ));

            // Phase 2: Passing phase (Right leg swings through, knee bent to clear ground)
            walk.add(Command.parallel(
                    Command.rotate("left_hip", -2, stepDuration),
                    Command.rotate("left_knee", 0, stepDuration),
                    Command.rotate("right_hip", -8, stepDuration),
                    Command.rotate("right_knee", 60, stepDuration),
                    Command.rotate("right_shoulder", 0, stepDuration),
                    Command.rotate("left_shoulder", 0, stepDuration),
                    Command.moveBy(forwardPerPhase, -3, stepDuration)
            ));

            // Phase 3: Right leg contact forward, Left leg pushes back; counter arm swing
            walk.add(Command.parallel(
                    Command.rotate("right_hip", -28, stepDuration),
                    Command.rotate("right_knee", 5, stepDuration),
                    Command.rotate("left_hip", 24, stepDuration),
                    Command.rotate("left_knee", 35, stepDuration),
                    Command.rotate("left_shoulder", -30, stepDuration),
                    Command.rotate("right_shoulder", 25, stepDuration),
                    Command.moveBy(forwardPerPhase, 3, stepDuration)
            ));

            // Phase 4: Passing phase (Left leg swings through)
            walk.add(Command.parallel(
                    Command.rotate("right_hip", -2, stepDuration),
                    Command.rotate("right_knee", 0, stepDuration),
                    Command.rotate("left_hip", -8, stepDuration),
                    Command.rotate("left_knee", 60, stepDuration),
                    Command.rotate("left_shoulder", 0, stepDuration),
                    Command.rotate("right_shoulder", 0, stepDuration),
                    Command.moveBy(forwardPerPhase, -3, stepDuration)
            ));
        }

        return walk;
    }

    public static void main(String[] args) {
        Stickman stickman = new Stickman(300, 280, 1.3);
        stickman.showWindow(600, 600, "Stickman - Walk Cycle Example");
        stickman.play(createAnimation(), true);
    }
}
