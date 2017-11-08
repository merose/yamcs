package org.yamcs.utils;

import java.nio.ByteOrder;

/**
 * Allows to read bits from a byte array (byte[])
 * keeps a bit position and the extractions are relative to the position.
 * It allows also to provide an offset (in bytes) inside the byte array and then the bit position is relative to the offset.
 * 
 * Supported operations are
 *  - extract up to 64 bits into a long - big endian or little endian
 *  - extract a byte array (throws exception if the position is not at the beginning of a byte)
 *  - exact a byte (throws exception if the position is not at the beginning of a byte)
 * 
 * 
 * Note on the Little Endian: it is designed to work on x86 architecture which uses internally little endian byte _and_ bit 
 * ordering but when accessing memory, full bytes are transferred in big endian order.
 * 
 * For example when in C you have a 32 bit structure:
 * struct S {
 *   unsigned int a: 3;
 *   unsigned int b: 12;
 *   unsigned int c: 17;
 * }
 * and you pack that in a packet by just reading the corresponding 4 bytes memory, you will get the following representation
 * (0 is the most significant bit):
 * 
 *   b7  b8  b9  b10 b11  a0  a1  a2    
 *  c16  b0  b1  b2   b3  b4  b5  b6
 *   c8  c9 c10 c11  c12 c13 c14 c15   
 *   c0  c1  c2  c3   c4  c5  c6  c7   
 * 
 * To read this with this BitBuffer you would naturally do like this:
 * 
 *   BitBuffer bb = new BitBuffer(..., 0);
 *   bb.setOrder(LITTLE_ENDIAN);
 *   
 *   a = bb.getBits(3);
 *   b = bb.getBits(12); 
 *   c = bb.getBits(17);
 * 
 * Note how the first call (when the bb.position=0) reads the 3 bits at position 5 instead of those at position 0
 * 
 * @author nm
 *
 */
public class BitBuffer {
    final byte[] b;
    int position;
    ByteOrder byteOrder;
    final int offset;
    
    /**
     * Creates a new bit buffer that wraps array b starting at offset 0
     * @param b
     */
    public BitBuffer(byte[] b) {
        this(b,0);
    }
    
    /**
     * Creates a new bit buffer that wraps the array b starting at offset (in bytes)
     * @param b
     * @param offset
     */
    public BitBuffer(byte[] b, int offset) {
        this.b = b;
        this.position = 0;
        this.byteOrder = ByteOrder.BIG_ENDIAN;
        this.offset = offset;
    }

    /**
     * reads numBits from the buffer and returns them into a long on the rightmost position.
     * 
     * numBits has to be max 64.
     * 
     * @param numBits
     * @return
     */
    public long getBits(int numBits) {
        if(byteOrder==ByteOrder.LITTLE_ENDIAN) {
            return getBitsLE(numBits);
        }
        long r = 0;
        
        int bytepos = position>>3;
        int n = numBits;
        int fbb = -position &0x7; //how many bits are from position until the end of the byte
        if(fbb>0) {
            
            if(n<=fbb) { //the value fits entirely within the first byte
                position+=numBits;
                return (b[idx(bytepos)]>>>(fbb-n)) & ((1<<n)-1);
            } else {
                r = b[idx(bytepos)] & ((1<<fbb)-1);
                n-=fbb;
                bytepos++;
            }
        }
        while(n>8) {
            r = (r<<8) | (b[idx(bytepos)]&0xFF);
            n-=8;
            bytepos++;
        }
        r = (r<<n) | ((b[idx(bytepos)]&0xFF )>>>(8-n)) ;
        
        position+=numBits;
        return r;
    }
    
    private long getBitsLE(int numBits) {
        long r = 0;
        
        int bytepos = (position+numBits-1)>>3;
        int n = numBits;
        int lbb = (position+numBits) &0x7; //how many bits are to be read from the last byte (which is the most significant)
        if(lbb>0) {
            if(lbb>=n) {//the value fits entirely within one byte
                position+=numBits;
                return (b[idx(bytepos)] >>(lbb-n)) & ((1<<n)-1);
            } else {
                r = b[idx(bytepos)] & ((1<<lbb)-1);
                n-=lbb;
                bytepos--;
            }
        }
        while(n>8) {
            r = (r<<8) | (b[idx(bytepos)]&0xFF);
            n-=8;
            bytepos--;
        }
        
        r = (r<<n) | ((b[idx(bytepos)]&0xFF)>>>(8-n));
        
        position+=numBits;
        return r;
    }
    
    /**
     * get position in bits
     * @return current position in bits
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * set position in bits
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }
    
    public void setByteOrder(ByteOrder order) {
        this.byteOrder = order;
    }
    
    public ByteOrder getByteOrder() {
        return byteOrder;
    }
    
    private void ensureByteBoundary() {
        if((position&0x7)!=0) {
            throw new IllegalStateException("bit position not at byte boundary");
        }
    }
    /**
     * fast getByte - only works when position%8 = 0 - otherwise throws an IllegalStateException
     * advances the position by 8 bits
     *  
     * @return the byte at the current position
     */
    public byte getByte() {
        ensureByteBoundary();
        
        int bytePos = position>>3;
        position+=8;
        return b[idx(bytePos)];
    }

    /**
     * Copies bytes from the buffer to the given destination array.
     * Works only when position%8 = 0 - otherwise throws an IllegalStateException
     * 
     * @param dst - destination array
     */
    public void getByteArray(byte[] dst) {
        ensureByteBoundary();
        int bytePos = idx(position>>3);
        
        System.arraycopy(b, bytePos, dst, 0, dst.length);
        position += (dst.length<<3);
    }
    
    /**
     * returns the size of the buffer (from the offset to the end of the byte array) in bits
     * @return size in bits
     */
    public int sizeInBits() {
        return (b.length-offset)<<3;
    }
    
    /**
     * returns the backing array length in bytes!
     * @return array length
     */
    public int arrayLength() {
        return b.length;
    }
    
    private int idx(int i) {
        return i+offset;
    }

    /**
     * Creates a new BitBuffer backed by the same array but with the offset set at the current position of this buffer
     * Works only when position%8 = 0 - otherwise throws an IllegalStateException
     * 
     * @return new bit buffer
     */
    public BitBuffer slice() {
        ensureByteBoundary();
        return new BitBuffer(b, position>>3);
    }

    public byte[] array() {
        return b;
    }

    /**
     * Returns the offset inside the byte array where this buffer starts
     * @return
     */
    public int offset() {
        return offset;
    }

    /**
     * Returns the remaining bytes from position until the end of the buffer.
     * Works only when position%8 = 0 - otherwise throws an IllegalStateException
     * @return
     */
    public int remainingBytes() {
        ensureByteBoundary();
        return b.length-offset-(position>>3);
    }
} 