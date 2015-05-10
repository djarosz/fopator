package org.github.djarosz.fopator.pdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.github.djarosz.fopator.config.Config;
import org.github.djarosz.fopator.xslt.XsltTransformerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PdfCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfCreator.class);

	private FopFactory fopFactory = FopFactory.newInstance();

	@Autowired
	private Config config;

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private XsltTransformerProvider xsltTransformerProvider;

	@PostConstruct
	public void init() throws Exception {
		config.getFontDir().mkdirs();
		fopFactory.getFontManager().setFontBaseURL(config.getFontDir().toURI().toURL().toString());
		fopFactory.getFontManager().setUseCache(true);
	}

	public void createPdf(File xmlFile) throws Exception {
		LOGGER.debug("{}: PDF generation started", xmlFile);

		long start = System.currentTimeMillis();
		File xsltFile = fileMapper.xsltFile(xmlFile);
		File outputPdfFile = fileMapper.pdfFile(xmlFile);

		LOGGER.debug("{}: Using xslt '{}'. Writing PDF to '{}'", xmlFile, xsltFile, outputPdfFile);

		xsltFile.getParentFile().mkdirs();
		outputPdfFile.getParentFile().mkdirs();

		writePdf(xmlFile, xsltFile, outputPdfFile);

		LOGGER.debug("{}: PDF generated to '{}' in {}ms", xmlFile, outputPdfFile, (System.currentTimeMillis() - start));

		if (config.isDeleteXmlAfterProcessing()) {
			if (!xmlFile.delete()) {
				LOGGER.warn("{}: Could not delete file.", xmlFile);
			}
		}
	}

	private void writePdf(File xmlFile, File xsltFile, File outputPdfFile) throws Exception {
		try (OutputStream pdfOS = new BufferedOutputStream(new FileOutputStream(outputPdfFile))) {
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent(), pdfOS);
			Source src = new StreamSource(xmlFile);
			Result res = new SAXResult(fop.getDefaultHandler());
			Transformer xmlToFoTransformer = xsltTransformerProvider.transformerFor(xsltFile);

			xmlToFoTransformer.transform(src, res);
		}
	}

	private FOUserAgent userAgent() {
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		foUserAgent.setProducer("Fopator https://github.com/djarosz/fopator");
		foUserAgent.setCreationDate(new Date());
		return foUserAgent;
	}

}