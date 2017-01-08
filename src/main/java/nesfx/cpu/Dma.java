package nesfx.cpu;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.cpu.memory.CpuMemoryMapper;

public class Dma {

	private CpuMemoryMapper memory;
	private boolean request;
	private boolean active;
	private boolean last;
	private boolean transferComplete;
	private byte page;
	private byte addressLow;
	private byte readValue;

	public Dma(final CpuMemoryMapper memory) {
		this.memory = memory;
	}

	public void setRequest(final byte page) {
		this.page = page;
		request = true;
	}

	public boolean isRequest() {
		return request;
	}

	public void startTransfer() {
		request = false;
		transferComplete = false;
		active = true;
		last = false;
		addressLow = (byte) 0x00;

		// first cycle = dummy read
	}

	public void stopTransfer() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}

	public void cycle(final boolean isOddCycle) {
		if (!isOddCycle) {
			// read cycle

			readValue = memory.read(ByteUtils.bytesToAddress(page, addressLow));
			addressLow++;

			if (addressLow == (byte) 0x00) {
				// overflow -> last
				last = true;
			}
		} else {
			// write cycle

			if (addressLow == (byte) 0x00 && !last) {
				// no read happened -> align cycles
				return;
			}
			memory.write(Constants.PPU_OAM_RW, readValue);

			if (last) {
				transferComplete = true;
			}
		}
	}

	public boolean isTransferComplete() {
		return transferComplete;
	}
}