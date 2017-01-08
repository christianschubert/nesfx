package nesfx.cpu.instruction;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class SetClearFlag extends Instruction {

	private int flag;
	private boolean set;

	public SetClearFlag(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo,
			final int flag, final boolean set) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.flag = flag;
		this.set = set;
	}

	@Override
	public void execute(final Cpu cpu) {
		cpu.getSr().setBit(flag, set);
	}
}