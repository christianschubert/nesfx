package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class AddCarry extends Instruction {

	public AddCarry(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		byte value = cpu.load(mode);
		adc(cpu, value);
	}

	public static void adc(final Cpu cpu, final byte value) {
		Register sr = cpu.getSr();
		Register ac = cpu.getAc();

		byte carry = sr.isBitSet(SRFlag.CARRY) ? (byte) 0x01 : (byte) 0x00;
		byte acBeforeAdd = ac.get();

		short sum = (short) ((acBeforeAdd & 0xFF) + (value & 0xFF) + carry);

		ac.set((byte) sum);

		sr.setBit(SRFlag.CARRY, (sum & 0xFFFF) > 255);
		sr.setBit(SRFlag.OVERFLOW, ((acBeforeAdd & 0x80) == (value & 0x80)) && ((acBeforeAdd & 0x80) != (sum & 0x80)));

		cpu.checkZeroNegative(ac);
	}
}