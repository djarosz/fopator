package org.github.djarosz.fopator.config;

import java.io.File;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fopator.generator")
@Data
public class Config {

	private File xmlDir;

	private File fontDir;

	private String xmlFilePattern;

	private boolean deleteXmlAfterProcessing;

	private int xmlFilesPerPoll;

	private long xmlFilesPollInterval;

	private int xmlSeenFilesQueueSize;

	private int pdfGeneratorQueueSize;

	private int pdfGeneratorThreads;

	private boolean validateXML;

	private boolean ignoreValidateXML;

	private int xsltTemplatesCacheSize;

}
