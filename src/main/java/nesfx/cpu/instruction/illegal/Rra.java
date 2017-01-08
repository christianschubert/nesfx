package nesfx.cpu.instruction.illegal;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.AddCarry;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.instruction.RotateRight;

public class Rra extends Instruction {

	public Rra(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		byte value = cpu.load(address);
		byte rotated = RotateRight.ror(cpu, value);
		cpu.store(address, rotated);

		AddCarry.adc(cpu, rotated);
	}
}