package nesfx.ppu.memory;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Memory;
import nesfx.rom.Rom.Mirroring;

public class Nametables {
	private Memory vram = new Memory(Constants.VRAM_SIZE);

	private Mirroring mirroring;

	public Nametables(final Mirroring mirroring) {
		this.mirroring = mirroring;
	}

	public void write(final short address, final byte b) {
		if (mirroring == Mirroring.Vertical) {
			vram.write((short) ((address & 0xFFFF) % 0x0800), b);
		} else if (mirroring == Mirroring.Horizontal) {
			short diff = (short) 0x0000;
			if (ByteUtils.isBetween(address, (short) 0x0400, (short) 0x0BFF)) {
				diff = (short) 0x0400;
			} else if (ByteUtils.isBetween(address, (short) 0x0C00, (short) 0x0FFF)) {
				diff = (short) 0x0800;
			}
			vram.write((short) ((address & 0xFFFF) - diff), b);
		} else {
			// TODO
		}
	}

	public byte read(final short address) {
		if (mirroring == Mirroring.Vertical) {
			return vram.read((short) ((address & 0xFFFF) % 0x0800));
		} else if (mirroring == Mirroring.Horizontal) {
			short diff = (short) 0x0000;
			if (ByteUtils.isBetween(address, (short) 0x0400, (short) 0x0BFF)) {
				diff = (short) 0x0400;
			} else if (ByteUtils.isBetween(address, (short) 0x0C00, (short) 0x0FFF)) {
				diff = (short) 0x0800;
			}
			return vram.read((short) ((address & 0xFFFF) - diff));
		} else {
			// TODO
		}
		return (byte) 0x00;
	}
}
