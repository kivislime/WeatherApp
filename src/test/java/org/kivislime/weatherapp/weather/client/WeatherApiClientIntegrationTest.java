package org.kivislime.weatherapp.weather.client;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weatherapp.config.WeatherApiTestConfig;
import org.kivislime.weatherapp.location.exception.LocationNotFoundException;
import org.kivislime.weatherapp.session.exception.QuotaExceededException;
import org.kivislime.weatherapp.weather.dto.GeocodingResponse;
import org.kivislime.weatherapp.weather.dto.WeatherResponse;
import org.kivislime.weatherapp.weather.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WeatherApiTestConfig.class)
@ActiveProfiles("test")
class WeatherApiClientIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IWeatherApiClient client;

    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void fetchCurrentWeatherByName_success() throws Exception {
        String city = "Berlin";
        String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "http://api.openweathermap.org/data/2.5/weather"
                + "?q=" + encoded
                + "&appid=DUMMY_KEY"
                + "&units=metric";

        String json = """
            {
              "coord": { "lon": 13.4050, "lat": 52.5200 },
              "weather": [ { "description":"sunny","icon":"01d" } ],
              "main": { "temp": 15.5, "feels_like": 14.0, "humidity": 42 }
            }
            """;

        server.expect(requestTo(url))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        WeatherResponse resp = client.fetchCurrentWeatherByName(city);

        assertThat(resp.getCoord().getLat()).isEqualTo(52.52);
        assertThat(resp.getCoord().getLon()).isEqualTo(13.4050);
        assertThat(resp.getMain().getTemp()).isEqualTo(15.5);
        assertThat(resp.getMain().getFeelsLike()).isEqualTo(14.0);
        assertThat(resp.getMain().getHumidity()).isEqualTo(42);
        assertThat(resp.getWeather()).hasSize(1)
                .first()
                .extracting("description", "icon")
                .containsExactly("sunny", "01d");

        server.verify();
    }

    @Test
    void fetchCurrentWeatherByCoordinates_notFound() {
        String url = "http://api.openweathermap.org/data/2.5/weather"
                + "?lat=0&lon=0"
                + "&appid=DUMMY_KEY"
                + "&units=metric";

        server.expect(requestTo(url))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"city not found\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCoordinates("0", "0"))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("city not found");

        server.verify();
    }

    @Test
    void fetchCurrentGeocodingByName_success() throws Exception {
        String city = "Paris";
        String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "http://api.openweathermap.org/geo/1.0/direct"
                + "?q=" + encoded
                + "&limit=5"
                + "&appid=DUMMY_KEY";

        String json = """
            [
              { "name":"Paris","lat":48.8566,"lon":2.3522 }
            ]
            """;

        server.expect(requestTo(url))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<GeocodingResponse> list = client.fetchCurrentGeocodingByName(city);

        assertThat(list).hasSize(1);
        GeocodingResponse geo = list.get(0);
        assertThat(geo.getName()).isEqualTo("Paris");
        assertThat(geo.getLat()).isEqualTo(48.8566);
        assertThat(geo.getLon()).isEqualTo(2.3522);

        server.verify();
    }

    @Test
    void fetchCurrentWeather_serverError() {
        String url = "http://api.openweathermap.org/data/2.5/weather"
                + "?lat=0&lon=0"
                + "&appid=DUMMY_KEY"
                + "&units=metric";

        server.expect(requestTo(url))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCoordinates("0", "0"))
                .isInstanceOf(ExternalApiException.class);

        server.verify();
    }

    @Test
    void fetchCurrentWeather_rateLimit() {
        String url = "http://api.openweathermap.org/data/2.5/weather"
                + "?lat=0&lon=0"
                + "&appid=DUMMY_KEY"
                + "&units=metric";

        server.expect(requestTo(url))
                .andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                        .body("{\"message\":\"quota exceeded\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCoordinates("0", "0"))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("quota exceeded");

        server.verify();
    }
}
