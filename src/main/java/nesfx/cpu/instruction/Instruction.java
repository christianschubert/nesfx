package nesfx.cpu.instruction;

import nesfx.cpu.AddrMode;
import nesfx.cpu.Cpu;

public abstract class Instruction {

	protected AddrMode mode;
	protected int cycles;
	private boolean checkPageCross;

	private String debugInfo;

	public Instruction(final AddrMode mode, final int cycles, final boolean checkPageCross, final String debugInfo) {
		this.mode = mode;
		this.cycles = cycles;
		this.checkPageCross = checkPageCross;
		this.debugInfo = debugInfo;
	}

	abstract public void execute(Cpu cpu);

	public int getCycles() {
		return cycles;
	}

	public AddrMode getAddrMode() {
		return mode;
	}

	public String getDebugInfo() {
		return debugInfo;
	}

	public boolean isCheckPageCross() {
		return checkPageCross;
	}
}
