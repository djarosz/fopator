package org.github.djarosz.fopator.pdf;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.github.djarosz.fopator.config.RegexFileMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegexFileMapper implements FileMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegexFileMapper.class);

	@Autowired
	private RegexFileMapperConfig config;

	private Pattern xsltMatchPattern;

	private Pattern pdfMatchPattern;

	@PostConstruct
	private void init() {
		xsltMatchPattern = Pattern.compile(config.getXsltMatchPattern());
		pdfMatchPattern = Pattern.compile(config.getPdfMatchPattern());
	}

	@Override
	public File xsltFile(File xmlFile) {
		return mapFile(xmlFile, xsltMatchPattern, config.getXsltReplacePattern());
	}

	@Override
	public File pdfFile(File xmlFile) {
		return mapFile(xmlFile, pdfMatchPattern, config.getPdfReplacePattern());
	}

	private File mapFile(File xmlFile, Pattern matchPattern, String replacePattern) {
		Matcher matcher = matchPattern.matcher(xmlFile.toString());
		if (!matcher.matches()) {
			throw new RuntimeException("File " + xmlFile + " does not match pattern " + matchPattern);
		}
		return new File(matcher.replaceFirst(replacePattern));
	}

}
