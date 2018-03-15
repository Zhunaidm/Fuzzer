import java.math.BigInteger;

import java.util.Random;

import java.security.Permission;

public class JAFL {

    private static int sizeOfString = 8;
    private static String generatedString = "";
    private final static String characters = "abcdefghijklmnopqrtsuvwxyz";
    private static Random random;

    public static void main(String[] args) {
        random = new Random();
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
        BigInteger big;
        while (!abort) {            
            System.out.println(base);
            big = new BigInteger(base.getBytes());
            for (int i = 0; i < random.nextInt(4); i++) {
                int randInt = random.nextInt(big.bitLength());
                big = big.flipBit(randInt);
            }
            try {
                DB_test.main(testArr);
            } catch (SystemExitControl.ExitTrappedException e) {
                System.out.println("Preventing abort...");
                abort = true;
            }
            if (Data.getBranches() > branches) {
                base =  new String(big.toByteArray());
                branches = Data.getBranches();
            }
                        
            testArr[0] = new String(big.toByteArray());
            
        }

       



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
