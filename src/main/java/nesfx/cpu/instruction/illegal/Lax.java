package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.Instruction;

public class Lax extends Instruction {

	public Lax(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		byte value = cpu.load(mode);
		cpu.getAc().set(value);
		cpu.getX().set(value);
		cpu.checkZeroNegative(cpu.getAc());
	}
}