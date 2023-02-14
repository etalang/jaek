import java_cup.runtime.Symbol;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

public class UltimateLexer extends JFlexLexer {
    private final HeaderToken header;
    private final PrintWriter output;
    private final boolean printLex;
    private boolean hasAbsorbed;

    /**
     * Creates a new scanner
     *
     * @param in the java.io.Reader to read input from.
     */
    public UltimateLexer(Reader in, HeaderToken header) {
        this(in, header, null, false);
    }

    public UltimateLexer(Reader in, HeaderToken header, PrintWriter output, boolean printLex) {
        super(in);
        this.hasAbsorbed = false;
        this.header = header;
        this.output = output;
        this.printLex = true;
    }

    @Override
    public Symbol next_token() throws IOException, LexicalError {
        if (!hasAbsorbed && header != null) {
            hasAbsorbed = true;
            return new Symbol(header.sym);
        } else {
            try {
                Symbol nextToken = super.next_token();
                if (printLex && nextToken instanceof Token) {
                    output.println(((Token<?>) nextToken).lexInfo());
                }
                return nextToken;
            } catch (LexicalError error) {
                if (printLex) output.println(error.getMsg());
                throw error;
            }
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
