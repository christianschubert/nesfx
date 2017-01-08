package nesfx.common;

public class ShiftRegister16 extends Register16 {

  public void loadLowerByte(final byte b) {
    reg = ByteUtils.bytesToAddress(ByteUtils.getHighByte(reg), b);
  }

  public void shift() {
    reg <<= 1;
  }
}