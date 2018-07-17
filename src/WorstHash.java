import java.io.File;
import java.util.Scanner;

public class WorstHash {

    private static final int TABLE_SIZE = 1001;
    private static entry_t[] hashtable = new entry_t[TABLE_SIZE];
    
    public static int compute_hash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = 31 * hash + (int)str.charAt(i);
        }

        return hash % TABLE_SIZE;
    }

   public static void add_word(String word) {
        int bucket = compute_hash(word);
        int index = bucket;

        while (hashtable[index] != null) {
            if (hashtable[index].getKey().compareTo(word) == 0) {
                hashtable[index].incValue();
                return;
            } else {
                index++;
            }
        }
        
        hashtable[bucket] = new entry_t(word, 1);

    }
    public static void main(String args[]) throws Exception {
            String filename = args[0];

            Scanner sc = new Scanner(new File(args[0]));
            while (sc.hasNext()) {
                add_word(sc.next());
            }
    }
}

class entry_t{
    private String key;
    private int value;
    public entry_t(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void incValue() {
        this.value++;
    }
}