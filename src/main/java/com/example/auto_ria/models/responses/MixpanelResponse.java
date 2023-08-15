package com.example.auto_ria.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MixpanelResponse {
    @JsonProperty("event")
    private String eventName;

    @JsonProperty("properties")
    private Properties properties;

    public String getEventName() {
        return eventName;
    }

    public Properties getProperties() {
        return properties;
    }

    public static class Properties {
        @JsonProperty("time")
        private long time;
        @JsonProperty("distinct_id")
        private String distinctId;
        @JsonProperty("$user_id")
        private String userId;
        @JsonProperty("$insert_id")
        private String insertId;
        @JsonProperty("car_id")
        private String carId;

        @JsonProperty("$mp_api_endpoint")
        private String $mp_api_endpoint;
        @JsonProperty("mp_processing_time_ms")
        private long mp_processing_time_ms;
        @JsonProperty("$mp_api_timestamp_ms")
        private long $mp_api_timestamp_ms;
        @JsonProperty("mp_lib")
        private String mp_lib;


        public long getTime() {
            return time;
        }

        public String getUserId() {
            return userId;
        }

        public String getCarId() {
            return carId;
        }
    }

    public static MixpanelResponse fromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, MixpanelResponse.class);
    }
}
