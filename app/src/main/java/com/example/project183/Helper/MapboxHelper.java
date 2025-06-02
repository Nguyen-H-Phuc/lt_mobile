package com.example.project183.Helper;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Callback;

import java.util.Locale;

public class MapboxHelper {

    public interface DistanceCallback {
        void onSuccess(double distanceInKm);
        void onFailure(String errorMessage);
    }

    public static void calculateDistance(Context context, Point origin, Point destination, DistanceCallback callback) {
        String accessToken = "pk.eyJ1IjoicGh1YzA1MTEyMDA0IiwiYSI6ImNtYmVpbjQybzFkMHEycG9jbTRvaDA0dXIifQ.KLFmE6u82cl6wpU8Vxcl9Q";
        String url = String.format(Locale.US,
                "https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?access_token=%s",
                origin.longitude(), origin.latitude(),
                destination.longitude(), destination.latitude(),
                accessToken);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            double distanceMeters = route.getDouble("distance");
                            double distanceKm = distanceMeters / 1000.0;
                            callback.onSuccess(distanceKm);
                        } else {
                            callback.onFailure("No route found.");
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                },
                error -> callback.onFailure("API error: " + error.toString())
        );

        queue.add(jsonRequest);
    }

    public static void geocodeAddress(Context context, String address, Callback<GeocodingResponse> callback) {
        MapboxGeocoding geocoding = MapboxGeocoding.builder()
                .accessToken("pk.eyJ1IjoicGh1YzA1MTEyMDA0IiwiYSI6ImNtYmVpbjQybzFkMHEycG9jbTRvaDA0dXIifQ.KLFmE6u82cl6wpU8Vxcl9Q")
                .query(address)
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();

        geocoding.enqueueCall(callback);
    }

}
