package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class SubCarry extends Instruction {

	public SubCarry(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		byte value = cpu.load(mode);
		sbc(cpu, value);
	}

	public static void sbc(final Cpu cpu, final byte value) {
		Register sr = cpu.getSr();
		Register ac = cpu.getAc();

		byte borrow = sr.isBitSet(SRFlag.CARRY) ? (byte) 0x00 : (byte) 0x01;
		byte acBeforeSub = ac.get();

		short diff = (short) ((acBeforeSub & 0xFF) - (value & 0xFF) - borrow);

		ac.set((byte) diff);

		sr.setBit(SRFlag.CARRY, (diff & 0xFFFF) <= 255);
		sr.setBit(SRFlag.OVERFLOW, ((acBeforeSub & 0x80) != (value & 0x80)) && ((acBeforeSub & 0x80) != (diff & 0x80)));

		cpu.checkZeroNegative(ac);
	}
}