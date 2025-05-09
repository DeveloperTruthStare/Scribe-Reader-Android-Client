package nl.siegmann.epublib.epub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.ResourceUtil;
import nl.siegmann.epublib.util.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

/**
 * Writes the ncx document as defined by namespace http://www.daisy.org/z3986/2005/ncx/
 * 
 * @author paul
 *
 */
public class NCXDocument {
	
	public static final String NAMESPACE_NCX = "http://www.daisy.org/z3986/2005/ncx/";
	public static final String PREFIX_NCX = "ncx";
	public static final String NCX_ITEM_ID = "ncx";
	public static final String DEFAULT_NCX_HREF = "toc.ncx";
	public static final String PREFIX_DTB = "dtb";
	

	private interface NCXTags {
		String ncx = "ncx";
		String meta = "meta";
		String navPoint = "navPoint";
		String navMap = "navMap";
		String navLabel = "navLabel";
		String content = "content";
		String text = "text";
		String docTitle = "docTitle";
		String docAuthor = "docAuthor";
		String head = "head";
	}
	
	private interface NCXAttributes {
		String src = "src";
		String name = "name";
		String content = "content";
		String id = "id";
		String playOrder = "playOrder";
		String clazz = "class";
		String version = "version";
	}

	private interface NCXAttributeValues {

		String chapter = "chapter";
		String version = "2005-1";
		
	}
	
	public static Resource read(Book book, EpubReader epubReader) {
		Resource ncxResource = null;
		if(book.getSpine().getTocResource() == null) {
			return ncxResource;
		}
		try {
			ncxResource = book.getSpine().getTocResource();
			if(ncxResource == null) {
				return ncxResource;
			}
			Document ncxDocument = ResourceUtil.getAsDocument(ncxResource);
			Element navMapElement = DOMUtil.getFirstElementByTagNameNS(ncxDocument.getDocumentElement(), NAMESPACE_NCX, NCXTags.navMap);
			TableOfContents tableOfContents = new TableOfContents(readTOCReferences(navMapElement.getChildNodes(), book));
			book.setTableOfContents(tableOfContents);
		} catch (Exception e) {
		}
		return ncxResource;
	}
	
	private static List<TOCReference> readTOCReferences(NodeList navpoints, Book book) {
		if(navpoints == null) {
			return new ArrayList<TOCReference>();
		}
		List<TOCReference> result = new ArrayList<TOCReference>(navpoints.getLength());
		for(int i = 0; i < navpoints.getLength(); i++) {
			Node node = navpoints.item(i);
			if (node.getNodeType() != Document.ELEMENT_NODE) {
				continue;
			}
			if (! (node.getLocalName().equals(NCXTags.navPoint))) {
				continue;
			}
			TOCReference tocReference = readTOCReference((Element) node, book);
			result.add(tocReference);
		}
		return result;
	}

	static TOCReference readTOCReference(Element navpointElement, Book book) {
		String label = readNavLabel(navpointElement);
		String tocResourceRoot = StringUtil.substringBeforeLast(book.getSpine().getTocResource().getHref(), '/');
		if (tocResourceRoot.length() == book.getSpine().getTocResource().getHref().length()) {
			tocResourceRoot = "";
		} else {
			tocResourceRoot = tocResourceRoot + "/";
		}
		String reference = StringUtil.collapsePathDots(tocResourceRoot + readNavReference(navpointElement));
		String href = StringUtil.substringBefore(reference, Constants.FRAGMENT_SEPARATOR_CHAR);
		String fragmentId = StringUtil.substringAfter(reference, Constants.FRAGMENT_SEPARATOR_CHAR);
		Resource resource = book.getResources().getByHref(href);

		TOCReference result = new TOCReference(label, resource, fragmentId);
		List<TOCReference> childTOCReferences = readTOCReferences(navpointElement.getChildNodes(), book);
		result.setChildren(childTOCReferences);
		return result;
	}
	
	private static String readNavReference(Element navpointElement) {
		Element contentElement = DOMUtil.getFirstElementByTagNameNS(navpointElement, NAMESPACE_NCX, NCXTags.content);
		String result = DOMUtil.getAttribute(contentElement, NAMESPACE_NCX, NCXAttributes.src);
		try {
			result = URLDecoder.decode(result, Constants.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	private static String readNavLabel(Element navpointElement) {
		Element navLabel = DOMUtil.getFirstElementByTagNameNS(navpointElement, NAMESPACE_NCX, NCXTags.navLabel);
		return DOMUtil.getTextChildrenContent(DOMUtil.getFirstElementByTagNameNS(navLabel, NAMESPACE_NCX, NCXTags.text));
	}

	
	public static void write(EpubWriter epubWriter, Book book, ZipOutputStream resultStream) throws IOException {
		resultStream.putNextEntry(new ZipEntry(book.getSpine().getTocResource().getHref()));
		XmlSerializer out = EpubProcessorSupport.createXmlSerializer(resultStream);
		write(out, book);
		out.flush();
	}
	

	/**
	 * Generates a resource containing an xml document containing the table of contents of the book in ncx format.
	 * 
	 * @param xmlSerializer the serializer used
	 * @param book the book to serialize
	 * 
	 * @throws FactoryConfigurationError
	 * @throws IOException 
	 * @throws IllegalStateException 
	 * @throws IllegalArgumentException 
	 */
	public static void write(XmlSerializer xmlSerializer, Book book) throws IllegalArgumentException, IllegalStateException, IOException {
		write(xmlSerializer, book.getMetadata().getIdentifiers(), book.getTitle(), book.getMetadata().getAuthors(), book.getTableOfContents());
	}
	
	public static Resource createNCXResource(Book book) throws IllegalArgumentException, IllegalStateException, IOException {
		return createNCXResource(book.getMetadata().getIdentifiers(), book.getTitle(), book.getMetadata().getAuthors(), book.getTableOfContents());
	}
	public static Resource createNCXResource(List<Identifier> identifiers, String title, List<Author> authors, TableOfContents tableOfContents) throws IllegalArgumentException, IllegalStateException, IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		XmlSerializer out = EpubProcessorSupport.createXmlSerializer(data);
		write(out, identifiers, title, authors, tableOfContents);
		Resource resource = new Resource(NCX_ITEM_ID, data.toByteArray(), DEFAULT_NCX_HREF, MediatypeService.NCX);
		return resource;
	}	
	
	public static void write(XmlSerializer serializer, List<Identifier> identifiers, String title, List<Author> authors, TableOfContents tableOfContents) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startDocument(Constants.CHARACTER_ENCODING, false);
		serializer.setPrefix(EpubWriter.EMPTY_NAMESPACE_PREFIX, NAMESPACE_NCX);
		serializer.startTag(NAMESPACE_NCX, NCXTags.ncx);
//		serializer.writeNamespace("ncx", NAMESPACE_NCX);
//		serializer.attribute("xmlns", NAMESPACE_NCX);
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.version, NCXAttributeValues.version);
		serializer.startTag(NAMESPACE_NCX, NCXTags.head);

		for(Identifier identifier: identifiers) {
			writeMetaElement(identifier.getScheme(), identifier.getValue(), serializer);
		}
		
		writeMetaElement("generator", Constants.EPUBLIB_GENERATOR_NAME, serializer);
		writeMetaElement("depth", String.valueOf(tableOfContents.calculateDepth()), serializer);
		writeMetaElement("totalPageCount", "0", serializer);
		writeMetaElement("maxPageNumber", "0", serializer);

		serializer.endTag(NAMESPACE_NCX, "head");
		
		serializer.startTag(NAMESPACE_NCX, NCXTags.docTitle);
		serializer.startTag(NAMESPACE_NCX, NCXTags.text);
		// write the first title
		serializer.text(StringUtil.defaultIfNull(title));
		serializer.endTag(NAMESPACE_NCX, NCXTags.text);
		serializer.endTag(NAMESPACE_NCX, NCXTags.docTitle);
		
		for(Author author: authors) {
			serializer.startTag(NAMESPACE_NCX, NCXTags.docAuthor);
			serializer.startTag(NAMESPACE_NCX, NCXTags.text);
			serializer.text(author.getLastname() + ", " + author.getFirstname());
			serializer.endTag(NAMESPACE_NCX, NCXTags.text);
			serializer.endTag(NAMESPACE_NCX, NCXTags.docAuthor);
		}
		
		serializer.startTag(NAMESPACE_NCX, NCXTags.navMap);
		writeNavPoints(tableOfContents.getTocReferences(), 1, serializer);
		serializer.endTag(NAMESPACE_NCX, NCXTags.navMap);
		
		serializer.endTag(NAMESPACE_NCX, "ncx");
		serializer.endDocument();
	}


	private static void writeMetaElement(String dtbName, String content, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException  {
		serializer.startTag(NAMESPACE_NCX, NCXTags.meta);
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.name, PREFIX_DTB + ":" + dtbName);
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.content, content);
		serializer.endTag(NAMESPACE_NCX, NCXTags.meta);
	}
	
	private static int writeNavPoints(List<TOCReference> tocReferences, int playOrder,
			XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException  {
		for(TOCReference tocReference: tocReferences) {
			if (tocReference.getResource() == null) {
				playOrder = writeNavPoints(tocReference.getChildren(), playOrder, serializer);
				continue;
			}
			writeNavPointStart(tocReference, playOrder, serializer);
			playOrder++;
			if(! tocReference.getChildren().isEmpty()) {
				playOrder = writeNavPoints(tocReference.getChildren(), playOrder, serializer);
			}
			writeNavPointEnd(tocReference, serializer);
		}
		return playOrder;
	}


	private static void writeNavPointStart(TOCReference tocReference, int playOrder, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException  {
		serializer.startTag(NAMESPACE_NCX, NCXTags.navPoint);
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.id, "navPoint-" + playOrder);
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.playOrder, String.valueOf(playOrder));
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.clazz, NCXAttributeValues.chapter);
		serializer.startTag(NAMESPACE_NCX, NCXTags.navLabel);
		serializer.startTag(NAMESPACE_NCX, NCXTags.text);
		serializer.text(tocReference.getTitle());
		serializer.endTag(NAMESPACE_NCX, NCXTags.text);
		serializer.endTag(NAMESPACE_NCX, NCXTags.navLabel);
		serializer.startTag(NAMESPACE_NCX, NCXTags.content);
		serializer.attribute(EpubWriter.EMPTY_NAMESPACE_PREFIX, NCXAttributes.src, tocReference.getCompleteHref());
		serializer.endTag(NAMESPACE_NCX, NCXTags.content);
	}

	private static void writeNavPointEnd(TOCReference tocReference, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException  {
		serializer.endTag(NAMESPACE_NCX, NCXTags.navPoint);
	}
}
