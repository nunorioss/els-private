package pt.up.fe.els2026.parser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects everything the parser has to say about a file: errors (the file cannot be played),
 * warnings (suspicious but playable) and, in verbose mode, informational notes.
 */
public final class Diagnostics {

    public enum Level { INFO, WARNING, ERROR }

    public record Message(Level level, String location, String text) {
        @Override
        public String toString() {
            String where = (location == null || location.isBlank()) ? "" : " [" + location + "]";
            return level + where + ": " + text;
        }
    }

    private final boolean verbose;
    private final List<Message> messages = new ArrayList<>();

    public Diagnostics() {
        this(false);
    }

    public Diagnostics(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    /** Recorded only when verbose. */
    public void info(String location, String text) {
        if (verbose) messages.add(new Message(Level.INFO, location, text));
    }

    public void warning(String location, String text) {
        messages.add(new Message(Level.WARNING, location, text));
    }

    public void error(String location, String text) {
        messages.add(new Message(Level.ERROR, location, text));
    }

    public List<Message> messages() {
        return Collections.unmodifiableList(messages);
    }

    public List<Message> errors() {
        return messages.stream().filter(m -> m.level() == Level.ERROR).toList();
    }

    public boolean hasErrors() {
        return messages.stream().anyMatch(m -> m.level() == Level.ERROR);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void printTo(PrintStream out) {
        for (Message message : messages) {
            out.println(message);
        }
    }
}
