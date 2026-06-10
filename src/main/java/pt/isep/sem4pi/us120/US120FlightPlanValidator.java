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

    public static void validate(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File doesn't exist: " + path);
        }

        US120ErrorListener errorListener = new US120ErrorListener();

        US120Lexer lexer = new US120Lexer(CharStreams.fromPath(path));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        US120Parser parser = new US120Parser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.flightPlan();

        if (errorListener.hasErrors()) {
            printErrors("Syntatic/Lexic errors: ", errorListener.getErrors());
            System.out.println("\nThe semantic validation wasn't executed since the file is structurally invalid.");
            return;
        }

        System.out.println("[Syntatic/Lexic] File structurally valid. No errors.");

        US120SemanticVisitor visitor = new US120SemanticVisitor();
        visitor.visit(tree);

        printErrors("Semantic errors: ", visitor.getErrors());
    }

    private static void printErrors(String source, List<String> errors) {
        if (errors.isEmpty()) {
            System.out.println("[" + source + "] Valid Rules. No errors.");
        } else {
            System.out.println("[" + source + "] Detected errors:");
            for (String e : errors) {
                System.out.println("  - " + e);
            }
        }
    }
}
