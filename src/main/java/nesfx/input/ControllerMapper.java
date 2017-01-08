package nesfx.input;

import nesfx.common.ByteUtils;
import nesfx.common.Register;

public class ControllerMapper {

  private ControllerBuffer controllerBuffer;
  private ControllerBuffer saveBuffer;

  private Register reg4016 = new Register();
  private Register reg4017 = new Register();

  private boolean strobe;

  public ControllerMapper(final ControllerBuffer controllerBuffer) {
    this.controllerBuffer = controllerBuffer;
    saveBuffer = new ControllerBuffer();
  }

  public void reset() {
    strobe = false;
    controllerBuffer.reset();
    saveBuffer.reset();
    reg4016.reset();
    reg4017.reset();
  }

  public void write4016(final byte b) {
    reg4016.set(b);

    if (ByteUtils.isBitSet(b, 0)) {
      strobe = true;
    }
    else if (strobe) {
      // strobe high to low -> save state of buffer
      strobe = false;
      saveBuffer.save(controllerBuffer);
    }
  }

  public void write4017(final byte b) {
    reg4017.set(b);
  }

  public byte read4016() {
    if (strobe) {
      return (byte) (controllerBuffer.isKeyPressed(0, Button.A) ? 0x01 : 0x00);
    }
    else {
      return (byte) (saveBuffer.isNextKeyPressed(0) ? 0x01 : 0x00);
    }
  }

  public byte read4017() {
    if (strobe) {
      return (byte) (controllerBuffer.isKeyPressed(1, Button.A) ? 0x01 : 0x00);
    }
    else {
      return (byte) (saveBuffer.isNextKeyPressed(1) ? 0x01 : 0x00);
    }
  }
}
