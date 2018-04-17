package org.yamcs.parameterarchive;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

import org.yamcs.parameter.Value;
import org.yamcs.parameter.ValueArray;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.utils.DecodingException;
import org.yamcs.utils.ValueUtility;
import org.yamcs.utils.VarIntUtil;

/**
 * Boolean value sgement uses BitSet to represent the boolean values as a set of bits
 * 
 * @author nm
 *
 */
public class BooleanValueSegment extends BaseSegment implements ValueSegment {
    BitSet bitSet;
    int size;
    
    public BooleanValueSegment() {
        super(FORMAT_ID_BooleanValueSegment);
        bitSet = new BitSet();
    }
    
    
    /**
     * write the segment to buffer
     * @param bb
     */
    @Override
    public void writeTo(ByteBuffer bb) {
        VarIntUtil.writeVarInt32(bb, size);
        
        long[]la = bitSet.toLongArray();
        VarIntUtil.writeVarInt32(bb, la.length);
        
        for(long l:la) {
            bb.putLong(l);
        }
    }

    private void parse(ByteBuffer bb) throws DecodingException {
        size = VarIntUtil.readVarInt32(bb);
        int n = VarIntUtil.readVarInt32(bb);
        long[]la = new long[n];
        for(int i=0; i<n; i++) {
            la[i]=bb.getLong();
        }
        bitSet = BitSet.valueOf(la);
    }
    
    public static BooleanValueSegment parseFrom(ByteBuffer bb) throws DecodingException {
        BooleanValueSegment r = new BooleanValueSegment();
        r.parse(bb);
        return r;
    }

    @Override
    public int getMaxSerializedSize() {
        return 8+bitSet.size()/8;
    }

    @Override
    public Value getValue(int index) {
        return ValueUtility.getBooleanValue(bitSet.get(index));
    }
    
    static BooleanValueSegment consolidate(List<Value> values) {
        BooleanValueSegment bvs = new BooleanValueSegment();
        int n = values.size();
        
        bvs.bitSet = new BitSet(n);
        bvs.size = values.size();
        for(int i=0; i<n; i++) {
            bvs.bitSet.set(i, values.get(i).getBooleanValue());
        }
        return bvs;
    }

    @Override
    public ValueArray getRange(int posStart, int posStop, boolean ascending) {
        ValueArray r = new ValueArray(Type.BOOLEAN, posStop-posStart);
        if(ascending) {
            for(int i = posStart; i<posStop; i++) {
                r.setValue(i-posStart, bitSet.get(i));
            }
        } else {
            for(int i = posStop; i>posStart; i--) {
                r.setValue(posStop-i, bitSet.get(i));
            }
        }
        
        return r;
    }


    /**
     * returns the size of the BitSet storing the values - this will round up to the size of long
     */
    @Override
    public int size() {
        return size;
    }


    @Override
    public void add(int pos, Value engValue) {
        if (pos == size) {
            size++;
            bitSet.set(pos, engValue.getBooleanValue());
        } else {
            // Ignore for now
            // throw new UnsupportedOperationException("add in the middle of bitSet is not supported");
        }
    }

    @Override
    public BaseSegment consolidate() {
        return this;
    }
}
