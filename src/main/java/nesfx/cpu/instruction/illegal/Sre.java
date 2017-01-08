package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.Eor;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.instruction.LogShiftRight;

public class Sre extends Instruction {

	public Sre(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		byte value = cpu.load(address);
		byte shifted = LogShiftRight.lsr(cpu, value);
		cpu.store(address, shifted);

		Eor.eor(cpu, shifted);
	}
}