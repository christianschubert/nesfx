package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class PullReg extends Instruction {

	private Register register;

	public PullReg(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final Register register) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.register = register;
	}

	@Override
	public void execute(final Cpu cpu) {
		if (register == cpu.getAc()) {
			register.set(cpu.pull());
			cpu.checkZeroNegative(register);
		} else if (register == cpu.getSr()) {
			cpu.pullSr();
		}
	}
}