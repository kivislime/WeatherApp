package org.kivislime.weatherapp.weather.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kivislime.weatherapp.weather.exception.ExternalApiException;
import org.kivislime.weatherapp.location.exception.LocationNotFoundException;
import org.kivislime.weatherapp.session.exception.QuotaExceededException;
import org.kivislime.weatherapp.weather.dto.GeocodingResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherApiClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    private WeatherApiClientImpl weatherApiClientImpl;

    private static final String BASE_URL      = "https://api.openweathermap.org/data/2.5/weather";
    private static final String GEOCODING_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String API_KEY       = "dummyApiKey";
    private static final int    MAX_CITIES    = 5;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        weatherApiClientImpl = new WeatherApiClientImpl(
                restTemplate,
                objectMapper,
                BASE_URL,
                GEOCODING_URL,
                API_KEY,
                MAX_CITIES
        );
    }


    @Test
    void fetchCurrentWeatherByCoordinates_SuccessfulResponse_ReturnsWeatherResponse() {
        String fakeJson = "{\"main\":{\"temp\":10.5,\"feels_like\":9.0,\"humidity\":80},"
                + "\"weather\":[{\"description\":\"clear sky\",\"icon\":\"01d\"}]}";

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenReturn(ResponseEntity.ok(fakeJson));

        var result = weatherApiClientImpl.fetchCurrentWeatherByCoordinates("55.75", "37.62");

        assertThat(result).isNotNull();
        assertThat(result.getMain().getTemp()).isEqualTo(10.5);
        assertThat(result.getWeather().get(0).getDescription()).isEqualTo("clear sky");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(
                uriCaptor.capture(),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class)
        );

        String used = uriCaptor.getValue().toString();
        assertThat(used)
                .contains("lat=55.75")
                .contains("lon=37.62")
                .contains("appid=" + API_KEY)
                .contains("units=metric");
    }

    @Test
    void fetchCurrentWeatherByName_SuccessfulResponse_ReturnsWeatherResponse() {
        String fakeJson = "{\"main\":{\"temp\":20.0,\"feels_like\":18.5,\"humidity\":50},"
                + "\"weather\":[{\"description\":\"rain\",\"icon\":\"09d\"}]}";

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenReturn(ResponseEntity.ok(fakeJson));

        var result = weatherApiClientImpl.fetchCurrentWeatherByName("Moscow");

        assertThat(result.getMain().getTemp()).isEqualTo(20.0);
        assertThat(result.getWeather().get(0).getDescription()).isEqualTo("rain");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(
                uriCaptor.capture(),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class)
        );

        String used = uriCaptor.getValue().toString();
        assertThat(used)
                .contains("q=Moscow")
                .contains("appid=" + API_KEY)
                .contains("units=metric");
    }

    @Test
    void fetchWeatherResponse_404_ShouldThrowLocationNotFoundException() {
        HttpClientErrorException notFoundEx = new HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "{\"message\":\"city not found\"}".getBytes(),
                null
        );

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenThrow(notFoundEx);

        LocationNotFoundException ex = assertThrows(
                LocationNotFoundException.class,
                () -> weatherApiClientImpl.fetchCurrentWeatherByName("Atlantis")
        );

        assertThat(ex.getMessage()).isEqualTo("city not found");
    }

    @Test
    void fetchWeatherResponse_401_ShouldThrowExternalApiException() {
        HttpClientErrorException unauthorizedEx = new HttpClientErrorException(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "{\"message\":\"invalid api key\"}".getBytes(),
                null
        );

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenThrow(unauthorizedEx);

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> weatherApiClientImpl.fetchCurrentWeatherByCoordinates("0", "0")
        );

        assertThat(ex.getMessage()).isEqualTo("invalid api key");
    }

    @Test
    void fetchWeatherResponse_429_ShouldThrowQuotaExceededException() {
        HttpClientErrorException tooManyEx = new HttpClientErrorException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                "{\"message\":\"quota exceeded\"}".getBytes(),
                null
        );

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenThrow(tooManyEx);

        QuotaExceededException ex = assertThrows(
                QuotaExceededException.class,
                () -> weatherApiClientImpl.fetchCurrentWeatherByName("Paris")
        );

        assertThat(ex.getMessage()).isEqualTo("quota exceeded");
    }

    @Test
    void fetchWeatherResponse_500_ShouldThrowExternalApiExceptionWithStatusAndBody() {
        String errJson = "{\"message\":\"server error\"}";
        HttpClientErrorException serverErrorEx = HttpClientErrorException
                .create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null,
                        errJson.getBytes(), null);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenThrow(serverErrorEx);

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> weatherApiClientImpl.fetchCurrentWeatherByCoordinates("10", "10")
        );

        assertThat(ex.getMessage()).isEqualTo("500 INTERNAL_SERVER_ERROR: server error");
    }

    @Test
    void fetchWeatherResponse_InvalidJson_ShouldThrowExternalApiException() {
        String invalidJson = "not a json";

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenReturn(ResponseEntity.ok(invalidJson));

        ExternalApiException ex = assertThrows(
                ExternalApiException.class,
                () -> weatherApiClientImpl.fetchCurrentWeatherByName("Berlin")
        );

        assertThat(ex.getMessage())
                .isEqualTo("It is impossible to parse the response of the weather service");
    }

    @Test
    void fetchCurrentGeocodingByName_SuccessfulResponse_ReturnsList() {
        String city = "London";

        GeocodingResponse gr1 = new GeocodingResponse();
        gr1.setName("London"); gr1.setLat(51.51); gr1.setLon(-0.13);

        GeocodingResponse gr2 = new GeocodingResponse();
        gr2.setName("London, UK"); gr2.setLat(51.50); gr2.setLon(-0.12);

        GeocodingResponse[] array = new GeocodingResponse[]{gr1, gr2};

        when(restTemplate.getForObject(any(URI.class), eq(GeocodingResponse[].class)))
                .thenReturn(array);

        List<GeocodingResponse> result = weatherApiClientImpl.fetchCurrentGeocodingByName(city);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("London");
        assertThat(result.get(1).getLat()).isEqualTo(51.50);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate, times(1))
                .getForObject(uriCaptor.capture(), eq(GeocodingResponse[].class));

        String used = uriCaptor.getValue().toString();
        assertThat(used).contains("q=" + city);
        assertThat(used).contains("limit=" + MAX_CITIES);
        assertThat(used).contains("appid=" + API_KEY);
    }

    @Test
    void fetchCurrentGeocodingByName_NullResponse_ReturnsEmptyList() {
        when(restTemplate.getForObject(any(URI.class), eq(GeocodingResponse[].class)))
                .thenReturn(null);

        List<GeocodingResponse> result = weatherApiClientImpl.fetchCurrentGeocodingByName("UnknownCity");

        assertThat(result).isEmpty();
    }
}
