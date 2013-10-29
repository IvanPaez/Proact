package org.jproact.weather;

import org.jproact.model.Weather;

import java.io.InputStream;

public class WeatherService {

	private YahooRetriever yahooRetriever;
	private YahooParser yahooParser;

    private NCDCRetriever ncdcRetriever;
    private NCDCParser ncdcParser;



    public WeatherService() {
	}

	public Weather retrieveForecast(String zip) throws Exception {
		// Retrieve Data
		InputStream dataIn = yahooRetriever.retrieve(zip);

		// Parse DataS
		Weather weather = yahooParser.parse(zip, dataIn);

		return weather;
	}


    public Weather retrieveForecast(String datasetId, String stationId, String startDate, String endDate)  throws Exception {
        //Retrieve Data
        InputStream dataIn = ncdcRetriever.retrieve(datasetId, stationId, startDate, endDate);

        // Parse DataS
        Weather weather = ncdcParser.parse(stationId, dataIn);

        return weather;

    }


	public YahooRetriever getYahooRetriever() {
		return yahooRetriever;
	}

	public void setYahooRetriever(YahooRetriever yahooRetriever) {
		this.yahooRetriever = yahooRetriever;
	}

	public YahooParser getYahooParser() {
		return yahooParser;
	}

	public void setYahooParser(YahooParser yahooParser) {
		this.yahooParser = yahooParser;
	}

    public NCDCRetriever getNcdcRetriever() {
        return ncdcRetriever;
    }

    public void setNcdcRetriever(NCDCRetriever ncdcRetriever) {
        this.ncdcRetriever = ncdcRetriever;
    }

    public NCDCParser getNcdcParser() {
        return ncdcParser;
    }

    public void setNcdcParser(NCDCParser ncdcParser) {
        this.ncdcParser = ncdcParser;
    }

}
