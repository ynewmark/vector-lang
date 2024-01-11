package org.vectorlang.compiler.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class Linker {
    
    public byte[] link(Chunk[] chunks) {
        Map<String, long[]> staticPositions = new HashMap<>();
        long[] functionPositions = new long[chunks.length];
        int staticSize = 0, instrSize = 0;
        int start = 0;
        int position = 0;
        int mainIndex = 0;
        for (int i = 0; i < chunks.length; i++) {
            Chunk chunk = chunks[i];
            staticPositions.put(chunk.getName(), chunk.getStaticPositions(start));
            start += chunk.getStaticsSize();
            functionPositions[i] = position;
            position += chunk.getInstrSize();
            staticSize += chunk.getStaticsSize();
            instrSize += chunk.getInstrSize();
            if (chunks[i].getName().equals("main")) {
                mainIndex = i;
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate((HEADER_SIZE * 8) + (staticSize + instrSize) * 8).order(ByteOrder.nativeOrder());
        buffer.put(MAGIC_BYTES);
        buffer.asLongBuffer().put(new long[]{
            HEADER_SIZE, HEADER_SIZE + staticSize, functionPositions[mainIndex]
        });
        buffer.position(HEADER_SIZE * 8);
        for (Chunk chunk : chunks) {
            buffer.put(chunk.assembleStatics());
        }
        for (int i = 0; i < chunks.length; i++) {
            buffer.put(chunks[i].assembleInstrs(
                functionPositions, staticPositions.get(chunks[i].getName()), functionPositions[i]
            ));
        }
        return buffer.array();
    }

    private static final int HEADER_SIZE = 4;
    private static final byte[] MAGIC_BYTES = new byte[]{
        'v', 'e', 'c', 't', '0', '0', '0', '0'
    };
}
