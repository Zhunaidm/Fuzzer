import java.math.BigInteger;

import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;
import java.util.BitSet;

import java.nio.ByteBuffer;

import java.security.Permission;

public class JAFL {

    private static int ARITH_MAX = 35;
    private static int[] interesting_8 = {-128, -1, 0, 1, 16, 32, 64, 100, 127};
    private static int[] interesting_16 = {-32768, -128, 128, 255, 256, 512, 1000, 1024, 4096, 32767};
    private static int[] interesting_32 = {-2147483648, -100663046, -32769, 32768, 65535, 65536, 100663045, 2147483647};
    private static String base = "1234";
    private static boolean abort = false;
    private static Class<?> cls;

    private static Queue<String> queue;
    public static void main(String[] args) throws Exception {
        cls = Class.forName(args[0]);
        queue = new LinkedList<String>();
        SystemExitControl.forbidSystemExitCall();
        String testArr[] = new String[1];        
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

            replaceInteresting(testArr[0].getBytes());

            havoc(testArr[0].getBytes());
        }

    }







    public static void execProgram(String progName, String[] arguments) {
        try {
            Method meth = cls.getMethod("main", String[].class);
            Data.resetTuples();
            Data.resetLocal();
            meth.invoke(null, (Object) arguments);
        } catch (SystemExitControl.ExitTrappedException e) {
            System.out.println("Preventing abort...");
            abort = true;
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof SystemExitControl.ExitTrappedException) {

                System.out.println("Preventing abort...");
                abort = true;
            }

        } catch (Exception e) {}




    }
    // Remove a byte from the byte array.
    public static byte[] removeByte(byte[] base, int index) {
        int count = 0;
        byte[] newBase = new byte[base.length - 1];
        for (int i = 0; i < base.length; i++) {
            if (i == index) {
                continue;
            }
            newBase[count++] = base[i];
        }

        return newBase;
    }

    // Add a byte from the byte array.
    public static byte[] addByte(byte[] base, byte temp, int index) {
        int count = 0;
        byte[] newBase = new byte[base.length + 1];
        for (int i = 0; i < newBase.length; i++) {
            if (i == index) {
                newBase[i] = temp;
                continue;
            }
            newBase[i] = base[count++];
        }

        return newBase;
    }

    // Replace a byte from the byte array.
    public static byte[] replaceByte(byte[] base, byte temp, int index) {
        base[index] = temp;
        return base;
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

    public static void replaceInteresting(byte[] base) {
        // Setting 1 byte integers
        for (int i = 0; i < interesting_8.length; i++) {
            for (int j = 0; j < base.length; j++) {
                byte currentVal = base[j];
                base[j] = (byte) interesting_8[i];
                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                }

                base[j] = currentVal;
            }
        }

        // Setting 2 byte integers
        for (int i = 0; i < interesting_16.length; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                byte currentVal1 = base[j];
                byte currentVal2 = base[j+1];
                byte[] temp = ByteBuffer.allocate(4).putInt(interesting_16[i]).array(); 


                base[j] = temp[0];
                base[j+1] = temp[1];

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                }

                base[j] = temp[1];
                base[j+1] = temp[0];

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                }

                base[j] = currentVal1;
                base[j+1] = currentVal2;

            }
        }
        // Setting 4 byte integers
        for (int i = 0; i < interesting_32.length; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                byte currentVal1 = base[j];
                byte currentVal2 = base[j+1];
                byte currentVal3 = base[j+1];
                byte currentVal4 = base[j+1];
                byte[] temp = ByteBuffer.allocate(4).putInt(interesting_32[i]).array(); 


                base[j] = temp[0];
                base[j+1] = temp[1];
                base[j+2] = temp[2];
                base[j+3] = temp[3];

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                }

                base[j] = temp[3];
                base[j+1] = temp[2];
                base[j+2] = temp[1];
                base[j+3] = temp[0];

                execProgram(null, new String(base).split(" "));
                if (Data.getNew()) {
                    queue.add(new String(base));
                    Data.resetTuples();
                }

                base[j] = currentVal1;
                base[j+1] = currentVal2;
                base[j+2] = currentVal3;
                base[j+3] = currentVal4;

            }
        }

    }

    public static void havoc(byte[] base) {
        Random rand = new Random();
        int byteNum, tmp;
        byte[] temp, backup = new byte[base.length];
        System.arraycopy(base, 0, backup, 0, base.length);
        int runs = rand.nextInt(984) + 16;
        int tweaks = rand.nextInt(32) + 1;

        for (int i = 0; i < runs; i++) {
            for (int j = 0; j < tweaks; j++) {
                int option = rand.nextInt(15);
                switch (option) {
                    case 0:
                        // Flip a random bit somewhere.
                        byteNum = rand.nextInt(base.length);
                        base[byteNum] = (byte) (base[byteNum] ^ (1 << i));                    
                        break;
                    case 1:
                        // Set random byte to an interesting value.
                        byteNum = rand.nextInt(base.length);
                        base[byteNum] = (byte) interesting_8[rand.nextInt(interesting_8.length)];
                        break;
                    case 2:
                        // Set two bytes to interesting value.
                        if (base.length < 2) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length-1);
                        temp = ByteBuffer.allocate(4).putInt(interesting_16[rand.nextInt(interesting_16.length)]).array();
                        if (rand.nextInt(2) == 0) {
                            base[byteNum] = temp[0];
                            base[byteNum+1] = temp[1];
                        } else {
                            base[byteNum] = temp[1];
                            base[byteNum+1] = temp[0];
                        }
                        break;
                    case 3:
                        // Set four bytes to interesting value.
                        if (base.length < 4) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length-3);
                        temp = ByteBuffer.allocate(4).putInt(interesting_16[rand.nextInt(interesting_32.length)]).array();
                        if (rand.nextInt(2) == 0) {
                            base[byteNum] = temp[0];
                            base[byteNum+1] = temp[1];
                            base[byteNum+2] = temp[2];
                            base[byteNum+3] = temp[3];
                        } else {
                            base[byteNum] = temp[3];
                            base[byteNum+1] = temp[2];
                            base[byteNum+2] = temp[1];
                            base[byteNum+3] = temp[0];
                        }
                        break;
                    case 4:
                        // Randomly subtract from a byte.
                        byteNum = rand.nextInt(base.length);
                        base[byteNum] = (byte) (base[byteNum] - (rand.nextInt(ARITH_MAX) + 1));
                        break;
                    case 5:
                        // Randomly subtract from two bytes.
                        if (base.length < 2) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length - 1);
                        tmp = rand.nextInt(ARITH_MAX) + 1;
                        base[byteNum] = (byte) (base[byteNum] - tmp);
                        base[byteNum + 1] = (byte) (base[byteNum + 1] - tmp);
                        break;
                    case 6:
                        // Randomly subtract from four bytes.
                        if (base.length < 4) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length - 3);
                        tmp = rand.nextInt(ARITH_MAX) + 1;
                        base[byteNum] = (byte) (base[byteNum] - tmp);
                        base[byteNum + 1] = (byte) (base[byteNum + 1] - tmp);
                        base[byteNum + 2] = (byte) (base[byteNum + 2] - tmp);
                        base[byteNum + 2] = (byte) (base[byteNum + 3] - tmp);
                        break;
                    case 7:
                        // Randomly add to byte.
                        byteNum = rand.nextInt(base.length);
                        base[byteNum] = (byte) (base[byteNum] - (rand.nextInt(ARITH_MAX) + 1));
                        break;
                    case 8:
                        // Randomly add to two bytes.
                        if (base.length < 2) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length - 1);
                        tmp = rand.nextInt(ARITH_MAX) + 1;
                        base[byteNum] = (byte) (base[byteNum] + tmp);
                        base[byteNum + 1] = (byte) (base[byteNum + 1] + tmp);
                        break;
                    case 9:
                        // Randomly add to four bytes.
                        if (base.length < 4) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length - 3);
                        tmp = rand.nextInt(ARITH_MAX) + 1;
                        base[byteNum] = (byte) (base[byteNum] - tmp);
                        base[byteNum + 1] = (byte) (base[byteNum + 1] + tmp);
                        base[byteNum + 2] = (byte) (base[byteNum + 2] + tmp);
                        base[byteNum + 2] = (byte) (base[byteNum + 3] + tmp);
                        break;
                    case 10:
                        // Set a random byte to a random value.
                        byteNum = rand.nextInt(base.length);
                        tmp = rand.nextInt(255) + 1;
                        base[byteNum] = (byte) (base[byteNum] ^ tmp);
                        break;
                    case 11:
                    case 12:
                        // Delete bytes.
                        if (base.length < 2) {
                            continue;
                        }
                        byteNum = rand.nextInt(base.length);
                        base = removeByte(base, byteNum);
                        break;
                    case 13:
                        // Clone or insert bytes.
                        if (rand.nextInt(4) == 0) {
                            // Insert constant bytes.
                            byteNum = rand.nextInt(base.length);
                            base = addByte(base, (byte) (rand.nextInt(255) + 1) , rand.nextInt(base.length + 1));
                        } else {
                            // Clone Bytes.
                            byteNum = rand.nextInt(base.length);
                            base = addByte(base, base[byteNum], rand.nextInt(base.length + 1)); 

                        }
                        break;
                    case 14:
                        // Overwrite bytes                        
                        // Random chunk or fixed bytes.
                        if (rand.nextInt(4) == 0) {
                            // Fixed bytes.
                            byteNum = rand.nextInt(base.length);                            
                            base = replaceByte(base, (byte) (rand.nextInt(255) + 1) , byteNum);
                        } else {
                            // Random chunk.
                            byteNum = rand.nextInt(base.length);
                            tmp = rand.nextInt(base.length);
                            if (byteNum == tmp) {
                                continue;
                            }
                            base = replaceByte(base, base[tmp], byteNum); 

                        }
                        break;
                    default:
                        break;
                }
            }
            //System.out.println(new String(base));
            execProgram(null, new String(base).split(" "));
            if (Data.getNew()) {
                queue.add(new String(base));
                Data.resetTuples();
            }
            base = new byte[backup.length];
            System.arraycopy(backup, 0, base, 0, backup.length);
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
