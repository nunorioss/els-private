package pt.up.fe.els2026.kinematics.examples;

import com.stickman.anim.Command;
import com.stickman.anim.Stickman;

import java.util.Arrays;
import java.util.List;

/**
 * Example 1: Waving Hand Animation
 *
 * Demonstrates:
 * 1. Instantiating a Stickman at (x=300, y=300).
 * 2. Opening the built-in simple window.
 * 3. Defining a friendly arm wave using sequential and parallel joint rotations.
 * 4. Playing the animation in a continuous loop.
 */
public class WaveExample {

    /**
     * Exposes the wave animation command list for reuse in other animations.
     */
    public static List<Command> createAnimation() {
        return Arrays.asList(
                // Step 1: Raise right arm and tilt head
                Command.parallel(
                        Command.rotate("right_shoulder", -145, 350),
                        Command.rotate("right_elbow", -60, 350),
                        Command.rotate("neck", -6, 350)
                ),
                // Steps 2-5: Wave forearm back and forth
                Command.rotate("right_elbow", -20, 180),
                Command.rotate("right_elbow", -75, 180),
                Command.rotate("right_elbow", -20, 180),
                Command.rotate("right_elbow", -75, 180),
                // Step 6: Lower arm back to rest pose
                Command.parallel(
                        Command.rotate("right_shoulder", 0, 350),
                        Command.rotate("right_elbow", 0, 350),
                        Command.rotate("neck", 0, 350)
                ),
                // Step 7: Pause briefly before repeating
                Command.pause(400)
        );
    }

    public static void main(String[] args) {
        Stickman stickman = new Stickman(300, 300, 1.3);
        stickman.showWindow(600, 600, "Stickman - Wave Example");
        stickman.play(createAnimation(), true);
    }
}
