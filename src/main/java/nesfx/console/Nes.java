package nesfx.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nesfx.common.Constants;
import nesfx.common.GUIUtils;
import nesfx.cpu.Cpu;
import nesfx.cpu.memory.CpuMemoryMapper;
import nesfx.input.ControllerBuffer;
import nesfx.ppu.DisplayBuffer;
import nesfx.ppu.Ppu;
import nesfx.ppu.memory.PpuMemoryMapper;
import nesfx.rom.Rom;
import nesfx.window.MainWindow;

public class Nes {

	Logger logger = LoggerFactory.getLogger(MainWindow.class);

	private Cpu cpu;
	private Ppu ppu;
	private ControllerBuffer controllerBuffer;

	public boolean init(final String romPath) {

		Rom rom = new Rom(romPath);
		if (!rom.isValid()) {
			GUIUtils.showWarningDialog("Rom not valid.");
			return false;
		}

		logger.info(rom.toString());

		controllerBuffer = new ControllerBuffer();

		PpuMemoryMapper ppuMemory = new PpuMemoryMapper(rom);
		ppu = new Ppu(ppuMemory);

		CpuMemoryMapper cpuMemory = new CpuMemoryMapper(rom, ppu, controllerBuffer);
		cpu = new Cpu(cpuMemory);
		ppu.setCpu(cpu);

		return true;
	}

	public void runCycles(final long delta) {
		int cycles = (int) (delta / Constants.CPU_TIME_PER_CYCLE);
		for (int i = 0; i < cycles; i++) {
			runSingleCycle();
		}
	}

	public void runSingleCycle() {
		cpu.cycle();
		for (int j = 0; j < Constants.CPU_PPU_DIVISOR; j++) {
			ppu.cycle();
		}
	}

	public void reset() {
		cpu.reset();
		controllerBuffer.reset();
		ppu.reset();
	}

	public boolean isRenderingEnabled() {
		return ppu.isRenderingEnabled();
	}

	public DisplayBuffer getDisplayBuffer() {
		return ppu.getDisplayBuffer();
	}

	public void setKeyPressed(final int controller, final Integer button) {
		controllerBuffer.setKeyPressed(controller, button);
	}

	public void setKeyReleased(final int controller, final Integer button) {
		controllerBuffer.setKeyReleased(controller, button);
	}
}