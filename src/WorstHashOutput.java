import java.util.Hashtable;                                          
                                                                     
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;    

public class WorstHashOutput {	
	private static final int TABLE_SIZE = 4001;                      
    private static entry_t[] hashtable;                              
    static int count = 0;                                            
                                                                     
    public static int compute_hash(String str) {                     
            long hash = 0;
            for (int i = 0; i < str.length(); i++) {
                int v = str.charAt(i);
                System.out.print(v+".");
                hash = 31 * hash + v;
                System.out.println(hash);
            }
            int r = (int)(hash % TABLE_SIZE);
            if (r < 0) {
                r += TABLE_SIZE;
            }
            System.out.println("="+r);
            return r;
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
	
	
	public static void main(String[] args) throws FileNotFoundException {
		hashtable = new entry_t[TABLE_SIZE];                     
        count = 0;                                               
        //String filename = args[0];                               
                                                                 
        String result = "";                                      
        Scanner sc = new Scanner(new File(args[0]));             
        //"t <81>v ^?@t <80>!^?@t <80>!t t^Rn t t t t t t t t t" 
        //String w = "t Qv ^?@t P!^?@t P!t t^Rn t t t t t t t t t";  
        //w = "t t t t i nv t X t 1 9 t l t l t t t t t";
        
        /*
         * 116.104.101.=687
103.107.91.101.107.=687
98.100.97.119.110.=687
111.120.=558
106.117.109.98.112.101.=990
84.91.74.87.=687
116.104.101.=687
108.97.122.121.20.88.99.91.=687
 the gk[ek bdawn ox jumbpe T[JW the lazyXc[ -> Final count: 14
 
 
 116.104.101.32.
 113.117.105.101.107.32.
 98.100.97.119.110.32.
 102.111.120.102.106.117.109.112.115.32.
 84.91.74.87.32.
 116.104.101.32.
 108.97.122.121.20.88.99.91.
 
 15
 Best innputs [116.104.111.101.32.103.107.91.101.107.32.98.100.97.119.110.32.102.111.120.32.106.117.109.112.115.32.84.91.74.87.32.116.104.101.32.108.97.122.121.20.88.99.91.] = 153
   
 21
 [116.104.101.32.113.117.105.90.32.98.100.97.119.110.32.102.111.120.32.106.114.109.112.115.60.32.84.91.74.87.32.116.104.111.101.32.108.97.122.121.20.88.99.91.] = 171  
         *
         */
        
        
       // w = "the gk[ek bdawn ox jumbpe T[JW the lazyXc[";
        //char[] w1 = new char[2]; w1[0] = 81; w += " "+w1[0]+"v"; 
        //char[] ins = {116,104,101,32,113,117,105,90,32,98,100,97,119,110,32,102,111,120,32,106,114,109,
        	//	112,115,60,32,84,91,74,87,32,116,104,111,101,32,108,97,122,121,20,88,99,91};
        //char[] ins = {116,104,101,58,32,127,117,96,104,32,98,114,111,68,127,118,32,102,117,96,32,106,117,109,120,123,32,111,0,0,25,32,0,116,104,101,58,32,108,97,122,113,40,119,111,103};
        //char[] ins = {0,88,29,88,9,48,52,62,54,88,26,10,0,0,0,88,30,23,12,88,28,23,28,48,11,88,32,0,0,88,10,88,12,88,29,88,28,88,28,88,10,0,88,28,88,31};
        //char[] ins = {0,0,0,113,117,78,72,107,32,98,98,95,119,110,32,102,117,120,9,106,117,109,112,115,32,111,49,101,114,32,90,104,101,27,108,65,122,121,32,100,111,103,104};
        //char[] ins = {0,0,0,113,117,78,72,107,32,109,97,94,119,110,32,102,117,120,9,106,117,109,112,115,32,111,49,101,114,32,90,104,101,27,108,65,122,121,32,100,111,103,104};
        //w = new String(ins);
        //System.out.println(w);
        //sc = new Scanner(w);      
        //try {
			//sc = new Scanner("t �v @t �!t tn t t t t t t t t t t t t");
		//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}  
        while (sc.hasNext()) {                                   
            String word = sc.next();                             
            result += " " + word;                                
            add_word(word);                                      
        }                                                        
        System.out.println(result + " -> Final count: " + count);
        sc.close();		

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
