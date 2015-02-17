package patmob.data.ops;

import java.io.InputStream;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author Piotr
 */
public abstract class OpsXPathParser {
    XPath xPath;
    InputSource inputSource;

    public OpsXPathParser() {
        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new OPSNamespaceContext());
    }
    
    public void setupParser(InputStream is) {
        inputSource = new InputSource(is);
    }
    
    public XPath getXPath() {return xPath;}
    
    public InputSource getInputSource() {return inputSource;}
    
    class OPSNamespaceContext implements NamespaceContext {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            }
            else if ("ops".equals(prefix)) {
                return "http://ops.epo.org";
            }
            else if ("ex".equals(prefix)) {
                return "http://www.epo.org/exchange";
            }
            else if ("ccd".equals(prefix)) {
                return "http://www.epo.org/ccd";
            }
            else if ("xlink".equals(prefix)) {
                return "http://www.w3.org/1999/xlink";
            }
            else if ("reg".equals(prefix)) {
                return "http://www.epo.org/register";
            }
            else if ("cpc".equals(prefix)) {
                return "http://www.epo.org/cpcexport";
            }
            else if ("xml".equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            }
            return XMLConstants.NULL_NS_URI;
        }

        // This method isn't necessary for XPath processing.
        @Override
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        @Override
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }
}
