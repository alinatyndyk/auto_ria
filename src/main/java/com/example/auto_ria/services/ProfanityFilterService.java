package com.example.auto_ria.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ProfanityFilterService {

    public String containsProfanity(String text) throws IOException {

        String apiUrl = "https://www.purgomalum.com/service/json?text=" +
                URLEncoder.encode(text, StandardCharsets.UTF_8);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream responseStream = connection.getInputStream();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseStream);

        System.out.println(root.path("result").asText());

        return root.path("result").asText();
    }

    public boolean containsProfanityBoolean(String filtered, String plain) throws IOException {
        return !filtered.equals(plain);
    }

}
