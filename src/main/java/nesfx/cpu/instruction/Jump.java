package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Jump extends Instruction {

	private boolean saveReturnAddress;

	public Jump(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final boolean saveReturnAddress) {
		super(mode, cycles, saveReturnAddress, debugInfo);
		this.saveReturnAddress = saveReturnAddress;
	}

	@Override
	public void execute(final Cpu cpu) {
		if (saveReturnAddress) {
			short pc = (short) (cpu.getPc().get() + 2);
			cpu.push(ByteUtils.getHighByte(pc));
			cpu.push(ByteUtils.getLowByte(pc));
		}
		cpu.jumpTo(cpu.getAddress(mode));
	}
}