package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.MeasurableListDataModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JSONConfig<T> {

    public T extractBodyFromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ResponseBody<T> responseBody = mapper.readValue(json, ResponseBody.class);
        return responseBody.getBody();
    }

    public <T> List<T> extractListFromBodyFromJson(String json, Class<T> clazz)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
//        ResponseBody<List<T>> responseBody = mapper.readValue(json, ResponseBody.class);
//        return responseBody.getBody();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode bodyNode = rootNode.path("body");

        return mapper.readerForListOf(clazz).readValue(bodyNode); }
}
