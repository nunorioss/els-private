package pt.up.fe.els2026.animation;

public class RotationMovement extends Movement  {
    String bodyPart;
    float targetAngle;
    public RotationMovement(float duration, String bodyPart, float targetAngle) {
        super(duration);
        this.bodyPart = bodyPart;
        this.targetAngle = targetAngle;
    }
}
