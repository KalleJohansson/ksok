//package models;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.StringReader;
//import java.net.URI;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.TimeUnit;
//
//import net.spy.memcached.compat.log.Level;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.time.StopWatch;
//
//import play.Logger;
////import org.apache.http.HttpResponse;
////import org.apache.http.client.HttpClient;
////import org.apache.http.client.methods.HttpGet;
////import org.apache.http.conn.ConnectTimeoutException;
////import org.apache.http.conn.scheme.PlainSocketFactory;
////import org.apache.http.conn.scheme.Scheme;
////import org.apache.http.conn.scheme.SchemeRegistry;
////import org.apache.http.impl.client.DefaultHttpClient;
////import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
////import org.apache.http.params.BasicHttpParams;
////import org.apache.http.params.CoreConnectionPNames;
////import org.apache.http.params.HttpParams;
////import org.apache.http.protocol.BasicHttpContext;
////import org.apache.http.protocol.HttpContext;
////import org.dom4j.Document;
////import org.dom4j.Element;
////import org.dom4j.Node;
////import org.dom4j.io.SAXReader;
////import org.jrdf.JRDFFactory;
////import org.jrdf.SortedMemoryJRDFFactory;
////import org.jrdf.collection.MemMapFactory;
////import org.jrdf.graph.AnyObjectNode;
////import org.jrdf.graph.AnySubjectNode;
////import org.jrdf.graph.Graph;
////import org.jrdf.graph.GraphElementFactory;
////import org.jrdf.graph.Literal;
////import org.jrdf.graph.PredicateNode;
////import org.jrdf.graph.SubjectNode;
////import org.jrdf.graph.Triple;
////import org.jrdf.graph.URIReference;
////import org.jrdf.parser.rdfxml.GraphRdfXmlParser;
////
////import se.raa.fornsok.FMISApplication;
////import se.raa.fornsok.flow.model.ReferenceType;
////import se.raa.fornsok.util.StringUtils;
//import static util.KSamsokConstants.*;
//
//public class KSamsok {
//	
//
//	/**
//	 * Enkel klass som gör en listhämntning av kmb-referenser från k-samsök i en
//	 * separat tråd och blockerar åtkomst till listan innan hämtningen är klar
//	 * eller gör fel/får timeout. Används genom att skapa en instans, köra
//	 * start, vänta tills tråden har startats och har läst listan (genom koll av
//	 * {@linkplain #isStarted()}.
//	 */
//	public static class KMBReferenceFetcher extends Thread {
//		private final Object sync = new Object();
//		private List<KMBReference> references = Collections.emptyList();
//		private long objectId;
//		private int max;
//		private volatile boolean started = false;
//
//		/**
//		 * Skapar och en instans som är redo att startas
//		 * 
//		 * @param objectId
//		 *            objekt
//		 * @param max
//		 *            max antal
//		 */
//		public KMBReferenceFetcher(long objectId, int max) {
//			super("Fetcher för " + objectId);
//			this.objectId = objectId;
//			this.max = max;
//			setDaemon(true);
//		}
//
//		@Override
//		public void run() {
//			Logger.debug("Fetching thread started");
//			synchronized (sync) {
//				// flagga att vi har startat så att klienten kan returnera
//				started = true;
//				// obs, viktigt att detta anrop inte hänger för länge
//				// så timeouts för webbkommunikation etc måste användas
//				references = doFindRelatedImages(objectId, max);
//			}
//			Logger.debug("Fetching thread done");
//		}
//
//		/**
//		 * Ger sant om tråden har startats och nödvändigt lås har tagits.
//		 * 
//		 * @return sant om tråden är startad och värdehållaren är redo att
//		 *         återlämnas till anroparen
//		 */
//		public boolean isStarted() {
//			return started;
//		}
//
//		/**
//		 * Ger lista med kmb-referenser från k-samsök. Anropet blockerar tills
//		 * hämtningen är klar.
//		 * 
//		 * @return list med kmb-referenser, kan vara tom men aldrig null
//		 */
//		public List<KMBReference> getList() {
//			Logger.debug("k-samsök referenser - getList wait?");
//			synchronized (sync) {
//				Logger.debug("k-samsök referenser - getList!");
//				
//				return references;
//			}
//		}
//	}
//
//	/**
//	 * Påbörjar hämtning av relaterade bilder från k-samsök och tar bara med
//	 * härledda bilder (isVisualizedBy) och bara träffar från kmb. Hämtningen
//	 * sker i separat tråd och en värdehållare ges tillbaka som blockerar
//	 * åtkomst till listan tills hämtningen är klar (eller gör fel/får timeout).
//	 * 
//	 * @param objectId
//	 *            objekt i fmis
//	 * @param max
//	 *            max antal (<= 0 för alla)
//	 * @return värdehållare som kommer innehålla en lista med referenser från
//	 *         kmb
//	 */
//	public static KMBReferenceFetcher findRelatedImages(long objectId, int max) {
//		// skapa ny värdehållare
//		KMBReferenceFetcher fetcher = new KMBReferenceFetcher(objectId, max);
//		// starta hämtning
//		fetcher.start();
//		// hoppa inte ur förrän tråden verkligen kör så inte klienten försöker
//		// hämta listan
//		// innan tråden hinner påbörja hämtningen
//		while (!fetcher.isStarted()) {
//			try {
//				// sov kort
//				Thread.sleep(20);
//			} catch (InterruptedException ignore) {
//			}
//		}
//		return fetcher;
//	}
//
//	/**
//	 * Gör hämtningen av relaterade bilder från k-samsök och tar bara med
//	 * härledda bilder (isVisualizedBy) och bara träffar från kmb.
//	 * 
//	 * @param objectId
//	 *            objekt i fmis
//	 * @param max
//	 *            max antal (<= 0 för alla)
//	 * @return lista med referenser från kmb
//	 */
//	private static List<KMBReference> doFindRelatedImages(long objectId, int max) {
//		List<KMBReference> relatedImages = Collections.emptyList();
//		StopWatch sw = new StopWatch();
//		sw.start();
//		List<String> relatedObjectURIs = findRelated(objectId, uriPrefixKMB,
//				true);
//		if (relatedObjectURIs != null && relatedObjectURIs.size() > 0) {
//			Map<String, String> parameters = new HashMap<String, String>();
//			parameters.put("method", "search");
//			parameters.put("x-api", KSAMSOK_APIKEY);
//			String query = "";
//			for (String uri : relatedObjectURIs) {
//				if (query.length() > 0) {
//					query += " OR ";
//				}
//				query += "itemId=\"" + uri + "\"";
//			}
//			parameters.put("query", query);
//			Document doc;
//			try {
//				doc = basicQuery(KSAMSOK_API_URL, parameters, true);
//				if (doc != null) {
//					relatedImages = new ArrayList<KMBReference>();
//					@SuppressWarnings("unchecked")
//					List<Node> records = doc
//							.selectNodes("result/records/record/*");
//					if (records != null && records.size() > 0) {
//						int count = 0;
//						for (Node n : records) {
//							// filtrera bort noder som inte ligger i rdf-ns
//							if (n.getNodeType() == Node.ELEMENT_NODE
//									&& uri_RDF_NS.equals(((Element) n)
//											.getNamespaceURI())) {
//								relatedImages.add(extractInfo(n.asXML()));
//								++count;
//								if (max > 0 && count >= max) {
//									break;
//								}
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//				Logger.error("Problem att söka fram relaterade kmb-poster från k-samsök",
//								e);
//			}
//		}
//		sw.stop();
//		Logger.info("Hämtade " + relatedImages.size()
//					+ " (max " + max + ") "
//					+ "relaterade bilder från k-samsök för objekt " + objectId
//					+ " på " + sw.getTime() + "ms");
//		
//		return relatedImages;
//	}
//
//	/**
//	 * Hämtar relaterade objekt som har relationen isVisualizedBy.
//	 * 
//	 * @param objectId
//	 *            objektId
//	 * @param startsWithFilter
//	 *            filter för att inkludera träffar
//	 * @param onlyDeduced
//	 *            om bara "härledda" (dvs bakriktade) relationer ska tas med
//	 * @return lista med identifierare (uris)
//	 */
//	private static List<String> findRelated(long objectId,
//			String startsWithFilter, boolean onlyDeduced) {
//		List<String> related = Collections.emptyList();
//		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put("method", "getRelations");
//		parameters.put("relation", "isVisualizedBy");
//		parameters.put("x-api", KSAMSOK_APIKEY);
//		// TODO // Fulfix for att fa svar i tid. Om parametern saknas tar
//		// requesten for lang tid. Bugg i ksamsok?!
//		// Har stamt av med borje som sager, ange maxCount flaggan tills vidare.
//		parameters.put("maxCount", "500");
//		// FMISApplication.LOGGER.warn("Anger MaxCountvardet hart till 1000");
//		parameters.put("objectId", "raa/fmi/" + objectId);
//		try {
//			Document doc = basicQuery(KSAMSOK_API_URL, parameters, false);
//			if (doc != null) {
//				@SuppressWarnings("unchecked")
//				List<Node> nodes = doc.selectNodes("result/relations/relation"
//						+ (onlyDeduced ? "[@source='deduced']" : ""));
//				if (nodes != null && nodes.size() > 0) {
//					related = new ArrayList<String>(nodes.size());
//					for (Node n : nodes) {
//						String value = StringUtils.trimToNull(n.getText());
//						if (value != null) {
//							if (startsWithFilter != null
//									&& !value.startsWith(startsWithFilter)) {
//								continue;
//							}
//							related.add(value);
//						}
//					}
//				} else {
//					Node errNode = doc.selectSingleNode("result/error");
//					if (errNode != null) {
//						Logger.warn("Fick felmeddelande från k-samsök: "
//										+ StringUtils.trimToEmpty(errNode
//												.getText()));
//					}
//				}
//			}
//		} catch (Exception e) {
//			Logger.error("Problem att hämta relaterad-info från k-samsök", e);
//		}
//		return related;
//	}
//
//	/**
//	 * Enkel värdehållare för k-samsökreferenser till kmb.
//	 */
//	public static class KMBReference {
//		private String identifier;
//		private String htmlURL;
//		private String itemLabel;
//		private String thumbnail;
//
//		public KMBReference() {
//			
//		}
//
//		/**
//		 * Returnerar visningstyp för denna modell. Påvisar för presentationen
//		 * vilken konkret klass denna instansen har och därmed hur den skall
//		 * presenteras.
//		 * 
//		 * @return visningstyp
//		 */
//		public ReferenceType getDisplayType() {
//			return ReferenceType.KSAMSOK_KMB;
//		}
//
//		/**
//		 * Setter för identifierare (uri)
//		 * 
//		 * @param identifier
//		 *            uri
//		 */
//		public void setIdentifier(String identifier) {
//			this.identifier = identifier;
//		}
//
//		/**
//		 * Getter för identifierare
//		 * 
//		 * @return identifierare (uri)
//		 */
//		public String getIdentifier() {
//			return identifier;
//		}
//
//		/**
//		 * Setter för url till hmtl-representation.
//		 * 
//		 * @param htmlURL
//		 *            url
//		 */
//		public void setHtmlURL(String htmlURL) {
//			this.htmlURL = htmlURL;
//		}
//
//		/**
//		 * Getter för url till html-representation.
//		 * 
//		 * @return url
//		 */
//		public String getHtmlURL() {
//			return htmlURL;
//		}
//
//		/**
//		 * Setter för itemLabel.
//		 * 
//		 * @param itemLabel
//		 *            itemLabel
//		 */
//		public void setItemLabel(String itemLabel) {
//			this.itemLabel = itemLabel;
//		}
//
//		/**
//		 * Getter för itemLabel
//		 * 
//		 * @return itemLabel
//		 */
//		public String getItemLabel() {
//			return itemLabel;
//		}
//
//		/**
//		 * Setter för tumnagel-url.
//		 * 
//		 * @param thumbnail
//		 *            url
//		 */
//		public void setThumbnail(String thumbnail) {
//			this.thumbnail = thumbnail;
//		}
//
//		/**
//		 * Getter för tumnagel-url.
//		 * 
//		 * @return tumnagel-url
//		 */
//		public String getThumbnail() {
//			return thumbnail;
//		}
//	}
//
//	/**
//	 * Extraherar information för att representera en kmb-referens från rdf:en
//	 * som antas finnas i den inskickade xml:en.
//	 * 
//	 * @param xmlContent
//	 *            xml i form av rds
//	 * @return en värdehållare
//	 * @throws Exception
//	 *             vid parsnings- eller dataproblem
//	 */
//	private static KMBReference extractInfo(String xmlContent) throws Exception {
//		StringReader r = null;
//		Graph graph = null;
//		KMBReference info = new KMBReference();
//		try {
//			graph = jrdfFactory.getNewGraph();
//			GraphRdfXmlParser parser = new GraphRdfXmlParser(graph,
//					new MemMapFactory());
//			r = new StringReader(xmlContent);
//			parser.parse(r, ""); // baseuri?
//			GraphElementFactory elementFactory = graph.getElementFactory();
//			URIReference rdfType = elementFactory
//					.createURIReference(uri_rdfType);
//			URIReference samsokEntity = elementFactory
//					.createURIReference(uri_samsokEntity);
//			URIReference rURL = elementFactory.createURIReference(uri_rURL);
//			URIReference rThumbnail = elementFactory
//					.createURIReference(uri_rThumbnail);
//			URIReference rItemLabel = elementFactory
//					.createURIReference(uri_rItemLabel);
//			SubjectNode s = null;
//			String identifier = null;
//			String htmlURL = null;
//			String thumbnail = null;
//			String itemLabel = null;
//			for (Triple triple : graph.find(AnySubjectNode.ANY_SUBJECT_NODE,
//					rdfType, samsokEntity)) {
//				if (identifier != null) {
//					throw new Exception("Ska bara finnas en entity");
//				}
//				s = triple.getSubject();
//				identifier = s.toString();
//				htmlURL = extractSingleValue(graph, s, rURL);
//				thumbnail = extractSingleValue(graph, s, rThumbnail);
//				itemLabel = extractSingleValue(graph, s, rItemLabel);
//			}
//			if (identifier == null) {
//				Logger.error("Kunde inte extrahera identifierare ur rdf-grafen:\n"
//								+ xmlContent);
//				throw new Exception(
//						"Kunde inte extrahera identifierare ur rdf-grafen");
//			}
//			info.setIdentifier(identifier);
//			info.setHtmlURL(htmlURL);
//			info.setItemLabel(itemLabel);
//			info.setThumbnail(thumbnail);
//		} finally {
//			if (r != null) {
//				try {
//					r.close();
//				} catch (Exception ignore) {
//				}
//			}
//			if (graph != null) {
//				graph.close();
//			}
//		}
//		return info;
//	}
//
//	// löser ut ett enkelt värde ur subjektoden där objektnoden måste vara en
//	// literal eller en uri-referens
//	// och lägger till det mha indexprocessorn
//	private static String extractSingleValue(Graph graph, SubjectNode s,
//			PredicateNode p) throws Exception {
//		String value = null;
//		for (Triple t : graph.find(s, p, AnyObjectNode.ANY_OBJECT_NODE)) {
//			if (value != null) {
//				throw new Exception("Fler värden än ett för s: " + s + " p: "
//						+ p);
//			}
//			if (t.getObject() instanceof Literal) {
//				value = StringUtils.trimToNull(((Literal) t.getObject())
//						.getValue().toString());
//			} else if (t.getObject() instanceof URIReference) {
//				value = ((URIReference) t.getObject()).getURI().toString();
//			} else {
//				throw new Exception("Måste vara literal/urireference o.class: "
//						+ t.getObject().getClass().getSimpleName() + " för s: "
//						+ s + " p: " + p);
//			}
//		}
//		return value;
//	}
//
//	/**
//	 * Gör ett http-anrop mot inskickad uri och lägger till ev parametrar från
//	 * parameters, ev url-kodade med utf-8 om urlEncodeParamValues är true.
//	 * 
//	 * @param baseURI
//	 *            uri
//	 * @param parameters
//	 *            parametrar
//	 * @param urlEncodeParamValues
//	 *            om parametervärden ska kodas
//	 * @return ett parsat xml-dokument eller null
//	 * @throws Exception
//	 *             vid problem
//	 */
//	private static Document basicQuery(String baseURI,
//			Map<String, String> parameters, boolean urlEncodeParamValues)
//			throws Exception {
//		String uri = baseURI;
//		if (parameters != null && parameters.size() > 0) {
//			boolean first = true;
//			for (Entry<String, String> e : parameters.entrySet()) {
//				uri += (first ? "?" : "&")
//						+ e.getKey()
//						+ "="
//						+ (urlEncodeParamValues ? URLEncoder.encode(
//								e.getValue(), "utf-8") : e.getValue());
//				first = false;
//			}
//		}
//		HttpGet httpMethod = new HttpGet(uri);
//		HttpParams params = new BasicHttpParams();
//		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
//		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
//		httpMethod.setParams(params);
//		HttpContext httpContext = new BasicHttpContext();
//		HttpResponse httpResponse = null; // = httpClient.execute(httpMethod,
//											// httpContext);
//
//		SAXReader saxReader = new SAXReader();
//		StopWatch sw = new StopWatch();
//		sw.start();
//
//		int result = -1;
//		try {
//			httpResponse = httpClient.execute(httpMethod, httpContext);
//			result = httpResponse.getStatusLine().getStatusCode();
//		} catch (ConnectTimeoutException to) {
//			Logger.warn("http call to host "
//					+ httpMethod.getURI() + " took too long to respond");
//		} catch (IOException e) {
//			Logger.warn("http call to host "
//					+ httpMethod.getURI() + " got time out");
//		}
//		sw.stop();
//		Document doc = null;
//		if (result == 200) {
//			
//			Logger.debug(httpMethod.getURI()
//						+ " Duration: " + sw.getTime() + "ms");
//			
//			InputStream is = null;
//			try {
//				is = httpResponse.getEntity().getContent();
//				doc = saxReader.read(is);
//			} finally {
//				IOUtils.closeQuietly(is);
//			}
//		} else {
//			Logger.error(httpMethod.getURI() + " Duration: "
//					+ sw.getTime() + "ms Status code: " + result);
//		}
//		return doc;
//	}
//
//}
