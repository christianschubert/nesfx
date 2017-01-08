package nesfx.common;

public class ShiftRegister extends Register {
  public void shift() {
    reg <<= 1;
  }
}