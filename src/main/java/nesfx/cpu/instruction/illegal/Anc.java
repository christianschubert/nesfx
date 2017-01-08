package nesfx.cpu.instruction.illegal;

import nesfx.common.ByteUtils;
import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;
import nesfx.cpu.instruction.And;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.register.SRFlag;

public class Anc extends Instruction {

	public Anc(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		super(mode, cycles, checkPageCross, debugInfo);
	}

	@Override
	public void execute(final Cpu cpu) {
		byte value = cpu.load(mode);
		And.and(cpu, value);

		// result is negative -> carry is set
		cpu.getSr().setBit(SRFlag.CARRY, ByteUtils.isBitSet(cpu.getAc().get(), 7));
	}
}