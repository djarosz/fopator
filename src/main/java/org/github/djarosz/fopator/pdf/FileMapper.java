package org.github.djarosz.fopator.pdf;

import java.io.File;

public interface FileMapper {

	File xsltFile(File xmlFile);
	File pdfFile(File xmlFile);

}
