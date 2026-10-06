package pt.up.fe.els2026.animation;

import java.util.List;

/**
 * A group of movements that run in parallel. The step finishes when its longest movement finishes.
 */
public record AnimationStep(List<Movement> movements) {

    public AnimationStep {
        movements = List.copyOf(movements);
        if (movements.isEmpty()) throw new IllegalArgumentException("a step needs at least one movement");
    }

    public AnimationStep(Movement... movements) {
        this(List.of(movements));
    }

    public int durationMs() {
        int max = 0;
        for (Movement movement : movements) {
            max = Math.max(max, movement.durationMs());
        }
        return max;
    }
}
