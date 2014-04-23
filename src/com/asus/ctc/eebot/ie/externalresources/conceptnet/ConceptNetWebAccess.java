package com.asus.ctc.eebot.ie.externalresources.conceptnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


import com.asus.ctc.eebot.ie.externalresources.conceptnet.EssentialEnums.callConceptAPI;
import com.asus.ctc.eebot.ie.externalresources.conceptnet.model.ConceptNetEdge;
import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.utilities.PorterStemmer;



public class ConceptNetWebAccess {
	static String baseURL = "http://conceptnet5.media.mit.edu/data/5.1";

	/**
	 * Send a Get Request
	 * 
	 * @param endpoint
	 * @param requestParameters
	 * @return
	 */

	public static String sendGetRequest(String endpoint,
			String requestParameters) {
		String result = null;
		if (endpoint.startsWith("http://")) {
			// Send a GET request to the servlet
			try {
				// Construct data
				//StringBuffer data = new StringBuffer();

				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length() > 0) {
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();

				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				rd.close();
				result = sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(result);
		return result;
	}

	/**
	 * Reads data from the data reader and posts it to a server via POST
	 * request. data - The data you want to send endpoint - The server's address
	 * output - writes the server's response to output
	 * 
	 * @throws Exception
	 */
	public static void postData(Reader data, URL endpoint, Writer output)
			throws Exception {
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) endpoint.openConnection();
			try {
				urlc.setRequestMethod("POST");
			} catch (ProtocolException e) {
				throw new Exception(
						"Shouldn't happen: HttpURLConnection doesn't support POST??",
						e);
			}
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestProperty("Content-type", "text/xml; charset="
					+ "UTF-8");

			OutputStream out = urlc.getOutputStream();

			try {
				Writer writer = new OutputStreamWriter(out, "UTF-8");
				pipe(data, writer);
				writer.close();
			} catch (IOException e) {
				throw new Exception("IOException while posting data", e);
			} finally {
				if (out != null)
					out.close();
			}

			InputStream in = urlc.getInputStream();
			try {
				Reader reader = new InputStreamReader(in);
				pipe(reader, output);
				reader.close();
			} catch (IOException e) {
				throw new Exception("IOException while reading response", e);
			} finally {
				if (in != null)
					in.close();
			}

		} catch (IOException e) {
			throw new Exception("Connection error (is server running at "
					+ endpoint + " ?): " + e);
		} finally {
			if (urlc != null)
				urlc.disconnect();
		}
	}

	/**
	 * Pipes everything from the reader to the writer via a buffer
	 */
	private static void pipe(Reader reader, Writer writer) throws IOException {
		char[] buf = new char[1024];
		int read = 0;
		while ((read = reader.read(buf)) >= 0) {
			writer.write(buf, 0, read);
		}
		writer.flush();
	}

	public static void main(String[] args) {

		extractConceptNetData(callConceptAPI.findConceptInfo, "duck",
				null);
		// extractConceptNetData(callConceptAPI.findAssociationBetweenTwoConcepts,
		// "tea", "coffee");

	}

	/**
	 * @param concept
	 * @return ConceptNetDataStructure
	 */
	public static void gatherCommonSenseInformationforConcept(Concept concept) {

		boolean doStemming = true;

		ConceptNetDataStructure cds;

		String conceptString = concept.getConcept();
		if (doStemming)
			conceptString = PorterStemmer.getInstance().stem(conceptString)
					.toLowerCase();
		cds = extractConceptNetData(callConceptAPI.findConceptInfo,
				conceptString);

		concept.setCommonSense(cds);

	}

	/**
	 * This function fetch the data depending on the call made
	 * 
	 * @param queryType
	 * @param vargs
	 * @return
	 */
	private static ConceptNetDataStructure extractConceptNetData(
			callConceptAPI queryType, String... vargs) {

		String endpoint = createURLforConcept(queryType, vargs);

		String result = sendGetRequest(endpoint, null);

		JsonDecoder jdecode = new JsonDecoder();
		ConceptNetDataStructure cds = jdecode
				.decodeJsonParametersforConceptNetResult(result);

		dealwithConceptNetData(cds);
		return cds;
	}

	private static void dealwithConceptNetData(ConceptNetDataStructure cds) {
		println("Total found:" + cds.numFound);
		println("Common sense facts:");
		List<ConceptNetEdge> edgeList = cds.getEdges();

		for (int i = 0; i < edgeList.size(); i++) {
			println(edgeList.get(i));
		}

		println(cds.getConceptDescription());
	}

	private static String createURLforConcept(callConceptAPI queryType,
			String... vargs) {

		String url = "";

		String concept1 = "";
		String concept2 = "";
		String language = "en";

		switch (queryType) {
		case findConceptInfo:
			concept1 = vargs[0];
			concept1 = concept1.replaceAll(" ", "_");

			language = "/" + "en";
			String type = "/" + "c";
			String var = "/" + concept1;
			String filter = "?limit=5";

			url = baseURL + type + language + var + filter;

			break;
		case findAssociationBetweenTwoConcepts:
			concept1 = vargs[0];
			concept2 = vargs[1];

			String searchString = "/search?";
			String start = "start=" + "/c/en/" + concept1;
			String end = "end=" + "/c/en/" + concept2;
			String limit = "limit=5";

			url = baseURL + searchString + "&" + start + "&" + end + "&"
					+ limit;
			break;
		default:
			break;
		}

		return url;

	}

	public static void println(Object obj) {
		System.out.println(obj);
	}

}
