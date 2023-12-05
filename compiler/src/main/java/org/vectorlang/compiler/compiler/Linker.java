package org.vectorlang.compiler.compiler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Linker {
    
    public byte[] link(List<Chunk> chunks) {
        System.out.println(chunks);
        Map<String, long[]> staticPositions = new HashMap<>();
        long[] functionPositions = new long[chunks.size()];
        int staticSize = 0, instrSize = 0;
        int start = 0;
        int position = 0;
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            staticPositions.put(chunk.getName(), chunk.getStaticPositions(start));
            start += chunk.getStaticsSize();
            functionPositions[i] = position;
            position += chunk.getInstrSize();
            staticSize += chunk.getStaticsSize();
            instrSize += chunk.getInstrSize();
        }
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + (staticSize + instrSize) * 8).order(ByteOrder.nativeOrder());
        buffer.put(MAGIC_BYTES);
        buffer.asLongBuffer().put(HEADER_SIZE / 8).put((HEADER_SIZE / 8) + functionPositions[functionPositions.length - 1]);
        for (Chunk chunk : chunks) {
            buffer.put(chunk.assembleStatics());
        }
        for (int i = 0; i < chunks.size(); i++) {
            buffer.put(chunks.get(i).assembleInstrs(
                functionPositions, staticPositions.get(chunks.get(i).getName()), functionPositions[i]
            ));
        }
        return buffer.array();
    }

    private static final int HEADER_SIZE = 24;
    private static final byte[] MAGIC_BYTES = new byte[]{
        'v', 'e', 'c', 't', '0', '0', '0', '0'
    };
}
