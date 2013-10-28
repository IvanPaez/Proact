package org.jproact.weather;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class NCDCRetriever {

	private static Logger log = Logger.getLogger(NCDCRetriever.class);

	public InputStream retrieve(String city, String code) throws Exception {
		log.info( "Retrieving NCDC Weather Data" );
		String url = "http://weather.yahooapis.com/forecastrss?p=" + zipcode;
		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}

}
