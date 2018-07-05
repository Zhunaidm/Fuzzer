import java.io.*;
import java.util.*;

public class test_worst
{
    public static void main(String[] args) throws Exception {
      
        Scanner sc = new Scanner(new File(args[0]));
        int value = sc.nextInt();
        sc.close();

        if (value > 0 && value <= 10) {
            for (int i = 0 ; i < 10 ; i++);
        } else if (value > 10 && value <= 20) {
            for (int i = 0 ; i < 20 ; i++);
        } else if (value > 20 && value <= 30) {
            for (int i = 0 ; i < 30 ; i++);
        } else if (value > 30 && value <= 40) {
            for (int i = 0 ; i < 40 ; i++);
        } else if (value > 40 && value <= 50) {
            for (int i = 0 ; i < 50 ; i++);
        } 

       
              


    }
}
