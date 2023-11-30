package org.vectorlang.compiler.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Chunk {
    private final long[] chunk;

    public Chunk(long[] chunk) {
        this.chunk = chunk;
    }

    public Chunk concat(long[] other) {
        long[] newChunk = new long[chunk.length + other.length];
        System.arraycopy(chunk, 0, newChunk, 0, chunk.length);
        System.arraycopy(other, 0, newChunk, chunk.length, other.length);
        return new Chunk(newChunk);
    }

    public Chunk concat(Chunk other) {
        return concat(other.chunk);
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
