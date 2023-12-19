package com.example.auto_ria.services.otherApi;

import com.example.auto_ria.exceptions.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class ProfanityFilterService {

    private Environment environment;

    public String containsProfanity(String text) {
        try {
            String apiUrl = environment.getProperty("profanity.filter.api") +
                    URLEncoder.encode(text, StandardCharsets.UTF_8);

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream responseStream = connection.getInputStream();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseStream);

            return root.path("result").asText();
        } catch (Exception e) {
            throw new CustomException("Error while filtering profanity: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public boolean containsProfanityBoolean(String filtered, String plain) {
        return !filtered.equals(plain);
    }

}
