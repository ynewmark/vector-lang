package org.vectorlang.compiler.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class Chunk {
    private final long[] chunk;
    private final long[] labels;
    private final long[][] statics;

    public Chunk(long[] chunk, long[] labels, long[][] statics) {
        this.chunk = chunk;
        this.labels = labels;
        this.statics = statics;
    }

    public Chunk(long[] chunk, long[] labels) {
        this(chunk, labels, new long[0][]);
    }

    public Chunk(long[] chunk) {
        this(chunk, new long[0], new long[0][]);
    }

    public Chunk concat(long[] other, long[] otherLabels, long[][] otherStatics) {
        long[] newChunk = new long[chunk.length + other.length];
        System.arraycopy(chunk, 0, newChunk, 0, chunk.length);
        System.arraycopy(other, 0, newChunk, chunk.length, other.length);
        long[] newLabels = new long[labels.length + otherLabels.length];
        System.arraycopy(labels, 0, newLabels, 0, labels.length);
        for (int i = 0; i < otherLabels.length; i++) {
            newLabels[labels.length + i] = otherLabels[i] + chunk.length;
        }
        long[][] newStatics = new long[statics.length + otherStatics.length][];
        System.arraycopy(statics, 0, newStatics, 0, statics.length);
        System.arraycopy(otherStatics, 0, newStatics, statics.length, otherStatics.length);
        return new Chunk(newChunk, newLabels, newStatics);
    }

    public Chunk concat(long[] other, long[] otherLabels) {
        return concat(other, otherLabels, new long[0][]);
    }

    public Chunk concat(long[] other) {
        return concat(other, new long[0]);
    }

    public Chunk concat(Chunk other) {
        return concat(other.chunk, other.labels, other.statics);
    }

    public int length() {
        return chunk.length;
    }

    public Chunk link() {
        long[] staticPositions = new long[0];
        if (statics.length > 0) {
            staticPositions = new long[statics.length];
            staticPositions[0] = 0;
            for (int i = 1; i < statics.length; i++) {
                staticPositions[i] = staticPositions[i - 1] + statics[i - 1].length + 1;
            }
        }
        long[] newChunk = new long[chunk.length];
        for (int i = 0; i < chunk.length; i++) {
            newChunk[i] = chunk[i];
            if (chunk[i] >= OpCode.JMP.ordinal()) {
                if (chunk[i] == OpCode.JIF.ordinal() || chunk[i] == OpCode.JMP.ordinal()) {
                    newChunk[i + 1] = labels[(int) chunk[i + 1]];
                } else if (chunk[i] == OpCode.LOADS.ordinal()) {
                    newChunk[i + 1] = staticPositions[(int) chunk[i + 1]];
                } else {
                    newChunk[i + 1] = chunk[i + 1];
                }
                i++;
            }
        }
        return new Chunk(newChunk, new long[0], statics);
    }

    public byte[] assemble() {
        ByteBuffer buffer = ByteBuffer.allocate(getSize());
        buffer = buffer.order(ByteOrder.nativeOrder());
        buffer.put(MAGIC_BYTES);
        LongBuffer longBuffer = buffer.asLongBuffer();
        long[] staticsCompiled = compileStatics();
        longBuffer.put(HEADER_SIZE / 8);
        longBuffer.put(HEADER_SIZE / 8 + staticsCompiled.length);
        longBuffer.put(staticsCompiled);
        longBuffer.put(chunk);
        return buffer.array();
    }

    private long[] compileStatics() {
        LongBuffer buffer = LongBuffer.allocate(getStaticsSize() / 8);
        for (long[] stat : statics) {
            buffer.put(stat.length);
            buffer.put(stat);
        }
        return buffer.array();
    }

    private int getSize() {
        int size = HEADER_SIZE;
        size += getStaticsSize();
        size += chunk.length * 8;
        return size;
    }

    private int getStaticsSize() {
        int size = 0;
        for (long[] stat : statics) {
            size += stat.length * 8 + PER_STATIC;
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunk.length; i++) {
            if (chunk[i] < OpCode.values().length) {
                OpCode opCode = OpCode.values()[(int) chunk[i]];
                builder.append(opCode);
                if (chunk[i] >= OpCode.JMP.ordinal()) {
                    builder.append(' ').append(chunk[++i]);
                }
            } else {
                builder.append(chunk[i]).append('?');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private static final int HEADER_SIZE = 24, PER_STATIC = 8;
    private static final byte[] MAGIC_BYTES = new byte[]{
        'v', 'e', 'c', 't', '0', '0', '0', '0'
    };
}
