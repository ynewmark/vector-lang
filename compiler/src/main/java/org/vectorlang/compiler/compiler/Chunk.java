package org.vectorlang.compiler.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class Chunk {
    
    private final String name;
    private final long[] instructions;
    private final long[] labels;
    private final long[][] staticData;

    public Chunk(String name, long[] instructions, long[] labels, long[][] staticData) {
        this.name = name;
        this.instructions = instructions;
        this.labels = labels;
        this.staticData = staticData;
    }

    public Chunk(String name) {
        this(name, new long[0], new long[0], new long[0][]);
    }

    public Chunk(String name, long[] instructions) {
        this(name, instructions, new long[0], new long[0][]);
    }

    public Chunk(String name, long[] instructions, long[] labels) {
        this(name, instructions, labels, new long[0][]);
    }

    public Chunk concat(long[] otherInstrs, long[] otherLabels, long[][] otherData) {
        long[] newInstructions = new long[instructions.length + otherInstrs.length];
        System.arraycopy(instructions, 0, newInstructions, 0, instructions.length);
        System.arraycopy(otherInstrs, 0, newInstructions, instructions.length, otherInstrs.length);
        long[] newLabels = new long[labels.length + otherLabels.length];
        System.arraycopy(labels, 0, newLabels, 0, labels.length);
        for (int i = 0; i < otherLabels.length; i++) {
            newLabels[labels.length + i] = instructions.length + otherLabels[i];
        }
        long[][] newStatics = new long[staticData.length + otherData.length][];
        System.arraycopy(staticData, 0, newStatics, 0, staticData.length);
        System.arraycopy(otherData, 0, newStatics, staticData.length, otherData.length);
        return new Chunk(name, newInstructions, newLabels, newStatics);
    }

    public Chunk concat(long[] otherInstrs, long[] otherLabels) {
        return concat(otherInstrs, otherLabels, new long[0][]);
    }

    public Chunk concat(long[] otherInstrs) {
        return concat(otherInstrs, new long[0], new long[0][]);
    }

    public Chunk concat(long[] otherInstrs, long[][] otherData) {
        return concat(otherInstrs, new long[0], otherData);
    }

    public Chunk concat(Chunk other) {
        return concat(other.instructions, other.labels, other.staticData);
    }

     public int getStaticsSize() {
        int size = 0;
        for (long[] stat : staticData) {
            size += stat.length;
        }
        return size;
    }

    public int getInstrSize() {
        return instructions.length;
    }

    public long[] getStaticPositions(int start) {
        long[] positions = new long[staticData.length];
        if (staticData.length > 0) {
            positions[0] = start + staticData[0].length - 1;
            for (int i = 1; i < staticData.length; i++) {
                positions[i] = positions[i - 1] + staticData[i].length;
            }
        }
        return positions;
    }

    public String getName() {
        return name;
    }

    public byte[] assembleInstrs(long[] functionPositions, long[] staticPositions, long offset) {
        ByteBuffer buffer = ByteBuffer.allocate(getInstrSize() * 8);
        long[] assembledInstrs = new long[instructions.length];
        for (int i = 0; i < instructions.length; i++) {
            assembledInstrs[i] = instructions[i];
            if (instructions[i] >= OpCode.ALLOC.ordinal()) {
                if (instructions[i] == OpCode.JIF.ordinal() || instructions[i] == OpCode.JMP.ordinal()) {
                    assembledInstrs[i + 1] = labels[(int) instructions[i + 1]] + offset;
                } else if (instructions[i] == OpCode.STATIC.ordinal()) {
                    assembledInstrs[i + 1] = staticPositions[(int) instructions[i + 1]];
                } else if (instructions[i] == OpCode.CALL.ordinal()) {
                    assembledInstrs[i + 1] = functionPositions[(int) instructions[i + 1]];
                } else {
                    assembledInstrs[i + 1] = instructions[i + 1];
                }
                i++;
            }
        }
        buffer.order(ByteOrder.nativeOrder()).asLongBuffer().put(assembledInstrs);
        return buffer.array();
    }

    public byte[] assembleStatics() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getStaticsSize() * 8);
        LongBuffer buffer = byteBuffer.order(ByteOrder.nativeOrder()).asLongBuffer();
        for (long[] stat : staticData) {
            buffer.put(stat);
        }
        return byteBuffer.array();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(name).append("]\n");
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i] < OpCode.values().length) {
                OpCode opCode = OpCode.values()[(int) instructions[i]];
                builder.append(opCode);
                if (instructions[i] >= OpCode.ALLOC.ordinal()) {
                    builder.append(' ').append(instructions[++i]);
                }
            } else {
                builder.append(instructions[i]).append('?');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
