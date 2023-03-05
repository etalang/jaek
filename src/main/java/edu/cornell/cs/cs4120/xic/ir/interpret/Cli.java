package edu.cornell.cs.cs4120.xic.ir.interpret;

import edu.cornell.cs.cs4120.xic.ir.parse.IRLexer;
import edu.cornell.cs.cs4120.xic.ir.parse.IRParser;
import edu.cornell.cs.cs4120.xic.ir.IRCompUnit;
import edu.cornell.cs.cs4120.xic.ir.IRNodeFactory_c;

import java.io.FileReader;

public class Cli {

    /**
     *  A command-line interface for directly invoking the IR interpreter on IR Code.
     *  Usage: use the first command line argument to pass a filename
     */
    public static void main(String[] args) {
        IRCompUnit ircli; 
        try (FileReader r = new FileReader(args[0])) {
            IRParser parser = new IRParser(new IRLexer(r), new IRNodeFactory_c());
            try {
            ircli = parser.parse().<IRCompUnit>value();
            } catch (RuntimeException e) {
                throw e;
            } 
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null) System.err.println("File not found or filename argument not given: " + msg);
            return; 
        }

        IRSimulator sim = new IRSimulator(ircli);

        // add command line args to memory
        long xi_args_addr = 0L; 
        if (args.length > 0) {
            long[] xi_args = new long[args.length];  
            for(int i = 0; i < args.length; i++) {
                xi_args[i] = sim.calloc(args[i].length() * 8L + 8L);
                sim.store(xi_args[i], (long) args[i].length());
                for (int j = 1; j <= args[i].length(); j++) {
                    sim.store(xi_args[i] + 8L * j, (long) args[i].codePoints().toArray()[j-1]);
                }
                xi_args[i] += 8L;
            }
            xi_args_addr = sim.calloc(args.length * 8L + 8L);
            sim.store(xi_args_addr, (long) args.length);
            for(int i = 1; i <= args.length; i++) {
                sim.store(xi_args_addr + 8L * i, xi_args[i-1]);
            }
            xi_args_addr += 8L;
        }
        long result = sim.call("_Imain_paai", xi_args_addr);
    }
}
