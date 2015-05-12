package com.github.djarosz.fopator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fopator.regex.mapper")
@Data
public class RegexFileMapperConfig {

	private String xsltMatchPattern;

	private String xsltReplacePattern;

	private String pdfMatchPattern;

	private String pdfReplacePattern;

}
