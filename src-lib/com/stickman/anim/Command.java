package com.stickman.anim;

import java.util.Arrays;
import java.util.List;

/**
 * Functional interface for stickman animation commands.
 * Commands modify stickman joint rotations or position over a specified duration in milliseconds.
 */
@FunctionalInterface
public interface Command {

    /**
     * Applies progress between 0.0 (start) and 1.0 (end) to the stickman.
     */
    void update(Stickman stickman, double progress);

    /**
     * Called once before the command begins animating.
     * Can be used to capture initial state.
     */
    default void start(Stickman stickman) {}

    /**
     * Guarantees exact final state is applied upon command completion.
     */
    default void finish(Stickman stickman) {
        update(stickman, 1.0);
    }

    /**
     * Duration of this command in milliseconds (default 0 for instant execution).
     */
    default int getDuration() {
        return 0;
    }

    // =========================================================================
    // Static Factory Helpers
    // =========================================================================

    static RotateCommand rotate(String jointName, double targetAngle, int durationMs) {
        return new RotateCommand(jointName, targetAngle, durationMs);
    }

    static RotateCommand rotateBy(String jointName, double deltaAngle, int durationMs) {
        return new RotateCommand(jointName, deltaAngle, true, durationMs);
    }

    static MoveCommand move(double targetX, double targetY, int durationMs) {
        return new MoveCommand(targetX, targetY, durationMs);
    }

    static MoveCommand moveBy(double dx, double dy, int durationMs) {
        return new MoveCommand(dx, dy, true, durationMs);
    }

    static WaitCommand pause(int durationMs) {
        return new WaitCommand(durationMs);
    }

    static WaitCommand wait(int durationMs) {
        return new WaitCommand(durationMs);
    }

    static ParallelCommand parallel(Command... commands) {
        return new ParallelCommand(commands);
    }

    static ParallelCommand parallel(List<Command> commands) {
        return new ParallelCommand(commands);
    }
}
