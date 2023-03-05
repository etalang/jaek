package edu.cornell.cs.cs4120.xic.ir.parse;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;

import java.math.BigInteger;

@SuppressWarnings({"unused", "fallthrough", "all"})
%%

%public
%class IRLexer
%function next_token
%cup

%unicode
%pack

%line
%column

%{
    private static ComplexSymbolFactory csf = new ComplexSymbolFactory();

    private Symbol sym(String name, int id) {
        return csf.newSymbol(name, id, beginPos(), endPos());
    }

    private Symbol name(String s) {
        return new Name(s, beginPos(), endPos());
    }

    private Symbol number(String s) {
        BigInteger x = new BigInteger(s);
        if (x.bitLength() > 64) {
            return lexError("Number literal \"" +
                        yytext() + "\" out of range.");
        }
        return new Number(x.longValue(), beginPos(), endPos());
    }

    private Symbol lexError(String msg) {
        System.err.println(msg);
        return new LexErrorToken(beginPos(), endPos());
    }

    private Position beginPos() {
        return new Position(yyline+1, yycolumn+1);
    }

    private Position endPos() {
        int len = yytext().length();
        return new Position(yyline+1, yycolumn+1+len);
    }

private static class Position extends Location {
    public Position(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return getLine() + ":" + getColumn();
    }
}

static class Name extends ComplexSymbol {
    public Name(String name, Position left, Position right) {
        super("NAME", IRSym.ATOM, left, right, name);
    }
}

static class Number extends ComplexSymbol {
    public Number(long val, Position left, Position right) {
        super("NUMBER", IRSym.NUMBER, left, right, val);
    }
}

static class LexErrorToken extends ComplexSymbol {
    public LexErrorToken(Position left, Position right) {
        super("error", IRSym.error, left, right);
    }

    @Override
    public String toString() {
        return "lexical error";
    }
}
%}

%eofval{
    return sym("EOF", IRSym.EOF);
%eofval}

LineTerminator = \n|\r|\r\n

WhiteSpace = [ \t\f] | {LineTerminator}

IdentifierSpecChar = [*'\._\-0-9]

/* Identifiers */
Identifier = ( {IdentifierSpecChar}* [:jletter:] {IdentifierSpecChar}* )+ | _

/* Integer Literals */
DecimalNumeral = 0 | "-"?[1-9][0-9]*

%%

"COMPUNIT"          { return sym("COMPUNIT", IRSym.COMPUNIT); }
"DATA"              { return sym("CTOR", IRSym.DATA);         }
"FUNC"              { return sym("FUNC", IRSym.FUNC);         }
"MOVE"              { return sym("MOVE", IRSym.MOVE);         }
"EXP"               { return sym("EXP", IRSym.EXP);           }
"SEQ"               { return sym("SEQ", IRSym.SEQ);           }
"JUMP"              { return sym("JUMP", IRSym.JUMP);         }
"CJUMP"             { return sym("CJUMP", IRSym.CJUMP);       }
"LABEL"             { return sym("LABEL", IRSym.LABEL);       }
"RETURN"            { return sym("RETURN", IRSym.RETURN);     }
"CONST"             { return sym("CONST", IRSym.CONST);       }
"TEMP"              { return sym("TEMP", IRSym.TEMP);         }
"MEM"               { return sym("MEM", IRSym.MEM);           }
"CALL_STMT"         { return sym("CALL_STMT", IRSym.CALL_STMT); }
"CALL"              { return sym("CALL", IRSym.CALL);         }
"NAME"              { return sym("NAME", IRSym.NAME);         }
"ESEQ"              { return sym("ESEQ", IRSym.ESEQ);         }

"ADD"               { return sym("ADD", IRSym.ADD);           }
"SUB"               { return sym("SUB", IRSym.SUB);           }
"MUL"               { return sym("MUL", IRSym.MUL);           }
"HMUL"              { return sym("HMUL", IRSym.HMUL);         }
"DIV"               { return sym("DIV", IRSym.DIV);           }
"MOD"               { return sym("MOD", IRSym.MOD);           }
"AND"               { return sym("AND", IRSym.AND);           }
"OR"                { return sym("OR", IRSym.OR);             }
"XOR"               { return sym("XOR", IRSym.XOR);           }
"LSHIFT"            { return sym("LSHIFT", IRSym.LSHIFT);     }
"RSHIFT"            { return sym("RSHIFT", IRSym.RSHIFT);     }
"ARSHIFT"           { return sym("ARSHIFT", IRSym.ARSHIFT);   }
"EQ"                { return sym("EQ", IRSym.EQ);             }
"NEQ"               { return sym("NEQ", IRSym.NEQ);           }
"LT"                { return sym("LT", IRSym.LT);             }
"ULT"               { return sym("ULT", IRSym.ULT);           }
"GT"                { return sym("GT", IRSym.GT);             }
"LEQ"               { return sym("LEQ", IRSym.LEQ);           }
"GEQ"               { return sym("GEQ", IRSym.GEQ);           }

"("                 { return sym("(", IRSym.LPAREN);          }
")"                 { return sym(")", IRSym.RPAREN);          }

{Identifier}        { return name(yytext()); }
{DecimalNumeral}    { return number(yytext()); }

{WhiteSpace}        { /* ignore */ }

/* Fallthrough case: anything not matched above is an error */
[^]                 { return lexError(beginPos() + ": Illegal character \"" +
                                 yytext() + "\""); }
