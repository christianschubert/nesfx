package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class PushReg extends Instruction {

	private Register register;

	public PushReg(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final Register register) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.register = register;
	}

	@Override
	public void execute(final Cpu cpu) {
		if (register == cpu.getAc()) {
			cpu.push(register.get());
		} else if (register == cpu.getSr()) {
			cpu.push((byte) (register.get() | (byte) 0x30));
		}
	}
}