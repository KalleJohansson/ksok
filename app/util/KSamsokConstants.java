package util;

import java.net.URI;
import java.util.concurrent.TimeUnit;

//import models.DefaultHttpClient;
//import models.HttpClient;
//import models.JRDFFactory;
//import models.Scheme;
//import models.SchemeRegistry;
//import models.ThreadSafeClientConnManager;

/**
 * Util-klass för att prata med k-samsök, fn bara för att få fram
 * kmb-referenser.
 */
public final class KSamsokConstants {
	
	public static final String uriPrefix = "http://kulturarvsdata.se/";
	public static final String uriPrefixKSamsok = uriPrefix + "ksamsok#";
	public static final String uriPrefixKMB = uriPrefix + "raa/kmb/";

	public static final String uri_RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public static final URI uri_rdfType = URI
			.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	public static final URI uri_samsokEntity = URI.create(uriPrefixKSamsok
			+ "Entity");

	public static final URI uri_rThumbnail = URI.create(uriPrefixKSamsok
			+ "thumbnail");
	public static final URI uri_rURL = URI.create(uriPrefixKSamsok + "url");
	public static final URI uri_rItemLabel = URI.create(uriPrefixKSamsok
			+ "itemLabel");


	// url till k-sams�k
	public static final String KSAMSOK_HOST = System.getProperty(
			"se.raa.fornsok.ksamsok.host", uriPrefix);
	public static final String KSAMSOK_API_URL = KSAMSOK_HOST
			+ (KSAMSOK_HOST.endsWith("/") ? "" : "/") + "ksamsok/api";

	// api-nyckel
	public static final String KSAMSOK_APIKEY = System.getProperty(
			"se.raa.fornsok.ksamsok.apikey", "forn815");
	
//	// har en close() men den g�r inget s� vi skapar bara en instans
//		public static final JRDFFactory jrdfFactory = SortedMemoryJRDFFactory
//				.getFactory();
//
//		public static final HttpClient httpClient;
//		static {
//			SchemeRegistry schemeRegistry = new SchemeRegistry();
//			// regga http med default-port 80
//			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
//					.getSocketFactory()));
//			// pool
//			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
//					schemeRegistry, 60, TimeUnit.MINUTES);
//			cm.setDefaultMaxPerRoute(20);
//			cm.setMaxTotal(20);
//			httpClient = new DefaultHttpClient(cm);
}
