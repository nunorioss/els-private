package pt.up.fe.els2026.animation;

/**
 * A single unit of motion inside an {@link AnimationStep}.
 * All movements inside the same step run in parallel; the step lasts as long as its longest movement.
 *
 * The interface is sealed so the runtime can exhaustively pattern-match over every kind of movement.
 */
public sealed interface Movement
        permits RotationMovement, TranslationMovement, WaitMovement, AnimationReference {

    /** Duration of this movement in milliseconds. */
    int durationMs();
}
