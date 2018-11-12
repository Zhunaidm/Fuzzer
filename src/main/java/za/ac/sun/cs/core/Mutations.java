package za.ac.sun.cs.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Mutations {

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
    public static byte[] addByte(byte[] base, byte temp, int index) throws IOException {
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
    public static byte[] replaceByte(byte[] base, byte temp, int index) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[index] = temp;
        return newBase;
    }

    // Flip bit.
    public static byte[] flipBit(byte[] base, int byteNum, int index) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);
        newBase[byteNum] = (byte) (newBase[byteNum] ^ (1 << index));
        return newBase;
    }

    // 1 Walking bit.
    public static byte[] oneWalkingBit(byte[] base, int bit) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);
        for (int j = 0; j < newBase.length; j++) {
            newBase[j] = (byte) (newBase[j] ^ (1 << bit));
        }

        return newBase;
    }

    // 2 Walking bits.
    public static byte[] twoWalkingBits(byte[] base, int bit) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);
        for (int j = 0; j < newBase.length; j++) {
            newBase[j] = (byte) (newBase[j] ^ (1 << bit));
            newBase[j] = (byte) (newBase[j] ^ (1 << (bit + 1)));
        }

        return newBase;
    }

    // 4 Walking bits.
    public static byte[] fourWalkingBits(byte[] base, int bit) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);
        for (int j = 0; j < newBase.length; j++) {
            newBase[j] = (byte) (newBase[j] ^ (1 << bit));
            newBase[j] = (byte) (newBase[j] ^ (1 << (bit + 1)));
            newBase[j] = (byte) (newBase[j] ^ (1 << (bit + 2)));
            newBase[j] = (byte) (newBase[j] ^ (1 << (bit + 3)));
        }

        return newBase;
    }

    // 1 Walking byte.
    public static byte[] oneWalkingByte(byte[] base, int byteNo) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (base[byteNo] ^ 0xFF);

        return newBase;
    }

    // 2 Walking bytes.
    public static byte[] twoWalkingBytes(byte[] base, int byteNo) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] ^ 0xFF);
        newBase[byteNo + 1] = (byte) (newBase[byteNo + 1] ^ 0xFF);

        return newBase;
    }

    // 4 Walking bytes.
    public static byte[] fourWalkingBytes(byte[] base, int byteNo) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] ^ 0xFF);
        newBase[byteNo + 1] = (byte) (newBase[byteNo + 1] ^ 0xFF);
        newBase[byteNo + 2] = (byte) (newBase[byteNo + 2] ^ 0xFF);
        newBase[byteNo + 3] = (byte) (newBase[byteNo + 3] ^ 0xFF);

        return newBase;
    }

    // Increment byte
    public static byte[] incrementByte(byte[] base, int byteNo, int amount) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] + amount);

        return newBase;
    }

    // Increment 2 bytes
    public static byte[] incrementTwoBytes(byte[] base, int byteNo, int amount) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] + amount);
        newBase[byteNo + 1] = (byte) (newBase[byteNo + 1] + amount);

        return newBase;
    }

    // Increment 4 bytes
    public static byte[] incrementFourBytes(byte[] base, int byteNo, int amount) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] + amount);
        newBase[byteNo + 1] = (byte) (newBase[byteNo + 1] + amount);
        newBase[byteNo + 2] = (byte) (newBase[byteNo + 2] + amount);
        newBase[byteNo + 3] = (byte) (newBase[byteNo + 3] + amount);

        return newBase;
    }

    // Increment byte
    public static byte[] decrementByte(byte[] base, int byteNo, int amount) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] - amount);

        return newBase;
    }

    // Increment 2 bytes
    public static byte[] decrementTwoBytes(byte[] base, int byteNo, int amount) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] - amount);
        newBase[byteNo + 1] = (byte) (newBase[byteNo + 1] - amount);

        return newBase;
    }

    // Increment 4 bytes
    public static byte[] decrementFourBytes(byte[] base, int byteNo, int amount) throws IOException {
        byte[] newBase = Arrays.copyOf(base, base.length);

        newBase[byteNo] = (byte) (newBase[byteNo] - amount);
        newBase[byteNo + 1] = (byte) (newBase[byteNo + 1] - amount);
        newBase[byteNo + 2] = (byte) (newBase[byteNo + 2] - amount);
        newBase[byteNo + 3] = (byte) (newBase[byteNo + 3] - amount);

        return newBase;
    }

    public static byte[] CloningOrInserting(byte[] base, boolean clone, int start, int newPos, int blockSize) {
        Random rand = new Random();
        // if things get too long lets stop this bus! This is super bad and needs to be
        // configured
        if (base.length > 20)
            return base;

        int blockStart = 0;
        if (clone) {
            blockStart = start;
        }
        byte[] newBase = new byte[base.length + blockSize];
        System.arraycopy(base, 0, newBase, 0, newPos);
        if (clone) {
            System.arraycopy(base, blockStart, newBase, newPos, blockSize);
        } else {
            byte data = (byte) (rand.nextInt(256) - 128);
            Arrays.fill(newBase, newPos, newPos + blockSize, data);
        }
        System.arraycopy(base, newPos, newBase, newPos + blockSize, base.length - newPos);
        return newBase;
    }
}