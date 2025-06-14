package org.kivislime.weather.client;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class StubWeatherApiClient implements IWeatherApiClient {
    @Override
    public List<GeocodingResponse> fetchCurrentGeocodingByName(String name) {
        GeocodingResponse response = new GeocodingResponse();
        response.setName(name + "-stub");
        response.setLat(10.0);
        response.setLon(20.0);
        return List.of(response);
    }

    @Override
    public WeatherResponse fetchCurrentWeatherByName(String name) {
        WeatherResponse.MainInfo main = new WeatherResponse.MainInfo();
        main.setTemp(25.0);
        main.setFeelsLike(24.0);
        main.setHumidity(50);
        WeatherResponse.Weather w = new WeatherResponse.Weather();
        w.setDescription("clear");
        w.setIcon("01d");
        WeatherResponse resp = new WeatherResponse();
        WeatherResponse.Coordinate coord = new WeatherResponse.Coordinate();
        coord.setLat(10.0);
        coord.setLon(20.0);
        resp.setCoord(coord);
        resp.setMain(main);
        resp.setWeather(List.of(w));
        return resp;
    }

    @Override
    public WeatherResponse fetchCurrentWeatherByCoordinates(String lat, String lon) {
        return fetchCurrentWeatherByName(lat + "," + lon);
    }
}
