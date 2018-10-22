import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.MasterPage;

/* For manipulating tables. */
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Cell;

import org.odftoolkit.simple.text.Span;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;

import org.odftoolkit.simple.text.Paragraph;

import org.odftoolkit.simple.text.list.List;
import org.odftoolkit.simple.text.list.ListItem;

import org.odftoolkit.odfdom.pkg.OdfElement;

/* for XML parsing. */

import javax.xml.parsers.*;
import java.io.*;
import org.w3c.dom.*;
/* Element, Document */


class Translate {
    public static Document parseInput(String filename) {
        File f = new File(filename);
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
            return builder.parse(f);
        } catch (ParserConfigurationException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        }  
    }

    public static void visitChildren(Node root) {
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            System.out.println(n.getNodeName());
            if (n.getNodeType() == Node.TEXT_NODE) {
                System.out.print("\"");
                System.out.print(n.getNodeValue());
                System.out.print("\"");
                System.out.println();
            } else {
                visitChildren(n);
            }
        }
    }

    public static void main(String[] args) 
    throws Exception
    {
        String filename = args[0];
        Document doc = parseInput(filename);
        if (doc == null) {
            return;
        }
        Element root = doc.getDocumentElement();
        System.out.println(root.getTagName());
        NodeList children = root.getChildNodes();
        System.out.println(children.getLength());
        visitChildren(root);

    }
}
