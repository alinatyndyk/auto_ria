package com.example.auto_ria.services;

import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.responses.StatisticsResponse;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@AllArgsConstructor
public class MixpanelService {

    private Environment environment;

    public void view(String car_id) {
        try {

            MessageBuilder messageBuilder = new MessageBuilder(environment.getProperty("maxpanel.project.id"));

            JSONObject props = new JSONObject();
            props.put("car_id", car_id);
            JSONObject sentEvent =
                    messageBuilder.event(new Date().toString(), "carView", props);


            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(sentEvent);

            MixpanelAPI mixpanel = new MixpanelAPI();
            mixpanel.deliver(delivery);
        } catch (Exception e) {
            throw new CustomException("Error while sending viewCar event: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

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
                .uri(URI.create(environment.getProperty("maxpanel.export.url") +
                        "?from_date=" + from_date + "&to_date=" + to_date))
                .header("accept", "text/plain")
                .header("authorization", environment.getProperty("maxpanel.basic.auth"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        System.out.println(responseBody);
        System.out.println("responseBody");

        String string = String.format("\"car_id\":\"%s\"", carId);

        String[] substrings = responseBody.split(string);
        return substrings.length - 1;
    }

}
