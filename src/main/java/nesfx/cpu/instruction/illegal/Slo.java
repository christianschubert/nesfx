package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.ArShiftLeft;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.instruction.Ora;

public class Slo extends Instruction {

	public Slo(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		byte value = cpu.load(address);
		byte shifted = ArShiftLeft.asl(cpu, value);
		cpu.store(address, shifted);

		Ora.ora(cpu, shifted);
	}
}