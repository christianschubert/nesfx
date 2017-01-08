package nesfx.ppu;

import nesfx.common.Constants;

public class DisplayBuffer {
	private int[][] display = new int[Constants.NTSC_DISPLAY_HEIGHT][Constants.NTSC_DISPLAY_WIDTH];

	public DisplayBuffer() {
		clear();
	}

	public int getWidth() {
		return display[0].length;
	}

	public int getHeight() {
		return display.length;
	}

	public void clear() {
		for (int row = 0; row < display.length; row++) {
			for (int col = 0; col < display[row].length; col++) {
				drawPixel(row, col, Constants.DISPLAY_CLEAR_COLOR);
			}
		}
	}

	public void drawPixel(final int row, final int col, final byte color) {
		// get hex value for byte color of palette
		int hex = DefaultPalette.palette[color];

		// compute argb value
		int a = 0xff << 24;
		int r = (hex & 0xFF0000) >> 16;
		int g = (hex & 0xFF00) >> 8;
		int b = (hex & 0xFF);

		// set argb value to displaybuffer
		display[row][col] = a | (r << 16) | (g << 8) | b;
	}

	public int getPixel(final int row, final int col) {
		return display[row][col];
	}
}