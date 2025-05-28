package org.kivislime.weather.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherResponse {
    private Coordinate coord;
    private List<Weather> weather;
    private String base;
    private MainInfo main;
    private Integer visibility;
    private Wind wind;
    private Rain rain;
    private Cloud clouds;
    private Long dt;
    private Sys sys;
    private Long timezone;
    private Long id;
    private String name;
    private Integer cod;

    @Getter
    @Setter
    public static class Coordinate {
        private Double lon;
        private Double lat;
    }

    @Getter
    @Setter
    public static class Weather {
        private Long id;
        private String main;
        private String description;
        private String icon;
    }

    @Getter
    @Setter
    public static class MainInfo {
        private Double temp;
        @JsonProperty("feels_like")
        private Double feelsLike;
        @JsonProperty("temp_min")
        private Double tempMin;
        @JsonProperty("temp_max")
        private Double tempMax;
        private Integer pressure;
        private Integer humidity;
        @JsonProperty("sea_level")
        private Integer seaLevel;
        @JsonProperty("grnd_level")
        private Integer grndLevel;
    }

    @Getter
    @Setter
    public static class Wind {
        private Double speed;
        private Integer deg;
        private Double gust;
    }

    @Getter
    @Setter
    public static class Rain {
        @JsonProperty("1h")
        private Double oneHour;
    }

    @Getter
    @Setter
    public static class Cloud {
        private Integer all;
    }

    @Getter
    @Setter
    public static class Sys {
        private Integer type;
        private Integer id;
        private String country;
        private Long sunrise;
        private Long sunset;
    }
}
