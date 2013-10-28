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


    public Weather retrieveForecast(String city, String code){
        //Retrieve Data
        InputStream dataIn = ncdcRetriever.retrieve(city, code);

        // Parse DataS
        Weather weather = ncdcParser.parse(city, dataIn);

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

}
