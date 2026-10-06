package com.stickman.anim;

/**
 * Command that waits/pauses for a specified duration in milliseconds without changing stickman pose.
 */
public class WaitCommand implements Command {
    private final int duration;

    public WaitCommand(int durationMs) {
        this.duration = Math.max(0, durationMs);
    }

    @Override
    public void update(Stickman stickman, double progress) {
        // No-op: simply holds the current pose
    }

    @Override
    public int getDuration() {
        return duration;
    }
}
