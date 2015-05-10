package org.github.djarosz.fopator;

import java.io.File;
import org.springframework.integration.file.filters.AbstractFileListFilter;

public class AgeFileListFilter extends AbstractFileListFilter<File> {

	private long age;

	public AgeFileListFilter(long age) {
		this.age = age;
	}

	@Override
	protected boolean accept(File file) {
		return file.lastModified() + age < System.currentTimeMillis();
	}
}
