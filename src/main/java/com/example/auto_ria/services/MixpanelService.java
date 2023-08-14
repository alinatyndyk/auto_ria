package com.example.auto_ria.services;

import com.example.auto_ria.models.responses.MixpanelResponse;
import com.example.auto_ria.models.responses.StatisticsResponse;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

@Service
@AllArgsConstructor
public class MixpanelService {

    @SneakyThrows
    public void view(String car_id) {

        MessageBuilder messageBuilder = new MessageBuilder("d70a01daa5e8be7718fd9b731b77bf1c");

        JSONObject props = new JSONObject();
        props.put("car_id", car_id);
        JSONObject sentEvent =
                messageBuilder.event(new Date().toString(), "carView", props);


        ClientDelivery delivery = new ClientDelivery();
        delivery.addMessage(sentEvent);

        MixpanelAPI mixpanel = new MixpanelAPI();
        mixpanel.deliver(delivery);

    }

    @SneakyThrows
    public StatisticsResponse getCarViewsStatistics(String carId) {

        LocalDate day = LocalDate.now();
        LocalDate week = LocalDate.now().minusDays(7);
        LocalDate month = LocalDate.now().minusDays(30);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String dayFormed = day.format(formatter);
        String weekFormed = week.format(formatter);
        String monthFormed = month.format(formatter);

        return StatisticsResponse.builder()
                .viewsDay(extractCarViews(dayFormed, dayFormed, carId))
                .viewsWeek(extractCarViews(weekFormed, dayFormed, carId))
                .viewsMonth(extractCarViews(monthFormed, dayFormed, carId))
                .viewsAll(extractCarViews("2011-07-10", dayFormed, carId))
                .build();

    }

    public int extractCarViews(String from_date, String to_date, String carId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://data-eu.mixpanel.com/api/2.0/export?from_date=" + from_date + "&to_date=" + to_date))
                .header("accept", "text/plain")
                .header("authorization", "Basic ODUxNmM1ZTUxZWRhZTQxZWY3OTUzYzhiMjJlNzllYTY6YWxpbmFhbm5hMzQw")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        String responseBody = response.body();

//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
//        headers.setBasicAuth("ODUxNmM1ZTUxZWRhZTQxZWY3OTUzYzhiMjJlNzllYTY6YWxpbmFhbm5hMzQw");
//        HttpEntity<String> entity = new HttpEntity<String>(headers);
//        String url = "https://data-eu.mixpanel.com/api/2.0/export?from_date=" + from_date + "&to_date=" + to_date;
//        MixpanelResponse[] events = restTemplate.exchange(url, HttpMethod.GET, entity, MixpanelResponse[].class).getBody();
//
//        assert events != null;
//        for (MixpanelResponse mixpanelResponse : events) {
//            System.out.println(mixpanelResponse); //todo diff approach
//            System.out.println("mixel reaponse");
//        }

        String string = String.format("\"car_id\":\"%s\"", carId);

        String[] substrings = responseBody.split(string);
        return substrings.length - 1;
    }

}
