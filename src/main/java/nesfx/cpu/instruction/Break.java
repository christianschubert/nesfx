package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class Break extends Instruction {

	public Break(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		Register sr = cpu.getSr();

		short pc = (short) (cpu.getPc().get() + 2);
		cpu.push(ByteUtils.getHighByte(pc));
		cpu.push(ByteUtils.getLowByte(pc));

		cpu.push((byte) (sr.get() | (byte) 0x30));

		sr.setBit(SRFlag.INTERRUPT);

		cpu.jumpTo(cpu.indirectAddress(Constants.IRQ_BRK_VECTOR));
	}
}