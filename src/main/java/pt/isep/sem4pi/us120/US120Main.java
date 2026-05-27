package pt.isep.sem4pi.us120;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main de apoio à US120 para validar ficheiros .dslig5.
 *
 * Faz:
 *  - validação léxica/sintática via US120Lexer/US120Parser
 *  - validação semântica via US120SemanticListener
 *  - validação semântica via US120SemanticVisitor
 */
public class US120Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso: java ... pt.isep.sem4pi.us120.US120Main <ficheiro.dslig5>");
            System.exit(1);
        }

        Path path = Path.of(args[0]);
        if (!Files.exists(path)) {
            System.err.println("Ficheiro não existe: " + path);
            System.exit(1);
        }

        // Criação do lexer e parser
        US120Lexer lexer = new US120Lexer(CharStreams.fromPath(path));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        US120Parser parser = new US120Parser(tokens);

        // Forçar reporting padrão de erros léxicos/sintáticos
        parser.removeErrorListeners();
        parser.addErrorListener(ConsoleErrorListener.INSTANCE);

        ParseTree tree = parser.flightPlan();

        // Listener semântico
        US120SemanticListener listener = new US120SemanticListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        // Visitor semântico
        US120SemanticVisitor visitor = new US120SemanticVisitor();
        tree.accept(visitor);

        // Agregar erros
        System.out.println("=== Resultados de validação para " + path.getFileName() + " ===");

        printErrors("Listener", listener.getErrors());
        printErrors("Visitor", visitor.getErrors());
    }

    private static void printErrors(String source, List<String> errors) {
        if (errors.isEmpty()) {
            System.out.println("[" + source + "] Sem erros semânticos.");
        } else {
            System.out.println("[" + source + "] Erros semânticos:");
            for (String e : errors) {
                System.out.println("  - " + e);
            }
        }
    }
}

