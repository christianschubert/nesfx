package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.Instruction;

public class Sax extends Instruction {

	public Sax(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		byte b = (byte) (cpu.getAc().get() & cpu.getX().get());
		cpu.store(cpu.getAddress(mode), b);
	}
}