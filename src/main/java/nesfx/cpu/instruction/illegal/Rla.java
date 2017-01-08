package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.And;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.instruction.RotateLeft;

public class Rla extends Instruction {

	public Rla(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		byte value = cpu.load(address);
		byte rotated = RotateLeft.rol(cpu, value);
		cpu.store(address, rotated);

		And.and(cpu, rotated);
	}
}