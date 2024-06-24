package com.example.auto_ria.configurations.InitialDataLoader.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class Main {

    public static void main(String[] args) {
        // Step 1: Create a Gson instance with the custom TypeAdapter
        Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Proxy.class, new ProxyTypeAdapter())
                        .create();

        // Step 2: Obtain a Proxy object (example: using proxy settings)
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

        // Step 3: Serialize Proxy object to JSON
        String json = gson.toJson(proxy);

        // Step 4: Deserialize JSON back to Proxy object (example)
        Proxy deserializedProxy = gson.fromJson(json, Proxy.class);
    }
}