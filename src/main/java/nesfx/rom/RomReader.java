package nesfx.rom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.rom.Rom.Mirroring;
import nesfx.rom.Rom.TvSystem;

public class RomReader {

	Logger logger = LoggerFactory.getLogger(RomReader.class);
	private Rom rom;

	public RomReader(final Rom rom) {
		this.rom = rom;
	}

	public boolean read() {
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(rom.getPath(), "r");

			byte[] header = new byte[16];
			raf.read(header);

			if (!isValidHeader(header)) {
				logger.error("Invalid rom header.");
				return false;
			}

			extractHeader(header);

			if (rom.hasTrainer()) {
				byte[] trainer = new byte[Constants.TRAINER_SIZE];
				raf.read(trainer);
				rom.setTrainer(trainer);
			}

			byte[] prg = new byte[rom.getPrgSize() * 16384];
			raf.read(prg);
			rom.getPrg().storeData(prg);

			byte[] chr = new byte[rom.getChrSize() * 8192];
			raf.read(chr);
			rom.getChr().storeData(chr);

		} catch (FileNotFoundException e) {
			logger.error("File not found.");
			return false;
		} catch (IOException e) {
			logger.error("Error reading file.", e);
			return false;
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					logger.error("Could not close RandomAccessFile.");
				}
			}
		}
		return true;
	}

	private void extractHeader(final byte[] header) {
		rom.setPrgSize(header[4] & 0xFF);
		rom.setChrSize(header[5] & 0xFF);

		byte flag6 = header[6];
		byte flag7 = header[7];

		int mapper = ByteUtils.bytesToAddress(ByteUtils.getLowNibble(flag7), ByteUtils.getHighNibble(flag6));
		rom.setMapper(mapper);

		if (ByteUtils.isBitSet(flag6, 3)) {
			rom.setMirroring(Mirroring.FourScreen);
		} else {
			rom.setMirroring(ByteUtils.isBitSet(flag6, 0) ? Mirroring.Vertical : Mirroring.Horizontal);
		}

		rom.setHasTrainer(ByteUtils.isBitSet(flag6, 2));
		rom.setHasRam(ByteUtils.isBitSet(flag6, 1));
		rom.setPrgRamSize(header[8] & 0xFF);

		byte flag9 = header[9];
		rom.setTvSystem(ByteUtils.isBitSet(flag9, 0) ? TvSystem.PAL : TvSystem.NTSC);
	}

	private boolean isValidHeader(final byte[] header) {
		boolean isValid = true;
		for (int i = 0; i < Constants.ROM_HEADER.length; i++) {
			if (header[i] != Constants.ROM_HEADER[i]) {
				isValid = false;
			}
		}

		return isValid;
	}
}
