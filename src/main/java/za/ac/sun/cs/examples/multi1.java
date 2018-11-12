package za.ac.sun.cs.examples;

import java.io.File;
import java.util.Scanner;

public class multi1 {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(new File(args[0]));
        String word = sc.nextLine();
        sc.close();
        int r = multi2.analyse(word);
    }
}