package com.example.neptune.ttsapp.Network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JSONConfig {

    public JsonNode extractBodyFromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ResponseBody responseBody = mapper.readValue(json, ResponseBody.class);
        return responseBody.getBody();
    }

    public <T> List<T> extractListFromBodyFromJson(String json, Class<T> clazz)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
//        ResponseBody<List<T>> responseBody = mapper.readValue(json, ResponseBody.class);
//        return responseBody.getBody();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode bodyNode = rootNode.path("body");

        return mapper.readerForListOf(clazz).readValue(bodyNode);
    }

    public String extractMessageFromBodyFromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ResponseBody responseBody = mapper.readValue(json,ResponseBody.class);
        return responseBody.getMessage();

    }
}
