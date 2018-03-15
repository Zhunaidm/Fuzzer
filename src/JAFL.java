import java.math.BigInteger;

import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;

import java.security.Permission;

public class JAFL {

    private static int sizeOfString = 8;
    private static String generatedString = "";
    private final static String characters = "abcdefghijklmnopqrtsuvwxyz";
    private static Random random;

    public static void main(String[] args) {
        random = new Random();
        Queue<String> queue = new LinkedList<String>();
        SystemExitControl.forbidSystemExitCall();
        String testArr[] = new String[1];        
        StringBuilder builder = new StringBuilder();
        int change, branches = 0;
        String base;
        boolean abort = false;
        for (int i = 0; i < sizeOfString ; i++) {
            builder.append(characters.charAt(random.nextInt(characters.length()))); 
        }
        testArr[0] = builder.toString();
        base = builder.toString();
       // queue.add(base);
        System.out.println("Base: " + base);
        BigInteger big = new BigInteger(base.getBytes());
        System.out.println("Base Size: " + big.bitLength());
        System.out.println("Deadbeef: " + (new BigInteger("deadbeef".getBytes())).bitLength());
        while (!abort) {   

            
            try {
                DB_test.main(testArr);
            } catch (SystemExitControl.ExitTrappedException e) {
                System.out.println("Preventing abort...");
                abort = true;
            }
            if (Data.getBranches() == branches) {
                queue.add(testArr[0]);
                System.out.println(testArr[0]);
            }
            if (Data.getBranches() > branches) {
                branches = Data.getBranches();
                queue.clear();
                queue.add(testArr[0]);                
                System.out.println(testArr[0] +  " Branches: " + branches );
            }
            String temp = queue.remove();
            big = new BigInteger(temp.getBytes());                        
            if (queue.size() != 10) {
                queue.add(temp);
            }
            int type = random.nextInt(2);
           
            if (type == 0) {
                big = flipBits(big,  random);
                testArr[0] = new String(big.toByteArray());
            } else {
                
                big = flipBytes(big, random);
                testArr[0] = new String(big.toByteArray());
            }

            //System.out.println(testArr[0]);
        }





    }

    public static BigInteger flipBits(BigInteger big, Random rand) {
       int size = rand.nextInt(4) + 1;
        int randInt = rand.nextInt(big.bitLength());
        
        
        if ((size + randInt) > big.bitLength()) {
           size = size - ((size + randInt) - big.bitLength());
        }
        BigInteger big2 = big;
        for (int i = 0; i < size; i++) {
            big = big.flipBit(randInt + i);
        }
        if (big.bitLength() != big2.bitLength()) {
            return big2;
        }
        
        return big;


    }

    public static BigInteger flipBytes(BigInteger big, Random rand) {
        int size = rand.nextInt(3);

        switch(size) {
            case 0:
                size = 6;
                break;
            case 1:
                size = 16;
                break;
            case 2:
                size = 32;
                break;
            default:
                break;
        }

        
        int randInt = rand.nextInt(big.bitLength());
        
        
        if ((size + randInt) > big.bitLength()) {
           size = size - ((size + randInt) - big.bitLength());
        }
        BigInteger big2 = big;
        for (int i = 0; i < size; i++) {
            big = big.flipBit(randInt + i);
        }
        if (big.bitLength() != big2.bitLength()) {
            return big2;
        }
        
        return big;


    }
}

class SystemExitControl {
    @SuppressWarnings("serial")
    public static class ExitTrappedException extends SecurityException {
    }

    public static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkPermission(Permission permission) {
                if (permission.getName().contains("exitVM")) {
                    throw new ExitTrappedException();
                }
            }
        };
        System.setSecurityManager(securityManager);
    }

    public static void enableSystemExitCall() {
        System.setSecurityManager(null);
    }
}
