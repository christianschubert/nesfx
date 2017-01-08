package nesfx.rom;

import java.io.File;

import nesfx.common.Memory;

public class Rom {

	private String path;
	private int prgSize; // 16 kB units
	private int chrSize; // 8 kB units
	private int prgRamSize; // 8 kB units
	private int mapper;
	private Mirroring mirroring;
	private boolean hasTrainer;
	private boolean hasRam;
	private TvSystem tvSystem;

	private byte[] trainer;
	private Memory prgRam = new Memory(8192); // Fixed: 8KiB
	private Memory prg;
	private Memory chr;

	private boolean valid;

	public Rom(final String path) {
		this.path = path;
		valid = new RomReader(this).read();
	}

	public boolean isValid() {
		return valid;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return new File(path).getName();
	}

	public int getPrgSize() {
		return prgSize;
	}

	public void setPrgSize(final int prgSize) {
		this.prgSize = prgSize;
		prg = new Memory(prgSize * 16384);
	}

	public int getChrSize() {
		return chrSize;
	}

	public void setChrSize(final int chrSize) {
		this.chrSize = chrSize;
		chr = new Memory(chrSize * 8192);
	}

	public int getMapper() {
		return mapper;
	}

	public void setMapper(final int mapper) {
		this.mapper = mapper;
	}

	public Mirroring getMirroring() {
		return mirroring;
	}

	public void setMirroring(final Mirroring mirroring) {
		this.mirroring = mirroring;
	}

	public boolean hasTrainer() {
		return hasTrainer;
	}

	public void setHasTrainer(final boolean hasTrainer) {
		this.hasTrainer = hasTrainer;
	}

	public boolean hasRam() {
		return hasRam;
	}

	public void setHasRam(final boolean hasRam) {
		this.hasRam = hasRam;
	}

	public int getPrgRamSize() {
		return prgRamSize;
	}

	public void setPrgRamSize(final int prgRamSize) {
		this.prgRamSize = prgRamSize;
	}

	public TvSystem getTvSystem() {
		return tvSystem;
	}

	public void setTvSystem(final TvSystem tvSystem) {
		this.tvSystem = tvSystem;
	}

	public Memory getPrgRam() {
		return prgRam;
	}

	public Memory getPrg() {
		return prg;
	}

	public Memory getChr() {
		return chr;
	}

	public byte[] getTrainer() {
		return trainer;
	}

	public void setTrainer(final byte[] trainer) {
		this.trainer = trainer;
	}

	public enum Mirroring {
		Horizontal, Vertical, FourScreen
	}

	public enum TvSystem {
		NTSC, PAL
	}

	@Override
	public String toString() {
		return getName() + "\nPRG: " + getPrgSize() * 16 + "KB\nCHR: " + getChrSize() * 8 + "KB\n" + "PRG RAM: "
				+ (hasRam() ? getPrgRamSize() * 8 + "KB" : "No") + "\nMapper: " + getMapper() + "\nMirroring: " + mirroring
				+ "\nTrainer: " + (hasTrainer() ? "Yes" : "No") + "\nTV-System: " + tvSystem;
	}
}