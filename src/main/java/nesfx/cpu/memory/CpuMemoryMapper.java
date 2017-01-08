package nesfx.cpu.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Memory;
import nesfx.input.ControllerBuffer;
import nesfx.input.ControllerMapper;
import nesfx.ppu.Ppu;
import nesfx.rom.Rom;

/*
 * $0000-$07FF 2KB internal RAM 
 * $0800-$0FFF Mirrors of $0000-$07FF 
 * $1000-$17FF
 * $1800-$1FFF
 * $2000-$2007 NES PPU registers 
 * $2008-$3FFF Mirrors of $2000-2007 (repeats every 8 bytes) 
 * $4000-$401F NES APU and I/O registers 
 * $4020-$FFFF Cartridge space: PRG ROM, PRG RAM, and mapper registers
 */

public class CpuMemoryMapper {

  Logger logger = LoggerFactory.getLogger(CpuMemoryMapper.class);

  private Memory internalRam = new Memory(Constants.RAM_SIZE);
  private ControllerMapper controllers;
  private Rom rom;
  private Ppu ppu;

  public CpuMemoryMapper(final Rom rom, final Ppu ppu, final ControllerBuffer controllerBuffer) {
    this.rom = rom;
    this.ppu = ppu;
    controllers = new ControllerMapper(controllerBuffer);
  }

  public byte read(final short address) {
    if (ByteUtils.isBetween(address, (short) 0x0000, (short) 0x1FFF)) {
      return internalRam.read((short) ((address & 0xFFFF) % 0x0800));
    }
    else if (ByteUtils.isBetween(address, (short) 0x2000, (short) 0x3FFF)) {
      return ppu.read((short) (((address & 0xFFFF) - 0x2000) % 8));
    }
    else if (ByteUtils.isBetween(address, (short) 0x4000, (short) 0x401F)) {
      // APU and I/O reg

      if ((address & 0xFFFF) == (short) 0x4016) {
        return controllers.read4016();
      }
      else if ((address & 0xFFFF) == (short) 0x4017) {
        return controllers.read4017();
      }
      else if ((address & 0xFFFF) == Constants.OAM_DMA_ADDR) {
        // OAM DMA
        ppu.read(address);
      }
    }
    else if (ByteUtils.isBetween(address, (short) 0x4020, (short) 0xFFFF)) {
      if (ByteUtils.isBetween(address, (short) 0x6000, (short) 0x7FFF)) {
        return rom.getPrgRam().read((short) ((address & 0xFFFF) - 0x6000));
      }
      else if (ByteUtils.isBetween(address, (short) 0x8000, (short) 0xFFFF)) {
        if (rom.getPrgSize() == 1) {
          // NROM-128 - prg bank mirrored
          return rom.getPrg().read((short) (((address & 0xFFFF) - 0x8000) % 0x4000));
        }
        // NROM-256
        return rom.getPrg().read((short) ((address & 0xFFFF) - 0x8000));
      }
    }

    return (byte) 0x00;
  }

  public void write(final short address, final byte b) {
    if (ByteUtils.isBetween(address, (short) 0x0000, (short) 0x1FFF)) {
      internalRam.write((short) ((address & 0xFFFF) % 0x0800), b);
    }
    else if (ByteUtils.isBetween(address, (short) 0x2000, (short) 0x3FFF)) {
      ppu.write((short) (((address & 0xFFFF) - 0x2000) % 8), b);
    }
    else if (ByteUtils.isBetween(address, (short) 0x4000, (short) 0x401F)) {
      // APU and I/O reg

      if ((address & 0xFFFF) == (short) 0x4016) {
        controllers.write4016(b);
      }
      else if ((address & 0xFFFF) == (short) 0x4017) {
        controllers.write4017(b);
      }
      else if ((address & 0xFFFF) == Constants.OAM_DMA_ADDR) {
        // OAM DMA
        ppu.write(address, b);
      }
    }
    else if (ByteUtils.isBetween(address, (short) 0x4020, (short) 0xFFFF)) {
      if (ByteUtils.isBetween(address, (short) 0x6000, (short) 0x7FFF)) {
        rom.getPrgRam().write((short) ((address & 0xFFFF) - 0x6000), b);
      }
      else if (ByteUtils.isBetween(address, (short) 0x8000, (short) 0xFFFF)) {
        logger.error("Rom is read-only. (Address: 0x" + ByteUtils.formatAddress(address) + ")");
      }
    }
  }
}
