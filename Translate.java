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
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.dom.element.text.TextLineBreakElement;

import org.odftoolkit.simple.text.list.List;
import org.odftoolkit.simple.text.list.ListItem;
import org.odftoolkit.simple.text.list.BulletDecorator;
import org.odftoolkit.simple.text.list.NumberDecorator;

import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;

/* For basic font style manipulation (bold, italic) */
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;

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
        /* Remove initial paragraph. */
        removeChildren(doc.getContentRoot());
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
            } else if (name == "list" || name == "ul" || name == "ol") {
                List l = doc.addList();
                makeList(child, l);
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
        traverseChildren(pNode, child -> {
            switch (child.getNodeType()) {
                case Node.TEXT_NODE :
                    String content = child.getNodeValue();
                    //p.appendTextContent(content);
                    /* This method of adding text I got from
                     * org.odftoolkit.simple.text.Paragraph.appendTextElements
                     * Unfortunately, if there's special whitespace
                     * characters in the content, they won't be
                     * handled appropriately. Frankly, I think I'd
                     * rather handle that at the pollen level than at
                     * this level, and add a few more tags to the
                     * specification. This part should really be as
                     * simple as possible, and just transform an SEXP
                     * into an ODT document.
                     */
                    Text textNode = dom.createTextNode(content);
                    p.getOdfElement().appendChild(textNode);
                    break;
                case Node.ELEMENT_NODE :
                    OdfTextSpan s = new OdfTextSpan(dom);
                    makeSpan(child, Span.getInstanceof(s));
                    p.getOdfElement().appendChild(s);
                    break;
            }
        });
    }

    public void makeSpan(Node spanNode, Span s) {
        s.appendTextContent(spanNode.getFirstChild().getNodeValue());
        if (spanNode.getNodeName() == "bold") {
            s.getStyleHandler().getTextPropertiesForWrite()
                .setFontStyle(FontStyle.BOLD);
        } else if (spanNode.getNodeName() == "italic") {
            s.getStyleHandler().getTextPropertiesForWrite()
                .setFontStyle(FontStyle.ITALIC);
        }
    }

    public void makeList(Node listNode, List list) {
        removeChildren(list.getOdfElement());
        String name = listNode.getNodeName();
        if (name == "ul") {
            list.setDecorator(new BulletDecorator(doc));
        } else if (name == "ol") {
            list.setDecorator(new NumberDecorator(doc));
        }
        traverseChildren(listNode, child -> {
            ListItem li = list.addItem("");
            makeListItem(child, li);
        });
    }

    public void makeUnorderedList(Node listNode, List list) {
        removeChildren(list.getOdfElement());
        traverseChildren(listNode, child -> {
            ListItem li = list.addItem("");
            makeListItem(child, li);
        });
    }

    public void makeListItem(Node liNode, ListItem li) {
        OdfElement e = li.getOdfElement();
        removeChildren(e);
        traverseChildren(liNode, child -> {
            String name = child.getNodeName();
            if (name == "p") {
                OdfTextParagraph previous = 
                    OdfElement.findFirstChildNode(
                            OdfTextParagraph.class, e);
                OdfTextParagraph odfp =
                    dom.newOdfElement(OdfTextParagraph.class);
                makeParagraph(child, Paragraph.getInstanceof(odfp));
                /* It seems that adding paragraphs to a list item
                 * results in new list items. So in order for the list
                 * to display correctly, I need to make sure that only
                 * one paragraph element is actually in the list item.
                 * However, I don't really care about that from the
                 * perspective of the SEXP. It should just visually
                 * look like a new paragraph started.
                 */
                if (previous == null) {
                    e.appendChild(odfp);
                } else {
                    TextLineBreakElement lineBreak =
                        dom.newOdfElement(TextLineBreakElement.class);
                    previous.appendChild(lineBreak);
                    traverseChildren(odfp, childOfp -> {
                        previous.appendChild(childOfp);
                    });
                }
            } else if (name == "list" || name == "ul" || name == "ol") {
                List subList = li.addList();
                makeList(child, subList);
            }
        });
    }

    public static void removeChildren(OdfElement e) {
        Node child;
        while ((child = e.getFirstChild()) != null) 
            e.removeChild(child);
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
