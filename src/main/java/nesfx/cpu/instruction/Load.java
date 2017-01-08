package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Load extends Instruction {

	private Register register;

	public Load(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final Register register) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.register = register;
	}

	@Override
	public void execute(final Cpu cpu) {
		register.set(cpu.load(mode));
		cpu.checkZeroNegative(register);
	}
}