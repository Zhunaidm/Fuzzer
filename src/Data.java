public class Data {

    private static int noBranches;

    public Data () {
        noBranches = 0;
    }

    public static void incBranches() {
        noBranches++;
    }

    public static void resetBranches() {
        noBranches = 0;
    }

    public static int getBranches() {
        return noBranches;
    }
}
