package org.yamcs.simulation.simulator.cfdp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NakPacket extends Packet {

    private Header header;
    private ByteBuffer buffer;

    private long scopeStart;
    private long scopeEnd;
    private List<SegmentRequest> segmentRequests = new ArrayList<SegmentRequest>();

    public NakPacket(ByteBuffer buffer, Header header) {
        this.header = header;
        this.buffer = buffer;

        this.scopeStart = Utils.getUnsignedInt(buffer);
        this.scopeEnd = Utils.getUnsignedInt(buffer);
        while (buffer.hasRemaining()) {
            segmentRequests.add(new SegmentRequest(Utils.getUnsignedInt(buffer),
                    Utils.getUnsignedInt(buffer)));
        }
    }

}