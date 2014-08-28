/*
 * Triggs u Gaeggs.
 */
package zisch.htmlparse;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Testing processing of Javadoc sites.
 * 
 * @author zisch
 */
public final class JavadocProcessor {
  /**
   * Main method.
   * 
   * @param args command line arguments (ignored)
   * 
   * @throws URISyntaxException in case of errors
   * @throws IOException in case of errors
   * @throws TransformerException in case of errors
   * @throws SAXException in case of errors
   * @throws ParserConfigurationException in case of errors
   */
  public static void main (String[] args) throws URISyntaxException, IOException, ParserConfigurationException,
          SAXException, TransformerException {
    final String fs = File.separator;
    final URL indexUrl = JavadocProcessor.class.getResource("testsite/index.html");
    final File srcRootDir = new File(indexUrl.toURI()).getParentFile();
    final File trgRootDir = new File(srcRootDir, ".." + fs + ".." + fs + ".." + fs + ".." + fs + "work" + fs
            + "doc-out").getCanonicalFile();
    System.out.println("srcRootDir: " + srcRootDir);
    System.out.println("trgRootDir: " + trgRootDir);
    final JavadocProcessor proc = new JavadocProcessor(srcRootDir, trgRootDir);
    proc.processJavadoc();
  }

  private final File mSrcRootDir;

  private final File mTrgRootDir;

  private final Map<String, Templates> mTemplates = new TreeMap<String, Templates>();

  private JavadocProcessor (final File srcRootDir, final File trgRootDir) {
    mSrcRootDir = srcRootDir;
    mTrgRootDir = trgRootDir;
  }

  private void processJavadoc () throws ParserConfigurationException, SAXException, IOException, TransformerException,
          URISyntaxException {
    mTrgRootDir.mkdirs();
    for (final File f : mSrcRootDir.listFiles()) {
      if (f.isDirectory()
              && !(f.getName().equals("resources") || f.getName().equals("index-files") || f.getName().equals(
                      "doc-files"))) {
        processPackage(f, f.getName());
      }
    }
  }

  private void processPackage (final File packageDir, final String packageName) throws ParserConfigurationException,
          SAXException, IOException, TransformerException, URISyntaxException {
    processPackageSummary(packageDir, packageName);
    for (final File f : packageDir.listFiles()) {
      if (f.isDirectory() && !(f.getName().equals("class-use") || f.getName().equals("doc-files"))) {
        processPackage(f, packageName + "." + f.getName());
      }
    }
  }

  private void processPackageSummary (final File packageDir, final String packageName)
          throws ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException {
    final File srcFile = new File(packageDir, "package-summary.html");
    if (srcFile.exists()) {
      final Document srcDom = parseHtml(srcFile);
      final Templates tmpl = tmpl("package-summary");
      final Source src = new DOMSource(srcDom, srcFile.toURI().toString());
      final File trgFile = new File(mTrgRootDir, packageName.replace('.', File.separatorChar) + File.separator
              + "package-summary.docml.xml");
      trgFile.getParentFile().mkdirs();
      final Result res = new StreamResult(trgFile);
      tmpl.newTransformer().transform(src, res);
    }
  }

  private Templates tmpl (final String templateName) throws TransformerException, URISyntaxException {
    Templates result = mTemplates.get(templateName);
    if (result == null) {
      final String fullName = "xslt/" + templateName + ".xslt";
      final URL srcUrl = JavadocProcessor.class.getResource(fullName);
      final TransformerFactory tf = TransformerFactory.newInstance();
      final Source src = new StreamSource(srcUrl.toURI().toASCIIString());
      result = tf.newTemplates(src);
    }
    assert result != null;
    return result;
  }

  private static Document parseHtml (final File f) throws ParserConfigurationException, SAXException, IOException {
    final SAXParserFactory spf = SAXParserFactory.newInstance("org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl", null);
    final Sax2DomHandler s2d = new Sax2DomHandler();
    final SAXParser sp = spf.newSAXParser();
    sp.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", s2d);
    sp.parse(f, s2d);
    return s2d.getDocument();
  }
}
