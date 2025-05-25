    package org.kivislime.weather;

    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import com.fasterxml.jackson.annotation.JsonProperty;
    import lombok.Getter;
    import lombok.Setter;

    @Setter
    @Getter
    public class GeocodingResponse {
        private String name;
        private Double lat;
        private Double lon;

        private String country;
        private String state;
    }
