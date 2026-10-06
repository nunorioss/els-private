package pt.up.fe.els2026.animation;

/**
 * Holds the current pose for a given amount of time.
 */
public record WaitMovement(int durationMs) implements Movement {

    public WaitMovement {
        if (durationMs < 0) throw new IllegalArgumentException("durationMs must be >= 0");
    }
}
