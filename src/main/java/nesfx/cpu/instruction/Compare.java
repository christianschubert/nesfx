package nesfx.cpu.instruction;

import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class Compare extends Instruction {

	private Register register;

	public Compare(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo, final Register register) {
		super(mode, cycles, checkPageCross, debugInfo);
		this.register = register;
	}

	@Override
	public void execute(final Cpu cpu) {
		byte value = cpu.load(mode);
		cmp(cpu, register, value);
	}

	public static void cmp(final Cpu cpu, final Register register, final byte value) {
		Register sr = cpu.getSr();
		if ((register.get() & 0xFF) >= (value & 0xFF)) {
			sr.setBit(SRFlag.CARRY);
		}
		else {
			sr.clearBit(SRFlag.CARRY);
		}

		byte cmpValue = (byte) (register.get() - value);
		cpu.checkZero(cmpValue);
		cpu.checkNegative(cmpValue);
	}
}