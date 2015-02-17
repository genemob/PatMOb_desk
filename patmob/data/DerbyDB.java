package patmob.data;

import java.util.logging.Level;
import java.util.logging.Logger;
import patmob.core.PatmobDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * This class provides access to the Java DB (Derby) database, as specified
 * by the PatmobDB interface.
 * @author piotr
 */
public class DerbyDB implements PatmobDB{
    static final String driver  = "org.apache.derby.jdbc.EmbeddedDriver",
                  dbName        = "PatmobDB",
                  connectionURL = "jdbc:derby:" + dbName + ";create=true";

    public DerbyDB() {
        Connection conn = null;
        try {
            // Load the Derby driver and start the Derby engine.
            Class.forName(driver);
            // Create (if needed) and connect to the database
            conn = getConnection();
            // Try to create tables - if exist, SQLException.
            if (conn!=null) createTables(conn);
            conn.close();
        } catch(ClassNotFoundException e) {
            System.err.print("DerbyDB - ClassNotFoundException: ");
            System.err.println(e.getMessage());
            System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
        } catch(Exception x) {x.printStackTrace();}
    }

    /**
     * 20120909 Gets the node with all children, recursively.
     * @param nodeID
     * @return 
     */
    @Override
    public PatentTreeNode getFullBranch(int nodeID) {
        PatentTreeNode fullBranch = null;
        
        
        Connection conn = getConnection();
        if (conn!=null) try {
            // 1. Get the requested node
            Statement stmnt = conn.createStatement();
            String sql = "select * from PATENT_COLLECTION where id = " + nodeID;
            ResultSet rs = stmnt.executeQuery(sql);
            fullBranch = addChildrenFromResults(
                    new PatentCollectionList("dummy"), rs)
                    .getChildren().iterator().next();
            
            // 2. Recursively, get the children
            addChildrenFromDB(fullBranch, stmnt);
        } catch (Exception x) {System.out.println("DerbyDB.getFullBranch(): " + x);}
        
        
        return fullBranch;
    }
    
    private void addChildrenFromDB(PatentTreeNode ptn, Statement stmnt) {
        try {
            String sql = "select * from PATENT_COLLECTION where parent_id = " +
                    ptn.getID();
            ResultSet rs = stmnt.executeQuery(sql);
            ptn = addChildrenFromResults(ptn, rs);
            if (ptn.size()>0) {
                Iterator<PatentTreeNode> it = ptn.getChildren().iterator();
                while (it.hasNext()) addChildrenFromDB(it.next(), stmnt);
            }
        } catch (SQLException ex) {
            System.out.println("DerbyDB.addChildrenFromDB(" + ptn.toString()
                    + "): " + ex);
        }
    }

    private PatentTreeNode addChildrenFromResults(PatentTreeNode parent, 
            ResultSet rs) throws SQLException {
        PatentTreeNode child = null;
        while (rs.next()) {
            int nodeType = rs.getInt("node_type");
            switch (nodeType) {
                case PatentTreeNode.PATENT_LIST:
                    child = new PatentCollectionList(rs.getString("name"));
                    break;
                case PatentTreeNode.PATENT_MAP:
                    child = new PatentCollectionMap(rs.getString("name"));
                    break;
                case PatentTreeNode.PATENT_FAMILY:
                    child = new PatentFamily(rs.getString("name"));
                    break;
                case PatentTreeNode.PATENT_DOCUMENT:
                    StringTokenizer st = new StringTokenizer(
                            rs.getString("name"));
                    String pn = "XX123", kind = "", date = "";
                    if (st.hasMoreTokens()) pn = st.nextToken();
                    if (st.hasMoreTokens()) kind = st.nextToken();
                    if (st.hasMoreTokens()) date = st.nextToken();
                    child = new PatentDocument(pn, kind, date);
                    break;
                case PatentTreeNode.NET_FEATURE:
                    child = new NetFeature(rs.getString("name"));
            }
            addChildDataFromResults(child, rs, true);
            parent.addChild(child);
        }
        return parent;
    }
    
    private void addChildDataFromResults(PatentTreeNode ptn, ResultSet rs,
            boolean deep) throws SQLException {
        ptn.setDeep(deep);
        ptn.setID(rs.getInt("id"));
        ptn.setParentID(rs.getInt("parent_id"));
        ptn.setDescription(rs.getString("notes"));
        ptn.setHilite(rs.getInt("hilite"));
    }
    
    /*20120909************ THE STUFF BELOW NEEDS CLEANUP! *********************/
    
    /**
     * Returns "shallow" node - only 2 levels deep.
     * All the nodes have deep set to false.
     */
    @Override
    public PatentTreeNode getUserCollections(int nodeID) {
        PatentTreeNode expandingNode = new PatentCollectionList();
        expandingNode.setID(nodeID);

        Connection conn = getConnection();
        if (conn!=null) try {
            Statement stmnt = conn.createStatement();
            String sql1 = "select * from PATENT_COLLECTION where parent_id = "
                    + nodeID,
                    sql2 = "select * from PATENT_COLLECTION where parent_id in ("
                    + "select distinct id from PATENT_COLLECTION where parent_id = "
                    + nodeID + ")";
            ArrayList<PatentTreeNode> collections = new ArrayList<PatentTreeNode>();
            ResultSet rs = stmnt.executeQuery(sql1);
            resultsToList(rs, collections);
            rs = stmnt.executeQuery(sql2);
            resultsToList(rs, collections);
//            expandingNode = nestCollections(expandingNode, collections);    //***CHECK THIS***
//            System.out.println("Ready to attach!");
            attachChildren(expandingNode, collections);    //***CHECK THIS***
        } catch (Exception x) {
            System.out.println("DerbyDB.getPatentCollections: " + x);
            x.printStackTrace();
        }
        return expandingNode;
    }
    
    private void resultsToList(ResultSet rs, ArrayList<PatentTreeNode> collections) {
        try {
            while (rs.next()) {
                int nodeType = rs.getInt("node_type");
                switch (nodeType) {                                             //REPETITIVE !!!
                    case PatentTreeNode.PATENT_LIST:
                        PatentCollectionList pc = new PatentCollectionList(
                                rs.getString("name"));
                        pc.setDeep(false);
                        pc.setID(rs.getInt("id"));
                        pc.setParentID(rs.getInt("parent_id"));
                        pc.setDescription(rs.getString("notes"));               //notes
                        pc.setHilite(rs.getInt("hilite"));
                        collections.add(pc);
                        break;
                    case PatentTreeNode.PATENT_MAP:
                        PatentCollectionMap pm = new PatentCollectionMap(
                                rs.getString("name"));
                        pm.setDeep(false);
                        pm.setID(rs.getInt("id"));
                        pm.setParentID(rs.getInt("parent_id"));
                        pm.setDescription(rs.getString("notes"));               //notes
                        pm.setHilite(rs.getInt("hilite"));
                        collections.add(pm);
                        break;
                    case PatentTreeNode.PATENT_DOCUMENT:
                        StringTokenizer st = new StringTokenizer(
                                rs.getString("name"));
                        String pn = "XX123", kind = "", date = "";
                        if (st.hasMoreTokens()) pn = st.nextToken();
                        if (st.hasMoreTokens()) kind = st.nextToken();
                        if (st.hasMoreTokens()) date = st.nextToken();
                        PatentDocument pd = new PatentDocument(pn, kind);
                        pd.setDeep(false);
//                        pd.setPublicationDate(pn);
                        pd.setID(rs.getInt("id"));
                        pd.setParentID(rs.getInt("parent_id"));
                        pd.setDescription(rs.getString("notes"));               //notes
                        pd.setHilite(rs.getInt("hilite"));
                        collections.add(pd);
                        break;
                    case PatentTreeNode.PATENT_FAMILY:
//                        PatentFamily pf = new PatentFamily(PatentFamily.UNKNOWN);
                        PatentFamily pf = new PatentFamily(rs.getString("name"));
                        pf.setDeep(false);
                        pf.setID(rs.getInt("id"));
                        pf.setParentID(rs.getInt("parent_id"));
                        pf.setDescription(rs.getString("notes"));               //notes
                        pf.setHilite(rs.getInt("hilite"));
                        collections.add(pf);
                }
            }
        } catch (Exception x) {
            System.out.println("DerbyDB.resultsToList: " + x);
            x.printStackTrace();
        }
    }

    public PatentTreeNode getPatentCollections() {
        PatentTreeNode rootNode = new PatentCollectionList("PatmobDB");
        Connection conn = getConnection();
        if (conn!=null) try {
            Statement stmnt = conn.createStatement();
            //parent_id is "foreign key" so need id=0 row
            String sql = "select * from patent_collection where id>0";
            ResultSet rs = stmnt.executeQuery(sql);
            ArrayList<PatentTreeNode> collections = new ArrayList<PatentTreeNode>();
            while (rs.next()) {
                int nodeType = rs.getInt("node_type");
                switch (nodeType) {                                             //REPETITIVE !!!
                    case PatentTreeNode.PATENT_LIST:
                        PatentCollectionList pc = new PatentCollectionList(
                                rs.getString("name"));
                        pc.setID(rs.getInt("id"));
                        pc.setParentID(rs.getInt("parent_id"));
                        pc.setDescription(rs.getString("notes"));               //notes
                        pc.setHilite(rs.getInt("hilite"));
                        collections.add(pc);
                        break;
                    case PatentTreeNode.PATENT_MAP:
                        PatentCollectionMap pm = new PatentCollectionMap(
                                rs.getString("name"));
                        pm.setID(rs.getInt("id"));
                        pm.setParentID(rs.getInt("parent_id"));
                        pm.setDescription(rs.getString("notes"));               //notes
                        pm.setHilite(rs.getInt("hilite"));
                        collections.add(pm);
                        break;
                    case PatentTreeNode.PATENT_DOCUMENT:
                        StringTokenizer st = new StringTokenizer(
                                rs.getString("name"));
                        String pn = "XX123", kind = "", date = "";
                        if (st.hasMoreTokens()) pn = st.nextToken();
                        if (st.hasMoreTokens()) kind = st.nextToken();
                        if (st.hasMoreTokens()) date = st.nextToken();
                        PatentDocument pd = new PatentDocument(pn, kind);
//                        pd.setPublicationDate(pn);
                        pd.setID(rs.getInt("id"));
                        pd.setParentID(rs.getInt("parent_id"));
                        pd.setDescription(rs.getString("notes"));               //notes
                        pd.setHilite(rs.getInt("hilite"));
                        collections.add(pd);
                        break;
                    case PatentTreeNode.PATENT_FAMILY:
                        PatentFamily pf = new PatentFamily(PatentFamily.UNKNOWN);
                        pf.setID(rs.getInt("id"));
                        pf.setParentID(rs.getInt("parent_id"));
                        pf.setDescription(rs.getString("notes"));               //notes
                        pf.setHilite(rs.getInt("hilite"));
                        collections.add(pf);
                }
            }
                rootNode = nestCollections(rootNode, collections);
        } catch (Exception x) {
            System.out.println("DerbyDB.getPatentCollections: " + x);
            x.printStackTrace();
        }
        return rootNode;
    }

    private PatentTreeNode nestCollections(PatentTreeNode rootNode,
            ArrayList<PatentTreeNode> collections) {
        for (int i=0; i<collections.size(); i++) {
            PatentTreeNode node = collections.get(i);
            if (node.getParentID()==0) {
                rootNode.addChild(node);
                attachChildren(node, collections);
            }
        }
        return rootNode;
    }

    private void attachChildren(PatentTreeNode parent, ArrayList<PatentTreeNode> al) {
//        if (parent.getType()!=PatentTreeNode.PATENT_DOCUMENT) //patents NO children???--NOT FASTER!
        for (int i=0; i<al.size(); i++) {
            PatentTreeNode child = al.get(i);
            if (parent.getID()==child.getParentID()) {
                parent.addChild(child);
//                System.out.println(child.getType());
//                if (child.getType()!=PatentTreeNode.PATENT_DOCUMENT)     //patents NO children???
                attachChildren(child, al);
            }
        }
    }

    //TODO: parentID from getParentID()
    public int savePatentCollection(PatentTreeNode collection) {
        Connection conn = getConnection();
        if (collection.getID()==0) 
            return insertPatentTreeNode(
                    collection, collection.getParentID(), conn);
        else return updatePatentTreeNode(
                collection, collection.getParentID(), conn);
    }


    private int updatePatentTreeNode(PatentTreeNode collection, int parentID, Connection conn) {
        int result = -1;
        if (conn!=null) try {
            String name = collection.toString();
            if (name.length()>50) {
                name = name.substring(0, 49);
                System.out.println("DB truncated name: " + collection.toString());
            }
            if (name.contains("'")) {
                name = name.replace("'", "");
                System.out.println("DB removed ' from name: " + collection.toString());
            }
            String notes = collection.getDescription();
            if (notes.length()>500) {
                notes = notes.substring(0, 499);
                System.out.println("DB truncated notes: " + collection.getDescription());
            }
            if (notes.contains("'")) {
                notes = notes.replace("'", "");
                System.out.println("DB removed ' from notes: " + collection.getDescription());
            }
            String sql = "update patent_collection set node_type = " +
                    collection.getType() + ", parent_id = " +
                    parentID + ", name = '" +
                    name + "', notes = '" +
                    notes + "', hilite = " +
                    collection.getHilite() + " where id = " +
                    collection.getID();
            Statement stmt = conn.createStatement();
            result = stmt.executeUpdate(sql);
            if (result>0 && collection.size()>0) {
                Collection<PatentTreeNode> children = collection.getChildren();
                Iterator<PatentTreeNode> it = children.iterator();
                while (it.hasNext()) {
                    PatentTreeNode ptn = it.next();
                    if (ptn.getID()==0)
                        insertPatentTreeNode(ptn, collection.getID(), conn);
                    else updatePatentTreeNode(ptn, collection.getID(), conn);
                }
            }
        }catch (Exception x) {
            System.out.println("DerbyDB.updatePatentTreeNode: " + x);
            x.printStackTrace();
        }
        return result;
    }

    private int insertPatentTreeNode(PatentTreeNode collection, int parentID, Connection conn) {
        int result = 0, collectionID = -1;
        if (conn!=null) try {
            String name = collection.toString();
            if (name.length()>50) {
                name = name.substring(0, 49);
                System.out.println("DB truncated name: " + collection.toString());
            }
            if (name.contains("'")) {
                name = name.replace("'", "");
                System.out.println("DB removed ' from name: " + collection.toString());
            }
            String notes = collection.getDescription();
            if (notes.length()>500) {
                notes = notes.substring(0, 499);
                System.out.println("DB truncated notes: " + collection.getDescription());
            }
            if (notes.contains("'")) {
                notes = notes.replace("'", "");
                System.out.println("DB removed ' from notes: " + collection.getDescription());
            }
            String sql = "insert into patent_collection values (DEFAULT, " +
                    collection.getType() + "," +
                    parentID + ",'" +
                    name + "','" +
                    notes + "', " +
                    collection.getHilite() + ")";                         //notes
            Statement stmt = conn.createStatement();
            result = stmt.executeUpdate(sql);
            ResultSet rs;
            if (result>0) {
                rs = stmt.executeQuery("select max(id) from patent_collection");
                if (rs.next()) collectionID = rs.getInt(1);
                collection.setID(collectionID);
                if (collection.size()>0) {
                    Collection<PatentTreeNode> children = collection.getChildren();
                    Iterator<PatentTreeNode> it = children.iterator();
                    while (it.hasNext()) {
                        PatentTreeNode ptn = it.next();
//                        if (ptn.getID()==0)                                   //patent in multiple nodes
                            insertPatentTreeNode(ptn, collectionID, conn);
//                        else updatePatentTreeNode(ptn, collectionID, conn);   //saved only once
                    }
                }
            }
        }catch (Exception x) {
            System.out.println("DerbyDB.insertPatentTreeNode: " + x);
            System.out.println("PatentTreeNode: " + collection.toString());
            x.printStackTrace();
        }

        return collectionID;
    }

    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionURL);
        } catch(SQLException x) {
            System.err.println("Couldn't connect to " + dbName);
            x.printStackTrace();
        }
        return connection;
    }

    private void createTables(Connection conn) {
        Statement stmt;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("create table patent_collection (" +
                    "id int primary key generated by default as identity " +
                    "(start with 1, increment by 1), " +
                    "node_type int, " +
                    "parent_id int, " +
                    "name varchar(50), " +
                    "notes varchar(500), " +                                    //notes
                    "hilite int, " +                                    //notes
                    "constraint parent_fk foreign key (parent_id) " +
                    "references patent_collection (id) " +
                    "on delete cascade)");
            stmt.executeUpdate("insert into patent_collection " +
                    "(id, node_type, name, notes, hilite) values (0, " +                //notes
                    PatentTreeNode.PATENT_LIST + ", 'PatmobDB', "
                    + "'Root node of the database', " +
                    PatentTreeNode.HILITE_NONE + ")");                          //notes
            stmt.close();
            System.out.println("Created tables in Derby PatmobDB");
        } catch(SQLException x) {
            System.out.println("DerbyDB.createTables: " + x.getMessage());
            if (!x.getSQLState().equals("X0Y32"))
                x.printStackTrace();
        }
    }

    public int deletePatentCollection(PatentTreeNode collection) {
        int result = -1;
        Connection conn = getConnection();
        if (conn!=null) try {
            Statement stmt = conn.createStatement();
            result = stmt.executeUpdate("delete from patent_collection "
                    + "where id = " + collection.getID());
            stmt.close();
            conn.close();
        } catch(Exception x) {
            System.out.println("DerbyDB.deletePatentCollection: " + x);
        }
        return result;
    }

    public Collection<String> keywordSearch(String query) {
        ArrayList<String> results = null;
        Connection conn = getConnection();
        if (conn!=null) try {
            Statement stmnt = conn.createStatement(),
                    stmnt2 = conn.createStatement();
            String sql = "select * from PATENT_COLLECTION where " + query;
            ResultSet rs = stmnt.executeQuery(sql);
            results = new ArrayList<String>();
            while (rs.next()) {
                String nodePath = rs.getString("name");
                int parentID = rs.getInt("parent_id");
                while (parentID!=0) {
                    ResultSet rs2 = stmnt2.executeQuery("select PARENT_ID, NAME "
                            + "from PATENT_COLLECTION where ID = " + parentID);
                    if (rs2.next()) {
                        nodePath += " < " + rs2.getString("name");
                        parentID = rs2.getInt("parent_id");
                    }
                }
                results.add(nodePath);
            }
        } catch(Exception x) {
            System.out.println("DerbyDB.keywordSearch: " + x);
        }
        if (results!=null) Collections.sort(results);
        return results;
    }
}
