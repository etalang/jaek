

package edu.cornell.cs.cs4120.xic.ir.parse;

import edu.cornell.cs.cs4120.xic.ir.*;
import java_cup.runtime.ComplexSymbolFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** CUP v0.11b 20150326 generated parser.
  */
public class IRParser
 extends java_cup.runtime.lr_parser {

  @Override
  public final Class<?> getSymbolContainer() {
    return IRSym.class;
  }

  /** Default constructor. */
  @Deprecated
  public IRParser() {super();}

  /** Constructor which sets the default scanner. */
  @Deprecated
  public IRParser(java_cup.runtime.Scanner s) {super(s);}

  /** Constructor which sets the default scanner and a SymbolFactory. */
  public IRParser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {super(s,sf);}

  /** Production table. */
  protected static final short _production_table[][] = 
    unpackFromStrings(new String[] {
    "\000\077\000\002\002\004\000\002\002\007\000\002\003" +
    "\002\000\002\003\003\000\002\004\005\000\002\004\006" +
    "\000\002\005\003\000\002\005\003\000\002\005\003\000" +
    "\002\015\007\000\002\016\002\000\002\016\003\000\002" +
    "\017\003\000\002\017\004\000\002\006\005\000\002\014" +
    "\003\000\002\010\005\000\002\011\005\000\002\011\006" +
    "\000\002\011\004\000\002\011\004\000\002\011\004\000" +
    "\002\011\006\000\002\011\005\000\002\011\004\000\002" +
    "\011\005\000\002\011\004\000\002\023\006\000\002\023" +
    "\006\000\002\020\005\000\002\021\004\000\002\021\004" +
    "\000\002\021\003\000\002\021\004\000\002\021\005\000" +
    "\002\021\004\000\002\021\005\000\002\024\003\000\002" +
    "\022\005\000\002\022\005\000\002\022\005\000\002\022" +
    "\005\000\002\022\005\000\002\022\005\000\002\022\005" +
    "\000\002\022\005\000\002\022\005\000\002\022\005\000" +
    "\002\022\005\000\002\022\005\000\002\022\005\000\002" +
    "\022\005\000\002\022\005\000\002\022\005\000\002\022" +
    "\005\000\002\022\005\000\002\022\005\000\002\007\003" +
    "\000\002\007\004\000\002\012\002\000\002\012\003\000" +
    "\002\013\003\000\002\013\004" });

  /** Access to production table. */
  @Override
  public short[][] production_table() {return _production_table;}

  /** Parse-action table. */
  protected static final short[][] _action_table = 
    unpackFromStrings(new String[] {
    "\000\220\000\004\004\004\001\002\000\004\051\007\001" +
    "\002\000\004\002\006\001\002\000\004\002\001\001\002" +
    "\000\004\052\011\001\002\000\006\004\013\005\uffff\001" +
    "\002\000\010\004\ufff2\005\ufff2\052\ufff2\001\002\000\004" +
    "\005\222\001\002\000\010\047\021\050\020\052\011\001" +
    "\002\000\006\004\015\005\ufffe\001\002\000\010\047\021" +
    "\050\020\052\011\001\002\000\004\005\217\001\002\000" +
    "\004\005\ufffa\001\002\000\004\052\210\001\002\000\004" +
    "\052\011\001\002\000\004\005\ufff9\001\002\000\004\005" +
    "\ufffb\001\002\000\004\004\025\001\002\000\022\006\032" +
    "\007\033\010\035\011\037\012\036\013\031\014\034\015" +
    "\030\001\002\000\004\005\ufff3\001\002\000\004\005\207" +
    "\001\002\000\006\004\205\005\uffc6\001\002\000\004\004" +
    "\043\001\002\000\004\004\170\001\002\000\004\053\146" +
    "\001\002\000\004\052\011\001\002\000\004\004\043\001" +
    "\002\000\004\004\043\001\002\000\004\004\025\001\002" +
    "\000\006\004\025\005\uffed\001\002\000\006\004\uffc8\005" +
    "\uffc8\001\002\000\006\004\uffc7\005\uffc7\001\002\000\064" +
    "\016\051\017\067\020\070\021\046\022\063\023\074\024" +
    "\045\025\054\026\057\027\077\030\065\031\055\032\072" +
    "\033\056\034\075\035\071\036\064\037\047\040\060\041" +
    "\076\042\050\043\052\044\062\045\073\046\066\001\002" +
    "\000\004\005\uffec\001\002\000\004\004\043\001\002\000" +
    "\004\004\043\001\002\000\004\004\043\001\002\000\004" +
    "\004\043\001\002\000\004\053\146\001\002\000\004\004" +
    "\043\001\002\000\004\005\uffe1\001\002\000\004\004\043" +
    "\001\002\000\004\004\043\001\002\000\004\004\043\001" +
    "\002\000\004\004\043\001\002\000\004\004\043\001\002" +
    "\000\004\005\131\001\002\000\004\004\043\001\002\000" +
    "\004\052\011\001\002\000\004\004\043\001\002\000\004" +
    "\004\043\001\002\000\004\004\043\001\002\000\004\052" +
    "\011\001\002\000\004\004\043\001\002\000\004\004\043" +
    "\001\002\000\004\004\043\001\002\000\004\004\043\001" +
    "\002\000\004\004\025\001\002\000\004\004\043\001\002" +
    "\000\004\004\043\001\002\000\004\004\043\001\002\000" +
    "\004\004\043\001\002\000\004\005\uffd8\001\002\000\004" +
    "\004\043\001\002\000\004\005\uffce\001\002\000\004\004" +
    "\043\001\002\000\004\005\uffd3\001\002\000\004\004\043" +
    "\001\002\000\004\005\uffdd\001\002\000\004\004\043\001" +
    "\002\000\004\005\uffca\001\002\000\004\004\043\001\002" +
    "\000\004\005\uffd5\001\002\000\004\004\043\001\002\000" +
    "\004\005\uffd2\001\002\000\004\005\uffe0\001\002\000\004" +
    "\005\uffe2\001\002\000\004\004\043\001\002\000\004\005" +
    "\uffc9\001\002\000\004\004\043\001\002\000\004\005\uffd7" +
    "\001\002\000\004\004\043\001\002\000\004\005\uffd1\001" +
    "\002\000\004\005\uffde\001\002\000\004\004\043\001\002" +
    "\000\004\005\uffcb\001\002\000\010\004\uffe4\005\uffe4\052" +
    "\uffe4\001\002\000\004\004\043\001\002\000\004\005\uffcf" +
    "\001\002\000\004\004\043\001\002\000\004\005\uffd9\001" +
    "\002\000\004\004\043\001\002\000\004\005\uffd4\001\002" +
    "\000\004\004\043\001\002\000\004\005\uffd6\001\002\000" +
    "\004\004\043\001\002\000\004\005\uffda\001\002\000\004" +
    "\004\043\001\002\000\004\005\uffcc\001\002\000\010\004" +
    "\uffdc\005\uffdc\053\uffdc\001\002\000\004\005\uffe3\001\002" +
    "\000\004\004\043\001\002\000\004\005\uffcd\001\002\000" +
    "\004\004\043\001\002\000\004\005\uffd0\001\002\000\006" +
    "\004\043\005\uffc6\001\002\000\004\005\uffdf\001\002\000" +
    "\006\004\043\005\uffc5\001\002\000\006\004\uffc4\005\uffc4" +
    "\001\002\000\006\004\uffc3\005\uffc3\001\002\000\004\004" +
    "\043\001\002\000\004\005\uffdb\001\002\000\004\005\uffee" +
    "\001\002\000\004\005\uffe9\001\002\000\004\004\043\001" +
    "\002\000\006\004\043\005\uffc6\001\002\000\004\005\uffef" +
    "\001\002\000\006\017\173\020\174\001\002\000\004\004" +
    "\043\001\002\000\004\005\ufff0\001\002\000\004\052\011" +
    "\001\002\000\004\004\043\001\002\000\004\005\176\001" +
    "\002\000\004\004\uffe5\001\002\000\004\005\200\001\002" +
    "\000\004\004\uffe6\001\002\000\004\052\011\001\002\000" +
    "\006\005\uffea\052\011\001\002\000\004\005\uffeb\001\002" +
    "\000\004\005\uffe7\001\002\000\066\005\206\016\051\017" +
    "\067\020\070\021\046\022\063\023\074\024\045\025\054" +
    "\026\057\027\077\030\065\031\055\032\072\033\056\034" +
    "\075\035\071\036\064\037\047\040\060\041\076\042\050" +
    "\043\052\044\062\045\073\046\066\001\002\000\004\005" +
    "\uffe8\001\002\000\006\004\ufff1\005\ufff1\001\002\000\004" +
    "\004\211\001\002\000\006\005\ufff7\053\146\001\002\000" +
    "\004\005\216\001\002\000\006\005\ufff5\053\ufff5\001\002" +
    "\000\006\005\ufff6\053\146\001\002\000\006\005\ufff4\053" +
    "\ufff4\001\002\000\004\005\ufff8\001\002\000\006\004\ufffc" +
    "\005\ufffc\001\002\000\004\005\221\001\002\000\006\004" +
    "\ufffd\005\ufffd\001\002\000\004\002\000\001\002" });

  /** Access to parse-action table. */
  @Override
  public short[][] action_table() {return _action_table;}

  /** {@code reduce_goto} table. */
  protected static final short[][] _reduce_table = 
    unpackFromStrings(new String[] {
    "\000\220\000\004\002\004\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\004\014\007\001\001" +
    "\000\006\003\011\004\013\001\001\000\002\001\001\000" +
    "\002\001\001\000\012\005\217\006\021\014\022\015\016" +
    "\001\001\000\002\001\001\000\012\005\015\006\021\014" +
    "\022\015\016\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\004\014\023\001\001\000\002\001" +
    "\001\000\002\001\001\000\004\010\025\001\001\000\004" +
    "\011\026\001\001\000\002\001\001\000\002\001\001\000" +
    "\010\012\203\013\155\020\156\001\001\000\004\020\200" +
    "\001\001\000\004\023\170\001\001\000\004\024\164\001" +
    "\001\000\004\014\163\001\001\000\004\020\162\001\001" +
    "\000\004\020\043\001\001\000\006\007\037\010\040\001" +
    "\001\000\004\010\041\001\001\000\002\001\001\000\002" +
    "\001\001\000\006\021\060\022\052\001\001\000\002\001" +
    "\001\000\004\020\160\001\001\000\004\020\153\001\001" +
    "\000\004\020\151\001\001\000\004\020\147\001\001\000" +
    "\004\024\146\001\001\000\004\020\143\001\001\000\002" +
    "\001\001\000\004\020\141\001\001\000\004\020\137\001" +
    "\001\000\004\020\135\001\001\000\004\020\133\001\001" +
    "\000\004\020\131\001\001\000\002\001\001\000\004\020" +
    "\126\001\001\000\004\014\125\001\001\000\004\020\123" +
    "\001\001\000\004\020\121\001\001\000\004\020\117\001" +
    "\001\000\004\014\116\001\001\000\004\020\115\001\001" +
    "\000\004\020\113\001\001\000\004\020\111\001\001\000" +
    "\004\020\107\001\001\000\004\010\105\001\001\000\004" +
    "\020\103\001\001\000\004\020\101\001\001\000\004\020" +
    "\077\001\001\000\004\020\100\001\001\000\002\001\001" +
    "\000\004\020\102\001\001\000\002\001\001\000\004\020" +
    "\104\001\001\000\002\001\001\000\004\020\106\001\001" +
    "\000\002\001\001\000\004\020\110\001\001\000\002\001" +
    "\001\000\004\020\112\001\001\000\002\001\001\000\004" +
    "\020\114\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\004\020\120\001\001\000\002\001\001" +
    "\000\004\020\122\001\001\000\002\001\001\000\004\020" +
    "\124\001\001\000\002\001\001\000\002\001\001\000\004" +
    "\020\127\001\001\000\002\001\001\000\002\001\001\000" +
    "\004\020\132\001\001\000\002\001\001\000\004\020\134" +
    "\001\001\000\002\001\001\000\004\020\136\001\001\000" +
    "\002\001\001\000\004\020\140\001\001\000\002\001\001" +
    "\000\004\020\142\001\001\000\002\001\001\000\004\020" +
    "\144\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\004\020\150\001\001\000\002\001\001\000" +
    "\004\020\152\001\001\000\002\001\001\000\010\012\154" +
    "\013\155\020\156\001\001\000\002\001\001\000\004\020" +
    "\157\001\001\000\002\001\001\000\002\001\001\000\004" +
    "\020\161\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\004\020\165\001\001\000\010\012\166" +
    "\013\155\020\156\001\001\000\002\001\001\000\002\001" +
    "\001\000\004\020\171\001\001\000\002\001\001\000\004" +
    "\014\176\001\001\000\004\020\174\001\001\000\002\001" +
    "\001\000\002\001\001\000\002\001\001\000\002\001\001" +
    "\000\004\014\201\001\001\000\004\014\202\001\001\000" +
    "\002\001\001\000\002\001\001\000\006\021\060\022\052" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\010\016\211\017\213\024\212\001\001\000\002" +
    "\001\001\000\002\001\001\000\004\024\214\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001" });

  /** Access to {@code reduce_goto} table. */
  @Override
  public short[][] reduce_table() {return _reduce_table;}

  /** Instance of action encapsulation class. */
  protected CUP$IRParser$actions action_obj;

  /** Action encapsulation object initializer. */
  @Override
  protected void init_actions()
    {
      action_obj = new CUP$IRParser$actions(this);
    }

  /** Invoke a user supplied parse action. */
  @Override
  public java_cup.runtime.Symbol do_action(
    int                        act_num,
    java_cup.runtime.lr_parser parser,
    java.util.Stack<java_cup.runtime.Symbol> stack,
    int                        top)
    throws java.lang.Exception
  {
    /* call code in generated class */
    return action_obj.CUP$IRParser$do_action(act_num, parser, stack, top);
  }

  /** Indicates start state. */
  @Override
  public int start_state() {return 0;}
  /** Indicates start production. */
  @Override
  public int start_production() {return 0;}

  /** {@code EOF} Symbol index. */
  @Override
  public int EOF_sym() {return 0;}

  /** {@code error} Symbol index. */
  @Override
  public int error_sym() {return 1;}




    protected IRNodeFactory nf;

    public IRParser(IRLexer lexer, IRNodeFactory nf) {
        super(lexer, new ComplexSymbolFactory());
        this.nf = nf;
    }


/** Cup generated class to encapsulate user supplied action code.*/
class CUP$IRParser$actions {
    private final IRParser parser;

    /** Constructor */
    CUP$IRParser$actions(IRParser parser) {
        this.parser = parser;
    }

    /** Method with the actual generated action code for actions 0 to 62. */
    public final java_cup.runtime.Symbol CUP$IRParser$do_action_part00000000(
            int                        CUP$IRParser$act_num,
            java_cup.runtime.lr_parser CUP$IRParser$parser,
            java.util.Stack<java_cup.runtime.Symbol> CUP$IRParser$stack,
            int                        CUP$IRParser$top)
            throws java.lang.Exception {
            /* Symbol object for return from actions */
            java_cup.runtime.Symbol CUP$IRParser$result;

        /* select the action based on the action number */
        switch (CUP$IRParser$act_num) {
        /*. . . . . . . . . . . . . . . . . . . .*/
        case 0: // $START ::= compunit EOF 
            {
                Object RESULT = null;
                IRCompUnit start_val = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRCompUnit> value();
                RESULT = start_val;
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("$START",0, RESULT);
            }
            /* ACCEPT */
            CUP$IRParser$parser.done_parsing();
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 1: // compunit ::= LPAREN COMPUNIT name ctor_or_data_or_funcdecl_list RPAREN 
            {
                IRCompUnit RESULT = null;
                String n = CUP$IRParser$stack.elementAt(CUP$IRParser$top-2).<String> value();
                List<Object> cdf_list = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<List<Object>> value();
                
        RESULT = parser.nf.IRCompUnit(n);

        for (Object cdf : cdf_list){
            if (cdf instanceof String) {
                RESULT.appendCtor((String) cdf);
            } else if (cdf instanceof IRData) {
                RESULT.appendData((IRData) cdf);
            } else if (cdf instanceof IRFuncDecl) {
                RESULT.appendFunc((IRFuncDecl) cdf);
            }
        }
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("compunit",0, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 2: // ctor_or_data_or_funcdecl_list ::= 
            {
                List<Object> RESULT = null;
                 RESULT = Collections.emptyList(); 
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl_list",1, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 3: // ctor_or_data_or_funcdecl_list ::= ctor_or_data_or_funcdecl_list_non_empty 
            {
                List<Object> RESULT = null;
                List<Object> l = CUP$IRParser$stack.peek().<List<Object>> value();
                 RESULT = l; 
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl_list",1, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 4: // ctor_or_data_or_funcdecl_list_non_empty ::= LPAREN ctor_or_data_or_funcdecl RPAREN 
            {
                List<Object> RESULT = null;
                Object cdf = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<Object> value();
                
        RESULT = new ArrayList<>();
        RESULT.add(cdf);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl_list_non_empty",2, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 5: // ctor_or_data_or_funcdecl_list_non_empty ::= ctor_or_data_or_funcdecl_list_non_empty LPAREN ctor_or_data_or_funcdecl RPAREN 
            {
                List<Object> RESULT = null;
                List<Object> cdf_list = CUP$IRParser$stack.elementAt(CUP$IRParser$top-3).<List<Object>> value();
                Object cdf = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<Object> value();
                
        RESULT = cdf_list;
        RESULT.add(cdf);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl_list_non_empty",2, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 6: // ctor_or_data_or_funcdecl ::= name 
            {
                Object RESULT = null;
                String c = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = c;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl",3, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 7: // ctor_or_data_or_funcdecl ::= data 
            {
                Object RESULT = null;
                IRData d = CUP$IRParser$stack.peek().<IRData> value();
                
        RESULT = d;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl",3, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 8: // ctor_or_data_or_funcdecl ::= funcdecl 
            {
                Object RESULT = null;
                IRFuncDecl fd = CUP$IRParser$stack.peek().<IRFuncDecl> value();
                
        RESULT = fd;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("ctor_or_data_or_funcdecl",3, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 9: // data ::= DATA ATOM LPAREN number_list RPAREN 
            {
                IRData RESULT = null;
                String a = CUP$IRParser$stack.elementAt(CUP$IRParser$top-3).<String> value();
                List<Long> n_list = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<List<Long>> value();
                
    int s = n_list.size();
    long[] rawData = new long[s];
    for (int i = 0; i < s; i++) {
        rawData[i] = n_list.get(i);
    }
    RESULT = new IRData(a, rawData);

                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("data",11, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 10: // number_list ::= 
            {
                List<Long> RESULT = null;
                 RESULT = Collections.emptyList(); 
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("number_list",12, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 11: // number_list ::= number_list_non_empty 
            {
                List<Long> RESULT = null;
                List<Long> l = CUP$IRParser$stack.peek().<List<Long>> value();
                 RESULT = l; 
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("number_list",12, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 12: // number_list_non_empty ::= num 
            {
                List<Long> RESULT = null;
                Long n = CUP$IRParser$stack.peek().<Long> value();
                
        RESULT = new ArrayList<>();
        RESULT.add(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("number_list_non_empty",13, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 13: // number_list_non_empty ::= number_list_non_empty num 
            {
                List<Long> RESULT = null;
                List<Long> n_list = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<List<Long>> value();
                Long n = CUP$IRParser$stack.peek().<Long> value();
                
        RESULT = n_list;
        RESULT.add(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("number_list_non_empty",13, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 14: // funcdecl ::= FUNC name stmt 
            {
                IRFuncDecl RESULT = null;
                String n = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<String> value();
                IRStmt s = CUP$IRParser$stack.peek().<IRStmt> value();
                
        RESULT = parser.nf.IRFuncDecl(n, s);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("funcdecl",4, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 15: // name ::= ATOM 
            {
                String RESULT = null;
                String a = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = a;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("name",10, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 16: // stmt ::= LPAREN bare_stmt RPAREN 
            {
                IRStmt RESULT = null;
                IRStmt s = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRStmt> value();
                
        RESULT = s;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("stmt",6, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 17: // bare_stmt ::= MOVE dest expr 
            {
                IRStmt RESULT = null;
                IRExpr dest = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRMove(dest, e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 18: // bare_stmt ::= CALL_STMT num expr exprs_opt 
            {
                IRStmt RESULT = null;
                Long n = CUP$IRParser$stack.elementAt(CUP$IRParser$top-2).<Long> value();
                IRExpr target = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                List<IRExpr> args = CUP$IRParser$stack.peek().<List<IRExpr>> value();
                
        RESULT = parser.nf.IRCallStmt(target, n, args);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 19: // bare_stmt ::= EXP expr 
            {
                IRStmt RESULT = null;
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRExp(e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 20: // bare_stmt ::= SEQ stmts 
            {
                IRStmt RESULT = null;
                List<IRStmt> l = CUP$IRParser$stack.peek().<List<IRStmt>> value();
                
        RESULT = parser.nf.IRSeq(l);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 21: // bare_stmt ::= JUMP expr 
            {
                IRStmt RESULT = null;
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRJump(e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 22: // bare_stmt ::= CJUMP expr name name 
            {
                IRStmt RESULT = null;
                IRExpr e = CUP$IRParser$stack.elementAt(CUP$IRParser$top-2).<IRExpr> value();
                String trueLabel = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<String> value();
                String falseLabel = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = parser.nf.IRCJump(e, trueLabel, falseLabel);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 23: // bare_stmt ::= CJUMP expr name 
            {
                IRStmt RESULT = null;
                IRExpr e = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                String trueLabel = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = parser.nf.IRCJump(e, trueLabel);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 24: // bare_stmt ::= LABEL name 
            {
                IRStmt RESULT = null;
                String n = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = parser.nf.IRLabel(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 25: // bare_stmt ::= RETURN LPAREN RPAREN 
            {
                IRStmt RESULT = null;
                
        RESULT = parser.nf.IRReturn(Collections.emptyList());
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 26: // bare_stmt ::= RETURN exprs_opt 
            {
                IRStmt RESULT = null;
                List<IRExpr> args = CUP$IRParser$stack.peek().<List<IRExpr>> value();
                
        RESULT = parser.nf.IRReturn(args);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_stmt",7, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 27: // dest ::= LPAREN TEMP name RPAREN 
            {
                IRExpr RESULT = null;
                String n = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<String> value();
                
        RESULT = parser.nf.IRTemp(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("dest",17, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 28: // dest ::= LPAREN MEM expr RPAREN 
            {
                IRExpr RESULT = null;
                IRExpr e = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                
        RESULT = parser.nf.IRMem(e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("dest",17, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 29: // expr ::= LPAREN bare_expr RPAREN 
            {
                IRExpr RESULT = null;
                IRExpr e = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                
        RESULT = e;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("expr",14, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 30: // bare_expr ::= CONST num 
            {
                IRExpr RESULT = null;
                Long n = CUP$IRParser$stack.peek().<Long> value();
                
        RESULT = parser.nf.IRConst(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 31: // bare_expr ::= TEMP name 
            {
                IRExpr RESULT = null;
                String n = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = parser.nf.IRTemp(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 32: // bare_expr ::= op 
            {
                IRExpr RESULT = null;
                IRExpr o = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = o;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 33: // bare_expr ::= MEM expr 
            {
                IRExpr RESULT = null;
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRMem(e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 34: // bare_expr ::= CALL expr exprs_opt 
            {
                IRExpr RESULT = null;
                IRExpr target = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                List<IRExpr> args = CUP$IRParser$stack.peek().<List<IRExpr>> value();
                
        RESULT = parser.nf.IRCall(target, args);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 35: // bare_expr ::= NAME name 
            {
                IRExpr RESULT = null;
                String n = CUP$IRParser$stack.peek().<String> value();
                
        RESULT = parser.nf.IRName(n);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 36: // bare_expr ::= ESEQ stmt expr 
            {
                IRExpr RESULT = null;
                IRStmt s = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRStmt> value();
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRESeq(s, e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("bare_expr",15, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 37: // num ::= NUMBER 
            {
                Long RESULT = null;
                Long n = CUP$IRParser$stack.peek().<Long> value();
                
        RESULT = n;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("num",18, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 38: // op ::= ADD expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.ADD, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 39: // op ::= SUB expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.SUB, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 40: // op ::= MUL expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.MUL, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 41: // op ::= HMUL expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.HMUL, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 42: // op ::= DIV expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.DIV, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 43: // op ::= MOD expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.MOD, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 44: // op ::= AND expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.AND, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 45: // op ::= OR expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.OR, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 46: // op ::= XOR expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.XOR, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 47: // op ::= LSHIFT expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.LSHIFT, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 48: // op ::= RSHIFT expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.RSHIFT, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 49: // op ::= ARSHIFT expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.ARSHIFT, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 50: // op ::= EQ expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.EQ, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 51: // op ::= NEQ expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.NEQ, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 52: // op ::= LT expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.LT, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 53: // op ::= ULT expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.ULT, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 54: // op ::= GT expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.GT, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 55: // op ::= LEQ expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.LEQ, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 56: // op ::= GEQ expr expr 
            {
                IRExpr RESULT = null;
                IRExpr x = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<IRExpr> value();
                IRExpr y = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = parser.nf.IRBinOp(IRBinOp.OpType.GEQ, x,y);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("op",16, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 57: // stmts ::= stmt 
            {
                List<IRStmt> RESULT = null;
                IRStmt s = CUP$IRParser$stack.peek().<IRStmt> value();
                
        RESULT = new LinkedList<>();
        RESULT.add(s);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("stmts",5, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 58: // stmts ::= stmts stmt 
            {
                List<IRStmt> RESULT = null;
                List<IRStmt> l = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<List<IRStmt>> value();
                IRStmt s = CUP$IRParser$stack.peek().<IRStmt> value();
                
        RESULT = l;
        RESULT.add(s);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("stmts",5, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 59: // exprs_opt ::= 
            {
                List<IRExpr> RESULT = null;
                
        RESULT = Collections.emptyList();
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("exprs_opt",8, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 60: // exprs_opt ::= exprs 
            {
                List<IRExpr> RESULT = null;
                List<IRExpr> l = CUP$IRParser$stack.peek().<List<IRExpr>> value();
                
        RESULT = l;
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("exprs_opt",8, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 61: // exprs ::= expr 
            {
                List<IRExpr> RESULT = null;
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = new LinkedList<>();
        RESULT.add(e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("exprs",9, RESULT);
            }
            return CUP$IRParser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
        case 62: // exprs ::= exprs expr 
            {
                List<IRExpr> RESULT = null;
                List<IRExpr> l = CUP$IRParser$stack.elementAt(CUP$IRParser$top-1).<List<IRExpr>> value();
                IRExpr e = CUP$IRParser$stack.peek().<IRExpr> value();
                
        RESULT = l;
        RESULT.add(e);
    
                CUP$IRParser$result = parser.getSymbolFactory().newSymbol("exprs",9, RESULT);
            }
            return CUP$IRParser$result;

        /* . . . . . .*/
        default:
            throw new Exception(
                  "Invalid action number " + CUP$IRParser$act_num + " found in internal parse table");

        }
    } /* end of method */

    /** Method splitting the generated action code into several parts. */
    public final java_cup.runtime.Symbol CUP$IRParser$do_action(
            int                        CUP$IRParser$act_num,
            java_cup.runtime.lr_parser CUP$IRParser$parser,
            java.util.Stack<java_cup.runtime.Symbol> CUP$IRParser$stack,
            int                        CUP$IRParser$top)
            throws java.lang.Exception {
            return CUP$IRParser$do_action_part00000000(
                           CUP$IRParser$act_num,
                           CUP$IRParser$parser,
                           CUP$IRParser$stack,
                           CUP$IRParser$top);
    }
}

}
