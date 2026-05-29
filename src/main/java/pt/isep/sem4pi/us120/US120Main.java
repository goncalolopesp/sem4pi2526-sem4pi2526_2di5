package pt.isep.sem4pi.us120;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main de apoio à US120 para validar ficheiros .dsldig5.
 */
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

        // 1. Criar o nosso Listener de erros Léxicos/Sintáticos
        US120ErrorListener errorListener = new US120ErrorListener();

        // 2. Lexer
        US120Lexer lexer = new US120Lexer(CharStreams.fromPath(path));
        lexer.removeErrorListeners(); // Remover listener por defeito que imprime na consola
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 3. Parser
        US120Parser parser = new US120Parser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        // Iniciar o parser processando a regra base "flightPlan"
        ParseTree tree = parser.flightPlan();

        System.out.println("=== Resultados de validação para " + path.getFileName() + " ===");

        // 4. Verificar se a fase de parse detetou problemas
        if (errorListener.hasErrors()) {
            printErrors("Léxico/Sintático", errorListener.getErrors());
            System.out.println("\nA validação semântica não foi executada devido a erros estruturais no ficheiro.");
            return;
        } else {
            System.out.println("[Léxico/Sintático] Ficheiro estruturalmente válido. Sem erros.");
        }

        // 5. Visitor Semântico (SÓ corre se não houver erros sintáticos)
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