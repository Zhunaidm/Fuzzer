package za.ac.sun.cs.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

import java.util.Properties;

import java.lang.Class;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.Thread;

import java.util.Random;
import java.util.Queue;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Set;
import java.util.Collections;

import za.ac.sun.cs.coastal.ConfigurationBuilder;
import za.ac.sun.cs.coastal.reporting.ReporterManager;
import za.ac.sun.cs.coastal.strategy.JAFLStrategy;

import za.ac.sun.cs.coastal.COASTAL;
import za.ac.sun.cs.coastal.Configuration;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Properties;

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
    private static String className;
    private static String initialClassName;
    private static int noWorstInputs = 0;
    private static int paths = 0;
    private static int totalPaths = 0;
    private static String file = "";
    private static Queue<Input> queue;
    private static double preTime;
    private static int runNumber = 0;
    private static int runs = 0;
    private static boolean debug = false;
    private static boolean coastalDebug = false;
    private static boolean worstCaseMode = false;
    private static boolean concolicMode = false;
    private static boolean fixedInput = false;
    private static boolean padInputs = false;
    private static int currentOperation = 0;
    private static ByteSet crashingInputs = new ByteSet();
    private static Properties prop = null;
    private static int concolicIterations = 0;
    private static String[] initialClassNames;


    public static void main(String[] args) throws Exception {


        parseProperties(args[0]);
        className = initialClassName + "_instrumented";
        // Instrument Code

        Instrumenter.instrument(initialClassNames);
        (new Thread(new FuzzUI())).start();

        cls = Class.forName(className);
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
        queue.add(new Input(base, false, score, false));
        BufferedReader br = new BufferedReader(new FileReader(".branches"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = (line.split(":"))[1];
            line = line.trim();
            totalPaths += Integer.parseInt(line);
        }
        br.close();
        preTime = System.currentTimeMillis();
        while (!abort) {
            runNumber++;
            Data.resetTuples();
            Input input = queue.remove();
            byte[] basic = input.getData();
            if (debug) {
                System.out.println("Base: " + new String(basic));
                System.out.println("ASCII: " + input);
            }
            queue.add(new Input(basic, true, input.getScore(), input.getCoastalEvaluated()));

            byte[] temp = Arrays.copyOf(basic, basic.length);
            if (!input.getEvaluated()) {
                currentOperation = 0;
                flipBits(temp);

                currentOperation = 1;
                flipBytes(temp);

                currentOperation = 2;
                arithInc(temp);

                currentOperation = 3;
                arithDec(temp);

                currentOperation = 4;
                replaceInteresting(temp);
            }
            currentOperation = 5;
            havoc(temp);

            if (runNumber % 5 == 0) {
                cullQueue();
            }

            // Run Coastal
            if (concolicMode && runNumber != 0 && (runNumber % concolicIterations) == 0) {
                // Create a temporary new queue
                ArrayList<Input> newInputs = new ArrayList<Input>();
                if (coastalDebug)
                    System.out.println("Starting coastal...");
                for (Input qInput : queue) {
                    newInputs.add(new Input(qInput.getData(), qInput.getEvaluated(), qInput.getScore(), true));
                    if (qInput.getCoastalEvaluated()) {
                        continue;
                    }

                    byte[] fuzzInput = qInput.getData();
                    byte[] send = padBytes(fuzzInput, 5, true, false);
                    // Redirect coastal logging.
                    PrintStream original = System.out;
                    System.setOut(new PrintStream(new OutputStream() {
                        public void write(int b) {
                            // DO NOTHING
                        }
                    }));

                    runCoastal(send);
                    System.setOut(original);
                    if (coastalDebug) {
                        System.out.println("COASTAL RAN SUCCESSFULLY");
                        System.out.println("Base Input: " + new String(fuzzInput));

                        System.out.println();
                        System.out.println("--------------------------------------------");
                    }
                    ArrayList<Byte[]> coastalInputs = JAFLStrategy.getCoastalInputs();

                    for (Byte[] cInput : coastalInputs) {
                        if (coastalDebug)  
                            System.out.print("Coastal output: ");
                        byte[] word = new byte[cInput.length];
                        int i = 0;

                        for (Byte b : cInput) {
                            if (coastalDebug)
                                System.out.print(" " + b.byteValue());
                            word[i++] = b.byteValue();
                        }
                        if (coastalDebug) {
                            System.out.println(" Word: " + new String(word));
                            System.out.println();
                        }

                        word = padBytes(word, 5, true, true);
                        // Execute program with the Coastal input
                        if (coastalDebug) 
                        System.out.println("Executing input...");
                            execProgram(word);
                        if (coastalDebug) 
                            System.out.println("Is New? :" + Data.getNew());
                        if (Data.getNew()) {
                            int inputScore = Data.getLocalBucketSize();
                            newInputs.add(new Input(Arrays.copyOf(word, word.length), false, inputScore, false));
                            Data.resetTuples();
                        }
                    }
                    if (coastalDebug) 
                        System.out.println("--------------------------------------------");
                    Data.clearCoastalInputs();

                }

                queue = new LinkedList<Input>(newInputs);

            }

        }

    }

    public static byte[] padBytes(byte[] input, int padAmount, boolean replaceNewLine, boolean randomPad) {
        Random rand = new Random();
        int start = input.length;
        byte[] output = Arrays.copyOf(input, input.length + padAmount);

        if (replaceNewLine) {
            for (int i = 0; i < output.length; i++) {
                if (output[i] == 10) {
                    output[i] = 0;
                }
            }
        }
        for (int i = 0; i < padAmount; i++) {
            if (randomPad) {
                output[start++] = (byte) (32 + rand.nextInt(94));
            } else {
                output[start++] = 0;
            }
        }

        return output;
    }

    public static String getClassName() {
        return className;
    }

    public static int getMode() {
        return worstCaseMode ? 1 : 0;
    }

    public static double getExecutionTime() {
        return (System.currentTimeMillis() - preTime) / 1000;
    }

    public static int getCurrentOperation() {
        return currentOperation;
    }

    public static double getCoverage() {
        if (totalPaths == 0) {
            return 0;
        }
        return Math.round(((Data.getNoBranches() / (double) totalPaths) * 100) * 100.0) / 100.0;
    }

    public static int getNumberPaths() {
        return Data.getPaths();
    }

    public static int getQueueSize() {
        if (queue == null) {
            return 0;
        }
        return queue.size();
    }

    public static int getNumberCrashes() {
        return crashingInputs.size();
    }

    public static int getNumberRuns() {
        return runs;
    }

    public static void parseProperties(String inputFile) {
        try {
            InputStream propFile = new FileInputStream(inputFile);
            prop = new Properties();

            prop.load(propFile);

            initialClassName = prop.getProperty("jafl.main");
            initialClassNames = prop.getProperty("jafl.classes").split(",");
            file = prop.getProperty("jafl.test");
            concolicMode = Boolean.parseBoolean(prop.getProperty("jafl.concolic", "false"));
            worstCaseMode =  Boolean.parseBoolean(prop.getProperty("jafl.performance", "false"));
            fixedInput = Boolean.parseBoolean(prop.getProperty("jafl.fixedlength", "false"));
            debug = Boolean.parseBoolean(prop.getProperty("jafl.debug", "false"));
            coastalDebug =  Boolean.parseBoolean(prop.getProperty("jafl.coastaldebug", "false"));

            concolicIterations = Integer.parseInt(prop.getProperty("jafl.concoliciterations", "100"));

            if (worstCaseMode) {
                Data.setWorstCaseMode(true);
            }

      

            propFile.close();

        } catch (Exception e) {
            System.out.println("Could not find specified properties file.");
        }
    }

    public static void execProgram(byte[] base) throws IOException {
        runs++;

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
            if (!crashingInputs.containsByteArray(base)) {
                crashingInputs.add(Arrays.copyOf(base, base.length));
                saveResult(base, 1);
            }
            System.out.println("Preventing abort...");
            // abort = true;
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof SystemExitControl.ExitTrappedException) {
                if (!crashingInputs.containsByteArray(base)) {
                    crashingInputs.add(Arrays.copyOf(base, base.length));
                    saveResult(base, 1);
                }
                System.out.println("Found: " + new String(base));
                System.out.println("Preventing abort...");
                // abort = true;
            }

        } catch (Exception e) {
        }

        if (Data.getNew()) {
            if (worstCaseMode && Data.newMaxWorst(base)) {
                saveResult(base, 2);
                noWorstInputs++;
            } else if (!worstCaseMode) {
                saveResult(base, 0);
            }
        }

    }

    // Run Concolic execution trough Coastal
    private static void runCoastal(byte[] input) throws Exception {
        storeInputFile(input);
        final Logger log = LogManager.getLogger("COASTAL");

        final String version = "coastal-test";
        final ReporterManager reporterManager = new ReporterManager();
        ConfigurationBuilder cb = new ConfigurationBuilder(log, version, reporterManager);
        cb.readFromProperties(prop);
        cb.setArgs(".temp");
        Configuration config = cb.construct();
        new COASTAL(config).start();
    }

    private static void storeInputFile(byte[] input) throws Exception {
        FileOutputStream fos = new FileOutputStream(".temp");
        fos.write(input);
        fos.close();
    }

    public static void saveResult(byte[] result, int type) throws IOException {
        File inFile = new File(".temp");
        File outFile;

        switch (type) {
        case 0:
            // Save inputs with new tuples.
            outFile = new File("output/" + className + "/" + className + "_output" + Data.getPaths() + ".txt");
            outFile.mkdirs();
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            break;
        case 1:
            // Save inputs which crash.
            outFile = new File("output/" + className + "/" + className + "_error" + crashingInputs.size() + ".txt");
            outFile.mkdirs();
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            break;
        case 2:
            // Save Worst case inputs with higher score.
            outFile = new File("output/" + className + "/" + className + "_worst" + noWorstInputs + ".txt");
            outFile.mkdirs();
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            break;
        default:
            break;
        }
    }

    public static void cullQueue() {
        ArrayList<Input> list = new ArrayList<Input>(queue);
        ArrayList<Tuple> tuples = Data.getTuples();
        Set<Input> newInputs = new HashSet<Input>();
        Set<Input> worstInputs = new HashSet<Input>();
        Set<Tuple> evaluatedTuples = new HashSet<Tuple>();
        boolean evaluated = false;
        boolean coastalEvaluated = false;
        int score = 0;
        int maxScore = 0;
        byte[] winningInput = null;

        for (Tuple tuple : tuples) {
            score = 0;
            if (!evaluatedTuples.contains(tuple)) {
                for (Input input : list) {
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
                            coastalEvaluated = input.getCoastalEvaluated();
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
                            coastalEvaluated = input.getCoastalEvaluated();

                        }
                    }
                }
                evaluatedTuples.addAll(Data.getInputList(winningInput));
                
                newInputs.add(new Input(winningInput, evaluated, score, coastalEvaluated));
            }
        }
        if (worstCaseMode) {
            Collections.sort(list, new Comparator<Input>() {
                @Override
                public int compare(Input lhs, Input rhs) {
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    return Data.getWorstCaseScore(lhs.getData()) > Data.getWorstCaseScore(rhs.getData()) ? -1 : Data.getWorstCaseScore(lhs.getData()) < Data.getWorstCaseScore(rhs.getData()) ? 1 : 0;
                }
            }); 
            for (Input input : list) {
                if (!worstInputs.contains(input) && Data.getWorstCaseScore(input.getData()) > maxScore) {
                    maxScore = Data.getWorstCaseScore(input.getData());
                    worstInputs.add(input);
                    newInputs.add(new Input(input.getData(), input.getEvaluated(),
                            Data.getWorstCaseScore(input.getData()), input.getCoastalEvaluated()));
                }
            }
        }
        queue = new LinkedList<Input>(newInputs);

    }

    public static void flipBits(byte[] base) throws Exception {
        byte[] tempBase;
        // 1 Walking bit.
        for (int i = 0; i < 8; i++) {

            tempBase = Mutations.oneWalkingBit(base, i);

            execProgram(tempBase);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                Data.resetTuples();
            }
        }
        // 2 Walking bits.
        for (int i = 0; i < 7; i++) {
            tempBase = Mutations.twoWalkingBits(base, i);
            execProgram(tempBase);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                Data.resetTuples();
            }

        }
        // 4 Walking bits.
        for (int i = 0; i < 5; i++) {
            tempBase = Mutations.fourWalkingBits(base, i);
            execProgram(tempBase);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                Data.resetTuples();
            }
        }

    }

    public static void flipBytes(byte[] base) throws Exception {
        byte[] tempBase;
        // Walking byte.
        for (int j = 0; j < base.length; j++) {
            tempBase = Mutations.oneWalkingByte(base, j);
            execProgram(tempBase);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                Data.resetTuples();
            }

        }
        // 2 Walking bytes.
        if (base.length < 2) {
            return;
        }
        for (int j = 0; j < base.length - 1; j++) {
            tempBase = Mutations.twoWalkingBytes(base, j);
            execProgram(tempBase);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                Data.resetTuples();
            }
        }

        // 4 Walking bytes.
        if (base.length < 4) {
            return;
        }

        for (int j = 0; j < base.length - 3; j++) {
            tempBase = Mutations.fourWalkingBytes(base, j);
            execProgram(tempBase);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                Data.resetTuples();
            }
        }

    }

    public static void arithInc(byte[] base) throws Exception {
        byte[] tempBase;

        // 1 Byte increment
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length; j++) {
                tempBase = Mutations.incrementByte(base, j, i);
                execProgram(tempBase);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                    Data.resetTuples();
                }
            }
        }
        // 2 Byte increment
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                tempBase = Mutations.incrementTwoBytes(base, j, i);
                execProgram(tempBase);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                    Data.resetTuples();
                }
            }
        }
        // 4 Byte increment
        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                tempBase = Mutations.incrementFourBytes(base, j, i);
                execProgram(tempBase);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                    Data.resetTuples();
                }
            }
        }

    }

    public static void arithDec(byte[] base) throws Exception {
        byte[] tempBase;

        // 1 Byte decrement

        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length; j++) {
                tempBase = Mutations.decrementByte(base, j, i);
                execProgram(tempBase);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                    Data.resetTuples();
                }
            }
        }
        // 2 Byte decrement

        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 1; j++) {
                tempBase = Mutations.decrementTwoBytes(base, j, i);
                execProgram(tempBase);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                    Data.resetTuples();
                }

            }
        }
        // 4 Byte decrement

        for (int i = 1; i <= ARITH_MAX; i++) {
            for (int j = 0; j < base.length - 3; j++) {
                tempBase = Mutations.decrementFourBytes(base, j, i);
                execProgram(tempBase);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(tempBase, tempBase.length), false, score, false));
                    Data.resetTuples();
                }
            }
        }

    }

    public static void replaceInteresting(byte[] base) throws IOException {

        // Setting 1 byte integers
        for (int i = 0; i < interesting_8.length; i++) {
            for (int j = 0; j < base.length; j++) {
                byte currentVal = base[j];
                base[j] = (byte) interesting_8[i];
                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score, false));
                    Data.resetTuples();
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
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score, false));
                    Data.resetTuples();
                }

                base[j] = temp[1];
                base[j + 1] = temp[0];

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score, false));
                    Data.resetTuples();
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
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score, false));
                    Data.resetTuples();
                }

                base[j] = temp[3];
                base[j + 1] = temp[2];
                base[j + 2] = temp[1];
                base[j + 3] = temp[0];

                execProgram(base);
                if (Data.getNew()) {
                    int score = Data.getLocalBucketSize();
                    queue.add(new Input(Arrays.copyOf(base, base.length), false, score, false));
                    Data.resetTuples();
                }

                base[j] = currentVal1;
                base[j + 1] = currentVal2;
                base[j + 2] = currentVal3;
                base[j + 3] = currentVal4;

            }
        }

    }

    public static void havoc(byte[] base) throws IOException {
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
                    tmp = rand.nextInt(8);
                    base = Mutations.flipBit(base, byteNum, tmp);
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
                    base = Mutations.decrementByte(base, byteNum, (rand.nextInt(ARITH_MAX) + 1));
                    break;
                case 5:
                    // Randomly subtract from two bytes.
                    if (base.length < 2) {
                        continue;
                    }
                    byteNum = rand.nextInt(base.length - 1);
                    tmp = rand.nextInt(ARITH_MAX) + 1;
                    base = Mutations.decrementByte(base, byteNum, tmp);
                    base = Mutations.decrementByte(base, byteNum + 1, tmp);
                    break;
                case 6:
                    // Randomly subtract from four bytes.
                    if (base.length < 4) {
                        continue;
                    }
                    byteNum = rand.nextInt(base.length - 3);
                    tmp = rand.nextInt(ARITH_MAX) + 1;
                    base = Mutations.decrementByte(base, byteNum, tmp);
                    base = Mutations.decrementByte(base, byteNum + 1, tmp);
                    base = Mutations.decrementByte(base, byteNum + 2, tmp);
                    base = Mutations.decrementByte(base, byteNum + 3, tmp);
                    break;
                case 7:
                    // Randomly add to byte.
                    byteNum = rand.nextInt(base.length);
                    base = Mutations.incrementByte(base, byteNum, (rand.nextInt(ARITH_MAX) + 1));
                    break;
                case 8:
                    // Randomly add to two bytes.
                    if (base.length < 2) {
                        continue;
                    }
                    byteNum = rand.nextInt(base.length - 1);
                    tmp = rand.nextInt(ARITH_MAX) + 1;
                    base = Mutations.incrementByte(base, byteNum, tmp);
                    base = Mutations.incrementByte(base, byteNum + 1, tmp);
                    break;
                case 9:
                    // Randomly add to four bytes.
                    if (base.length < 4) {
                        continue;
                    }
                    byteNum = rand.nextInt(base.length - 3);
                    tmp = rand.nextInt(ARITH_MAX) + 1;
                    base = Mutations.incrementByte(base, byteNum, tmp);
                    base = Mutations.incrementByte(base, byteNum + 1, tmp);
                    base = Mutations.incrementByte(base, byteNum + 2, tmp);
                    base = Mutations.incrementByte(base, byteNum + 3, tmp);
                    break;
                case 10:
                    // Set a random byte to a random value.
                    byteNum = rand.nextInt(base.length);
                    tmp = rand.nextInt(255) + 1;
                    base[byteNum] = (byte) (base[byteNum] ^ tmp);
                    break;
                case 11:
                case 12:
                    if (fixedInput) {
                        j--;
                        continue;
                    }                   
                    // Delete bytes.
                    if (base.length < 2) {
                        continue;
                    }
                    byteNum = rand.nextInt(base.length);
                    base = Mutations.removeByte(base, byteNum);
                    break;
                    
                case 13:
                    // Insert or clone random bytes
                    if (fixedInput) {
                        j--;
                        continue;
                    }                   
                    boolean clone = (rand.nextInt(4) > 0);
                    int blockSize = rand.nextInt(base.length); // how much you want to clone or insert
                    int blockStart = rand.nextInt(base.length - blockSize + 1); // where do you start from
                    int newPos = rand.nextInt(base.length); // where are we putting the new stuff
                    base = Mutations.CloningOrInserting(base, clone, blockStart, newPos, blockSize);
                    break;
                    
                case 14:
                    // Overwrite bytes
                    // Random chunk or fixed bytes.
                    if (rand.nextInt(4) == 0) {
                        // Fixed bytes.
                        byteNum = rand.nextInt(base.length);
                        base = Mutations.replaceByte(base, (byte) (rand.nextInt(255) + 1), byteNum);
                    } else {
                        // Random chunk.
                        byteNum = rand.nextInt(base.length);
                        tmp = rand.nextInt(base.length);
                        if (byteNum == tmp) {
                            continue;
                        }
                        base = Mutations.replaceByte(base, base[tmp], byteNum);

                    }
                    break;
                default:
                    break;
                }

            }

            execProgram(base);
            if (Data.getNew()) {
                int score = Data.getLocalBucketSize();
                queue.add(new Input(Arrays.copyOf(base, base.length), false, score, false));
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
                if (permission.getName().contains("exitVM.100")) {

                } else if (permission.getName().contains("exitVM")) {
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
    private boolean coastalEvaluated;

    public Input(byte[] data, boolean evaluated, int score, boolean coastalEvaluated) {
        this.data = data;
        this.evaluated = evaluated;
        this.score = score;
        this.coastalEvaluated = coastalEvaluated;
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

    public boolean getCoastalEvaluated() {
        return this.coastalEvaluated;
    }

    public void setCoastalEvaluated(boolean evaluated) {
        this.coastalEvaluated = evaluated;
    }

    public String toString() {
        String out = "[";
        for (int i = 0; i < data.length; i++) {
            out += data[i] + ",";
        }
        out += "] = " + score;
        return out;
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

class ByteSet extends HashSet<byte[]> {
    public boolean containsByteArray(byte[] input) {
        for (byte[] base : this) {
            if (Arrays.equals(base, input)) {
                return true;
            }
        }
        return false;
    }
}
