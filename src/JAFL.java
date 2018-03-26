import java.math.BigInteger;

import java.lang.Class;
import java.lang.reflect.Method;

import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;
import java.util.BitSet;

import java.security.Permission;

public class JAFL {

    private static int ARITH_MAX = 35;
    private static String base = "helloooo";
    private static boolean abort = false;
    private static Class<?> cls;

    private static Queue<String> queue;
    public static void main(String[] args) throws Exception {
        cls = Class.forName("DB_test");
        queue = new LinkedList<String>();
        SystemExitControl.forbidSystemExitCall();
        String testArr[] = new String[1];        
        boolean abort = false;
        Data.resetAll();
        int count = 0;
        queue.add(base);
        execProgram(null, new String(base).split(" "));
        while(!abort) {
            Data.resetTuples();
            count++;
            testArr[0] = queue.remove();
            System.out.println("Base: " + testArr[0]);
            queue.add(testArr[0]);

            //    System.out.println("Performing Bit Flips...\n");
            flipBits(testArr[0].getBytes());

            //  System.out.println("Performing Byte Flips...\n");
             flipBytes(testArr[0].getBytes());

            arithInc(testArr[0].getBytes());

            arithDec(testArr[0].getBytes());
        }

    }







    public static void execProgram(String progName, String[] arguments) {
        try {
            Method meth = cls.getMethod("main", String[].class);
            Data.resetTuples();
            meth.invoke(null, (Object) arguments);
        } catch (SystemExitControl.ExitTrappedException e) {
            System.out.println("Found: " + arguments[0]);
            System.out.println("Preventing abort...");
            abort = true;
        } catch (Exception e) {}




    }

    public static void flipBits(byte[] base) throws Exception {
        //  System.out.println("Single bit flip...");
        // 1 Walking bit.
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
            }
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            } 
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
            }
        }
        // 2 Walking bits.
        //System.out.println("2 bit flips...");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
            }
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            }
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
            }
        }
        // 4 Walking bits. 
        //  System.out.println("4 bit flips...");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 2))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 3))); 
            }
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            }
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i)); 
                base[j] = (byte) (base[j] ^ (1 << (i + 1))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 2))); 
                base[j] = (byte) (base[j] ^ (1 << (i + 3))); 
            }
        }




    }

    public static void flipBytes(byte[] base) throws Exception {
        // Walking byte.
        //System.out.println("Single byte flip...");
        for (int j = 0; j < base.length; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            }
            base[j] = (byte) (base[j] ^ 0xFF); 

        }
        // 2 Walking bytes.
        // System.out.println("2 byte flips...");
        if (base.length < 2) {
            return;
        }
        for (int j = 0; j < base.length - 1; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j+1] = (byte) (base[j+1] ^ 0xFF);
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            }
            base[j] = (byte) (base[j] ^ 0xFF); 
            base[j+1] = (byte) (base[j+1] ^ 0xFF);

        }

        // 4 Walking bytes. 
        //System.out.println("4 byte flips...");
        if (base.length < 4) {
            return;
        }

        for (int j = 0; j < base.length - 3; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j+1] = (byte) (base[j+1] ^ 0xFF);
            base[j+2] = (byte) (base[j+2] ^ 0xFF);
            base[j+3] = (byte) (base[j+3] ^ 0xFF);
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            }
            base[j] = (byte) (base[j] ^ 0xFF); 
            base[j+1] = (byte) (base[j+1] ^ 0xFF);
            base[j+2] = (byte) (base[j+2] ^ 0xFF);
            base[j+3] = (byte) (base[j+3] ^ 0xFF);

        }


    }

    public static void arithInc(byte[] base) throws Exception {

        // 1 Byte increment 
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] + i); 

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                } 
                base[j] = (byte) (base[j] -  i);
            }
        }
        // 2 Byte increment 
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                base[j] = (byte) (base[j] + i); 
                base[j + 1] = (byte) (base[j+1] + i);

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                } 
                base[j] = (byte) (base[j] -  i);
                base[j + 1] = (byte) (base[j + 1] - i);
            }
        }
        // 4 Byte increment 
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                base[j] = (byte) (base[j] + i); 
                base[j + 1] = (byte) (base[j+1] + i);
                base[j + 2] = (byte) (base[j+2] + i);
                base[j + 3] = (byte) (base[j+3] + i);

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                } 
                base[j] = (byte) (base[j] -  i);
                base[j + 1] = (byte) (base[j + 1] - i);
                base[j + 2] = (byte) (base[j + 2] - i);
                base[j + 3] = (byte) (base[j + 3] - i);
            }
        }




    }

    public static void arithDec(byte[] base) throws Exception {
        // 1 Byte deccrement 
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] - i); 

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                } 
                base[j] = (byte) (base[j] +  i);
            }
        }
        // 2 Byte decrement 
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                base[j] = (byte) (base[j] - i); 
                base[j + 1] = (byte) (base[j+1] - i);

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                } 
                base[j] = (byte) (base[j] +  i);
                base[j + 1] = (byte) (base[j + 1] + i);
            }
        }
        // 4 Byte decrement 
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                base[j] = (byte) (base[j] - i); 
                base[j + 1] = (byte) (base[j+1] - i);
                base[j + 2] = (byte) (base[j+2] - i);
                base[j + 3] = (byte) (base[j+3] - i);

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                } 
                base[j] = (byte) (base[j] +  i);
                base[j + 1] = (byte) (base[j + 1] + i);
                base[j + 2] = (byte) (base[j + 2] + i);
                base[j + 3] = (byte) (base[j + 3] + i);
            }
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
