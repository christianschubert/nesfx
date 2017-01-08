package nesfx.window;

import java.nio.IntBuffer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import nesfx.common.Constants;
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

		int[] pixels = new int[(int) getWidth() * (int) getHeight()];

		for (int row = 0; row < displayBuffer.getHeight(); row++) {
			for (int col = 0; col < displayBuffer.getWidth(); col++) {
				int argbColor = displayBuffer.getPixel(row, col);

				for (int i = 0; i < scale; i++) {
					for (int j = 0; j < scale; j++) {
						int index = (row * scale + j) * (int) getWidth() + col * scale + i;
						pixels[index] = argbColor;
					}
				}
			}
		}

		writer.setPixels(0, 0, (int) getWidth(), (int) getHeight(), format, pixels, 0, (int) getWidth());

	}
}