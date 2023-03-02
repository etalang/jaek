import ast.Terminal;
import ast.UnaryOp;

import java.util.ArrayList;

public class ParseUtil {
    public static <T> ArrayList<T> singleton(T item) {
        ArrayList<T> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    public static final class UnOpBundle{
        UnaryOp.Operation operation;
        Terminal terminal;

        public UnOpBundle(UnaryOp.Operation operation, Terminal terminal) {
            this.operation = operation;
            this.terminal = terminal;
        }
    }
}
