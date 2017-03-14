package com.evistek.gallery.net.json;

import com.evistek.gallery.model.Product;

import java.util.ArrayList;

public class JsonRespAds extends JsonRespBase {
    private ArrayList<Product> results;

    public ArrayList<Product> getResults() {
        return results;
    }

    public void setResults(ArrayList<Product> results) {
        this.results = results;
    }

}
