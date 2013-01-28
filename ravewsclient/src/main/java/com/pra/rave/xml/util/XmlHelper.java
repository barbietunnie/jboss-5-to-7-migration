package com.pra.rave.xml.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.pra.util.logger.LoggerHelper;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class XmlHelper {
	static Logger logger = LoggerHelper.getLogger();
	static boolean isDebugEnabled = logger.isDebugEnabled();

    private static final String S_EMPTYSTRING = "";

    public static List<Element> getElements(Element elm, String tag) {
        List<Element> elmList = new ArrayList<Element>();
        if (elm != null) {
            NodeList nl = elm.getChildNodes();
            if (nl != null) {
                int count = nl.getLength();
                for (int i = 0; i < count; i++) {
                    Node n = nl.item(i);
                    if (tag.equalsIgnoreCase(n.getNodeName()) ||
                            tag.equalsIgnoreCase(n.getLocalName())) {
                        elmList.add((Element)n);
                    }
                }
            }
        }
        return (elmList);
    }

    public static List<Element> getElements(Element elm) {
        List<Element> elmList = new ArrayList<Element>();
        if (elm != null) {
            NodeList nl = elm.getChildNodes();
            if (nl != null) {
                int count = nl.getLength();
                for (int i = 0; i < count; i++) {
                    Node n = nl.item(i);
                    if (Node.ELEMENT_NODE == n.getNodeType()) {
                        elmList.add((Element)n);
                    }
                }
            }
        }
        return (elmList);
    }

    public static List<Text> getTexts(Element elm) {
        List<Text> textList = new ArrayList<Text>();
        if (elm != null) {
            NodeList nl = elm.getChildNodes();
            if (nl != null) {
                int count = nl.getLength();
                for (int i = 0; i < count; i++) {
                    Node n = nl.item(i);
                    if (Node.TEXT_NODE == n.getNodeType()) {
                        textList.add((Text)n);
                    }
                }
            }
        }
        return (textList);
    }

    public static Element getElement(Element elm, String tag) {
        List<Element> elmList = getElements(elm, tag);
        Element child = null;
        if (elmList.size() > 0) {
            child = (Element) elmList.get(0);
        }
        return (child);
    }

    public static Element getElement(Element elm) {
        List<Element> elmList = getElements(elm);
        Element child = null;
        if (elmList.size() > 0) {
            child = (Element) elmList.get(0);
        }
        return (child);
    }

    public static Text getText(Element elm) {
        List<Text> textList = getTexts(elm);
        Text child = null;
        if (textList.size() > 0) {
            child = (Text) textList.get(0);
        }
        return (child);
    }

    public static String getElementValue(Element elm, String tag) {
        String val = S_EMPTYSTRING;
        if (elm != null) {
            NodeList nl = elm.getChildNodes();
            if (nl != null) {
                int count = nl.getLength();
                for (int i = 0; i < count; i++) {
                    Node n = nl.item(i);
                    if (tag.equalsIgnoreCase(n.getNodeName()) ||
                            tag.equalsIgnoreCase(n.getLocalName())) {
                        Element e = (Element) n;
                        String v = getChildCharacterData(e);
                        if (v != null) {
                            val = v.trim();
                        }
                    }
                }
            }
        }
        return (val);
    }

    public static String getElementValue(Element elm) {
        String val = S_EMPTYSTRING;
        if (elm != null) {
            String v = getChildCharacterData(elm);
            if (v != null) {
                val = v.trim();
            }
        }
        return (val);
    }

    public static String getAttributeValue(Element elm, String attr) {
        String val = S_EMPTYSTRING;
        if (elm != null) {
            String v = elm.getAttribute(attr);
            if (v != null) {
                val = v.trim();
            }
        }
        return (val);
    }

    /**
	 * Transform document using XSLT.
	 * 
	 * @param doc
	 *            - document to be transformed
	 * @param xslt
	 *            - XSLT document as string
	 * @return - transformed document
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
    public static Document transform(Document doc, String xslt)
            throws TransformerConfigurationException, TransformerException {
        Document xdoc = null;
        try {
            TransformerFactory xfactory = TransformerFactory.newInstance();
            Transformer xformer = xfactory.newTransformer(new StreamSource(new StringReader(xslt)));
            DOMResult domResult = new DOMResult();
            xformer.transform(new DOMSource(doc), domResult);
            xdoc = (Document) domResult.getNode();
        } catch (TransformerConfigurationException e) {
            logger.error("Caught exception", e);
            throw (e);
        } catch (TransformerException e) {
            logger.error("Caught exception", e);
            throw (e);
        }
        return (xdoc);
    }

	/**
	 * Load xml file into Document without schema validation
	 */
	public static Document loadDocumentFromFile(File doc_file)
			throws ParserConfigurationException, SAXException, IOException {
		if (doc_file == null || doc_file.getPath() == null) {
			throw new IllegalArgumentException("Invalid input file.");
		}
		if (isDebugEnabled) {
			logger.debug("File Absolute path : " + doc_file.getAbsolutePath());
			logger.debug("File Canonical path: " + doc_file.getCanonicalPath());
		}
		logger.info("File path: " + doc_file.getPath());
		return loadDocumentFromFilePath(doc_file.getPath());
	}
	
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	/**
	 * Load xml file into Document without schema validation
	 */
	public static Document loadDocumentFromFilePath(String doc_path)
			throws ParserConfigurationException, SAXException, IOException {
		InputSource source = loadInputSourceFromFilePath(doc_path);
		// use jaxp to initialize a DOM parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(source);
		return document;
	}

	public static InputSource loadInputSourceFromFilePath(String doc_path)
			throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream doc_is = loader.getResourceAsStream(doc_path);
		if (doc_is == null) {
			throw new IllegalArgumentException("Could not find xml file: " + doc_path);
		}
		InputSource source = new InputSource(doc_is);
		return source;
	}


	/**
	 * Load xml string document into Document without schema validation
	 */
	public static Document loadDocumentFromString(String doc_string)
			throws ParserConfigurationException, SAXException, IOException {
		if (doc_string == null) {
			throw new IllegalArgumentException("XML document is not present");
		}
		// use jaxp to initialize a DOM parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		//builder.setEntityResolver(new AocEntityResolver());
		Document document = builder.parse(new InputSource(doc_string));
		return document;
	}

	/**
	 * Normalize xml using Normalizer with default Form.NFKD.
	 * @param xmlStr
	 * @return normalized xml
	 * @throws UnsupportedEncodingException
	 */
	public static String normalizeXml(String xmlStr) {
		java.text.Normalizer.Form form = java.text.Normalizer.Form.NFKD;
		return normalizeXml(xmlStr, form);
	}

	public static String normalizeXml(String xmlStr, java.text.Normalizer.Form form) {
		int pos = 0;
		if ((pos=xmlStr.indexOf("<"))>0) {
			/*
			 * XXX strip off garbage characters at the beginning of the xml
			 * received from Rave to avoid SAXParserException: Content is not
			 * allowed in prolog.
			 */
			xmlStr = xmlStr.substring(pos);
		}
		/*
		 * reconstruct the string using "UTF-8" charset will address this issue:
		 * "Invalid byte 1 of 1-byte UTF-8 sequence" 
		 */
//		try {
//			xmlStr = new String(xmlStr.getBytes("UTF-8"));
//		}
//		catch (UnsupportedEncodingException e) { // should never happen
//			logger.error("UnsupportedEncodingException caught", e);
//		}
		String normStr = Normalizer.normalize(xmlStr, form);
		return normStr;
	}

    public static void normalize(Element elm) {
        if (elm != null) {
            NodeList nl = elm.getChildNodes();
            if (nl != null) {
                int count = nl.getLength();
                for (int i = 0; i < count; i++) {
                    Node n = nl.item(i);
                    if (n != null) {
                        if (n.getNodeType() == Node.TEXT_NODE) {
                            String v = n.getNodeValue();
                            if (v == null || v.trim().length() == 0) {
                                elm.removeChild(n);
                            }
                        } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                            normalize((Element) n);
                        }
                    }
                }
            }
        }
    }

    /**
	 * Pretty print XML document using Document Builder and XML Serializer.
	 * 
	 * @param xmldoc
	 *            - document as string
	 * @return - pretty document as string
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
    public static String printXml(String xmlString) throws IOException, SAXException,
			ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
		return printXml(doc);
    }

    /**
	 * Pretty print XML document using XML Serializer.
	 * 
	 * @param doc
	 *            - DOM document
	 * @return - pretty document as string
	 */
    public static String printXml(Document doc) {
		OutputFormat format = new OutputFormat(doc);
		format.setLineWidth(80);
		format.setIndenting(true);
		format.setIndent(2);
		Writer out = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(out, format);
		try {
			serializer.serialize(doc);
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught during serialize() - ", e);
		}
		return (out.toString());
	}

    /**
	 * Pretty print XML document using either XML Serializer if the input is a
	 * Document or XSLT Transformer if the input is an Element.
	 * 
	 * @param node
	 *            - input Node, either a Document or an Element
	 * @return - pretty document as string
	 * @throws TransformerException
	 * @throws IOException
	 */
	public static String printXml(Node node) throws TransformerException {
		if (node.getOwnerDocument() != null)
			return printXml(node.getOwnerDocument());
		else
			return XsltTransformer.printXml(node);
	}

	
	public static String  getChildCharacterData (Element  parentEl) {
	   if (parentEl == null) {
	       return null;
	     }
	     Node  tempNode = parentEl.getFirstChild();
	     StringBuffer  strBuf = new StringBuffer ();
	     CharacterData  charData;
	 
	     while (tempNode != null) {
	       switch (tempNode.getNodeType()) {
	         case Node.TEXT_NODE :
	         case Node.CDATA_SECTION_NODE : charData = (CharacterData )tempNode;
	                                        strBuf.append(charData.getData());
	                                        break;
	       }
	       tempNode = tempNode.getNextSibling();
	     }
	     return strBuf.toString();
	}
	
	/**
	 * This method ensures that the output String has only valid XML unicode
	 * characters as specified by the XML 1.0 standard. For reference, please
	 * see <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>. This method will return an empty String if the input is
	 * null or empty.
	 * 
	 * @param in
	 *            The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String stripNonValidXMLCharacters(String in) {
		StringBuffer out = new StringBuffer(); // Used to hold the output.
		char current; // Used to reference the current character.

		if (in == null || ("".equals(in)))
			return ""; // vacancy test.
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught
									// here; it should not happen.
			if ((current == 0x9) || (current == 0xA) || (current == 0xD)
					|| ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}

}
