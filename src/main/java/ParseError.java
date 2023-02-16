import java_cup.runtime.Symbol;

public class ParseError extends RuntimeException{
    private final Symbol sym;

    public ParseError(Symbol sym) {
        this.sym = sym;
    }

    public Symbol getSym() {
        return sym;
    }
}
