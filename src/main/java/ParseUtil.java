import java.util.ArrayList;

public class ParseUtil {
    public static <T> ArrayList<T> singleton(T item) {
        ArrayList<T> list = new ArrayList<>();
        list.add(item);
        return list;
    }
}
