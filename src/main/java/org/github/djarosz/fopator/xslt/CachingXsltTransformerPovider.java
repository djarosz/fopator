package org.github.djarosz.fopator.xslt;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.github.djarosz.fopator.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

@Component
public class CachingXsltTransformerPovider implements XsltTransformerProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingXsltTransformerPovider.class);

	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String XSLT20_XSD = "xslt20.xsd";

	private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
	private final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

	@Autowired
	private Config config;

	private Map<File, Templates> templatesMap = new LinkedHashMap<File, Templates>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > config.getXsltTemplatesCacheSize();
		}
	};

	@PostConstruct
	public void init() {
		parserFactory.setNamespaceAware(true);
		parserFactory.setXIncludeAware(true);
		parserFactory.setValidating(true);
	}

	@Override
	public Transformer transformerFor(File xsltFile) throws Exception {
		Transformer transformer = templatesFor(xsltFile).newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setParameter("versionParam", "2.0");
		return transformer;
	}

	private Templates templatesFor(File xsltFile) throws Exception {
		Templates templates = templatesMap.get(xsltFile);
		if (templates == null) {
			Source source = config.isValidateXML() ? validatingSource(xsltFile) : nonValidatingSource(xsltFile);
			templates = transformerFactory.newTemplates(source);
			templatesMap.put(xsltFile, templates);
		}

		return templates;
	}

	private Source validatingSource(File xsltFile) throws Exception {
		SAXParser parser = parserFactory.newSAXParser();
		parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

		String xsltXml = addXsltNamespacesNeededForValidation(xsltFile);
		SAXSource source = new SAXSource(parser.getXMLReader(), new InputSource(new StringReader(xsltXml)));
		source.getXMLReader().setErrorHandler(new LoggingErrorHandler());

		return source;
	}

	private Source nonValidatingSource(File xsltFile) throws Exception {
		return new StreamSource(xsltFile);
	}

	private String addXsltNamespacesNeededForValidation(File xmlFile) throws Exception {
		NS[] namespaces = {
				new NS("http://www.w3.org/2001/XMLSchema", "xs", null),
				new NS("http://www.w3.org/2001/XMLSchema-instance", "xsi", null),
				//{ "http://www.w3.org/1999/XSL/Format", "xmlns:fo", new File(RESOURCES_DIR, "/fop.xsd").toURI().toString() },
				new NS("http://www.w3.org/1999/XSL/Transform", "xsl", this.getClass().getClassLoader().getResource(XSLT20_XSD).toString())
		};
		return addNamespacesToRootElement(xmlFile, namespaces);
	}

	private String addNamespacesToRootElement(File xmlFile, NS[] namespaces) throws Exception {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.parse(xmlFile);
		doc.setXmlVersion("1.0");

		Element rootElement = doc.getDocumentElement();

		for (NS ns : namespaces) {
			rootElement.setAttribute("xmlns:" + ns.prefix, ns.uri);
			if (ns.location != null) {
				String schemaLoc = rootElement.getAttribute("xsi:schemaLocation");
				rootElement.setAttribute("xsi:schemaLocation", schemaLoc + " " + ns.uri + " " + ns.location);
			}
		}

		Transformer trans = transformerFactory.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.VERSION, "1.0");
		trans.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);

		return sw.toString();
	}

	private static class NS {

		private String uri;
		private String prefix;
		private String location;

		private NS(String uri, String prefix, String location) {
			this.uri = uri;
			this.prefix = prefix;
			this.location = location;
		}
	}

	private class LoggingErrorHandler extends DefaultHandler {

		public void warning(SAXParseException e) throws SAXException {
			printInfo("Warning:", e);
		}

		public void error(SAXParseException e) throws SAXException {
			printInfo("Error:", e);
			if (!config.isIgnoreValidateXML()) {
				throw e;
			}
		}

		public void fatalError(SAXParseException e) throws SAXException {
			printInfo("Fatal error:", e);
			if (!config.isIgnoreValidateXML()) {
				throw e;
			}
		}

		private void printInfo(String message, SAXParseException e) {
			LOGGER.info("{}\n\tPublic ID: {}\n\tSystem ID: {}\n\tLine number: {}\n\tColumn number: {}\n\tMessage: {}",
					message, e.getPublicId(),  e.getSystemId(), e.getLineNumber(), e.getColumnNumber(),  e.getMessage());
		}
	}

}
