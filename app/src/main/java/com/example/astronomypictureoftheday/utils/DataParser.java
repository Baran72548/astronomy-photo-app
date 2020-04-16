package com.example.astronomypictureoftheday.utils;

import com.example.astronomypictureoftheday.data.AstronomyData;

import org.json.JSONException;
import org.json.JSONObject;

public class DataParser {

    private static final String DATE = "date";
    private static final String TITLE = "title";
    private static final String EXPLANATION = "explanation";
    private static final String URL = "url";
    private static final String HD_URL = "hdurl";
    private static final String MEDIA_TYPE = "media_type";

    public static AstronomyData getAstronomyInfoFromJson(JSONObject jsonObject) throws JSONException {
        AstronomyData astronomyData = new AstronomyData();

        astronomyData.setDate(jsonObject.getString(DATE));
        astronomyData.setTitle(jsonObject.getString(TITLE));
        astronomyData.setDescription(jsonObject.getString(EXPLANATION));
        astronomyData.setUrl(jsonObject.getString(URL));
        astronomyData.setMediaType(jsonObject.getString(MEDIA_TYPE));

        if (astronomyData.getMediaType().equals("image")) {
            astronomyData.setHdUrl(jsonObject.getString(HD_URL));
        }

        return astronomyData;
    }
}
