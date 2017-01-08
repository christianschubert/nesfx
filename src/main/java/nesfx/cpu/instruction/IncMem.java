package nesfx.cpu.instruction;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class IncMem extends Instruction {

	public IncMem(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		cpu.store(address, inc(cpu, cpu.load(address)));
	}

	public static byte inc(final Cpu cpu, final byte value) {
		byte inc = (byte) (value + 1);
		cpu.checkZero(inc);
		cpu.checkNegative(inc);

		return inc;
	}
}