package nesfx.cpu.instruction;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Nop extends Instruction {

	public Nop(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		// NOP
	}
}