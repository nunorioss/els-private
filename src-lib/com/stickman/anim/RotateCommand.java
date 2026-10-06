package com.stickman.anim;

/**
 * Command to smoothly rotate a stickman joint to a target angle (or by a relative delta)
 * over a specified duration in milliseconds.
 */
public class RotateCommand implements Command {
    private final String jointName;
    private final double targetOrDelta;
    private final boolean isRelative;
    private final int duration;

    private double startAngle;
    private double targetAngle;

    public RotateCommand(String jointName, double targetAngle, int durationMs) {
        this(jointName, targetAngle, false, durationMs);
    }

    public RotateCommand(String jointName, double targetOrDelta, boolean isRelative, int durationMs) {
        this.jointName = jointName;
        this.targetOrDelta = targetOrDelta;
        this.isRelative = isRelative;
        this.duration = Math.max(0, durationMs);
    }

    @Override
    public void start(Stickman stickman) {
        Stickman.Joint joint = stickman.getJoint(jointName);
        this.startAngle = joint != null ? joint.getAngle() : 0.0;
        if (isRelative) {
            this.targetAngle = this.startAngle + targetOrDelta;
        } else {
            this.targetAngle = targetOrDelta;
        }
    }

    @Override
    public void update(Stickman stickman, double progress) {
        double currentAngle = (duration <= 0) ? targetAngle : startAngle + (targetAngle - startAngle) * progress;
        stickman.rotate(jointName, currentAngle);
    }

    @Override
    public void finish(Stickman stickman) {
        stickman.rotate(jointName, targetAngle);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public String getJointName() {
        return jointName;
    }

    public double getTargetAngle() {
        return targetAngle;
    }

    public boolean isRelative() {
        return isRelative;
    }
}
