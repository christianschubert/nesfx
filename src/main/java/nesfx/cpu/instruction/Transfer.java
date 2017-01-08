package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Transfer extends Instruction {

	private Register regFrom, regTo;

	public Transfer(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final Register regFrom, final Register regTo) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.regFrom = regFrom;
		this.regTo = regTo;
	}

	@Override
	public void execute(final Cpu cpu) {
		regTo.set(regFrom.get());

		if (regTo != cpu.getSp()) {
			// no status checking for TXS
			cpu.checkZeroNegative(regTo);
		}
	}
}