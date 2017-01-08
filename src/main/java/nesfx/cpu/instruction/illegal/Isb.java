package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.IncMem;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.instruction.SubCarry;

public class Isb extends Instruction {

	public Isb(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		byte value = cpu.load(address);
		byte inc = IncMem.inc(cpu, value);
		cpu.store(address, inc);

		SubCarry.sbc(cpu, inc);
	}
}