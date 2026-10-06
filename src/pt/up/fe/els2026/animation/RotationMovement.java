package pt.up.fe.els2026.animation;

/**
 * Rotates a body part to a target angle (absolute) or by a delta (relative), in degrees.
 */
public record RotationMovement(BodyPart bodyPart, double angle, boolean relative, int durationMs)
        implements Movement {

    public RotationMovement {
        if (bodyPart == null) throw new IllegalArgumentException("bodyPart must not be null");
        if (durationMs < 0) throw new IllegalArgumentException("durationMs must be >= 0");
    }

    public static RotationMovement to(BodyPart bodyPart, double targetAngle, int durationMs) {
        return new RotationMovement(bodyPart, targetAngle, false, durationMs);
    }

    public static RotationMovement by(BodyPart bodyPart, double deltaAngle, int durationMs) {
        return new RotationMovement(bodyPart, deltaAngle, true, durationMs);
    }
}
