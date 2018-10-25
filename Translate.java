import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.MasterPage;

/* For manipulating tables. */
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Cell;

import org.odftoolkit.simple.text.Span;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;

import org.odftoolkit.simple.text.Paragraph;

import org.odftoolkit.simple.text.list.List;
import org.odftoolkit.simple.text.list.ListItem;

import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;

/* for XML parsing. */

import javax.xml.parsers.*;
import java.io.*;
import org.w3c.dom.*;
/* Element, Document */

/* For lambda expressions */
import java.util.function.*;

class Translate {
    public TextDocument doc;
    public OdfFileDom dom;

    public Translate() 
    throws Exception
    {
        doc = TextDocument.newTextDocument();
        dom = doc.getContentDom();
    }

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

    public void makeDocument(Document input) {
        Element root = input.getDocumentElement();
        //NodeList children = root.getChildNodes();
        traverseChildren(root, child -> {
            String name = child.getNodeName();
            if (name == "p") {
                Paragraph p = doc.addParagraph("");
                makeParagraph(child, p);
            }
        });
    }

    public static void traverseChildren(Node n, Consumer<Node> visitor) {
        NodeList children = n.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            visitor.accept(children.item(i));
        }
    }

    /* Make a paragraph and add it to the "current element". Which may
     * be the text document's root, or it may be a list-item. This
     * last part will be tough to do with the Simple API. I think
     * ODFDOM is easier for this.
     */
    public void makeParagraph(Node pNode, Paragraph p) {
        NodeList children = pNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            switch (n.getNodeType()) {
                case Node.TEXT_NODE :
                    p.appendTextContent(n.getNodeValue());
                    break;
                case Node.ELEMENT_NODE :
                    OdfTextSpan s = new OdfTextSpan(dom);
                    makeSpan(n, Span.getInstanceof(s));
                    p.getOdfElement().appendChild(s);
                    break;
            }
        }
    }

    public void makeSpan(Node spanNode, Span s) {
        s.appendTextContent(spanNode.getFirstChild().getNodeValue());
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
        Translate t = new Translate();
        t.makeDocument(doc);
        t.doc.save("Translate.odt");
    }
}
