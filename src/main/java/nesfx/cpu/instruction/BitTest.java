package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class BitTest extends Instruction {

	public BitTest(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		Register sr = cpu.getSr();
		byte value = cpu.load(mode);
		sr.setBit(SRFlag.NEGATIVE, ByteUtils.isBitSet(value, 7));
		sr.setBit(SRFlag.OVERFLOW, ByteUtils.isBitSet(value, 6));
		sr.setBit(SRFlag.ZERO, (cpu.getAc().get() & value) == (byte) 0x00);
	}
}