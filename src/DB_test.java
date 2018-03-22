import java.io.*;
import java.util.*;

public class DB_test
{
    public static void main(String[] args) {
       /* if (args.length != 1) {
            System.out.println("Please provide filename as argument.");
            System.exit(0);
        }*/

        String word = args[0];

       /* if (word.length() != 8) {
            System.out.println("File should only contain a single 8 letter word.");
            System.exit(0);
        }*/

        if (word.charAt(0) == 'd') {
            if (word.charAt(1) == 'e') {
                if (word.charAt(2) == 'a') {
                    if (word.charAt(3) == 'd') {
                        if (word.charAt(4) == 'b') {
                            if (word.charAt(5) == 'e') {
                                if (word.charAt(6) == 'e') {
                                    if (word.charAt(7) == 'f') {
                                        //System.out.println("The word: " + word);
                                        //System.out.println("Aborting ...");
                                        //System.exit(0);
                                        System.out.println("This code shouldn't be reached.");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}
