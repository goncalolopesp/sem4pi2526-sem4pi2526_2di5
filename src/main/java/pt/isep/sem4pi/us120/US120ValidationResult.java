package pt.isep.sem4pi.us120;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class US120ValidationResult {

    private final List<String> lexicalSyntacticErrors;
    private final List<String> semanticErrors;
    private final ParseTree parseTree;

    public US120ValidationResult(final List<String> lexicalSyntacticErrors,
                                 final List<String> semanticErrors,
                                 final ParseTree parseTree) {
        this.lexicalSyntacticErrors = lexicalSyntacticErrors == null
                ? List.of() : List.copyOf(lexicalSyntacticErrors);
        this.semanticErrors = semanticErrors == null ? List.of() : List.copyOf(semanticErrors);
        this.parseTree = parseTree;
    }

    public boolean isValid() {
        return lexicalSyntacticErrors.isEmpty() && semanticErrors.isEmpty();
    }

    public List<String> getLexicalSyntacticErrors() {
        return Collections.unmodifiableList(lexicalSyntacticErrors);
    }

    public List<String> getSemanticErrors() {
        return Collections.unmodifiableList(semanticErrors);
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public List<String> getAllErrors() {
        final List<String> all = new ArrayList<>(lexicalSyntacticErrors);
        all.addAll(semanticErrors);
        return Collections.unmodifiableList(all);
    }
}
