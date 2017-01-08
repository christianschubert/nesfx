package nesfx.common;

public class Register16 {

	protected short reg;

	public Register16() {
		reset();
	}

	public void reset() {
		reg = (short) 0x0000;
	}

	public void set(final short value) {
		reg = value;
	}

	public short get() {
		return reg;
	}

	public void inc() {
		reg++;
	}

	public void inc(final short times) {
		reg += times;
	}

	public void dec() {
		reg--;
	}

	public void dec(final short times) {
		reg -= times;
	}

	public void setBit(final int pos, final boolean set) {
		if (set) {
			setBit(pos);
		} else {
			clearBit(pos);
		}
	}

	public void setBit(final int pos) {
		reg |= (0x01 << pos);
	}

	public void clearBit(final int pos) {
		reg &= ~(0x01 << pos);
	}

	public boolean isBitSet(final int pos) {
		return ByteUtils.isBitSetShort(reg, pos);
	}

	@Override
	public String toString() {
		return "Hex: " + ByteUtils.formatAddress(reg) + "\nBin: " + ByteUtils.formatShortToBin(reg) + "\nDec: "
				+ (reg & 0xFFFF);
	}
}