package com.example.auto_ria.models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MixpanelResponse {
    @JsonProperty("event")
    private String event;

    @JsonProperty("properties")
    private MixpanelProperties properties;

    public String getEvent() {
        return event;
    }

    public MixpanelProperties getProperties() {
        return properties;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class MixpanelProperties {
    @JsonProperty("time")
    private long time;

    @JsonProperty("distinct_id")
    private String distinctId;

    @JsonProperty("$insert_id")
    private String insertId;

    @JsonProperty("$mp_api_endpoint")
    private String apiEndpoint;

    @JsonProperty("$mp_api_timestamp_ms")
    private long apiTimestampMs;

    @JsonProperty("$user_id")
    private String userId;

    @JsonProperty("car_id")
    private String carId;

    @JsonProperty("mp_lib")
    private String lib;

    @JsonProperty("mp_processing_time_ms")
    private long processingTimeMs;

    public long getTime() {
        return time;
    }

    public String getDistinctId() {
        return distinctId;
    }

    public String getInsertId() {
        return insertId;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public long getApiTimestampMs() {
        return apiTimestampMs;
    }

    public String getUserId() {
        return userId;
    }

    public String getCarId() {
        return carId;
    }

    public String getLib() {
        return lib;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
}

//[
//  {"event": "Signup", "properties": {"time": 1618716477000,"distinct_id": "91304156-cafc-4673-a237-623d1129c801","$insert_id": "29fc2962-6d9c-455d-95ad-95b84f09b9e4","Referred by": "Friend","URL": "mixpanel.com/signup"}},
//  {"event": "Purchase", "properties": {"time": 1618716477000,"distinct_id": "91304156-cafc-4673-a237-623d1129c801","$insert_id": "935d87b1-00cd-41b7-be34-b9d98dd08b42","Item": "Coffee", "Amount": 5.0}}
//]
