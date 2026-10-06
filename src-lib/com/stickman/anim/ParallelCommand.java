package com.stickman.anim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Composite command that executes multiple commands in parallel over the duration
 * of the longest child command.
 */
public class ParallelCommand implements Command {
    private final List<Command> commands = new ArrayList<>();
    private final int maxDuration;

    public ParallelCommand(Command... commands) {
        this(Arrays.asList(commands));
    }

    public ParallelCommand(List<Command> commands) {
        if (commands != null) {
            for (Command cmd : commands) {
                if (cmd != null) {
                    this.commands.add(cmd);
                }
            }
        }
        int max = 0;
        for (Command cmd : this.commands) {
            if (cmd.getDuration() > max) {
                max = cmd.getDuration();
            }
        }
        this.maxDuration = max;
    }

    @Override
    public void start(Stickman stickman) {
        for (Command cmd : commands) {
            cmd.start(stickman);
        }
    }

    @Override
    public void update(Stickman stickman, double progress) {
        double currentMs = progress * maxDuration;
        for (Command cmd : commands) {
            int dur = cmd.getDuration();
            double subProgress = dur <= 0 ? 1.0 : Math.min(1.0, currentMs / dur);
            cmd.update(stickman, subProgress);
        }
    }

    @Override
    public void finish(Stickman stickman) {
        for (Command cmd : commands) {
            cmd.finish(stickman);
        }
    }

    @Override
    public int getDuration() {
        return maxDuration;
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
