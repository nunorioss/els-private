package pt.up.fe.els2026.parser;

import java.util.stream.Collectors;

/**
 * Thrown when a JSON animation file contains at least one error. All collected messages are available
 * through {@link #diagnostics()}; the exception message lists the errors.
 */
public class AnimationParseException extends RuntimeException {

    private final transient Diagnostics diagnostics;

    public AnimationParseException(Diagnostics diagnostics) {
        super(summarize(diagnostics));
        this.diagnostics = diagnostics;
    }

    public Diagnostics diagnostics() {
        return diagnostics;
    }

    private static String summarize(Diagnostics diagnostics) {
        var errors = diagnostics.errors();
        String list = errors.stream().map(Object::toString).collect(Collectors.joining("\n"));
        return errors.size() + " error(s) found:\n" + list;
    }
}
