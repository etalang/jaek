// DO NOT EDIT
// Generated by JFlex 1.8.2 http://jflex.de/
// source: src/main/jflex/Lexer.flex

import java.util.ArrayList;
import java.math.BigInteger;
import errors.*;


// See https://github.com/jflex-de/jflex/issues/222
@SuppressWarnings("FallThrough")
public class JFlexLexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file. */
  public static final int YYEOF = -1;

  /** Initial size of the lookahead buffer. */
  private static final int ZZ_BUFFERSIZE = 16384;

  // Lexical states.
  public static final int YYINITIAL = 0;
  public static final int COMMENT = 2;
  public static final int STRING = 4;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2, 2
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\u10ff\u0100";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\1\0\2\3\22\0\1\1\1\4"+
    "\1\5\2\0\2\6\1\7\2\6\1\10\2\6\1\11"+
    "\1\0\1\12\1\13\1\14\1\15\1\16\1\17\1\20"+
    "\1\21\1\22\1\23\1\24\2\6\1\4\1\25\1\26"+
    "\2\0\6\27\24\30\1\6\1\31\1\6\1\0\1\32"+
    "\1\0\1\33\1\34\2\27\1\35\1\36\1\37\1\40"+
    "\1\41\2\30\1\42\1\30\1\43\1\44\2\30\1\45"+
    "\1\46\1\47\1\50\1\30\1\51\1\52\2\30\1\53"+
    "\1\6\1\54\u0182\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[512];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\1\1\1\2\1\3\1\4\1\3\1\5\3\3"+
    "\2\6\12\7\1\10\1\11\1\12\1\13\1\14\1\13"+
    "\1\15\1\0\2\15\3\0\1\16\1\7\1\17\6\7"+
    "\2\20\1\21\2\0\4\7\3\0\2\7\3\0\2\7"+
    "\30\0";

  private static int [] zzUnpackAction() {
    int [] result = new int[89];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\55\0\132\0\207\0\207\0\264\0\207\0\207"+
    "\0\341\0\u010e\0\u013b\0\u0168\0\207\0\u0195\0\u01c2\0\u01ef"+
    "\0\u021c\0\u0249\0\u0276\0\u02a3\0\u02d0\0\u02fd\0\u032a\0\u0357"+
    "\0\207\0\207\0\207\0\207\0\207\0\u0384\0\u03b1\0\u03b1"+
    "\0\207\0\u03de\0\u040b\0\u013b\0\u0438\0\207\0\u0465\0\u01c2"+
    "\0\u0492\0\u04bf\0\u04ec\0\u0519\0\u0546\0\u0573\0\207\0\u05a0"+
    "\0\207\0\u05cd\0\u05fa\0\u0627\0\u0654\0\u0681\0\u06ae\0\u06db"+
    "\0\u0708\0\u0735\0\u0762\0\u078f\0\u07bc\0\u07e9\0\u0816\0\u0843"+
    "\0\u0870\0\u089d\0\u08ca\0\u08f7\0\u0924\0\u0951\0\u097e\0\u09ab"+
    "\0\u09d8\0\u0a05\0\u0a32\0\u0a5f\0\u0a8c\0\u0ab9\0\u0ae6\0\u0b13"+
    "\0\u0b40\0\u0b6d\0\u0b9a\0\u0bc7\0\u0bf4\0\u0c21\0\u0c4e\0\u0c7b"+
    "\0\u0ca8";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[89];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\4\3\5\1\6\1\7\1\10\1\11\1\12\1\13"+
    "\1\14\1\15\11\16\2\6\2\17\1\4\1\10\1\17"+
    "\1\20\1\21\1\22\2\17\1\23\1\24\2\17\1\25"+
    "\1\17\1\26\1\27\1\30\1\17\2\10\2\31\1\32"+
    "\52\31\2\33\1\34\2\33\1\35\23\33\1\36\23\33"+
    "\102\0\1\10\27\0\2\37\1\40\4\37\1\41\21\37"+
    "\1\42\23\37\26\0\1\43\27\0\1\44\22\0\1\45"+
    "\42\0\1\46\55\0\12\16\37\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\21\17\11\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\12\17\1\47\6\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\10\17\1\27\10\17"+
    "\11\0\1\17\3\0\12\17\2\0\2\17\1\0\1\17"+
    "\1\21\17\17\11\0\1\17\3\0\12\17\2\0\2\17"+
    "\1\0\4\17\1\50\4\17\1\51\7\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\3\17\1\52\15\17"+
    "\11\0\1\17\3\0\12\17\2\0\2\17\1\0\3\17"+
    "\1\53\15\17\11\0\1\17\3\0\12\17\2\0\2\17"+
    "\1\0\13\17\1\54\5\17\11\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\14\17\1\55\4\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\6\17\1\56\12\17"+
    "\2\0\5\57\1\33\1\57\1\33\21\57\1\33\11\57"+
    "\1\33\1\57\1\33\1\57\1\33\2\57\1\60\2\57"+
    "\7\0\1\61\52\0\1\40\1\0\1\40\21\0\1\40"+
    "\11\0\1\40\1\0\1\40\1\0\1\40\2\0\1\62"+
    "\30\0\1\10\43\0\1\63\46\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\12\17\1\64\6\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\15\17\1\50\3\17"+
    "\11\0\1\17\3\0\12\17\2\0\2\17\1\0\11\17"+
    "\1\65\7\17\11\0\1\17\3\0\12\17\2\0\2\17"+
    "\1\0\15\17\1\66\3\17\11\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\16\17\1\55\2\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\3\17\1\50\15\17"+
    "\11\0\1\17\3\0\12\17\2\0\2\17\1\0\7\17"+
    "\1\67\11\17\55\0\1\70\54\0\1\71\16\0\1\72"+
    "\46\0\1\17\3\0\12\17\2\0\2\17\1\0\10\17"+
    "\1\50\10\17\11\0\1\17\3\0\12\17\2\0\2\17"+
    "\1\0\5\17\1\73\13\17\11\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\16\17\1\74\2\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\10\17\1\55\10\17"+
    "\15\0\12\75\2\0\1\75\3\0\4\75\31\0\12\76"+
    "\2\0\1\76\3\0\4\76\34\0\1\77\45\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\15\17\1\100\3\17"+
    "\11\0\1\17\3\0\12\17\2\0\2\17\1\0\13\17"+
    "\1\101\5\17\15\0\12\102\2\0\1\102\3\0\4\102"+
    "\15\0\1\33\13\0\12\103\2\0\1\103\3\0\4\103"+
    "\15\0\1\40\16\0\1\104\45\0\1\17\3\0\12\17"+
    "\2\0\2\17\1\0\6\17\1\50\12\17\11\0\1\17"+
    "\3\0\12\17\2\0\2\17\1\0\11\17\1\50\7\17"+
    "\15\0\12\105\2\0\1\105\3\0\4\105\15\0\1\33"+
    "\13\0\12\106\2\0\1\106\3\0\4\106\15\0\1\40"+
    "\22\0\1\107\45\0\12\110\2\0\1\110\3\0\4\110"+
    "\15\0\1\33\13\0\12\111\2\0\1\111\3\0\4\111"+
    "\15\0\1\40\15\0\1\112\52\0\12\113\2\0\1\113"+
    "\3\0\4\113\15\0\1\33\13\0\12\114\2\0\1\114"+
    "\3\0\4\114\15\0\1\40\13\0\1\115\54\0\12\116"+
    "\2\0\1\116\3\0\4\116\15\0\1\33\13\0\12\117"+
    "\2\0\1\117\3\0\4\117\15\0\1\40\16\0\1\120"+
    "\112\0\1\33\54\0\1\40\21\0\1\121\56\0\1\122"+
    "\51\0\1\123\53\0\1\124\57\0\1\125\54\0\1\126"+
    "\52\0\1\127\57\0\1\130\44\0\1\131\64\0\1\15"+
    "\31\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[3285];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** Error code for "Unknown internal scanner error". */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  /** Error code for "could not match input". */
  private static final int ZZ_NO_MATCH = 1;
  /** Error code for "pushback value was too large". */
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /**
   * Error messages for {@link #ZZ_UNKNOWN_ERROR}, {@link #ZZ_NO_MATCH}, and
   * {@link #ZZ_PUSHBACK_2BIG} respectively.
   */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\3\0\2\11\1\1\2\11\4\1\1\11\13\1\5\11"+
    "\2\1\1\0\1\11\1\1\3\0\1\11\10\1\1\11"+
    "\1\1\1\11\2\0\4\1\3\0\2\1\3\0\2\1"+
    "\30\0";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[89];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** Input device. */
  private java.io.Reader zzReader;

  /** Current state of the DFA. */
  private int zzState;

  /** Current lexical state. */
  private int zzLexicalState = YYINITIAL;

  /**
   * This buffer contains the current text to be matched and is the source of the {@link #yytext()}
   * string.
   */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** Text position at the last accepting state. */
  private int zzMarkedPos;

  /** Current text position in the buffer. */
  private int zzCurrentPos;

  /** Marks the beginning of the {@link #yytext()} string in the buffer. */
  private int zzStartRead;

  /** Marks the last character in the buffer, that has been read from input. */
  private int zzEndRead;

  /**
   * Whether the scanner is at the end of file.
   * @see #yyatEOF
   */
  private boolean zzAtEOF;

  /**
   * The number of occupied positions in {@link #zzBuffer} beyond {@link #zzEndRead}.
   *
   * <p>When a lead/high surrogate has been read from the input stream into the final
   * {@link #zzBuffer} position, this will have a value of 1; otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /** Number of newlines encountered up to the start of the matched text. */
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  private int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  @SuppressWarnings("unused")
  private boolean zzEOFDone;

  /* user code: */
    private LexUtil.StringTokenBuilder currentString;

    /** Returns the line number the lexer head is currently at in the file, numbered from 1. */
    public int lineNumber() {
        return yyline + 1;
    }

    /** Returns the column the lexer head is currently at in the file, numbered from 1. */
    public int column() {
        return yycolumn + 1;
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public JFlexLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  /**
   * Refills the input buffer.
   *
   * @return {@code false} iff there was new input.
   * @exception java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead - zzStartRead);

      /* translate stored positions */
      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzBuffer.length * 2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException(
          "Reader returned 0 characters. See JFlex examples/zero-reader for a workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
        if (numRead == requested) { // We requested too few chars to encode a full Unicode character
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        } else {                    // There is room in the buffer for at least one more char
          int c = zzReader.read();  // Expecting to read a paired low surrogate char
          if (c == -1) {
            return true;
          } else {
            zzBuffer[zzEndRead++] = (char)c;
          }
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }


  /**
   * Closes the input reader.
   *
   * @throws java.io.IOException if the reader could not be closed.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true; // indicate end of file
    zzEndRead = zzStartRead; // invalidate buffer

    if (zzReader != null) {
      zzReader.close();
    }
  }


  /**
   * Resets the scanner to read from a new input stream.
   *
   * <p>Does not close the old reader.
   *
   * <p>All internal variables are reset, the old input stream <b>cannot</b> be reused (internal
   * buffer is discarded and lost). Lexical state is set to {@code ZZ_INITIAL}.
   *
   * <p>Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader The new input stream.
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzEOFDone = false;
    yyResetPosition();
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE) {
      zzBuffer = new char[ZZ_BUFFERSIZE];
    }
  }

  /**
   * Resets the input position.
   */
  private final void yyResetPosition() {
      zzAtBOL  = true;
      zzAtEOF  = false;
      zzCurrentPos = 0;
      zzMarkedPos = 0;
      zzStartRead = 0;
      zzEndRead = 0;
      zzFinalHighSurrogate = 0;
      yyline = 0;
      yycolumn = 0;
      yychar = 0L;
  }


  /**
   * Returns whether the scanner has reached the end of the reader it reads from.
   *
   * @return whether the scanner has reached EOF.
   */
  public final boolean yyatEOF() {
    return zzAtEOF;
  }


  /**
   * Returns the current lexical state.
   *
   * @return the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state.
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   *
   * @return the matched text.
   */
  public final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
  }


  /**
   * Returns the character at the given position from the matched text.
   *
   * <p>It is equivalent to {@code yytext().charAt(pos)}, but faster.
   *
   * @param position the position of the character to fetch. A value from 0 to {@code yylength()-1}.
   *
   * @return the character at {@code position}.
   */
  public final char yycharat(int position) {
    return zzBuffer[zzStartRead + position];
  }


  /**
   * How many characters were matched.
   *
   * @return the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * <p>In a well-formed scanner (no or only correct usage of {@code yypushback(int)} and a
   * match-all fallback rule) this method will only be called with things that
   * "Can't Possibly Happen".
   *
   * <p>If this method is called, something is seriously wrong (e.g. a JFlex bug producing a faulty
   * scanner etc.).
   *
   * <p>Usual syntax/scanner level error handling should be done in error fallback rules.
   *
   * @param errorCode the code of the error message to display.
   */
  private static void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * <p>They will be read again by then next call of the scanning method.
   *
   * @param number the number of characters to be read again. This number must not be greater than
   *     {@link #yylength()}.
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }




  /**
   * Resumes scanning until the next regular expression is matched, the end of input is encountered
   * or an I/O-Error occurs.
   *
   * @return the next token.
   * @exception java.io.IOException if any I/O-Error occurs.
   */
  public java_cup.runtime.Symbol next_token() throws java.io.IOException, LexicalError {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      int zzCh;
      int zzCharCount;
      for (zzCurrentPosL = zzStartRead  ;
           zzCurrentPosL < zzMarkedPosL ;
           zzCurrentPosL += zzCharCount ) {
        zzCh = Character.codePointAt(zzBufferL, zzCurrentPosL, zzMarkedPosL);
        zzCharCount = Character.charCount(zzCh);
        switch (zzCh) {
        case '\u000B':  // fall through
        case '\u000C':  // fall through
        case '\u0085':  // fall through
        case '\u2028':  // fall through
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn += zzCharCount;
        }
      }

      if (zzR) {
        // peek one character ahead if it is
        // (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof)
            zzPeek = false;
          else
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
            switch (zzLexicalState) {
            case YYINITIAL: {
              return new Token.EOFToken(lineNumber(), column());
            }  // fall though
            case 90: break;
            case COMMENT: {
              return new Token.EOFToken(lineNumber(), column());
            }  // fall though
            case 91: break;
            case STRING: {
              throw new LexicalError(LexicalError.errType.BadString, currentString.lineNumber(), currentString.column());
            }  // fall though
            case 92: break;
            default:
          {    return new java_cup.runtime.Symbol(SymbolTable.EOF);
 }
        }
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { throw new LexicalError(LexicalError.errType.InvalidId, lineNumber(), column());
            }
            // fall through
          case 18: break;
          case 2:
            { /* ignore */
            }
            // fall through
          case 19: break;
          case 3:
            { return new Token.SymbolToken(yytext(), lineNumber(), column());
            }
            // fall through
          case 20: break;
          case 4:
            { currentString = new LexUtil.StringTokenBuilder(lineNumber(), column()); yybegin(STRING);
            }
            // fall through
          case 21: break;
          case 5:
            { throw new LexicalError(LexicalError.errType.CharNotEnd, lineNumber(), column());
            }
            // fall through
          case 22: break;
          case 6:
            { return new Token.IntegerToken(yytext(), lineNumber(), column());
            }
            // fall through
          case 23: break;
          case 7:
            { return new Token.IdToken(yytext(), lineNumber(), column());
            }
            // fall through
          case 24: break;
          case 8:
            { 
            }
            // fall through
          case 25: break;
          case 9:
            { yybegin(YYINITIAL);
            }
            // fall through
          case 26: break;
          case 10:
            { yycolumn -= LexUtil.unicodeAdjustment(yytext()); currentString.append(LexUtil.parseToChar(yytext(), currentString.lineNumber(), currentString.column()));
            }
            // fall through
          case 27: break;
          case 11:
            { throw new LexicalError(LexicalError.errType.BadString, currentString.lineNumber(), currentString.column());
            }
            // fall through
          case 28: break;
          case 12:
            { Token.StringToken t = currentString.complete(); yybegin(YYINITIAL); return t;
            }
            // fall through
          case 29: break;
          case 13:
            { throw new LexicalError(LexicalError.errType.CharWrong, lineNumber(), column());
            }
            // fall through
          case 30: break;
          case 14:
            { yybegin(COMMENT);
            }
            // fall through
          case 31: break;
          case 15:
            { return new Token.KeywordToken(yytext(), lineNumber(), column());
            }
            // fall through
          case 32: break;
          case 16:
            { throw new LexicalError(LexicalError.errType.CharWrong, currentString.lineNumber(), currentString.column());
            }
            // fall through
          case 33: break;
          case 17:
            { return new Token.CharacterToken(yytext(), lineNumber(), column());
            }
            // fall through
          case 34: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
