import java.util.*;
import java.math.*;

public class Test {
    public static void main(String[] args) {
        String src = "X", dest = "Y";

        Data.addTuple(src, dest);

        System.out.println(Data.containsTuple(src, dest));
    }
}
