package nesfx.ppu.memory;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Memory;
import nesfx.common.Register;

public class Oam {
	private Memory oam = new Memory(Constants.PRIMARY_OAM_SIZE);
	private Register oamAddr = new Register();

	public void reset() {
		oamAddr.reset();
	}

	public void setAddress(final byte b) {
		oamAddr.set(b);
	}

	public byte read() {
		return oam.read(oamAddr.get());
	}

	public void write(final byte b) {
		oam.write(ByteUtils.bytesToAddress((byte) 0x00, oamAddr.get()), b);
		oamAddr.inc();
	}
}