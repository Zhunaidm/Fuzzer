import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileOutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Random;
import java.util.Queue;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;

import java.nio.ByteBuffer;

import java.security.Permission;

public class JAFL {

    private static int ARITH_MAX = 35;
    private static int[] interesting_8 = { -128, -1, 0, 1, 16, 32, 64, 100, 127 };
    private static int[] interesting_16 = { -32768, -128, 128, 255, 256, 512, 1000, 1024, 4096, 32767 };
    private static int[] interesting_32 = { -2147483648, -100663046, -32769, 32768, 65535, 65536, 100663045,
            2147483647 };
    private static boolean abort = false;
    private static Class<?> cls;
    private static int paths = 0;
    private static int count = 0;
    private static String file = "";
    //private static PriorityQueue<Input> queue;
    private static Queue<Input> queue;
   // private static Comparator<Input> comparator = new InputComparator();
    private static double preTime;
    private static int runNumber = 0;

    private static boolean printCoverage = false;
    private static boolean printPaths = false;
    private static boolean printTime = false;
    private static boolean printQueueSize = false;
    private static boolean worstCaseMode = true;
    private static boolean printScore = true;

    public static void main(String[] args) throws Exception {
        if (worstCaseMode) {
            Data.setWorstCaseMode(true);
        }
        file = args[1];
        cls = Class.forName(args[0]);
        //queue = new PriorityQueue<Input>(10, comparator);
        queue = new LinkedList<Input>();
        Path path = Paths.get(file);
        byte[] base = Files.readAllBytes(path);
        SystemExitControl.forbidSystemExitCall();
        Data.resetAll();
        execProgram(base);
        Data.addInitialList(base);
        int score = 0;
        if (worstCaseMode) {
            score = Data.getWorstCaseScore(base);
        } else {
            score = Data.getLocalBucketSize();
        }
        queue.add(new Input(base, false, score));
        new Thread(new OutputGenerator()).start();

        BufferedReader br = new BufferedReader(new FileReader(".branches"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = (line.split(":"))[1];
            line = line.trim();
            count += Integer.parseInt(line);
        }
        br.close();
        preTime = System.currentTimeMillis();
        while (!abort) {
            runNumber++;            
            Data.resetTuples();
            Input input = queue.remove();
            byte[] basic = input.getData();
            System.out.println("Base: " + new String(basic));
            if (worstCaseMode) {
                System.out.println("Score: " + Data.getWorstCaseScore(basic));
            }
            queue.add(new Input(basic, true, input.getScore()));
            byte[] temp = Arrays.copyOf(basic, basic.length);
            if (!input.getEvaluated()) {
                // System.out.println("Performing Bit Flips...\n");
                flipBits(temp);

                // System.out.println("Performing Byte Flips...\n");
                flipBytes(temp);

                arithInc(temp);

                arithDec(temp);

                replaceInteresting(temp);
            }

            havoc(temp);
            
            
            if (runNumber % 5 == 0) {
                System.out.println("Before Size: " + queue.size());
                cullQueue();
                System.out.println("After Size: " + queue.size());
            }

        }

    }

    public static void execProgram(byte[] base) {
        try {
            Method meth = cls.getMethod("main", String[].class);
            Data.resetTuples();
            Data.resetLocal();
            Data.setCurrentInput(base);
            FileOutputStream fos = new FileOutputStream(".temp");
            fos.write(base);
            fos.close();
            meth.invoke(null, (Object) (new String[] { ".temp" }));

        } catch (SystemExitControl.ExitTrappedException e) {
            System.out.println("Preventing abort...");
           // abort = true;
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof SystemExitControl.ExitTrappedException) {

                System.out.println("Preventing abort...");
                //abort = true;
            }

        } catch (Exception e) {
        }

    }

    public static void cullQueue() {
        ArrayList<Input> list = new ArrayList<Input>(queue);
        ArrayList<Tuple> tuples = Data.getTuples();
        Set<Input> newInputs = new HashSet<Input>();
        Set<Input> worstInputs = new HashSet<Input>();
        Set<Tuple> evaluatedTuples = new HashSet<Tuple>();
        boolean evaluated = false;
        int score = 0;
        int maxScore = 0;
        byte[] winningInput = null;

        for (Tuple tuple : tuples) {
            score = 0;
            if (!evaluatedTuples.contains(tuple)) {
                for (Input input: list) {
                    byte[] inputArr = input.getData();
                    ArrayList<Tuple> inputList = Data.getInputList(inputArr);
                    if (inputList == null && list.size() != 1) {
                        continue;
                    } else if (inputList == null && list.size() == 1) {
                        newInputs.add(input);
                        break;
                    }
                    if (worstCaseMode) {
                        if (inputList.contains(tuple) && (Data.getWorstCaseScore(inputArr) > score)) {
                            score = Data.getWorstCaseScore(inputArr);
                            winningInput = inputArr;
                            evaluated = input.getEvaluated();
                            worstInputs.add(input);
                            if (maxScore < score) {
                                maxScore = score;
                            }
                        }
                    } else {
                        if (inputList.contains(tuple) && (inputList.size() > score)) {
                            score = inputList.size();
                            winningInput = inputArr;
                            evaluated = input.getEvaluated();
                            
                       }
                    }
                }
                evaluatedTuples.addAll(Data.getInputList(winningInput));
                newInputs.add(new Input(winningInput, evaluated, score));
            }
        }
        if (worstCaseMode) {
            for (Input input: list) {
                if (!worstInputs.contains(input) && Data.getWorstCaseScore(input.getData()) > maxScore ) {
                    worstInputs.add(input);
                    newInputs.add(new Input(input.getData(), input.getEvaluated(), Data.getWorstCaseScore(input.getData())));
                }
            }
        }
        queue = new LinkedList<Input>(newInputs);
        
        
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
        // System.out.println("Single bit flip...");
        // 1 Walking bit.
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i));
            }
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
            }
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i));
            }
        }
        // 2 Walking bits.
        // System.out.println("2 bit flips...");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i));
                base[j] = (byte) (base[j] ^ (1 << (i + 1)));
            }
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
            }
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i));
                base[j] = (byte) (base[j] ^ (1 << (i + 1)));
            }
        }
        // 4 Walking bits.
        // System.out.println("4 bit flips...");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] ^ (1 << i));
                base[j] = (byte) (base[j] ^ (1 << (i + 1)));
                base[j] = (byte) (base[j] ^ (1 << (i + 2)));
                base[j] = (byte) (base[j] ^ (1 << (i + 3)));
            }
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
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
        // System.out.println("Single byte flip...");
        for (int j = 0; j < base.length; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
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
            base[j + 1] = (byte) (base[j + 1] ^ 0xFF);
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
            }
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j + 1] = (byte) (base[j + 1] ^ 0xFF);

        }

        // 4 Walking bytes.
        // System.out.println("4 byte flips...");
        if (base.length < 4) {
            return;
        }

        for (int j = 0; j < base.length - 3; j++) {
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j + 1] = (byte) (base[j + 1] ^ 0xFF);
            base[j + 2] = (byte) (base[j + 2] ^ 0xFF);
            base[j + 3] = (byte) (base[j + 3] ^ 0xFF);
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
            }
            base[j] = (byte) (base[j] ^ 0xFF);
            base[j + 1] = (byte) (base[j + 1] ^ 0xFF);
            base[j + 2] = (byte) (base[j + 2] ^ 0xFF);
            base[j + 3] = (byte) (base[j + 3] ^ 0xFF);

        }

    }

    public static void arithInc(byte[] base) throws Exception {

        // 1 Byte increment
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length; j++) {
                base[j] = (byte) (base[j] + i);

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }
                base[j] = (byte) (base[j] - i);
            }
        }
        // 2 Byte increment
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                base[j] = (byte) (base[j] + i);
                base[j + 1] = (byte) (base[j + 1] + i);

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }
                base[j] = (byte) (base[j] - i);
                base[j + 1] = (byte) (base[j + 1] - i);
            }
        }
        // 4 Byte increment
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                base[j] = (byte) (base[j] + i);
                base[j + 1] = (byte) (base[j + 1] + i);
                base[j + 2] = (byte) (base[j + 2] + i);
                base[j + 3] = (byte) (base[j + 3] + i);

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }
                base[j] = (byte) (base[j] - i);
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

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }
                base[j] = (byte) (base[j] + i);
            }
        }
        // 2 Byte decrement
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                base[j] = (byte) (base[j] - i);
                base[j + 1] = (byte) (base[j + 1] - i);

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }
                base[j] = (byte) (base[j] + i);
                base[j + 1] = (byte) (base[j + 1] + i);
            }
        }
        // 4 Byte decrement
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                base[j] = (byte) (base[j] - i);
                base[j + 1] = (byte) (base[j + 1] - i);
                base[j + 2] = (byte) (base[j + 2] - i);
                base[j + 3] = (byte) (base[j + 3] - i);

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }
                base[j] = (byte) (base[j] + i);
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
                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }

                base[j] = currentVal;
            }
        }

        // Setting 2 byte integers
        for (int i = 0; i < interesting_16.length; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                byte currentVal1 = base[j];
                byte currentVal2 = base[j + 1];
                byte[] temp = ByteBuffer.allocate(4).putInt(interesting_16[i]).array();

                base[j] = temp[0];
                base[j + 1] = temp[1];

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }

                base[j] = temp[1];
                base[j + 1] = temp[0];

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }

                base[j] = currentVal1;
                base[j + 1] = currentVal2;

            }
        }
        // Setting 4 byte integers
        for (int i = 0; i < interesting_32.length; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                byte currentVal1 = base[j];
                byte currentVal2 = base[j + 1];
                byte currentVal3 = base[j + 1];
                byte currentVal4 = base[j + 1];
                byte[] temp = ByteBuffer.allocate(4).putInt(interesting_32[i]).array();

                base[j] = temp[0];
                base[j + 1] = temp[1];
                base[j + 2] = temp[2];
                base[j + 3] = temp[3];

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }

                base[j] = temp[3];
                base[j + 1] = temp[2];
                base[j + 2] = temp[1];
                base[j + 3] = temp[0];

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                    Data.resetTuples();
                    paths++;
                }

                base[j] = currentVal1;
                base[j + 1] = currentVal2;
                base[j + 2] = currentVal3;
                base[j + 3] = currentVal4;

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
                    byteNum = rand.nextInt(base.length - 1);
                    temp = ByteBuffer.allocate(4).putInt(interesting_16[rand.nextInt(interesting_16.length)]).array();
                    if (rand.nextInt(2) == 0) {
                        base[byteNum] = temp[0];
                        base[byteNum + 1] = temp[1];
                    } else {
                        base[byteNum] = temp[1];
                        base[byteNum + 1] = temp[0];
                    }
                    break;
                case 3:
                    // Set four bytes to interesting value.
                    if (base.length < 4) {
                        continue;
                    }
                    byteNum = rand.nextInt(base.length - 3);
                    temp = ByteBuffer.allocate(4).putInt(interesting_16[rand.nextInt(interesting_32.length)]).array();
                    if (rand.nextInt(2) == 0) {
                        base[byteNum] = temp[0];
                        base[byteNum + 1] = temp[1];
                        base[byteNum + 2] = temp[2];
                        base[byteNum + 3] = temp[3];
                    } else {
                        base[byteNum] = temp[3];
                        base[byteNum + 1] = temp[2];
                        base[byteNum + 2] = temp[1];
                        base[byteNum + 3] = temp[0];
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
                        base = addByte(base, (byte) (rand.nextInt(255) + 1), rand.nextInt(base.length + 1));

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
                        base = replaceByte(base, (byte) (rand.nextInt(255) + 1), byteNum);
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
            // System.out.println(base);
            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score));
                Data.resetTuples();
                paths++;
            }
            base = new byte[backup.length];
            System.arraycopy(backup, 0, base, 0, backup.length);
        }
    }

    private static class OutputGenerator implements Runnable {

        public void run() {
            while (true) {
                if (printCoverage) {
                    System.out.println("Coverage: " + ((double) Data.getSize() / (double) count * 100.0));
                }
                if (printPaths) {
                    System.out.println("No Paths: " + paths);
                }
                if (printTime) {
                    System.out.println("Running Time: " + (System.currentTimeMillis() - preTime) / 1000 + " seconds");
                }
                if (printQueueSize) {
                    System.out.println("Queue Size: " + queue.size());
                }

                try {

                    Thread.sleep(2000);
                } catch (Exception e) {
                    // Do Nothing
                }
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

class Input {
    private byte[] data;
    private boolean evaluated;
    private int score;

    public Input(byte[] data, boolean evaluated, int score) {
        this.data = data;
        this.evaluated = evaluated;
        this.score = score;
    }

    public byte[] getData() {
        return this.data;
    }

    public boolean getEvaluated() {
        return this.evaluated;
    }

    public int getScore() {
        return this.score;
    }
}

class InputComparator implements Comparator<Input> {
    @Override
    public int compare(Input x, Input y) {

        if (x.getScore() > y.getScore()) {
            return -1;
        }
        if (x.getScore() < y.getScore()) {
            return 1;
        }
        return 0;
    }
}
class Tuple {
    private String src;
    private String dest;

    public Tuple(String src, String dest) {
        this.src = src;
        this.dest = dest;
    }

    public String getSrc() {
        return this.src;
    }

    public String getDest() {
        return this.dest;
    }

    @Override
    public int hashCode() {
        return src.hashCode() + dest.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Tuple tuple = (Tuple) obj;
        if (this.src.equals(tuple.getSrc()) && this.dest.equals(tuple.getDest())) {
            return true;
        } else {
            return false;
        }

    }
}
