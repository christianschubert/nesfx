package nesfx.ppu.memory;

import nesfx.common.Constants;
import nesfx.common.Memory;

public class Palettes {
	private Memory palettes = new Memory(Constants.PALETTE_SIZE);

	public byte read(final short address) {
		if ((address & 0xFFFF) == (short) 0x0010 || (address & 0xFFFF) == (short) 0x0014
				|| (address & 0xFFFF) == (short) 0x0018 || (address & 0xFFFF) == (short) 0x001C) {
			// mirroring
			return palettes.read((short) (address - (short) 0x0010));
		}
		return palettes.read(address);
	}

	public void write(final short address, final byte b) {
		if ((address & 0xFFFF) == (short) 0x0010 || (address & 0xFFFF) == (short) 0x0014
				|| (address & 0xFFFF) == (short) 0x0018 || (address & 0xFFFF) == (short) 0x001C) {
			// mirroring
			palettes.write((short) (address - (short) 0x0010), b);
			return;
		}
		palettes.write(address, b);
	}
}