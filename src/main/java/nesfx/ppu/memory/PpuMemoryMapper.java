package nesfx.ppu.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nesfx.common.ByteUtils;
import nesfx.rom.Rom;

/*
 * $0000-$1FFF CHR-ROM or CHR-RAM
 * $2000-$2FFF 2kB VRAM
 * $3000-$3EFF Mirror of $2000-$2EFF
 * $3F00-$3FFF Not configurable, mapped to the internal palette control
 */

public class PpuMemoryMapper {

	Logger logger = LoggerFactory.getLogger(PpuMemoryMapper.class);

	private Palettes palettes = new Palettes();
	private Nametables nametables;
	private Rom rom;

	public PpuMemoryMapper(final Rom rom) {
		this.rom = rom;
		nametables = new Nametables(rom.getMirroring());
	}

	public byte read(final short address) {
		if (ByteUtils.isBetween(address, (short) 0x0000, (short) 0x1FFF)) {
			return rom.getChr().read((short) (address & 0xFFFF));
		}
		else if (ByteUtils.isBetween(address, (short) 0x2000, (short) 0x3EFF)) {
			return nametables.read((short) (((address & 0xFFFF) - 0x2000) % 0x1000));
		}
		else if (ByteUtils.isBetween(address, (short) 0x3F00, (short) 0x3FFF)) {
			return palettes.read((short) (((address & 0xFFFF) - 0x3F00) % 0x0020));
		}
		return (byte) 0x00;
	}

	public void write(final short address, final byte b) {
		if (ByteUtils.isBetween(address, (short) 0x0000, (short) 0x1FFF)) {
			logger.error("Rom is read-only. (Address: 0x" + ByteUtils.formatAddress(address) + ")");
		}
		else if (ByteUtils.isBetween(address, (short) 0x2000, (short) 0x3EFF)) {
			nametables.write((short) (((address & 0xFFFF) - 0x2000) % 0x1000), b);
		}
		else if (ByteUtils.isBetween(address, (short) 0x3F00, (short) 0x3FFF)) {
			palettes.write((short) (((address & 0xFFFF) - 0x3F00) % 0x0020), b);
		}
	}
}
