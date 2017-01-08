package nesfx.window;

import java.nio.IntBuffer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import nesfx.common.Constants;
import nesfx.ppu.DefaultPalette;
import nesfx.ppu.DisplayBuffer;

public class DisplayCanvas extends Canvas {

  private int scale;
  private GraphicsContext gc;
  private WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();

  public DisplayCanvas() {
    super();
    setScale(Constants.DEFAULT_SCREEN_SCALE);
    gc = getGraphicsContext2D();
  }

  public void setScale(final int scale) {
    this.scale = scale;
    setWidth(Constants.NTSC_DISPLAY_WIDTH * scale);
    setHeight(Constants.NTSC_DISPLAY_HEIGHT * scale);
  }

  public void draw(final DisplayBuffer displayBuffer) {

    PixelWriter writer = gc.getPixelWriter();

    int[] pixels = new int[(int) (getWidth() * getHeight())];

    for (int x = 0; x < getWidth(); x++) {
      for (int y = 0; y < getHeight(); y++) {
        int index = x + y * (int) getWidth();

        byte color = displayBuffer.getPixel(y, x);
        int hex = DefaultPalette.palette[color];
        int a = 0xff << 24;
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);

        pixels[index] = a | (r << 16) | (g << 8) | b;
      }

    }

    writer.setPixels(0, 0, (int) getWidth(), (int) getHeight(), format, pixels, 0, (int) getWidth());

  }
}