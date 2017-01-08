package nesfx.ppu.register;

public class PPUCTRL {
	public static final int BASE_NAMETABLE_ADDRESS_LOW = 0x00;
	public static final int BASE_NAMETABLE_ADDRESS_HIGH = 0x01;
	public static final int VRAM_ADDRESS_INCREMENT = 0x02;
	public static final int SPRITE_PATTERN_TABLE_ADDRESS = 0x03;
	public static final int BG_PATTERN_TABLE_ADDRESS = 0x04;
	public static final int SPRITE_SIZE = 0x05;
	public static final int PPU_MASTER_SLAVE_SELECT = 0x06;
	public static final int GENERATE_NMI_VBLANK = 0x07;
}
