package com.example.tidalapplication;

import java.util.List;

public class TideResponse {
    private List<String> fields;
    private List<List<String>> data;

    public List<String> getFields() {
        return fields;
    }

    public List<List<String>> getData() {
        return data;
    }
}
