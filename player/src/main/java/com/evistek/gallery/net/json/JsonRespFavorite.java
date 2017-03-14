package com.evistek.gallery.net.json;

import com.evistek.gallery.model.Favorite;

import java.util.List;


public class JsonRespFavorite extends JsonRespBase {

    private List<Favorite> results;

    public List<Favorite> getResults() {
        return results;
    }

    public void setResults(List<Favorite> results) {
        this.results = results;
    }

}
