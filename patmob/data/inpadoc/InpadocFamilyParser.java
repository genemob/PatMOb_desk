package patmob.data.inpadoc;

import java.io.InputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import patmob.data.ops.OpsXPathParser;

/**
 * Parser for OPS INPADOC family XML. In the XML, each publication forms
 * a separate <ops:family-member> node, along with its corresponding application
 * and priorities. This parser organizes family as a list of applications, each
 * with the corresponding publications and priorities. Some applications
 * (e.g. abandoned or provisional) may not have publications, but are still
 * listed if they are claimed as priorities by other family members.
 * @author Piotr
 */
public class InpadocFamilyParser extends OpsXPathParser {
    
    /**
     * Parse the InputStrem to add applications (with publications and
     * priorities) to the family.
     * @param is
     * @param family 
     */
    public void parseFamily(InputStream is, InpadocFamily family) {
        String familyMemberExpression = "//ops:family-member",
                publicationExpression = "ex:publication-reference",
                applicationExpression = "ex:application-reference",
                priorityExpression = "ex:priority-claim";
        setupParser(is);
        XPath xPath = getXPath();
        InputSource inputSource = getInputSource();
        
        try {
            NodeList familyMemberNodes = (NodeList) xPath.evaluate(
                    familyMemberExpression, inputSource, XPathConstants.NODESET);
            for (int i=0; i<familyMemberNodes.getLength(); i++) {
                Node memberNode = familyMemberNodes.item(i);
                Node appNode = (Node) xPath.evaluate(applicationExpression, 
                        memberNode, XPathConstants.NODE),
                     pubNode = (Node) xPath.evaluate(publicationExpression, 
                        memberNode, XPathConstants.NODE);                        
                
                //Assume docdb format is always first item
                PatentEntity publication = 
                        parsePatentEntityNode(pubNode, xPath);
                PatentApplication application = 
                        parsePatentEntityNode(appNode, xPath);
                String appKey = application.toString();

                // if the map does not have this application, add it
                if (!family.containsKey(appKey)) {
                    family.put(appKey, application);
                }
                // add the publication
                family.get(appKey).addPublication(publication.toString());
                // if the application doesn't have priorities (was added from
                // priorities) - add them
                if (family.get(appKey).getPriorities()==null) {
                    NodeList priorityNodes = (NodeList) xPath.evaluate(
                        priorityExpression, memberNode, XPathConstants.NODESET);
                    for (int j=0; j<priorityNodes.getLength(); j++) {
                        PatentApplication priority = 
                            parsePatentEntityNode(priorityNodes.item(j), xPath);
                        family.get(appKey).addPriority(priority.toString());
                        // applications without publications found only in
                        // priorities
                        if (!family.containsKey(priority.toString())) {
                            family.put(priority.toString(), priority);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            family.setErrorMessage(ex.toString());
        }
    }
    
    // works for now
    private PatentApplication parsePatentEntityNode(Node entNode, XPath xPath) {
        PatentApplication pEnt = null;
        try {
            pEnt = new PatentApplication(
                    xPath.evaluate("ex:document-id/ex:country", entNode),
                    xPath.evaluate("ex:document-id/ex:doc-number", entNode),
                    xPath.evaluate("ex:document-id/ex:kind", entNode),
                    xPath.evaluate("ex:document-id/ex:date", entNode));
        } catch (Exception ex) {ex.printStackTrace();}
        return pEnt;
    }
    
}
