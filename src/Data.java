import java.util.HashMap;
import java.util.Map;

public class Data {

    private static Map<Tuple, String> tuples = new HashMap<Tuple, String>();
    private static String prevBranch = "Source";

    public static void addTuple(String src, String dest) {
        if (!tuples.containsKey(new Tuple(src, dest))) {
            tuples.put(new Tuple(src, dest), "");
        }
    }

    public static boolean containsTuple(String src, String dest) {
        return tuples.containsKey(new Tuple(src, dest));
    }

    public static String getPrevious() {
        return prevBranch;
    }
    
    public static void setPrevious(String branch) {
        prevBranch = branch;
    }       

}

class Tuple {
    private String src;
    private String dest;
    public Tuple (String src, String dest) {
        this.src = src;
        this.dest = dest;
    }

    public String getSrc() {
        return this.src;
    }

    public String getDest() {
        return this.dest;
    }
    @Override
    public int hashCode() {
        return src.hashCode() + dest.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        Tuple tuple = (Tuple) obj;
        if (this.src.equals(tuple.getSrc()) && this.dest.equals(tuple.getDest())) {
            return true;
        } else {
            return false;
        }

    }
}
