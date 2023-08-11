package com.example.auto_ria.services;

import com.google.gson.Gson;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bson.json.JsonObject;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class MaxPanelService {

    @SneakyThrows
    public void view(String car_id) {

        MessageBuilder messageBuilder = new MessageBuilder("d70a01daa5e8be7718fd9b731b77bf1c");

        JSONObject props = new JSONObject();
        props.put("car_id", car_id);
        JSONObject sentEvent =
                messageBuilder.event("99", "carView", props);


        ClientDelivery delivery = new ClientDelivery();
        delivery.addMessage(sentEvent);

        MixpanelAPI mixpanel = new MixpanelAPI();
        mixpanel.deliver(delivery);

    }

    @SneakyThrows
    public void getViews () {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://data-eu.mixpanel.com/api/2.0/export?from_date=2023-08-10&to_date=2023-08-11"))
                .header("accept", "text/plain")
                .header("authorization", "Basic ODUxNmM1ZTUxZWRhZTQxZWY3OTUzYzhiMjJlNzllYTY6YWxpbmFhbm5hMzQw")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
