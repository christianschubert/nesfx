package nesfx.cpu.instruction;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Register;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.register.SRFlag;

public class Nmi extends Instruction {

	public Nmi(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		Register sr = cpu.getSr();

		cpu.push(ByteUtils.getHighByte(cpu.getPc().get()));
		cpu.push(ByteUtils.getLowByte(cpu.getPc().get()));

		// BRK flag clear if hardware interrupt
		cpu.push((byte) (sr.get() | (byte) 0x20));

		sr.setBit(SRFlag.INTERRUPT);

		cpu.jumpTo(cpu.indirectAddress(Constants.NMI_VECTOR));
	}
}