package pt.isep.sem4pi.us120;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        final US120SemanticListener listener = new US120SemanticListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        final US120SemanticVisitor visitor = new US120SemanticVisitor();
        visitor.visit(tree);

        final List<String> semanticErrors = mergeSemanticErrors(listener.getErrors(), visitor.getErrors());
        return new US120ValidationResult(List.of(), semanticErrors, tree);
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

    private static List<String> mergeSemanticErrors(final List<String> listenerErrors,
                                                    final List<String> visitorErrors) {
        final Set<String> merged = new LinkedHashSet<>();
        merged.addAll(listenerErrors);
        merged.addAll(visitorErrors);
        return new ArrayList<>(merged);
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
