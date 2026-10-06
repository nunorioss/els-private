package pt.up.fe.els2026.animation;

/**
 * Moves the whole skeleton in the world, either by an offset (relative) or to an absolute position.
 */
public record TranslationMovement(double x, double y, boolean relative, int durationMs)
        implements Movement {

    public TranslationMovement {
        if (durationMs < 0) throw new IllegalArgumentException("durationMs must be >= 0");
    }

    public static TranslationMovement by(double dx, double dy, int durationMs) {
        return new TranslationMovement(dx, dy, true, durationMs);
    }

    public static TranslationMovement to(double x, double y, int durationMs) {
        return new TranslationMovement(x, y, false, durationMs);
    }
}
