package pt.isep.sem4pi.us120;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class US120Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso: java pt.isep.sem4pi.us120.US120Main <ficheiro.dsldig5>");
            System.exit(1);
        }

        Path path = Path.of(args[0]);
        if (!Files.exists(path)) {
            System.err.println("Ficheiro não existe: " + path);
            System.exit(1);
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

        System.out.println("=== Resultados de validação para " + path.getFileName() + " ===");

        if (errorListener.hasErrors()) {
            printErrors("Léxico/Sintático", errorListener.getErrors());
            System.out.println("\nA validação semântica não foi executada devido a erros estruturais no ficheiro.");
            return;
        } else {
            System.out.println("[Léxico/Sintático] Ficheiro estruturalmente válido. Sem erros.");
        }

        US120SemanticVisitor visitor = new US120SemanticVisitor();
        visitor.visit(tree);

        printErrors("Semântico", visitor.getErrors());
    }

    private static void printErrors(String source, List<String> errors) {
        if (errors.isEmpty()) {
            System.out.println("[" + source + "] Regras válidas. Sem erros.");
        } else {
            System.out.println("[" + source + "] Erros detetados:");
            for (String e : errors) {
                System.out.println("  - " + e);
            }
        }
    }
}