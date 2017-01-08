package nesfx.common;

public class Register {

  protected byte reg;

  public Register() {
    reset();
  }

  public void reset() {
    reg = (byte) 0x00;
  }

  public void set(final byte value) {
    reg = value;
  }

  public byte get() {
    return reg;
  }

  public void inc() {
    reg++;
  }

  public void inc(final byte times) {
    reg += times;
  }

  public void dec() {
    reg--;
  }

  public void dec(final byte times) {
    reg -= times;
  }

  public void setBit(final int pos, final boolean set) {
    if (set) {
      setBit(pos);
    }
    else {
      clearBit(pos);
    }
  }

  public void setBit(final int pos) {
    reg |= (0x01 << pos);
  }

  public void clearBit(final int pos) {
    reg &= ~(0x01 << pos);
  }

  public boolean isBitSet(final int pos) {
    return ByteUtils.isBitSet(reg, pos);
  }

  @Override
  public String toString() {
    return "Hex: " + ByteUtils.formatByte(reg) + "\nBin: " + ByteUtils.formatByteToBin(reg) + "\nDec: " + (reg & 0xFF);
  }
}