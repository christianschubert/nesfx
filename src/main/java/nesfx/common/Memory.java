package nesfx.common;

public class Memory {

	private byte[] memory;

	public Memory(final int size) {
		memory = new byte[size];
	}

	public void reset() {
		for (int i = 0; i < memory.length; i++) {
			memory[i] = (byte) 0x00;
		}
	}

	public byte read(final short address) {
		return memory[address & 0xFFFF];
	}

	public void write(final short address, final byte b) {
		memory[address & 0xFFFF] = b;
	}

	public void storeData(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			memory[i] = data[i];
		}
	}

	public int getSize() {
		return memory.length;
	}

	@Override
	public String toString() {
		String buffer = "";
		short address = 0;

		for (int i = 0; i < memory.length; i++) {
			if (i % 16 == 0) {
				if (i != 0) {
					buffer += "\n";
				}
				buffer += String.format("%04X: ", address);
				address += 16;
			}

			buffer += String.format("%02X ", memory[i]);
		}

		return buffer;
	}
}