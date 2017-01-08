package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class Eor extends Instruction {

	public Eor(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		byte value = cpu.load(mode);
		eor(cpu, value);
	}

	public static void eor(final Cpu cpu, final byte value) {
		Register ac = cpu.getAc();
		ac.set((byte) (ac.get() ^ value));
		cpu.checkZeroNegative(ac);
	}
}