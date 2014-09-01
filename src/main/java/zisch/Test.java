package zisch;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


public class Test {

  public static void main (final String[] args) throws Exception {
    System.setProperty("java.net.useSystemProxies", "true");
    final URL url = new URL("http://www.w3.org/2001/xml.xsd");
    final File outfile = new File(
            "C:\\UBS\\Dev\\t471091\\prj\\testrepo1\\src\\main\\resources\\zisch\\catalog\\xml\\xml.xsd");
    final InputStream is = url.openStream();
    final OutputStream os = new BufferedOutputStream(new FileOutputStream(outfile));
    final byte[] buf = new byte[8 * 1024];
    try {
      for (int cnt = is.read(buf); cnt > 0; cnt = is.read(buf)) {
        os.write(buf, 0, cnt);
      }
    } finally {
      os.close();
      is.close();
    }
  }
}
