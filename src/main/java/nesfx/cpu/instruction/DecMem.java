package nesfx.cpu.instruction;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public class DecMem extends Instruction {

	public DecMem(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		short address = cpu.getAddress(mode);
		cpu.store(address, dec(cpu, cpu.load(address)));
	}

	public static byte dec(final Cpu cpu, final byte value) {
		byte dec = (byte) (value - 1);
		cpu.checkZero(dec);
		cpu.checkNegative(dec);

		return dec;
	}
}