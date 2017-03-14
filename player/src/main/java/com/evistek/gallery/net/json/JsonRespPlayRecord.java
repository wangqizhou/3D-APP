package com.evistek.gallery.net.json;

import com.evistek.gallery.model.PlayRecord;

import java.util.List;


public class JsonRespPlayRecord extends JsonRespBase {

    private List<PlayRecord> results;

    public List<PlayRecord> getResults() {
        return results;
    }

    public void setResults(List<PlayRecord> results) {
        this.results = results;
    }

}
