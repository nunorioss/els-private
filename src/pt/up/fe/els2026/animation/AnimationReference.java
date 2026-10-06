package pt.up.fe.els2026.animation;

/**
 * A movement that plays a whole (previously defined) animation, optionally several times in a row.
 *
 * This is what lets user-defined "verbs" (walk, dance, raise knee, ...) be composed: the referenced
 * animation keeps its own sequence of steps and simply runs alongside the other movements of the step
 * that contains it.
 */
public record AnimationReference(Animation target, int repetitions) implements Movement {

    public AnimationReference {
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (repetitions < 1) throw new IllegalArgumentException("repetitions must be >= 1");
    }

    public AnimationReference(Animation target) {
        this(target, 1);
    }

    @Override
    public int durationMs() {
        return target.durationMs() * repetitions;
    }

    @Override
    public String toString() {
        return "AnimationReference[" + target.name() + (repetitions > 1 ? " x" + repetitions : "") + "]";
    }
}
