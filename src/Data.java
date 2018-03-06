public class Data {

    static int noBranches;

    public Data () {
        noBranches = 0;
    }

    public static void incBranches() {
        noBranches++;
    }

    public static int getBranches() {
        return noBranches;
    }
}
