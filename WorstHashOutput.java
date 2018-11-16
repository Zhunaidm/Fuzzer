
import java.util.Hashtable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class WorstHashOutput {
    private static final int TABLE_SIZE = 50;
    private static entry_t[] hashtable;
    static int count = 0;
    static int count2 = 0;


    public static int compute_hash(String str) {
        long hash = 0;
        for (int i = 0; i < str.length(); i++) {
            int v = str.charAt(i);
            hash = 31 * hash + v;
        }
        int r = (int) (hash % TABLE_SIZE);
        if (r < 0) {
            r += TABLE_SIZE;
        }
        System.out.println( str+ " : " + r);
        return r;
    }

    public static void add_word(String word) {
        int bucket = compute_hash(word);
        entry_t entry = hashtable[bucket];

        while (entry != null) {
            if (entry.getKey().compareTo(word) == 0) {
                entry.incValue();
                count2++;
                return;
            } else {
                count++;
                entry = entry.getNext();
            }
        }

        hashtable[bucket] = new entry_t(word, 1, hashtable[bucket]);

    }

    public static void main(String[] args) throws FileNotFoundException {
        hashtable = new entry_t[TABLE_SIZE];
        count = 0;
        String result = "";
        Scanner sc = new Scanner(new File(args[0]));     
        while (sc.hasNext()) {
           String word = sc.next();
            result += " " + word;
            add_word(word);
        }
        System.out.println(result + " -> Final count: " + "Traversals: " + count + " Hits: " + count2);

        sc.close();

    }

}

class entry_t {
    private String key;
    private int value;
    private entry_t next;

    public entry_t(String key, int value, entry_t n) {
        this.key = key;
        this.value = value;
        this.next = n;
    }

    public String getKey() {
        return this.key;
    }

    public void incValue() {
        this.value++;
    }

    public entry_t getNext() {
        return next;
    }
}