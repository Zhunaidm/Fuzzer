package za.ac.sun.cs.fuzz;

import org.junit.Test;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import za.ac.sun.cs.core.Mutations;

public class TestMutations {

    @Test
    public void TestWalkingBits() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.oneWalkingBit(bytes, 1);

        assertEquals("vgqv", new String(bytes));
    }

    @Test
    public void TestTwoWalkingBits() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.twoWalkingBits(bytes, 1);

        assertEquals("rcur", new String(bytes));
    }

    @Test
    public void TestFourWalkingBits() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.fourWalkingBits(bytes, 1);

        assertEquals("j{mj", new String(bytes));
    }

    @Test
    public void TestWalkingByte() throws IOException {
        byte[] bytes = { (byte) 0x8b, (byte) 0x65, (byte) 0x73, (byte) 0x74 };
        bytes = Mutations.oneWalkingByte(bytes, 0);

        assertEquals("test", new String(bytes));
    }

    @Test
    public void TestTwoWalkingBytes() throws IOException {
        byte[] bytes = { (byte) 0x8b, (byte) 0x9a, (byte) 0x73, (byte) 0x74 };
        bytes = Mutations.twoWalkingBytes(bytes, 0);

        assertEquals("test", new String(bytes));
    }

    @Test
    public void TestFourWalkingBytes() throws IOException {
        byte[] bytes = { (byte) 0x8b, (byte) 0x9a, (byte) 0x8c, (byte) 0x8b };

        bytes = Mutations.fourWalkingBytes(bytes, 0);

        assertEquals("test", new String(bytes));
    }

    @Test
    public void TestIncrementByte() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.incrementByte(bytes, 0, 1);

        assertEquals("uest", new String(bytes));
    }

    @Test
    public void TestIncrementTwoBytes() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.incrementTwoBytes(bytes, 0, 1);

        assertEquals("ufst", new String(bytes));
    }

    @Test
    public void TestIncrementFourBytes() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.incrementFourBytes(bytes, 0, 1);

        assertEquals("uftu", new String(bytes));
    }

    @Test
    public void TestDecrementByte() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.decrementByte(bytes, 0, 1);

        assertEquals("sest", new String(bytes));
    }

    @Test
    public void TestDecrementTwoBytes() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.decrementTwoBytes(bytes, 0, 1);

        assertEquals("sdst", new String(bytes));
    }

    @Test
    public void TestDecrementFourBytes() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.decrementFourBytes(bytes, 0, 1);

        assertEquals("sdrs", new String(bytes));
    }

    @Test
    public void TestCloningBytes() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.CloningOrInserting(bytes, true, 0, bytes.length, bytes.length);

        assertEquals("testtest", new String(bytes));
    }

    @Test
    public void TestFlipBit() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.flipBit(bytes, 2, 4);

        assertEquals("tect", new String(bytes));
    }

    @Test
    public void TestAddByte() throws IOException {
        String word = "test";
        String word2 = "ing";

        byte[] bytes = word.getBytes();
        byte[] bytes2 = word2.getBytes();
        bytes = Mutations.addByte(bytes, bytes2[0], bytes.length);
        bytes = Mutations.addByte(bytes, bytes2[1], bytes.length);
        bytes = Mutations.addByte(bytes, bytes2[2], bytes.length);

        assertEquals("testing", new String(bytes));
    }

    @Test
    public void TestRemoveByte() throws IOException {
        String word = "test";

        byte[] bytes = word.getBytes();
        bytes = Mutations.removeByte(bytes, bytes.length - 1);
        bytes = Mutations.removeByte(bytes, bytes.length - 1);

        assertEquals("te", new String(bytes));
    }

    @Test
    public void TestReplaceByte() throws IOException {
        String word = "test";
        String word2 = "ru";

        byte[] bytes = word.getBytes();
        byte[] bytes2 = word2.getBytes();

        bytes = Mutations.replaceByte(bytes, bytes2[0], 0);
        bytes = Mutations.replaceByte(bytes, bytes2[1], 1);

        assertEquals("rust", new String(bytes));

    }

}
