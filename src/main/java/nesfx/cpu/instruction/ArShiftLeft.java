package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class ArShiftLeft extends Instruction {

	public ArShiftLeft(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		if (mode == AddrMode.Accumulator) {
			Register ac = cpu.getAc();
			ac.set(asl(cpu, ac.get()));
		}
		else {
			short address = cpu.getAddress(mode);
			byte value = cpu.load(address);
			cpu.store(address, asl(cpu, value));
		}
	}

	public static byte asl(final Cpu cpu, final byte value) {
		cpu.getSr().setBit(SRFlag.CARRY, ByteUtils.isBitSet(value, 7));

		byte shifted = (byte) (value << 1);

		cpu.checkZero(shifted);
		cpu.checkNegative(shifted);

		return shifted;
	}
}