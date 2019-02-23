package at.ac.tuwien.touristguide.tools;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.util.List;

import at.ac.tuwien.touristguide.entities.Poi;




// Got this from a helpful StackOverflow Post http://stackoverflow.com/a/6977616
public class RestClient {

	public enum RequestMethod {
		GET,
		POST
	}

	private int responseCode = 0;
	private String message;
	private String response;
	private Document doc;
	private List<Poi> pois;


	public List<Poi> getUpdate(RequestMethod method, String url) throws Exception {
		boolean success = false;
		switch (method) {
			case GET: {
				HttpGet request = new HttpGet(url);
				success = executeRequest(request, url, false);
				break;
			}
	
			case POST: {
				HttpPost request = new HttpPost(url);
				success = executeRequest(request, url, false);
				break;
			}
		}

		if (success)
			return pois;

		return null;
	}

	public int getPoisSize(RequestMethod method, String url) throws Exception {
		boolean success = false;
		switch (method) {
			case GET: {
				HttpGet request = new HttpGet(url);
				success = executeRequest(request, url, true);
				break;
			}
	
			case POST: {
				HttpPost request = new HttpPost(url);
				success = executeRequest(request, url, true);
				break;
			}
		}

		if (success)
			return Integer.parseInt(response.trim());

		return -1;
	}


	public String getUpdateId(RequestMethod method, String url) {
		boolean success = false;
		switch (method) {
			case GET: {
				HttpGet request = new HttpGet(url);
				success = executeRequest(request, url, true);
				break;
			}
	
			case POST: {
				HttpPost request = new HttpPost(url);
				success = executeRequest(request, url, true);
				break;
			}
		}

		if (success)
			return response.trim();

		return "";
	}


	private boolean executeRequest(HttpUriRequest request, String url, boolean sizeRequest) {
		HttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse;

		try {
			httpResponse = client.execute(request);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();
			HttpEntity entity = httpResponse.getEntity();


			if (entity != null) {
				InputStream instream = entity.getContent();

				if (sizeRequest) {
					doc = Jsoup.parse(instream, "UTF-8", "");
					response = doc.text();  
				}
				else 
					pois = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.readValue(instream, new TypeReference<List<Poi>>(){});	

				instream.close();
			}
		}
		catch (Exception e) { 
			return false;
		}

		return true;
	}

	public int getResponseCode() {
		return responseCode;
	}


	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getResponse() {
		return response;
	}


	public void setResponse(String response) {
		this.response = response;
	}

	

}