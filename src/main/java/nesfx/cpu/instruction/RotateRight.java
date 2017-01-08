package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class RotateRight extends Instruction {

	public RotateRight(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		if (mode == AddrMode.Accumulator) {
			Register ac = cpu.getAc();
			ac.set(ror(cpu, ac.get()));
		}
		else {
			short address = cpu.getAddress(mode);
			byte value = cpu.load(address);
			cpu.store(address, ror(cpu, value));
		}
	}

	public static byte ror(final Cpu cpu, final byte value) {
		Register sr = cpu.getSr();

		byte carry = sr.isBitSet(SRFlag.CARRY) ? (byte) 0x01 : (byte) 0x00;
		sr.setBit(SRFlag.CARRY, ByteUtils.isBitSet(value, 0));

		byte rotated = (byte) (value >> 1);
		rotated &= ~0x80;
		rotated |= (carry << 7);

		cpu.checkZero(rotated);
		cpu.checkNegative(rotated);

		return rotated;
	}
}