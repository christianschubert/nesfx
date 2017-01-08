package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.Compare;
import nesfx.cpu.instruction.DecMem;
import nesfx.cpu.instruction.Instruction;

public class Dcp extends Instruction {

	public Dcp(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		byte value = cpu.load(address);
		byte dec = DecMem.dec(cpu, value);
		cpu.store(address, dec);

		Compare.cmp(cpu, cpu.getAc(), dec);
	}
}