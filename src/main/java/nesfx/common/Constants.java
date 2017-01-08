package nesfx.common;

public class Constants {

	// Emulator
	public static final int FPS_TIMER_INTERVAL = 1000;
	public static final int DEFAULT_SCREEN_SCALE = 2;

	// CPU
	public static final int CPU_CLOCK = 1789773; // 1.789773 MHz
	public static final int CPU_TIME_PER_CYCLE = (int) Math.round((1 / (double) CPU_CLOCK * 1000000000));
	public static final int CPU_PPU_DIVISOR = 3;
	public static final short NMI_VECTOR = (short) 0xFFFA;
	public static final short RESET_VECTOR = (short) 0xFFFC;
	public static final short IRQ_BRK_VECTOR = (short) 0xFFFE;
	public static final byte SP_START = (byte) 0xFD;
	public static final byte SR_START = (byte) 0x34;

	// PPU
	public static final int PRIMARY_OAM_SIZE = 256;
	public static final int SECONDARY_OAM_SIZE = 32;
	public static final int VRAM_SIZE = 2048;
	public static final int PALETTE_SIZE = 32;

	// OAM DMA
	public static final short OAM_DMA_ADDR = (short) 0x4014;
	public static final short PPU_OAM_RW = (short) 0x2004;

	// Controller
	public static final int CONTROLLER_COUNT = 2;
	public static final int BUTTON_COUNT = 8;

	// Internal Ram
	public static final int RAM_SIZE = 2048;

	// Display
	public static final int NTSC_DISPLAY_WIDTH = 256;
	public static final int NTSC_DISPLAY_HEIGHT = 240;
	public static final byte DISPLAY_CLEAR_COLOR = (byte) 0x0F; // black

	// Rom
	public static final byte[] ROM_HEADER = { (byte) 0x4E, (byte) 0x45, (byte) 0x53, (byte) 0x1A };
	public static final int TRAINER_SIZE = 256;
}
