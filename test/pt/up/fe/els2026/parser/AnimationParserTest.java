package pt.up.fe.els2026.parser;

import org.junit.Test;
import pt.up.fe.els2026.animation.Animation;
import pt.up.fe.els2026.animation.AnimationReference;
import pt.up.fe.els2026.animation.BodyPart;
import pt.up.fe.els2026.animation.RotationMovement;
import pt.up.fe.els2026.animation.TranslationMovement;
import pt.up.fe.els2026.animation.WaitMovement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AnimationParserTest {

    private static final String BASICS = """
            {
              "animations": {
                "raise knee": [[["rotate", "right_knee", 60, 300]]],
                "stretch":    [[["rotate", "right_knee", 0, 250], ["wait", 400]]],
                "dance":      [["raise knee"], ["stretch"]]
              },
              "timeline": [
                "a comment",
                [["dance"], ["raise knee"]],
                ["move", 10, 0, 100],
                ["stretch", 2]
              ]
            }
            """;

    @Test
    public void parsesMovementsReferencesAndDurations() {
        AnimationProgram program = new AnimationParser().parse(BASICS);

        Animation raiseKnee = program.animation("raise knee");
        RotationMovement rotation = (RotationMovement) raiseKnee.steps().get(0).movements().get(0);
        assertEquals(BodyPart.RIGHT_KNEE, rotation.bodyPart());
        assertEquals(60, rotation.angle(), 0);
        assertFalse(rotation.relative());
        assertEquals(300, raiseKnee.durationMs());

        // parallel step lasts as long as its longest movement
        assertEquals(400, program.animation("stretch").durationMs());
        assertTrue(program.animation("stretch").steps().get(0).movements().get(1) instanceof WaitMovement);

        // sequence of references adds up
        assertEquals(700, program.animation("dance").durationMs());

        Animation timeline = program.timeline();
        assertEquals(3, timeline.steps().size());
        // dance (700) || raise knee (300) -> 700
        assertEquals(700, timeline.steps().get(0).durationMs());
        assertTrue(timeline.steps().get(1).movements().get(0) instanceof TranslationMovement);
        AnimationReference repeated = (AnimationReference) timeline.steps().get(2).movements().get(0);
        assertEquals(2, repeated.repetitions());
        assertEquals(800, repeated.durationMs());
        assertEquals(700 + 100 + 800, timeline.durationMs());
    }

    @Test
    public void allowsForwardReferences() {
        AnimationProgram program = new AnimationParser().parse("""
                { "animations": { "a": [["b"]], "b": [[["wait", 10]]] }, "timeline": [["a"]] }
                """);
        assertEquals(10, program.animation("a").durationMs());
    }

    @Test
    public void reportsDuplicateAnimation() {
        assertError("""
                { "animations": { "walk": [[["wait", 1]]], "walk": [[["wait", 2]]] }, "timeline": [] }
                """, "animation 'walk' has already been defined");
    }

    @Test
    public void reportsUnknownAnimationWithSuggestion() {
        assertError("""
                { "animations": { "raise knee": [[["wait", 1]]] }, "timeline": [["raise kne"]] }
                """, "unknown animation 'raise kne'. Did you mean 'raise knee'?");
    }

    @Test
    public void reportsUnknownBodyPart() {
        assertError("""
                { "timeline": [[["rotate", "leg", 10, 100]]] }
                """, "unknown body part 'leg'");
    }

    @Test
    public void reportsCycles() {
        assertError("""
                { "animations": { "a": [["b"]], "b": [["a"]] }, "timeline": [["a"]] }
                """, "cyclic reference: a -> b -> a");
    }

    @Test
    public void reportsReservedNamesAndArity() {
        Diagnostics diagnostics = assertError("""
                { "animations": { "move": [[["wait", 1]]] }, "timeline": [["rotate", "torso", 10]] }
                """, "'move' is a reserved keyword");
        assertTrue(diagnostics.errors().stream()
                .anyMatch(m -> m.text().contains("'rotate' expects 3 argument(s) but got 2")));
    }

    @Test
    public void reportsAllErrorsAtOnceAndMissingTimeline() {
        Diagnostics diagnostics = assertError("""
                { "animations": { "a": [[["rotate", "nope", 1, 1]], [["move", 1, 1, -5]]] } }
                """, "missing required field \"timeline\"");
        assertEquals(3, diagnostics.errors().size());
    }

    @Test
    public void verboseModeRecordsInfoMessages() {
        Diagnostics diagnostics = new Diagnostics(true);
        new AnimationParser(diagnostics).parse(BASICS);
        List<String> texts = diagnostics.messages().stream().map(Diagnostics.Message::text).toList();
        assertTrue(texts.contains("comment ignored: \"a comment\""));
        assertTrue(texts.contains("defined animation 'dance': 2 step(s), 700 ms"));
        assertFalse(diagnostics.hasErrors());
    }

    @Test
    public void parsesBundledSamples() throws IOException {
        for (String sample : List.of("assets/test.json", "assets/demo.json")) {
            Diagnostics diagnostics = new Diagnostics(true);
            AnimationProgram program = new AnimationParser(diagnostics).parse(Path.of(sample));
            assertFalse(sample + " should have no errors", diagnostics.hasErrors());
            assertTrue(sample + " should have a non-empty timeline", program.timeline().durationMs() > 0);
        }
    }

    private static Diagnostics assertError(String json, String expectedFragment) {
        Diagnostics diagnostics = new Diagnostics();
        try {
            new AnimationParser(diagnostics).parse(json);
            fail("expected a parse error containing: " + expectedFragment);
        } catch (AnimationParseException e) {
            boolean found = diagnostics.errors().stream().anyMatch(m -> m.text().contains(expectedFragment));
            assertTrue("expected an error containing '" + expectedFragment + "' but got:\n" + e.getMessage(), found);
        }
        return diagnostics;
    }
}
