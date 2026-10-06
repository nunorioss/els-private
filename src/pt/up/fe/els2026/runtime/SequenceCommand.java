package pt.up.fe.els2026.runtime;

import com.stickman.anim.Command;
import com.stickman.anim.Stickman;

import java.util.List;

/**
 * Runs child commands one after the other, as a single {@link Command} whose duration is the sum of the
 * children's durations.
 *
 * This is the counterpart of {@link com.stickman.anim.ParallelCommand} and is what lets a multi-step
 * animation (e.g. "dance", 2 steps) run in parallel with single-step ones (e.g. "raise knee") without
 * cutting any movement: the enclosing ParallelCommand simply waits for the longest child, and this
 * command advances through its own steps based on the elapsed time it is given.
 */
public final class SequenceCommand implements Command {

    private final List<Command> commands;
    private final int totalDuration;

    private int active;
    private int activeStartMs;

    public SequenceCommand(Command... commands) {
        this(List.of(commands));
    }

    public SequenceCommand(List<Command> commands) {
        this.commands = List.copyOf(commands);
        int total = 0;
        for (Command command : this.commands) {
            total += command.getDuration();
        }
        this.totalDuration = total;
    }

    @Override
    public void start(Stickman stickman) {
        active = 0;
        activeStartMs = 0;
        if (!commands.isEmpty()) {
            commands.get(0).start(stickman);
        }
    }

    @Override
    public void update(Stickman stickman, double progress) {
        if (commands.isEmpty()) return;

        double nowMs = progress * totalDuration;

        // Finish every child whose time window has already passed and start the next one, so each child
        // captures its start state (e.g. current joint angle) exactly when it begins.
        while (active < commands.size() - 1 && nowMs >= activeStartMs + commands.get(active).getDuration()) {
            commands.get(active).finish(stickman);
            activeStartMs += commands.get(active).getDuration();
            active++;
            commands.get(active).start(stickman);
        }

        Command current = commands.get(active);
        int duration = current.getDuration();
        double localProgress = duration <= 0 ? 1.0 : Math.min(1.0, (nowMs - activeStartMs) / duration);
        current.update(stickman, localProgress);
    }

    @Override
    public void finish(Stickman stickman) {
        if (commands.isEmpty()) return;

        commands.get(active).finish(stickman);
        // Guarantee the exact final state even if update() never reached the remaining children.
        for (int i = active + 1; i < commands.size(); i++) {
            commands.get(i).start(stickman);
            commands.get(i).finish(stickman);
        }
        active = commands.size() - 1;
    }

    @Override
    public int getDuration() {
        return totalDuration;
    }

    public List<Command> getCommands() {
        return commands;
    }
}
