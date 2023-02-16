import java_cup.runtime.Symbol;

import java.io.IOException;
import java.io.Reader;

public class UltimateLexer extends JFlexLexer {
    private final HeaderToken header;
    private boolean hasAbsorbed;

    /**
     * Creates a new scanner
     *
     */
    public UltimateLexer(Reader in, HeaderToken header) {
        super(in);
        this.hasAbsorbed = false;
        this.header = header;
    }

    @Override
    public Symbol next_token() throws IOException, LexicalError {
        if (!hasAbsorbed && header != null) {
            hasAbsorbed = true;
            return new Symbol(header.sym);
        } else {
            return super.next_token();
        }
    }

    public enum HeaderToken {
        PROGRAM(SymbolTable.PROGRAM), INTERFACE(SymbolTable.INTERFACE);
        private final int sym;

        HeaderToken(int sym) {
            this.sym = sym;
        }
    }
}
