package com.example.auto_ria.configurations.InitialDataLoader.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.Proxy;

public class ProxyTypeAdapter extends TypeAdapter<Proxy> {

    @Override
    public void write(JsonWriter out, Proxy value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("type").value(value.type().name()); // Serialize the type of the proxy
        // You can add more fields if necessary, like address, port, etc.
        out.endObject();
    }

    @Override
    public Proxy read(JsonReader in) throws IOException {
        // Implement this method if you need to deserialize Proxy objects
        return null; // Replace with actual implementation
    }
}
