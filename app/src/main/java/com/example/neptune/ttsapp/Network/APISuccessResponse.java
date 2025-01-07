package com.example.neptune.ttsapp.Network;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APISuccessResponse<T> extends APIResponse<T> {

    private final List<T> bodyContentAsAList;
    private final T bodyContentAsAnObject;
    private final String message;
    private final T body;
  //  private final Map<String, String> links;

    private static final Pattern LINK_PATTERN = Pattern.compile("<([^>]*)>\\s*;\\s*rel=\"([a-zA-Z0-9]+)\"");
    private static final Pattern PAGE_PATTERN = Pattern.compile("\\bpage=(\\d+)");
    private static final String NEXT_LINK = "next";

    public APISuccessResponse(List<T> bodyContentAsAList, String message) {
        this.bodyContentAsAList = bodyContentAsAList;
        this.bodyContentAsAnObject = null;
        this.message = message;
        this.body = null;

    }

    public APISuccessResponse(T bodyContentAsAnObject, String message) {
        this.bodyContentAsAList = null;
        this.bodyContentAsAnObject = bodyContentAsAnObject;
        this.message = message;
        this.body = null;
    }

    public APISuccessResponse(T body) {
        this.bodyContentAsAList = null;
        this.bodyContentAsAnObject = null;
        this.message = null;
        this.body = body;
    }
    //    public APISuccessResponse(T body, Map<String, String> links) {
//        this.body = body;
//        this.links = links;
//    }
//
    public ResponseBody getBody() {
        return (ResponseBody) body;
    }

//    public Map<String, String> getLinks() {
//        return links;
//    }

    public Integer getNextPage() {
        //String next = links.get(NEXT_LINK);
        String next = "1";
        if (next != null) {
            Matcher matcher = PAGE_PATTERN.matcher(next);
            if (matcher.find() && matcher.groupCount() == 1) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    System.out.println("Cannot parse next page from %s" + next);
                }
            }
        }
        return null;
    }

    private static Map<String, String> extractLinks(String linkHeader){
        Map<String, String> links = new HashMap<>();
        Matcher matcher = LINK_PATTERN.matcher(linkHeader);
        while (matcher.find()) {
            if (matcher.groupCount() == 2) links.put(matcher.group(2),matcher.group(1));
        }
        return links;
    }

    public List<T> getBodyContentAsAList() {
        return bodyContentAsAList;
    }

    public T getBodyContentAsAnObject() {
        return bodyContentAsAnObject;
    }

    public String getMessage() {
        return message;
    }
}