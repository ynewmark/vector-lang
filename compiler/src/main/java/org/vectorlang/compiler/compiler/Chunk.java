package org.vectorlang.compiler.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Chunk {
    private final long[] chunk;
    private final long[] labels;

    public Chunk(long[] chunk, long[] labels) {
        this.chunk = chunk;
        this.labels = labels;
    }

    public Chunk(long[] chunk) {
        this(chunk, new long[0]);
    }

    public Chunk concat(long[] other, long[] otherLabels) {
        long[] newChunk = new long[chunk.length + other.length];
        System.arraycopy(chunk, 0, newChunk, 0, chunk.length);
        System.arraycopy(other, 0, newChunk, chunk.length, other.length);
        long[] newLabels = new long[labels.length + otherLabels.length];
        System.arraycopy(labels, 0, newLabels, 0, labels.length);
        for (int i = 0; i < otherLabels.length; i++) {
            newLabels[labels.length + i] = otherLabels[i] + chunk.length;
        }
        return new Chunk(newChunk, newLabels);
    }

    public Chunk concat(long[] other) {
        return concat(other, new long[0]);
    }

    public Chunk concat(Chunk other) {
        return concat(other.chunk, other.labels);
    }

    public int length() {
        return chunk.length;
    }

    public Chunk link() {
        long[] newChunk = new long[chunk.length];
        for (int i = 0; i < chunk.length; i++) {
            newChunk[i] = chunk[i];
            if (chunk[i] >= OpCode.JMP.ordinal()) {
                if (chunk[i] == OpCode.JIF.ordinal() || chunk[i] == OpCode.JMP.ordinal()) {
                    newChunk[i + 1] = labels[(int) chunk[i + 1]];
                } else {
                    newChunk[i + 1] = chunk[i + 1];
                }
                i++;
            }
        }
        return new Chunk(newChunk);
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(chunk.length * Long.BYTES);
        buffer.order(ByteOrder.nativeOrder()).asLongBuffer().put(chunk);
        return buffer.array();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunk.length; i++) {
            if (chunk[i] < OpCode.values().length) {
                OpCode opCode = OpCode.values()[(int) chunk[i]];
                builder.append(opCode);
                if (opCode == OpCode.PUSH || opCode == OpCode.STORE || opCode == OpCode.LOAD ||
                    opCode == OpCode.ALLOC || opCode == OpCode.JMP || opCode == OpCode.JIF
                    || opCode == OpCode.PUSHI || opCode == OpCode.INDEX || opCode == OpCode.PRINT) {
                        builder.append(' ').append(chunk[++i]);
                    }
            } else {
                builder.append(chunk[i]).append('?');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
