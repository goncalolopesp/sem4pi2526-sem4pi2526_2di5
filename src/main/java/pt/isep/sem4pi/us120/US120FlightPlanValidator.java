package pt.isep.sem4pi.us120;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class US120FlightPlanValidator {

    private US120FlightPlanValidator() {
        // utility
    }

    public static US120ValidationResult analyze(final Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File doesn't exist: " + path);
        }

        final US120ErrorListener errorListener = new US120ErrorListener();

        final US120Lexer lexer = new US120Lexer(CharStreams.fromPath(path));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final US120Parser parser = new US120Parser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        final ParseTree tree = parser.flightPlan();

        if (errorListener.hasErrors()) {
            return new US120ValidationResult(errorListener.getErrors(), List.of(), null);
        }

        final US120SemanticVisitor visitor = new US120SemanticVisitor();
        visitor.visit(tree);

        return new US120ValidationResult(List.of(), visitor.getErrors(), tree);
    }

    public static void validate(final Path path) throws IOException {
        final US120ValidationResult result = analyze(path);

        if (!result.getLexicalSyntacticErrors().isEmpty()) {
            printErrors("Syntatic/Lexic errors: ", result.getLexicalSyntacticErrors());
            System.out.println("\nThe semantic validation wasn't executed since the file is structurally invalid.");
            return;
        }

        System.out.println("[Syntatic/Lexic] File structurally valid. No errors.");
        printErrors("Semantic errors: ", result.getSemanticErrors());
    }

    private static void printErrors(final String source, final List<String> errors) {
        if (errors.isEmpty()) {
            System.out.println("[" + source + "] Valid Rules. No errors.");
        } else {
            System.out.println("[" + source + "] Detected errors:");
            for (final String e : errors) {
                System.out.println("  - " + e);
            }
        }
    }
}
