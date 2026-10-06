package pt.up.fe.els2026.runtime;

import pt.up.fe.els2026.animation.Animation;
import pt.up.fe.els2026.animation.AnimationReference;
import pt.up.fe.els2026.animation.AnimationStep;
import pt.up.fe.els2026.animation.Movement;
import pt.up.fe.els2026.animation.RotationMovement;
import pt.up.fe.els2026.animation.TranslationMovement;
import pt.up.fe.els2026.animation.WaitMovement;

/**
 * Human-readable dump of an animation, useful to check what will be played without opening a window.
 */
public final class AnimationPrinter {

    private AnimationPrinter() {
    }

    public static String print(Animation animation) {
        StringBuilder out = new StringBuilder();
        out.append(animation.name()).append(" (").append(animation.durationMs()).append(" ms)\n");
        int index = 1;
        for (AnimationStep step : animation.steps()) {
            out.append("  step ").append(index++).append(" (").append(step.durationMs()).append(" ms)\n");
            for (Movement movement : step.movements()) {
                out.append("    ").append(describe(movement)).append('\n');
            }
        }
        return out.toString();
    }

    public static String describe(Movement movement) {
        return switch (movement) {
            case RotationMovement r -> "rotate " + r.bodyPart().jointName() + (r.relative() ? " by " : " to ")
                    + format(r.angle()) + " deg (" + r.durationMs() + " ms)";
            case TranslationMovement t -> "move " + (t.relative() ? "by" : "to") + " (" + format(t.x()) + ", "
                    + format(t.y()) + ") (" + t.durationMs() + " ms)";
            case WaitMovement w -> "wait (" + w.durationMs() + " ms)";
            case AnimationReference ref -> "-> '" + ref.target().name() + "'"
                    + (ref.repetitions() > 1 ? " x" + ref.repetitions() : "") + " (" + ref.durationMs() + " ms)";
        };
    }

    private static String format(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
