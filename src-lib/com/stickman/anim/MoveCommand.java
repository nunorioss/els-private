package com.stickman.anim;

/**
 * Command to smoothly translate the stickman root position over a specified duration in milliseconds.
 */
public class MoveCommand implements Command {
    private final double targetXOrDx;
    private final double targetYOrDy;
    private final boolean isRelative;
    private final int duration;

    private double startX;
    private double startY;
    private double finalX;
    private double finalY;

    public MoveCommand(double targetX, double targetY, int durationMs) {
        this(targetX, targetY, false, durationMs);
    }

    public MoveCommand(double targetXOrDx, double targetYOrDy, boolean isRelative, int durationMs) {
        this.targetXOrDx = targetXOrDx;
        this.targetYOrDy = targetYOrDy;
        this.isRelative = isRelative;
        this.duration = Math.max(0, durationMs);
    }

    @Override
    public void start(Stickman stickman) {
        this.startX = stickman.getX();
        this.startY = stickman.getY();
        if (isRelative) {
            this.finalX = this.startX + targetXOrDx;
            this.finalY = this.startY + targetYOrDy;
        } else {
            this.finalX = targetXOrDx;
            this.finalY = targetYOrDy;
        }
    }

    @Override
    public void update(Stickman stickman, double progress) {
        double currentX = (duration <= 0) ? finalX : startX + (finalX - startX) * progress;
        double currentY = (duration <= 0) ? finalY : startY + (finalY - startY) * progress;
        stickman.moveTo(currentX, currentY);
    }

    @Override
    public void finish(Stickman stickman) {
        stickman.moveTo(finalX, finalY);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public double getFinalX() {
        return finalX;
    }

    public double getFinalY() {
        return finalY;
    }

    public boolean isRelative() {
        return isRelative;
    }
}
