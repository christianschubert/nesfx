package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class LogShiftRight extends Instruction {

	public LogShiftRight(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		if (mode == AddrMode.Accumulator) {
			Register ac = cpu.getAc();
			ac.set(lsr(cpu, ac.get()));
		}
		else {
			short address = cpu.getAddress(mode);
			byte value = cpu.load(address);
			cpu.store(address, lsr(cpu, value));
		}
	}

	public static byte lsr(final Cpu cpu, final byte value) {
		cpu.getSr().setBit(SRFlag.CARRY, ByteUtils.isBitSet(value, 0));

		byte shifted = (byte) (value >> 1);
		shifted &= ~0x80;

		cpu.checkZero(shifted);
		cpu.checkNegative(shifted);

		return shifted;
	}
}