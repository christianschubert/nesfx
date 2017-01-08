package nesfx.common;

public class ByteUtils {

	public static final short bytesToAddress(final byte high, final byte low) {
		return (short) (((high & 0XFF) << 8) | (low & 0xFF));
	}

	public static final byte getHighByte(final short address) {
		return (byte) ((address >> 8) & 0xFF);
	}

	public static final byte getLowByte(final short address) {
		return (byte) (address & 0xFF);
	}

	public static final byte getHighNibble(final byte b) {
		return (byte) ((b >> 4) & 0x0F);
	}

	public static final byte getLowNibble(final byte b) {
		return (byte) (b & 0x0F);
	}

	public static final String formatAddress(final short address) {
		return String.format("%04X", address);
	}

	public static final String formatByte(final byte b) {
		return String.format("%02X", b);
	}

	public static final String formatByteToBin(final byte b) {
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}

	public static final String formatShortToBin(final short s) {
		return String.format("%16s", Integer.toBinaryString(s & 0xFFFF)).replace(' ', '0');
	}

	public static final boolean isBitSet(final byte b, final int pos) {
		return (b & (0x01 << pos)) != 0;
	}

	public static final boolean isBitSetShort(final short s, final int pos) {
		return (s & (0x01 << pos)) != 0;
	}

	public static boolean isBetween(final short address, final short start, final short stop) {
		return (address & 0xFFFF) >= (start & 0xFFFF) && (address & 0xFFFF) <= (stop & 0xFFFF);
	}
}