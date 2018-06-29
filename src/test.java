import java.util.ArrayList;

public class test {
public static void main(String args[]) {
    ArrayList<Tuple> tuples = Data.getTuples();

    for (Tuple tuple : tuples) {
        System.out.println(tuple.getSrc() + " -> " + tuple.getDest());
    }
}
}

class Tuple {
    private String src;
    private String dest;

    public Tuple(String src, String dest) {
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