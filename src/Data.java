import java.util.HashMap;
import java.util.Map;

public class Data {

    private static Map<Tuple, String> tuples = new HashMap<Tuple, String>();
    private static Map<Tuple, bucket> buckets = new HashMap<Tuple, bucket>();
    private static Map<Tuple, Integer> localBuckets;
    private static String prevBranch = "Source";
    private static boolean newTuple = false;


    public static void resetAll() {
        tuples = null;
        tuples = new HashMap<Tuple, String>();
        buckets = null;
        buckets = new HashMap<Tuple, bucket>();
        prevBranch = "Source";
        newTuple = false;
    }

    public static void resetTuples() {
        prevBranch = "Source";
        newTuple = false;

    }

    public static void resetLocal() {
        localBuckets = null;
        localBuckets = new HashMap<Tuple, Integer>();
    }

    public static void addTuple(String src, String dest) {
        Tuple tuple = new Tuple(src, dest);
        if (!tuples.containsKey(tuple) && !src.equals(dest)) {        
            tuples.put(tuple, "");
            buckets.put(tuple, bucket.ONE);
            localBuckets.put(tuple, 1);
            newTuple = true;
        } else if (!buckets.containsKey(tuple) && !src.equals(dest)) {
            localBuckets.put(tuple, 1);
        }
    }

    public static void incrementBucketCount(Tuple tuple) {
        localBuckets.put(tuple, localBuckets.get(tuple) + 1);
    }

    public static bucket getBucketValue(Tuple tuple) {
        int count = localBuckets.get(tuple);
        bucket type = null;
       
        if (count == 1) {
            type = bucket.ONE;
        } else if (count == 2) {
            type = bucket.TWO;
        } else if (count == 3) {
            type = bucket.THREE;
        } else if (count >= 4 && count <= 7) {
            type = bucket.FOUR;
        } else if (count >= 8 && count <= 15) {
            type = bucket.FIVE;
        } else if (count >= 16 && count <= 31) {
            type = bucket.SIX;
        } else if (count >= 32 && count <= 127) {
            type = bucket.SEVEN;
        } else if (count >= 128) {
            type = bucket.EIGHT;
        }

        return type;
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

    public static boolean getNew() {
        return newTuple;
    }


    public enum bucket {
        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT;
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

