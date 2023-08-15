package com.example.auto_ria.services;

import com.example.auto_ria.models.responses.MixpanelResponse;
import com.example.auto_ria.models.responses.StatisticsResponse;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

        LocalDate day = LocalDate.now().minusDays(0);
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
        String encodedToDate = URLEncoder.encode(to_date, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://data-eu.mixpanel.com/api/2.0/export?from_date=" + from_date + "&to_date=" + encodedToDate))
                .header("accept", "text/plain")
                .header("authorization", "Basic ODUxNmM1ZTUxZWRhZTQxZWY3OTUzYzhiMjJlNzllYTY6YWxpbmFhbm5hMzQw")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        List<MixpanelResponse> events = Collections.singletonList(MixpanelResponse.fromJson(responseBody));
//        int count = 0;
//        System.out.println(count);
        System.out.println("count");
        for (MixpanelResponse event : events) {
            System.out.println(event.getEventName());
//            if(event.getEventName().equals("carView") && event.getProperties().getCarId().equals(carId)) {
//                count++;
//            }

        }

        return 3;
    }

}
