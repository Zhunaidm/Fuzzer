import java.math.BigInteger;

import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;
import java.util.BitSet;

import java.security.Permission;

public class JAFL {

    private static String base = "hello";
    private static boolean abort = false;

    public static void main(String[] args) {

        Random random = new Random();
        Queue<String> queue = new LinkedList<String>();
        SystemExitControl.forbidSystemExitCall();
        String testArr[] = new String[1];        
        int change, branches = 0;
        boolean abort = false;
        testArr[0] = base;

        System.out.println("Performing Bit Flips...\n");
        flipBits(base.getBytes());

        System.out.println("Performing Byte Flips...\n");
        flipBytes(base.getBytes());

    }







    public static void execProgram(String progName, String[] arguments) {
        try {
            DB_test.main(arguments);
        } catch (SystemExitControl.ExitTrappedException e) {
            System.out.println("Preventing abort...");
            abort = true;
        }




    }

    public static void flipBits(byte[] base) {
        System.out.println("Single bit flip...");
        // 1 Walking bit.
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
            }
            System.out.println(new String(base));
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
            }
        }
        // 2 Walking bits.
        System.out.println("2 bit flips...");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
            }
            System.out.println(new String(base));
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
            }
        }
        // 4 Walking bits. 
        System.out.println("4 bit flips...");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 2))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 3))); 
            }
            System.out.println(new String(base));
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 2))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 3))); 
            }
        }




    }

    public static void flipBytes(byte[] base) {
        // Walking byte.
        System.out.println("Single byte flip...");
        for (int j = 0; j < base.length; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            System.out.println(new String(base));
            base[j] = (byte) (base[j] ^ 0xFF); 

        }
        // 2 Walking bytes.
        System.out.println("2 byte flips...");
        if (base.length < 2) {
            return;
        }
        for (int j = 0; j < base.length - 1; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j+1] = (byte) (base[j+1] ^ 0xFF);
            System.out.println(new String(base));
            base[j] = (byte) (base[j] ^ 0xFF); 
            base[j+1] = (byte) (base[j+1] ^ 0xFF);

        }
       
        // 4 Walking bytes. 
        System.out.println("4 byte flips...");
        if (base.length < 4) {
            return;
        }

        for (int j = 0; j < base.length - 3; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j+1] = (byte) (base[j+1] ^ 0xFF);
            base[j+2] = (byte) (base[j+2] ^ 0xFF);
            base[j+3] = (byte) (base[j+3] ^ 0xFF);
            System.out.println(new String(base));
            base[j] = (byte) (base[j] ^ 0xFF); 
            base[j+1] = (byte) (base[j+1] ^ 0xFF);
            base[j+2] = (byte) (base[j+2] ^ 0xFF);
            base[j+3] = (byte) (base[j+3] ^ 0xFF);

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
