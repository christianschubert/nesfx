package nesfx.cpu;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nesfx.common.ByteUtils;
import nesfx.common.Constants;
import nesfx.common.Register;
import nesfx.common.Register16;
import nesfx.cpu.instruction.AddCarry;
import nesfx.cpu.instruction.And;
import nesfx.cpu.instruction.ArShiftLeft;
import nesfx.cpu.instruction.BitTest;
import nesfx.cpu.instruction.Branch;
import nesfx.cpu.instruction.Break;
import nesfx.cpu.instruction.Compare;
import nesfx.cpu.instruction.DecMem;
import nesfx.cpu.instruction.DecReg;
import nesfx.cpu.instruction.Eor;
import nesfx.cpu.instruction.IncMem;
import nesfx.cpu.instruction.IncReg;
import nesfx.cpu.instruction.Instruction;
import nesfx.cpu.instruction.Jump;
import nesfx.cpu.instruction.Load;
import nesfx.cpu.instruction.LogShiftRight;
import nesfx.cpu.instruction.Nmi;
import nesfx.cpu.instruction.Nop;
import nesfx.cpu.instruction.Ora;
import nesfx.cpu.instruction.PullReg;
import nesfx.cpu.instruction.PushReg;
import nesfx.cpu.instruction.Return;
import nesfx.cpu.instruction.RotateLeft;
import nesfx.cpu.instruction.RotateRight;
import nesfx.cpu.instruction.SetClearFlag;
import nesfx.cpu.instruction.Store;
import nesfx.cpu.instruction.SubCarry;
import nesfx.cpu.instruction.Transfer;
import nesfx.cpu.instruction.illegal.Anc;
import nesfx.cpu.instruction.illegal.Dcp;
import nesfx.cpu.instruction.illegal.Isb;
import nesfx.cpu.instruction.illegal.Lax;
import nesfx.cpu.instruction.illegal.Rla;
import nesfx.cpu.instruction.illegal.Rra;
import nesfx.cpu.instruction.illegal.Sax;
import nesfx.cpu.instruction.illegal.Slo;
import nesfx.cpu.instruction.illegal.Sre;
import nesfx.cpu.memory.CpuMemoryMapper;
import nesfx.cpu.register.SRFlag;

public class Cpu {

  Logger logger = LoggerFactory.getLogger(Cpu.class);

  private HashMap<Byte, Instruction> instructions = new HashMap<>();
  private Instruction nmi;

  private CpuMemoryMapper memory;
  private Dma dma;

  private Register ac = new Register();
  private Register x = new Register();
  private Register y = new Register();

  private Register sr = new Register();
  private Register sp = new Register();

  private Register16 pc = new Register16();

  private boolean nmiReqest;

  private Instruction instruction;
  private int cycles;
  private boolean isOddCycle;
  private boolean isPcChange;

  private boolean isPageCross;
  private boolean addCycle;

  public Cpu(final CpuMemoryMapper memory) {
    this.memory = memory;
    init();
  }

  /*
   * POWER-UP
   * 
   * P = $34 (IRQ disabled) A, X, Y = 0 S = $FD $4017 = $00 (frame irq enabled)
   * $4015 = $00 (all channels disabled) $4000-$400F = $00 (not sure about
   * $4010-$4013)
   */
  private void init() {
    initInstructions();

    dma = new Dma(memory);

    sr.set(Constants.SR_START);
    sp.set(Constants.SP_START);
    ac.reset();
    x.reset();
    y.reset();

    cycles = 0;
    isOddCycle = false;

    resetPC();
  }

  /*
   * RESET
   * 
   * A, X, Y were not affected S was decremented by 3 (but nothing was written
   * to the stack) The I (IRQ disable) flag was set to true (status ORed with
   * $04) The internal memory was unchanged APU mode in $4017 was unchanged APU
   * was silenced ($4015 = 0)
   */
  public void reset() {
    sp.dec((byte) 3);

    sr.setBit(SRFlag.INTERRUPT);

    memory.write((short) 0x4015, (byte) 0x00);

    cycles = 0;
    isOddCycle = false;

    nmiReqest = false;

    resetPC();
  }

  private void resetPC() {
    pc.set(indirectAddress(Constants.RESET_VECTOR));
  }

  private void initInstructions() {
    nmi = new Nmi(AddrMode.Implied, 7, false, "NMI");

    instructions.put((byte) 0x00, new Break(AddrMode.Implied, 7, false, "BRK"));
    instructions.put((byte) 0x01, new Ora(AddrMode.IndexedIndirect, 6, false, "ORA"));
    instructions.put((byte) 0x03, new Slo(AddrMode.IndexedIndirect, 8, false, "*SLO"));
    instructions.put((byte) 0x04, new Nop(AddrMode.Zeropage, 3, false, "*DOP"));
    instructions.put((byte) 0x05, new Ora(AddrMode.Zeropage, 3, false, "ORA"));
    instructions.put((byte) 0x06, new ArShiftLeft(AddrMode.Zeropage, 5, false, "ASL"));
    instructions.put((byte) 0x07, new Slo(AddrMode.Zeropage, 5, false, "*SLO"));
    instructions.put((byte) 0x08, new PushReg(AddrMode.Implied, 3, false, "PHP", sr));
    instructions.put((byte) 0x09, new Ora(AddrMode.Immidiate, 2, false, "ORA"));
    instructions.put((byte) 0x0A, new ArShiftLeft(AddrMode.Accumulator, 2, false, "ASL"));
    instructions.put((byte) 0x0B, new Anc(AddrMode.Immidiate, 2, false, "*ANC"));
    instructions.put((byte) 0x0C, new Nop(AddrMode.Absolute, 4, false, "*TOP"));
    instructions.put((byte) 0x0D, new Ora(AddrMode.Absolute, 4, false, "ORA"));
    instructions.put((byte) 0x0E, new ArShiftLeft(AddrMode.Absolute, 6, false, "ASL"));
    instructions.put((byte) 0x0F, new Slo(AddrMode.Absolute, 6, false, "*SLO"));
    instructions.put((byte) 0x10, new Branch(AddrMode.Relative, 3, true, "BPL", SRFlag.NEGATIVE, false));
    instructions.put((byte) 0x11, new Ora(AddrMode.IndirectIndexed, 5, true, "ORA"));
    instructions.put((byte) 0x13, new Slo(AddrMode.IndirectIndexed, 8, false, "*SLO"));
    instructions.put((byte) 0x14, new Nop(AddrMode.ZeropageIndexedX, 4, false, "*DOP"));
    instructions.put((byte) 0x15, new Ora(AddrMode.ZeropageIndexedX, 4, false, "ORA"));
    instructions.put((byte) 0x16, new ArShiftLeft(AddrMode.ZeropageIndexedX, 6, false, "ASL"));
    instructions.put((byte) 0x17, new Slo(AddrMode.ZeropageIndexedX, 6, false, "*SLO"));
    instructions.put((byte) 0x18, new SetClearFlag(AddrMode.Implied, 2, false, "CLC", SRFlag.CARRY, false));
    instructions.put((byte) 0x19, new Ora(AddrMode.AbsoluteIndexedY, 4, true, "ORA"));
    instructions.put((byte) 0x1A, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0x1B, new Slo(AddrMode.AbsoluteIndexedY, 7, false, "*SLO"));
    instructions.put((byte) 0x1C, new Nop(AddrMode.AbsoluteIndexedX, 4, true, "*TOP"));
    instructions.put((byte) 0x1D, new Ora(AddrMode.AbsoluteIndexedX, 4, true, "ORA"));
    instructions.put((byte) 0x1E, new ArShiftLeft(AddrMode.AbsoluteIndexedX, 7, false, "ASL"));
    instructions.put((byte) 0x1F, new Slo(AddrMode.AbsoluteIndexedX, 7, false, "*SLO"));
    instructions.put((byte) 0x20, new Jump(AddrMode.Absolute, 6, false, "JSR", true));
    instructions.put((byte) 0x21, new And(AddrMode.IndexedIndirect, 6, false, "AND"));
    instructions.put((byte) 0x23, new Rla(AddrMode.IndexedIndirect, 8, false, "*RLA"));
    instructions.put((byte) 0x24, new BitTest(AddrMode.Zeropage, 3, false, "BIT"));
    instructions.put((byte) 0x25, new And(AddrMode.Zeropage, 3, false, "AND"));
    instructions.put((byte) 0x27, new Rla(AddrMode.Zeropage, 5, false, "*RLA"));
    instructions.put((byte) 0x26, new RotateLeft(AddrMode.Zeropage, 5, false, "ROL"));
    instructions.put((byte) 0x28, new PullReg(AddrMode.Implied, 4, false, "PLP", sr));
    instructions.put((byte) 0x29, new And(AddrMode.Immidiate, 2, false, "AND"));
    instructions.put((byte) 0x2A, new RotateLeft(AddrMode.Accumulator, 2, false, "ROL"));
    instructions.put((byte) 0x2B, new Anc(AddrMode.Immidiate, 2, false, "*ANC"));
    instructions.put((byte) 0x2C, new BitTest(AddrMode.Absolute, 4, false, "BIT"));
    instructions.put((byte) 0x2D, new And(AddrMode.Absolute, 4, false, "AND"));
    instructions.put((byte) 0x2E, new RotateLeft(AddrMode.Absolute, 6, false, "ROL"));
    instructions.put((byte) 0x2F, new Rla(AddrMode.Absolute, 6, false, "*RLA"));
    instructions.put((byte) 0x30, new Branch(AddrMode.Relative, 3, true, "BMI", SRFlag.NEGATIVE, true));
    instructions.put((byte) 0x31, new And(AddrMode.IndirectIndexed, 5, true, "AND"));
    instructions.put((byte) 0x33, new Rla(AddrMode.IndirectIndexed, 8, false, "*RLA"));
    instructions.put((byte) 0x34, new Nop(AddrMode.ZeropageIndexedX, 4, false, "*DOP"));
    instructions.put((byte) 0x35, new And(AddrMode.ZeropageIndexedX, 4, false, "AND"));
    instructions.put((byte) 0x36, new RotateLeft(AddrMode.ZeropageIndexedX, 6, false, "ROL"));
    instructions.put((byte) 0x37, new Rla(AddrMode.ZeropageIndexedX, 6, false, "*RLA"));
    instructions.put((byte) 0x38, new SetClearFlag(AddrMode.Implied, 2, false, "SEC", SRFlag.CARRY, true));
    instructions.put((byte) 0x39, new And(AddrMode.AbsoluteIndexedY, 4, true, "AND"));
    instructions.put((byte) 0x3A, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0x3B, new Rla(AddrMode.AbsoluteIndexedY, 7, false, "*RLA"));
    instructions.put((byte) 0x3C, new Nop(AddrMode.AbsoluteIndexedX, 4, true, "*TOP"));
    instructions.put((byte) 0x3D, new And(AddrMode.AbsoluteIndexedX, 4, true, "AND"));
    instructions.put((byte) 0x3E, new RotateLeft(AddrMode.AbsoluteIndexedX, 7, false, "ROL"));
    instructions.put((byte) 0x3F, new Rla(AddrMode.AbsoluteIndexedX, 7, false, "*RLA"));
    instructions.put((byte) 0x40, new Return(AddrMode.Implied, 6, false, "RTI", true));
    instructions.put((byte) 0x45, new Eor(AddrMode.Zeropage, 3, false, "EOR"));
    instructions.put((byte) 0x41, new Eor(AddrMode.IndexedIndirect, 6, false, "EOR"));
    instructions.put((byte) 0x43, new Sre(AddrMode.IndexedIndirect, 8, false, "*SRE"));
    instructions.put((byte) 0x44, new Nop(AddrMode.Zeropage, 3, false, "*DOP"));
    instructions.put((byte) 0x46, new LogShiftRight(AddrMode.Zeropage, 5, false, "LSR"));
    instructions.put((byte) 0x47, new Sre(AddrMode.Zeropage, 5, false, "*SRE"));
    instructions.put((byte) 0x48, new PushReg(AddrMode.Implied, 3, false, "PHA", ac));
    instructions.put((byte) 0x49, new Eor(AddrMode.Immidiate, 2, false, "EOR"));
    instructions.put((byte) 0x4A, new LogShiftRight(AddrMode.Accumulator, 2, false, "LSR"));
    instructions.put((byte) 0x4C, new Jump(AddrMode.Absolute, 3, false, "JMP", false));
    instructions.put((byte) 0x4D, new Eor(AddrMode.Absolute, 4, false, "EOR"));
    instructions.put((byte) 0x4E, new LogShiftRight(AddrMode.Absolute, 6, false, "LSR"));
    instructions.put((byte) 0x4F, new Sre(AddrMode.Absolute, 6, false, "*SRE"));
    instructions.put((byte) 0x50, new Branch(AddrMode.Relative, 3, true, "BVC", SRFlag.OVERFLOW, false));
    instructions.put((byte) 0x51, new Eor(AddrMode.IndirectIndexed, 5, true, "EOR"));
    instructions.put((byte) 0x53, new Sre(AddrMode.IndirectIndexed, 8, false, "*SRE"));
    instructions.put((byte) 0x54, new Nop(AddrMode.ZeropageIndexedX, 5, false, "*DOP"));
    instructions.put((byte) 0x55, new Eor(AddrMode.ZeropageIndexedX, 4, false, "EOR"));
    instructions.put((byte) 0x56, new LogShiftRight(AddrMode.ZeropageIndexedX, 6, false, "LSR"));
    instructions.put((byte) 0x57, new Sre(AddrMode.ZeropageIndexedX, 6, false, "*SRE"));
    instructions.put((byte) 0x58, new SetClearFlag(AddrMode.Implied, 2, false, "CLI", SRFlag.INTERRUPT, false));
    instructions.put((byte) 0x59, new Eor(AddrMode.AbsoluteIndexedY, 4, true, "EOR"));
    instructions.put((byte) 0x5A, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0x5B, new Sre(AddrMode.AbsoluteIndexedY, 7, false, "*SRE"));
    instructions.put((byte) 0x5C, new Nop(AddrMode.AbsoluteIndexedX, 4, true, "*TOP"));
    instructions.put((byte) 0x5D, new Eor(AddrMode.AbsoluteIndexedX, 4, true, "EOR"));
    instructions.put((byte) 0x5E, new LogShiftRight(AddrMode.AbsoluteIndexedX, 6, false, "LSR"));
    instructions.put((byte) 0x5F, new Sre(AddrMode.AbsoluteIndexedX, 7, false, "*SRE"));
    instructions.put((byte) 0x60, new Return(AddrMode.Implied, 6, false, "RTS", false));
    instructions.put((byte) 0x61, new AddCarry(AddrMode.IndexedIndirect, 6, false, "ADC"));
    instructions.put((byte) 0x63, new Rra(AddrMode.IndexedIndirect, 8, false, "*RRA"));
    instructions.put((byte) 0x64, new Nop(AddrMode.Zeropage, 3, false, "*DOP"));
    instructions.put((byte) 0x65, new AddCarry(AddrMode.Zeropage, 3, false, "ADC"));
    instructions.put((byte) 0x66, new RotateRight(AddrMode.Zeropage, 5, false, "ROR"));
    instructions.put((byte) 0x67, new Rra(AddrMode.Zeropage, 5, false, "*RRA"));
    instructions.put((byte) 0x68, new PullReg(AddrMode.Implied, 4, false, "PLA", ac));
    instructions.put((byte) 0x69, new AddCarry(AddrMode.Immidiate, 2, false, "ADC"));
    instructions.put((byte) 0x6A, new RotateRight(AddrMode.Accumulator, 2, false, "ROR"));
    instructions.put((byte) 0x6C, new Jump(AddrMode.IndirectBugged, 5, false, "JMP", false));
    instructions.put((byte) 0x6D, new AddCarry(AddrMode.Absolute, 4, false, "ADC"));
    instructions.put((byte) 0x6E, new RotateRight(AddrMode.Absolute, 6, false, "ROR"));
    instructions.put((byte) 0x6F, new Rra(AddrMode.Absolute, 6, false, "*RRA"));
    instructions.put((byte) 0x70, new Branch(AddrMode.Relative, 3, true, "BVS", SRFlag.OVERFLOW, true));
    instructions.put((byte) 0x71, new AddCarry(AddrMode.IndirectIndexed, 6, true, "ADC"));
    instructions.put((byte) 0x73, new Rra(AddrMode.IndirectIndexed, 8, false, "*RRA"));
    instructions.put((byte) 0x74, new Nop(AddrMode.ZeropageIndexedX, 4, false, "*DOP"));
    instructions.put((byte) 0x75, new AddCarry(AddrMode.ZeropageIndexedX, 4, false, "ADC"));
    instructions.put((byte) 0x76, new RotateRight(AddrMode.ZeropageIndexedX, 6, false, "ROR"));
    instructions.put((byte) 0x77, new Rra(AddrMode.ZeropageIndexedX, 6, false, "*RRA"));
    instructions.put((byte) 0x78, new SetClearFlag(AddrMode.Implied, 2, false, "SEI", SRFlag.INTERRUPT, true));
    instructions.put((byte) 0x79, new AddCarry(AddrMode.AbsoluteIndexedY, 4, true, "ADC"));
    instructions.put((byte) 0x7A, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0x7B, new Rra(AddrMode.AbsoluteIndexedY, 7, false, "*RRA"));
    instructions.put((byte) 0x7C, new Nop(AddrMode.AbsoluteIndexedX, 4, true, "*TOP"));
    instructions.put((byte) 0x7D, new AddCarry(AddrMode.AbsoluteIndexedX, 4, true, "ADC"));
    instructions.put((byte) 0x7E, new RotateRight(AddrMode.AbsoluteIndexedX, 7, false, "ROR"));
    instructions.put((byte) 0x7F, new Rra(AddrMode.AbsoluteIndexedX, 7, false, "*RRA"));
    instructions.put((byte) 0x80, new Nop(AddrMode.Immidiate, 2, false, "*DOP"));
    instructions.put((byte) 0x81, new Store(AddrMode.IndexedIndirect, 6, false, "STA", ac));
    instructions.put((byte) 0x82, new Nop(AddrMode.Immidiate, 2, false, "*DOP"));
    instructions.put((byte) 0x83, new Sax(AddrMode.IndexedIndirect, 6, false, "*SAX"));
    instructions.put((byte) 0x84, new Store(AddrMode.Zeropage, 3, false, "STY", y));
    instructions.put((byte) 0x85, new Store(AddrMode.Zeropage, 3, false, "STA", ac));
    instructions.put((byte) 0x86, new Store(AddrMode.Zeropage, 3, false, "STX", x));
    instructions.put((byte) 0x87, new Sax(AddrMode.Zeropage, 3, false, "*SAX"));
    instructions.put((byte) 0x88, new DecReg(AddrMode.Implied, 2, false, "DEY", y));
    instructions.put((byte) 0x8A, new Transfer(AddrMode.Implied, 2, false, "TXA", x, ac));
    instructions.put((byte) 0x8C, new Store(AddrMode.Absolute, 4, false, "STY", y));
    instructions.put((byte) 0x8D, new Store(AddrMode.Absolute, 4, false, "STA", ac));
    instructions.put((byte) 0x8E, new Store(AddrMode.Absolute, 4, false, "STX", x));
    instructions.put((byte) 0x8F, new Sax(AddrMode.Absolute, 4, false, "*SAX"));
    instructions.put((byte) 0x89, new Nop(AddrMode.Immidiate, 2, false, "*DOP"));
    instructions.put((byte) 0x90, new Branch(AddrMode.Relative, 3, true, "BCC", SRFlag.CARRY, false));
    instructions.put((byte) 0x91, new Store(AddrMode.IndirectIndexed, 6, false, "STA", ac));
    instructions.put((byte) 0x94, new Store(AddrMode.ZeropageIndexedX, 4, false, "STY", y));
    instructions.put((byte) 0x95, new Store(AddrMode.ZeropageIndexedX, 4, false, "STA", ac));
    instructions.put((byte) 0x96, new Store(AddrMode.ZeropageIndexedY, 4, false, "STX", x));
    instructions.put((byte) 0x97, new Sax(AddrMode.ZeropageIndexedY, 4, false, "*SAX"));
    instructions.put((byte) 0x98, new Transfer(AddrMode.Implied, 2, false, "TYA", y, ac));
    instructions.put((byte) 0x99, new Store(AddrMode.AbsoluteIndexedY, 5, false, "STA", ac));
    instructions.put((byte) 0x9A, new Transfer(AddrMode.Implied, 2, false, "TXS", x, sp));
    instructions.put((byte) 0x9D, new Store(AddrMode.AbsoluteIndexedX, 5, false, "STA", ac));
    instructions.put((byte) 0xA0, new Load(AddrMode.Immidiate, 2, false, "LDY", y));
    instructions.put((byte) 0xA1, new Load(AddrMode.IndexedIndirect, 6, false, "LDA", ac));
    instructions.put((byte) 0xA2, new Load(AddrMode.Immidiate, 2, false, "LDX", x));
    instructions.put((byte) 0xA3, new Lax(AddrMode.IndexedIndirect, 6, false, "*LAX"));
    instructions.put((byte) 0xA4, new Load(AddrMode.Zeropage, 2, false, "LDY", y));
    instructions.put((byte) 0xA5, new Load(AddrMode.Zeropage, 3, false, "LDA", ac));
    instructions.put((byte) 0xA6, new Load(AddrMode.Zeropage, 3, false, "LDX", x));
    instructions.put((byte) 0xA7, new Lax(AddrMode.Zeropage, 3, false, "*LAX"));
    instructions.put((byte) 0xA8, new Transfer(AddrMode.Implied, 2, false, "TAY", ac, y));
    instructions.put((byte) 0xA9, new Load(AddrMode.Immidiate, 2, false, "LDA", ac));
    instructions.put((byte) 0xAA, new Transfer(AddrMode.Implied, 2, false, "TAX", ac, x));
    instructions.put((byte) 0xAC, new Load(AddrMode.Absolute, 4, false, "LDY", y));
    instructions.put((byte) 0xAD, new Load(AddrMode.Absolute, 4, false, "LDA", ac));
    instructions.put((byte) 0xAE, new Load(AddrMode.Absolute, 4, false, "LDX", x));
    instructions.put((byte) 0xAF, new Lax(AddrMode.Absolute, 4, false, "*LAX"));
    instructions.put((byte) 0xB0, new Branch(AddrMode.Relative, 3, true, "BCS", SRFlag.CARRY, true));
    instructions.put((byte) 0xB1, new Load(AddrMode.IndirectIndexed, 5, true, "LDA", ac));
    instructions.put((byte) 0xB3, new Lax(AddrMode.IndirectIndexed, 5, true, "*LAX"));
    instructions.put((byte) 0xB4, new Load(AddrMode.ZeropageIndexedX, 4, false, "LDY", y));
    instructions.put((byte) 0xB5, new Load(AddrMode.ZeropageIndexedX, 4, false, "LDA", ac));
    instructions.put((byte) 0xB6, new Load(AddrMode.ZeropageIndexedY, 4, false, "LDX", x));
    instructions.put((byte) 0xB7, new Lax(AddrMode.ZeropageIndexedY, 4, false, "*LAX"));
    instructions.put((byte) 0xB8, new SetClearFlag(AddrMode.Implied, 2, false, "CLV", SRFlag.OVERFLOW, false));
    instructions.put((byte) 0xB9, new Load(AddrMode.AbsoluteIndexedY, 4, true, "LDA", ac));
    instructions.put((byte) 0xBA, new Transfer(AddrMode.Implied, 2, false, "TSX", sp, x));
    instructions.put((byte) 0xBC, new Load(AddrMode.AbsoluteIndexedX, 4, true, "LDY", y));
    instructions.put((byte) 0xBD, new Load(AddrMode.AbsoluteIndexedX, 4, true, "LDA", ac));
    instructions.put((byte) 0xBE, new Load(AddrMode.AbsoluteIndexedY, 4, true, "LDX", x));
    instructions.put((byte) 0xBF, new Lax(AddrMode.AbsoluteIndexedY, 4, true, "*LAX"));
    instructions.put((byte) 0xC0, new Compare(AddrMode.Immidiate, 2, false, "CPY", y));
    instructions.put((byte) 0xC1, new Compare(AddrMode.IndexedIndirect, 6, false, "CMP", ac));
    instructions.put((byte) 0xC2, new Nop(AddrMode.Immidiate, 2, false, "*DOP"));
    instructions.put((byte) 0xC3, new Dcp(AddrMode.IndexedIndirect, 8, false, "*DCP"));
    instructions.put((byte) 0xC4, new Compare(AddrMode.Zeropage, 3, false, "CPY", y));
    instructions.put((byte) 0xC5, new Compare(AddrMode.Zeropage, 3, false, "CMP", ac));
    instructions.put((byte) 0xC6, new DecMem(AddrMode.Zeropage, 5, false, "DEC"));
    instructions.put((byte) 0xC7, new Dcp(AddrMode.Zeropage, 5, false, "*DCP"));
    instructions.put((byte) 0xC8, new IncReg(AddrMode.Implied, 2, false, "INY", y));
    instructions.put((byte) 0xC9, new Compare(AddrMode.Immidiate, 2, false, "CMP", ac));
    instructions.put((byte) 0xCA, new DecReg(AddrMode.Implied, 2, false, "DEX", x));
    instructions.put((byte) 0xCC, new Compare(AddrMode.Absolute, 2, false, "CPY", y));
    instructions.put((byte) 0xCD, new Compare(AddrMode.Absolute, 4, false, "CMP", ac));
    instructions.put((byte) 0xCE, new DecMem(AddrMode.Absolute, 6, false, "DEC"));
    instructions.put((byte) 0xCF, new Dcp(AddrMode.Absolute, 6, false, "*DCP"));
    instructions.put((byte) 0xD0, new Branch(AddrMode.Relative, 3, true, "BNE", SRFlag.ZERO, false));
    instructions.put((byte) 0xD1, new Compare(AddrMode.IndirectIndexed, 5, true, "CMP", ac));
    instructions.put((byte) 0xD3, new Dcp(AddrMode.IndirectIndexed, 8, false, "*DCP"));
    instructions.put((byte) 0xD4, new Nop(AddrMode.ZeropageIndexedX, 4, false, "*DOP"));
    instructions.put((byte) 0xD5, new Compare(AddrMode.ZeropageIndexedX, 4, false, "CMP", ac));
    instructions.put((byte) 0xD6, new DecMem(AddrMode.ZeropageIndexedX, 6, false, "DEC"));
    instructions.put((byte) 0xD7, new Dcp(AddrMode.ZeropageIndexedX, 6, false, "*DCP"));
    instructions.put((byte) 0xD8, new SetClearFlag(AddrMode.Implied, 2, false, "CLD", SRFlag.DECIMAL, false));
    instructions.put((byte) 0xD9, new Compare(AddrMode.AbsoluteIndexedY, 4, true, "CMP", ac));
    instructions.put((byte) 0xDA, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0xDB, new Dcp(AddrMode.AbsoluteIndexedY, 7, false, "*DCP"));
    instructions.put((byte) 0xDC, new Nop(AddrMode.AbsoluteIndexedX, 4, true, "*TOP"));
    instructions.put((byte) 0xDD, new Compare(AddrMode.AbsoluteIndexedX, 4, true, "CMP", ac));
    instructions.put((byte) 0xDE, new DecMem(AddrMode.AbsoluteIndexedX, 7, false, "DEC"));
    instructions.put((byte) 0xDF, new Dcp(AddrMode.AbsoluteIndexedX, 7, false, "*DCP"));
    instructions.put((byte) 0xE0, new Compare(AddrMode.Immidiate, 2, false, "CPX", x));
    instructions.put((byte) 0xE1, new SubCarry(AddrMode.IndexedIndirect, 6, false, "SBC"));
    instructions.put((byte) 0xE2, new Nop(AddrMode.Immidiate, 2, false, "*DOP"));
    instructions.put((byte) 0xE3, new Isb(AddrMode.IndexedIndirect, 8, false, "*ISB"));
    instructions.put((byte) 0xE4, new Compare(AddrMode.Zeropage, 3, false, "CPX", x));
    instructions.put((byte) 0xE5, new SubCarry(AddrMode.Zeropage, 3, false, "SBC"));
    instructions.put((byte) 0xE6, new IncMem(AddrMode.Zeropage, 5, false, "INC"));
    instructions.put((byte) 0xE7, new Isb(AddrMode.Zeropage, 5, false, "*ISB"));
    instructions.put((byte) 0xE8, new IncReg(AddrMode.Implied, 2, false, "INX", x));
    instructions.put((byte) 0xE9, new SubCarry(AddrMode.Immidiate, 2, false, "SBC"));
    instructions.put((byte) 0xEA, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0xEB, new SubCarry(AddrMode.Immidiate, 2, false, "*SBC"));
    instructions.put((byte) 0xEC, new Compare(AddrMode.Absolute, 4, false, "CPX", x));
    instructions.put((byte) 0xED, new SubCarry(AddrMode.Absolute, 4, false, "SBC"));
    instructions.put((byte) 0xEE, new IncMem(AddrMode.Absolute, 6, false, "INC"));
    instructions.put((byte) 0xEF, new Isb(AddrMode.Absolute, 6, false, "*ISB"));
    instructions.put((byte) 0xF0, new Branch(AddrMode.Relative, 3, true, "BEQ", SRFlag.ZERO, true));
    instructions.put((byte) 0xF1, new SubCarry(AddrMode.IndirectIndexed, 5, true, "SBC"));
    instructions.put((byte) 0xF3, new Isb(AddrMode.IndirectIndexed, 8, false, "*ISB"));
    instructions.put((byte) 0xF4, new Nop(AddrMode.ZeropageIndexedX, 4, false, "*DOP"));
    instructions.put((byte) 0xF5, new SubCarry(AddrMode.ZeropageIndexedX, 4, false, "SBC"));
    instructions.put((byte) 0xF6, new IncMem(AddrMode.ZeropageIndexedX, 6, false, "INC"));
    instructions.put((byte) 0xF7, new Isb(AddrMode.ZeropageIndexedX, 6, false, "*ISB"));
    instructions.put((byte) 0xF8, new SetClearFlag(AddrMode.Implied, 2, false, "SED", SRFlag.DECIMAL, true));
    instructions.put((byte) 0xF9, new SubCarry(AddrMode.AbsoluteIndexedY, 4, true, "SBC"));
    instructions.put((byte) 0xFA, new Nop(AddrMode.Implied, 2, false, "NOP"));
    instructions.put((byte) 0xFB, new Isb(AddrMode.AbsoluteIndexedY, 7, false, "*ISB"));
    instructions.put((byte) 0xFC, new Nop(AddrMode.AbsoluteIndexedX, 4, true, "*TOP"));
    instructions.put((byte) 0xFD, new SubCarry(AddrMode.AbsoluteIndexedX, 4, true, "SBC"));
    instructions.put((byte) 0xFE, new IncMem(AddrMode.AbsoluteIndexedX, 7, false, "INC"));
    instructions.put((byte) 0xFF, new Isb(AddrMode.AbsoluteIndexedX, 7, false, "*ISB"));
  }

  public void cycle() {
    isOddCycle = !isOddCycle;

    if (addCycle) {
      addCycle = false;
      return;
    }

    cycles++;

    if (cycles == 1) {
      if (dma.isRequest()) {
        dma.startTransfer();
        return;
      }
      else if (nmiReqest) {
        instruction = nmi;
      }
      else {
        // fetch instruction
        byte opcode = memory.read(pc.get());
        instruction = instructions.get(opcode);

        if (instruction == null) {
          logger.warn(printOpcode("Unknown opcode " + ByteUtils.formatByte(opcode), AddrMode.Implied));
          pc.inc();
          cycles = 0;
          return;
        }
      }

      isPcChange = false;
      //      logger.info(printOpcode(instruction.getDebugInfo(), instruction.getAddrMode()));
    }

    if (dma.isActive()) {
      dma.cycle(isOddCycle);
      if (dma.isTransferComplete()) {
        dma.stopTransfer();
        cycles = 0;
      }
      return;
    }

    if (cycles >= instruction.getCycles() - 1) {
      // execute instruction on last cycle
      instruction.execute(this);

      if (instruction.isCheckPageCross() && isPageCross) {
        addCycle();
      }

      if (nmiReqest) {
        nmiReqest = false;
      }
      else if (!isPcChange) {
        // no jump, rt, branch instruction -> increment PC according to address
        // mode
        switch (instruction.getAddrMode()) {
          case Accumulator:
          case Implied:
            pc.inc();
            break;
          case Immidiate:
          case Relative:
          case Zeropage:
          case ZeropageIndexedX:
          case ZeropageIndexedY:
          case IndirectIndexed:
          case IndexedIndirect:
            pc.inc((short) 2);
            break;
          case Indirect:
          case IndirectBugged:
          case Absolute:
          case AbsoluteIndexedX:
          case AbsoluteIndexedY:
            pc.inc((short) 3);
            break;
          default:
            break;
        }
      }

      cycles = 0;
      return;
    }
  }

  private void addCycle() {
    addCycle = true;
  }

  public void setNmi() {
    nmiReqest = true;
  }

  public void setOamDma(final byte b) {
    dma.setRequest(b);
  }

  private short immidiateAddress() {
    return (short) (pc.get() + 1);
  }

  private short absoluteAddress() {
    byte low = memory.read((short) (pc.get() + 1));
    byte high = memory.read((short) (pc.get() + 2));
    return ByteUtils.bytesToAddress(high, low);
  }

  private short zeropageAddress() {
    byte low = memory.read((short) (pc.get() + 1));
    byte high = (byte) 0x00;
    return ByteUtils.bytesToAddress(high, low);
  }

  private short relativeAddress() {
    byte relativeValue = memory.read((short) (pc.get() + 1));
    short result = (short) (pc.get() + relativeValue);
    checkPageCrossed(pc.get(), result);
    return result;
  }

  private short indirectAddress() {
    short address = absoluteAddress();

    byte low = memory.read(address);
    byte high = memory.read((short) (address + 1));

    return ByteUtils.bytesToAddress(high, low);
  }

  public short indirectAddress(final short address) {
    byte low = memory.read(address);
    byte high = memory.read((short) (address + 1));
    return ByteUtils.bytesToAddress(high, low);
  }

  public short indirectBuggedAddress() {
    byte low = memory.read((short) (pc.get() + 1));
    byte high = memory.read((short) (pc.get() + 2));

    byte lowAddress = memory.read(ByteUtils.bytesToAddress(high, low));
    low++;
    byte highAddress = memory.read(ByteUtils.bytesToAddress(high, low));

    return ByteUtils.bytesToAddress(highAddress, lowAddress);
  }

  private short zeropageIndexedAddress(final Register index) {
    byte low = memory.read((short) (pc.get() + 1));
    byte high = (byte) 0x00;
    low += (index.get() & 0xFF);
    return ByteUtils.bytesToAddress(high, low);
  }

  private short absoluteIndexedAddress(final Register index) {
    short absolute = absoluteAddress();
    short result = (short) (absolute + (index.get() & 0xFF));

    checkPageCrossed(absolute, result);

    return result;
  }

  private short indexedIndirectAddress() {
    byte low = memory.read((short) (pc.get() + 1));
    byte high = (byte) 0x00;
    low += (x.get() & 0xFF);

    byte lowAddress = memory.read(ByteUtils.bytesToAddress(high, low));
    byte highAddress = memory.read(ByteUtils.bytesToAddress(high, ++low));

    return ByteUtils.bytesToAddress(highAddress, lowAddress);
  }

  private short indirectIndexedAddress() {
    byte low = memory.read((short) (pc.get() + 1));
    byte high = (byte) 0x00;

    byte lowAddress = memory.read(ByteUtils.bytesToAddress(high, low));
    byte highAddress = memory.read(ByteUtils.bytesToAddress(high, ++low));

    short absolute = ByteUtils.bytesToAddress(highAddress, lowAddress);
    short result = (short) (absolute + (y.get() & 0xFF));

    checkPageCrossed(absolute, result);

    return result;
  }

  private short stackAddress() {
    return ByteUtils.bytesToAddress((byte) 0x01, sp.get());
  }

  public short getAddress(final AddrMode mode) {
    switch (mode) {
      case Immidiate:
        return immidiateAddress();
      case Absolute:
        return absoluteAddress();
      case AbsoluteIndexedX:
        return absoluteIndexedAddress(x);
      case AbsoluteIndexedY:
        return absoluteIndexedAddress(y);
      case Zeropage:
        return zeropageAddress();
      case ZeropageIndexedX:
        return zeropageIndexedAddress(x);
      case ZeropageIndexedY:
        return zeropageIndexedAddress(y);
      case Indirect:
        return indirectAddress();
      case IndirectBugged:
        return indirectBuggedAddress();
      case Relative:
        return relativeAddress();
      case IndexedIndirect:
        return indexedIndirectAddress();
      case IndirectIndexed:
        return indirectIndexedAddress();
      default:
        return 0;
    }
  }

  public byte load(final AddrMode mode) {
    return memory.read(getAddress(mode));
  }

  public byte load(final short address) {
    return memory.read(address);
  }

  public void store(final short address, final byte value) {
    memory.write(address, value);
  }

  public void push(final byte value) {
    memory.write(stackAddress(), value);
    sp.dec();
  }

  public byte pull() {
    sp.inc();
    return memory.read(stackAddress());
  }

  public void pullSr() {
    // break and ignore flag are ignored when pulling -> save, and set after
    // pulling
    boolean breakBit = sr.isBitSet(SRFlag.BREAK);
    boolean ignoreBit = sr.isBitSet(SRFlag.IGNORED);

    sr.set(pull());
    sr.setBit(SRFlag.BREAK, breakBit);
    sr.setBit(SRFlag.IGNORED, ignoreBit);
  }

  public void jumpTo(final short address) {
    pc.set(address);
    isPcChange = true;
  }

  public Register getAc() {
    return ac;
  }

  public Register getX() {
    return x;
  }

  public Register getY() {
    return y;
  }

  public Register getSp() {
    return sp;
  }

  public Register getSr() {
    return sr;
  }

  public Register16 getPc() {
    return pc;
  }

  public void checkZero(final byte b) {
    sr.setBit(SRFlag.ZERO, b == (byte) 0x00);
  }

  public void checkNegative(final byte b) {
    sr.setBit(SRFlag.NEGATIVE, ByteUtils.isBitSet(b, 7));
  }

  public void checkZeroNegative(final Register reg) {
    checkZero(reg.get());
    checkNegative(reg.get());
  }

  private void checkPageCrossed(final short oldAdress, final short newAdress) {
    isPageCross = ByteUtils.getHighByte(oldAdress) != ByteUtils.getHighByte(newAdress);
  }

  private String printOpcode(final String text, final AddrMode mode) {
    String info = "$" + ByteUtils.formatAddress(pc.get()) + ": " + text;
    switch (mode) {
      case Accumulator:
        info += " A";
        break;
      case Immidiate:
        info += " #$" + ByteUtils.formatByte(load(mode));
        break;
      case Zeropage:
        info += " $" + ByteUtils.formatByte((byte) zeropageAddress());
        break;
      case ZeropageIndexedX:
        info += " $" + ByteUtils.formatByte((byte) zeropageAddress()) + ",X";
        break;
      case ZeropageIndexedY:
        info += " $" + ByteUtils.formatByte((byte) zeropageAddress()) + ",Y";
        break;
      case Absolute:
        info += " $" + ByteUtils.formatAddress(absoluteAddress());
        break;
      case AbsoluteIndexedX:
        info += " $" + ByteUtils.formatAddress(absoluteAddress()) + ",X";
        break;
      case AbsoluteIndexedY:
        info += " $" + ByteUtils.formatAddress(absoluteAddress()) + ",Y";
        break;
      case Indirect:
      case IndirectBugged:
        info += " ($" + ByteUtils.formatAddress(absoluteAddress()) + ")";
        break;
      case Relative:
        info += " $" + ByteUtils.formatAddress((short) (relativeAddress() + 2));
        break;
      case IndexedIndirect:
        info += " ($" + ByteUtils.formatByte((byte) zeropageAddress()) + ",X)";
        break;
      case IndirectIndexed:
        info += " ($" + ByteUtils.formatByte((byte) zeropageAddress()) + "),Y";
        break;
      default:
        break;
    }

    info += "\tAC: $" + ByteUtils.formatByte(ac.get()) + "; X: $" + ByteUtils.formatByte(x.get()) + "; Y: $"
        + ByteUtils.formatByte(y.get()) + " SR: $" + ByteUtils.formatByte(sr.get()) + " SP: $"
        + ByteUtils.formatByte(sp.get());

    return info;
  }
}