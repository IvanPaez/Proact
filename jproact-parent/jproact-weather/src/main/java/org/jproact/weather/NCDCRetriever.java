package org.jproact.weather;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class NCDCRetriever {

	private static Logger log = Logger.getLogger(NCDCRetriever.class);

	public InputStream retrieve(String datasetId, String stationId, String startDate, String endDate) throws Exception {
		log.info( "Retrieving NCDC Weather Data" );
		//String url = "http://weather.yahooapis.com/forecastrss?p=" + city;
        //datasetID = ANNUAL, GHCND, GHCNDMS, NEXRAD2, NEXRAD3, NORMAL_ANN, NORMAL_DLY, NORMAL_HLY, NORMAL_MLY, PRECIP_15, PRECIP_HLY
        //stationId=GHCND:USW00094075
        //startDate=2010-01-01
        //endDate=2010-01-31
        String url = "http://www.ncdc.noaa.gov/cdo-web/api/v2/data?datasetid="+datasetId
                +"&stationid="+stationId
                +"&startdate="+startDate
                +"&enddate="+endDate+"&limit=1000\n";

		URLConnection conn = new URL(url).openConnection();
		return conn.getInputStream();
	}

}
