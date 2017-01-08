package nesfx.ppu;

import nesfx.common.Constants;

public class DisplayBuffer {
  private byte[][] display = new byte[Constants.NTSC_DISPLAY_HEIGHT][Constants.NTSC_DISPLAY_WIDTH];

  public DisplayBuffer() {
    clear();
  }

  public void clear() {
    for (int row = 0; row < display.length; row++) {
      for (int col = 0; col < display[row].length; col++) {
        display[row][col] = Constants.DISPLAY_CLEAR_COLOR;
      }
    }
  }

  public void drawPixel(final int row, final int col, final byte color) {
    display[row][col] = color;
  }

  public byte getPixel(final int row, final int col) {
    return display[row][col];
  }
}