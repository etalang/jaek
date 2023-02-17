public class IntLitInfo {
    public String data;
    public int line;
    public int column;
    public IntLitInfo(String d, int l, int c)   {
        data = d; line = l; column = c;
    }

    @Override
    public String toString() {
        return data;
    }
}
