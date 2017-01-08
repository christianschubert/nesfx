package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Branch extends Instruction {

	private int flag;
	private boolean set;

	public Branch(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final int flag, final boolean set) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.flag = flag;
		this.set = set;
	}

	@Override
	public void execute(final Cpu cpu) {
		Register sr = cpu.getSr();

		if ((set && sr.isBitSet(flag)) || (!set && !sr.isBitSet(flag))) {
			cpu.jumpTo((short) (cpu.getAddress(mode) + 2));
		}
	}
}