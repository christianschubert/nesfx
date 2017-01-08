package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class IncReg extends Instruction {

	private Register register;

	public IncReg(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final Register register) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.register = register;
	}

	@Override
	public void execute(final Cpu cpu) {
		register.inc();
		cpu.checkZeroNegative(register);
	}
}