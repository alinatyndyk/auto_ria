package com.example.auto_ria.services;

import com.example.auto_ria.dto.CarDTORequest;
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
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CitiesService {

    private Environment environment;

    public void isValidUkrainianCity(CarDTORequest carDTO) {
        try {
            int limit = 10;
            int offset = 0;
            String cityPrefix = carDTO.getRegion();
            String cityPrefixUrl = cityPrefix.replaceAll(" ", "%20");

            JsonNode region = null;

            while (region == null) {
                String findRegion =
                        "http://geodb-free-service.wirefreethought.com" +
                                "/v1/geo/countries/UA/regions?limit=" + limit +
                                "&offset=" + offset + "&namePrefix=" + cityPrefixUrl;

                URL urlRegion = new URL(findRegion);
                HttpURLConnection connectionRegion = (HttpURLConnection) urlRegion.openConnection();
                connectionRegion.setRequestProperty("accept", "application/json");

                InputStream responseStream = connectionRegion.getInputStream();

                ObjectMapper mapperRegion = new ObjectMapper();
                JsonNode rootRegion = mapperRegion.readTree(responseStream);

                JsonNode dataNode = rootRegion.get("data");

                if (dataNode.isArray() && dataNode.size() > 0) {
                    for (JsonNode objNode : dataNode) {
                        String name = objNode.get("name").asText();
                        if (name.equals(cityPrefix)) {
                            region = objNode;
                            break;
                        }
                    }
                }


                if (region == null) {

                    offset += limit;

                    if (offset >= rootRegion.get("metadata").get("totalCount").asInt()) {
                        throw new CustomException("Make sure to write the region name properly", HttpStatus.BAD_REQUEST);
                    }
                }
            }

            int regionCode = region.get("isoCode").asInt();

            String findCity = "http://geodb-free-service.wirefreethought.com" +
                    "/v1/geo/countries/UA/" +
                    "regions/" + regionCode + "/places?" +
                    "limit=10&offset=0";

            URL urlCity = new URL(findCity);
            HttpURLConnection connectionCity = (HttpURLConnection) urlCity.openConnection();
            connectionCity.setRequestProperty("accept", "application/json");

            InputStream responseStreamCity = connectionCity.getInputStream();

            ObjectMapper mapperCity = new ObjectMapper();
            JsonNode rootCity = mapperCity.readTree(responseStreamCity);


            JsonNode cityDataNode = rootCity.get("data");

            if (cityDataNode.isArray() && cityDataNode.size() > 0) {
                for (JsonNode objNode : cityDataNode) {
                    String name = objNode.get("name").asText();
                    if (name.equals(carDTO.getCity())) {
                        break;
                    }
                }
            }

            if (cityDataNode.isEmpty()) {
                List<String> validCities = new ArrayList<>();
                for (JsonNode objNode : cityDataNode) {
                    validCities.add(objNode.get("name").asText());
                }
                throw new CustomException("Invalid city for provided region: " +
                        region.get("name") + " Valid options: " + validCities, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

}
