package nesfx.ppu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Memory;
import nesfx.common.Register;
import nesfx.common.Register16;
import nesfx.common.ShiftRegister;
import nesfx.common.ShiftRegister16;
import nesfx.cpu.Cpu;
import nesfx.ppu.memory.Oam;
import nesfx.ppu.memory.PpuMemoryMapper;
import nesfx.ppu.register.PPUCTRL;
import nesfx.ppu.register.PPUMASK;
import nesfx.ppu.register.PPUSTATUS;

public class Ppu {

  Logger logger = LoggerFactory.getLogger(Ppu.class);

  private Cpu cpu;
  private PpuMemoryMapper memory;
  private Oam oam = new Oam();
  private Memory oamSecondary = new Memory(Constants.SECONDARY_OAM_SIZE);

  private DisplayBuffer displayBuffer = new DisplayBuffer();

  private boolean oddFrame = false;
  private int frameCount = 0;

  private int cycle = 0;
  private int currentScanline = -1;

  private Register ppuCtrl = new Register();
  private Register ppuMask = new Register();
  private Register ppuStatus = new Register();

  private Register ppuReadBuffer = new Register();

  private Register16 ppuAddr = new Register16();
  private Register16 ppuAddrTemp = new Register16();
  private Register fineXScroll = new Register();
  private boolean ppuAddrWriteToggle = false;

  private Register nametableByte = new Register();
  private Register attributeTableByte = new Register();
  private Register tileBitmapLow = new Register();
  private Register tileBitmapHigh = new Register();

  private ShiftRegister16 bitmapDataHigh = new ShiftRegister16();
  private ShiftRegister16 bitmapDataLow = new ShiftRegister16();
  private ShiftRegister paletteDataHigh = new ShiftRegister();
  private ShiftRegister paletteDataLow = new ShiftRegister();

  public Ppu(final PpuMemoryMapper memory) {
    this.memory = memory;
    displayBuffer.clear();
  }

  public void reset() {
    oam.reset();
    oamSecondary.reset();

    ppuCtrl.reset();
    ppuMask.reset();
    ppuStatus.reset();

    ppuReadBuffer.reset();

    ppuAddr.reset();
    ppuAddrTemp.reset();
    fineXScroll.reset();
    ppuAddrWriteToggle = false;

    nametableByte.reset();
    attributeTableByte.reset();
    tileBitmapLow.reset();
    tileBitmapHigh.reset();

    displayBuffer.clear();

    bitmapDataHigh.reset();
    bitmapDataLow.reset();
    paletteDataHigh.reset();
    paletteDataLow.reset();

    frameCount = 0;
    oddFrame = false;

    cycle = 0;
    currentScanline = -1;
  }

  public void cycle() {
    boolean preRenderScanline = (currentScanline == -1);
    boolean visibleScanline = (currentScanline >= 0 && currentScanline <= 239);
    boolean vBlankScanline = (currentScanline == 241);

    if (preRenderScanline) {
      if (cycle == 1) {
        ppuStatus.clearBit(PPUSTATUS.VBLANK);
      }

      if (isRenderingEnabled()) {
        scanline();

        if (cycle >= 280 && cycle <= 304) {
          // vert(v) = vert(t)
          reloadVertical();
        }
        else if (cycle == 339 && oddFrame) {
          // skip last cycle on odd frames if rendering is enabled
          cycle++;
        }
      }
    }
    else if (visibleScanline) {
      if (isRenderingEnabled()) {
        scanline();

        if (cycle >= 1 && cycle <= 256) {
          createPixel();
          //          display.draw(displayBuffer);
        }
      }
    }
    else if (vBlankScanline) {
      // vertical blank
      if (cycle == 1) {
        ppuStatus.setBit(PPUSTATUS.VBLANK);

        checkNMI();
      }
    }

    cycle++;

    if (cycle >= 341) {
      // next scanline
      cycle = 0;
      currentScanline++;

      if (currentScanline >= 261) {
        // next frame
        currentScanline = -1;
        oddFrame = !oddFrame;
        frameCount++;
      }
    }
  }

  private void scanline() {
    boolean shift = ((cycle >= 2 && cycle <= 257) || (cycle >= 322 && cycle <= 337));
    boolean fetch = ((cycle >= 1 && cycle <= 256) || (cycle >= 321 && cycle <= 336));

    if (shift) {
      // shift background registers
      bitmapDataHigh.shift();
      bitmapDataLow.shift();

      if (cycle % 8 == 1) {
        // reload shift registers
        bitmapDataLow.loadLowerByte(tileBitmapLow.get());
        bitmapDataHigh.loadLowerByte(tileBitmapHigh.get());
        paletteDataLow.set(attributeTableByte.get());
        paletteDataHigh.set((byte) (attributeTableByte.get() >> 1));
      }
    }

    if (fetch) {
      fetchBgData();

      if (cycle == 256) {
        // inc vert(v)
        yInc();
      }
      else if (cycle % 8 == 0) {
        // inc hori(v)
        coarseXInc();
      }
    }
    else if (cycle == 257) {
      // hori(v) = hori(t)
      reloadHorizontal();
    }
    else if (cycle == 337 || cycle == 339) {
      // unused NT fetches
      fetchNTByte();
    }
  }

  private void createPixel() {
    byte tilePartHigh = ByteUtils.getHighByte(bitmapDataHigh.get());
    byte tilePartLow = ByteUtils.getHighByte(bitmapDataLow.get());

    byte bg = 0;
    if (ByteUtils.isBitSet(tilePartLow, 7 - fineXScroll.get())) {
      bg += 1;
    }
    if (ByteUtils.isBitSet(tilePartHigh, 7 - fineXScroll.get())) {
      bg += 2;
    }

    byte pal = 0;
    if (ByteUtils.isBitSet(paletteDataLow.get(), 0)) {
      pal += 1;
    }
    if (ByteUtils.isBitSet(paletteDataHigh.get(), 0)) {
      pal += 2;
    }

    byte color = (bg == 0) ? memory.read((short) 0x3F00) : memory.read((short) (0x3F00 | pal << 2 | bg));
    displayBuffer.drawPixel(currentScanline, cycle - 1, color);
  }

  private void reloadHorizontal() {
    ppuAddr.set((short) ((ppuAddr.get() & ~0x41F) | (ppuAddrTemp.get() & 0x41F)));
  }

  private void reloadVertical() {
    ppuAddr.set((short) ((ppuAddr.get() & ~0x7BE0) | (ppuAddrTemp.get() & 0x7BE0)));
  }

  private void coarseXInc() {
    if ((ppuAddr.get() & 0x001F) == 31) {
      ppuAddr.set((short) (ppuAddr.get() & ~0x001F));
      ppuAddr.set((short) (ppuAddr.get() ^ 0x0400));
    }
    else {
      ppuAddr.inc();
    }
  }

  private void yInc() {
    short v = ppuAddr.get();

    if ((v & 0x7000) != 0x7000) { // if fine Y < 7
      v += 0x1000; // increment fine Y
    }
    else {
      v &= ~0x7000; // fine Y = 0
      int y = (v & 0x03E0) >> 5; // let y = coarse Y
      if (y == 29) {
        y = 0; // coarse Y = 0
        v ^= 0x0800; // switch vertical nametable
      }
      else if (y == 31) {
        y = 0; // coarse Y = 0, nametable not switched
      }
      else {
        y += 1; // increment coarse Y;
      }

      v = (short) ((v & ~0x03E0) | (y << 5)); // put coarse Y back into v
    }

    ppuAddr.set(v);
  }

  private void fetchBgData() {
    int fetchType = cycle % 8;

    if (fetchType == 1) {
      fetchNTByte();
    }
    else if (fetchType == 3) {
      fetchAttributeTableByte();
    }
    else if (fetchType == 5) {
      fetchTileBitmapByte(true);
    }
    else if (fetchType == 7) {
      fetchTileBitmapByte(false);
    }
  }

  private void fetchNTByte() {
    nametableByte.set(memory.read((short) (0x2000 | (ppuAddr.get() & 0x0FFF))));
  }

  private void fetchAttributeTableByte() {
    attributeTableByte.set(memory.read(
        (short) (0x23C0 | (ppuAddr.get() & 0x0C00) | ((ppuAddr.get() >> 4) & 0x38) | ((ppuAddr.get() >> 2) & 0x07))));
  }

  private void fetchTileBitmapByte(final boolean low) {
    short tableAddress = (short) (ppuCtrl.isBitSet(PPUCTRL.BG_PATTERN_TABLE_ADDRESS) ? 0x1000 : 0x0000);
    byte fineY = (byte) ((ppuAddr.get() >> 12) & 0x07);

    if (low) {
      tileBitmapLow.set(memory.read((short) (tableAddress + 16 * (nametableByte.get() & 0xFF) + fineY)));
    }
    else {
      tileBitmapHigh.set(memory.read((short) (tableAddress + 16 * (nametableByte.get() & 0xFF) + fineY + 8)));
    }
  }

  private void checkNMI() {
    if (ppuCtrl.isBitSet(PPUCTRL.GENERATE_NMI_VBLANK) && ppuStatus.isBitSet(PPUSTATUS.VBLANK)) {
      cpu.setNmi();
    }
  }

  private void ppuScrollWrite(final byte b) {
    if (!ppuAddrWriteToggle) {
      // first write

      // set fineXScroll (lowest 3 bits)
      fineXScroll.set((byte) (b & 0x07));

      // set lowest part of temp address with high 5 bits
      ppuAddrTemp.set((short) (ppuAddrTemp.get() | ((b >> 3) & 0x1F)));
    }
    else {
      // second write

      // construct address
      ppuAddrTemp
          .set((short) (ppuAddrTemp.get() | ((b & 0x07) << 12) | (((b >> 3) & 0x07) << 5) | (((b >> 6) & 0x03) << 8)));
    }

    ppuAddrWriteToggle = !ppuAddrWriteToggle;
  }

  private void ppuCtrlWrite(final byte b) {
    ppuCtrl.set(b);

    ppuAddrTemp.setBit(11, ppuCtrl.isBitSet(PPUCTRL.BASE_NAMETABLE_ADDRESS_HIGH));
    ppuAddrTemp.setBit(10, ppuCtrl.isBitSet(PPUCTRL.BASE_NAMETABLE_ADDRESS_LOW));

    checkNMI();
  }

  private byte ppuStatusRead() {
    byte status = ppuStatus.get();

    // VBLANK cleared after reading status
    ppuStatus.clearBit(PPUSTATUS.VBLANK);

    // clear ppu address write toggle
    ppuAddrWriteToggle = false;

    return status;
  }

  private void ppuAddrInc() {
    if (isRenderingEnabled() && currentScanline >= -1 && currentScanline <= 239) {
      // during rendering both increments of coarse X and Y are done simultaneously
      coarseXInc();
      yInc();
    }
    else {
      ppuAddr.inc(ppuCtrl.isBitSet(PPUCTRL.VRAM_ADDRESS_INCREMENT) ? (short) 32 : (short) 1);
    }
  }

  private byte ppuDataRead() {
    if (ByteUtils.isBetween(ppuAddr.get(), (short) 0x0000, (short) 0x3EFF)) {
      // data before palette is read differently (from internal buffer)

      byte value = ppuReadBuffer.get();
      ppuReadBuffer.set(memory.read(ppuAddr.get()));
      ppuAddrInc();
      return value;
    }

    byte value = memory.read(ppuAddr.get());
    ppuReadBuffer.set(value);
    ppuAddrInc();
    return value;
  }

  private void ppuDataWrite(final byte b) {
    memory.write(ppuAddr.get(), b);
    ppuAddrInc();
  }

  private void ppuAddrWrite(final byte b) {
    if (!ppuAddrWriteToggle) {
      // first write

      // set high bytes of temp address
      ppuAddrTemp.set(ByteUtils.bytesToAddress(b, (byte) 0x00));

      // bit 14 of temp gets set to zero
      ppuAddrTemp.clearBit(14);

      // size of address is 15 bit, clear highest bit to make sure
      ppuAddrTemp.clearBit(15);
    }
    else {
      // second write

      // set low bytes of temp address
      ppuAddrTemp.set(ByteUtils.bytesToAddress(ByteUtils.getHighByte(ppuAddrTemp.get()), b));

      // ppu address = temp address
      ppuAddr.set(ppuAddrTemp.get());
    }

    ppuAddrWriteToggle = !ppuAddrWriteToggle;
  }

  public byte read(final short address) {
    switch (address & 0xFFFF) {
      case 0:
        logger.error("PPUCTRL is write-only.");
        break;
      case 1:
        logger.error("PPUMASK is write-only.");
        break;
      case 2:
        return ppuStatusRead();
      case 3:
        logger.error("OAMADDR is write-only.");
        break;
      case 4:
        return oam.read();
      case 5:
        logger.error("PPUSCROLL is write-only.");
        break;
      case 6:
        logger.error("PPUADDR is write-only.");
        break;
      case 7:
        return ppuDataRead();
      case Constants.OAM_DMA_ADDR:
        logger.error("OAMDMA is write-only.");
        break;
    }
    return 0;
  }

  public void write(final short address, final byte b) {
    switch (address & 0xFFFF) {
      case 0:
        ppuCtrlWrite(b);
        break;
      case 1:
        ppuMask.set(b);
        break;
      case 2:
        logger.error("PPUSTATUS is read-only.");
        break;
      case 3:
        oam.setAddress(b);
        break;
      case 4:
        oam.write(b);
        break;
      case 5:
        ppuScrollWrite(b);
        break;
      case 6:
        ppuAddrWrite(b);
        break;
      case 7:
        ppuDataWrite(b);
        break;
      case Constants.OAM_DMA_ADDR:
        cpu.setOamDma(b);
        break;
    }
  }

  public void setCpu(final Cpu cpu) {
    this.cpu = cpu;
  }

  public int getFrameCount() {
    int count = frameCount;
    frameCount = 0;
    return count;
  }

  public boolean isRenderingEnabled() {
    return ppuMask.isBitSet(PPUMASK.SHOW_BG) || ppuMask.isBitSet(PPUMASK.SHOW_SPRITES);
  }

  public DisplayBuffer getDisplayBuffer() {
    return displayBuffer;
  }
}
