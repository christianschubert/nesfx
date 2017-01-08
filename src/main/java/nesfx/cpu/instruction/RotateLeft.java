package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class RotateLeft extends Instruction {

	public RotateLeft(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		if (mode == AddrMode.Accumulator) {
			Register ac = cpu.getAc();
			ac.set(rol(cpu, ac.get()));
		}
		else {
			short address = cpu.getAddress(mode);
			byte value = cpu.load(address);
			cpu.store(address, rol(cpu, value));
		}
	}

	public static byte rol(final Cpu cpu, final byte value) {
		Register sr = cpu.getSr();

		byte carry = sr.isBitSet(SRFlag.CARRY) ? (byte) 0x01 : (byte) 0x00;
		sr.setBit(SRFlag.CARRY, ByteUtils.isBitSet(value, 7));

		byte rotated = (byte) ((value << 1) | carry);

		cpu.checkZero(rotated);
		cpu.checkNegative(rotated);

		return rotated;
	}
}