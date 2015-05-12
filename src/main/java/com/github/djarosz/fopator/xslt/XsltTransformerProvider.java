package com.github.djarosz.fopator.xslt;

import java.io.File;
import javax.xml.transform.Transformer;

public interface XsltTransformerProvider {

	Transformer transformerFor(File xsltFile) throws Exception;
}
