import java.util.Hashtable;

import java.io.File;
import java.util.Scanner;

public class WorstHash {

    private static final int TABLE_SIZE = 4001;
    private static entry_t[] hashtable;
    static int count = 0;
    static int maxWords = 15;
    static int currentWords;
    
    public static int compute_hash(String str) {
        long hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = 31 * hash + (int)str.charAt(i);
        }

        int result = (int)(hash % TABLE_SIZE);
        if (result < 0) {
            result += TABLE_SIZE;
        }

        return result;
    }
    public static void add_word(String word) {                        
        int bucket = compute_hash(word);                             
        entry_t entry = hashtable[bucket];
                                                                     
        while (entry != null) {                           
            count++;                                                 
            if (entry.getKey().compareTo(word) == 0) {    
                entry.incValue();                         
                return;                                              
            } else {                                                 
                entry = entry.getNext();                                            
            }                                                        
        }                                                            
                                                                     
        hashtable[bucket] = new entry_t(word, 1, hashtable[bucket]);                    
                                                                     
    }      
    public static void main(String args[]) throws Exception {
            hashtable = new entry_t[TABLE_SIZE];
            currentWords = 0;
            count = 0;
            String filename = args[0];

            Scanner sc = new Scanner(new File(args[0]));
            while (sc.hasNext() && (currentWords < maxWords)) {
                currentWords++;
                add_word(sc.next());
            }
            if (count > 38)
            System.out.println("Final count: " + count);
    }
}
class entry_t{                                                       
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
