package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Return extends Instruction {

	private boolean isInterrupt;

	public Return(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo, final boolean isInterrupt) {
		super(mode, cycles, isInterrupt, debugInfo);
		this.isInterrupt = isInterrupt;
	}

	@Override
	public void execute(final Cpu cpu) {
		if (isInterrupt) {
			cpu.pullSr();
		}

		byte low = cpu.pull();
		byte high = cpu.pull();

		cpu.jumpTo(ByteUtils.bytesToAddress(high, low));

		if (!isInterrupt) {
			cpu.getPc().inc();
		}
	}
}