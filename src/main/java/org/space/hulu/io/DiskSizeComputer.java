package org.space.hulu.io;

import java.io.File;

import org.space.hulu.util.Validation;

public class DiskSizeComputer {

	private final File file;

	private DiskSizeComputer(String filePath) {
		super();
		Validation.isExistedFilePath(filePath);
		this.file = new File(filePath);
	}

	public static DiskSizeComputer getInstance(String filePath) {
		return new DiskSizeComputer(filePath);
	}

	public long getTotalSpace() {
		return file.getTotalSpace();
	}

	/**
	 * according to disk file path right and other control factor to computer.
	 * @return
	 */
	public long getUsableSpace() {
		return file.getUsableSpace();
	}

	public File getFile() {
		return file;
	}

	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiskSizeComputer [file=");
		builder.append(file.getAbsolutePath());
		builder.append("]");
		return builder.toString();
	} 
}
