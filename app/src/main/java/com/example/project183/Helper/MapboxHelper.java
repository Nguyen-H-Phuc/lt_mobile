package com.example.project183.Helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.project183.R;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;

public class MapboxHelper {

    public interface RouteResultCallback {
        void onRouteFound(int durationInMinutes, int distanceInKm);
        void onError(String message);
    }

    //Mã hoá địa chỉ thành toạ độ trên bảng đồ
    public static void geocodeAddress(Context context, String address, Callback<GeocodingResponse> callback) {
        MapboxGeocoding geocoding = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.mapbox_access_token))
                .query(address)
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();
        geocoding.enqueueCall(callback);
    }

    // tính lấy khoảng cách và thời gian đi lại giữa 2 điểm trong mapbox
    public static void getRoute(Context context,Point destination, RouteResultCallback callback) {
        Point origin = Point.fromLngLat(106.58166, 10.90045); // Tọa độ điểm đi

        MapboxDirections client = MapboxDirections.builder()
                .accessToken(context.getString(R.string.mapbox_access_token))
                .routeOptions(
                        RouteOptions.builder()
                                .coordinatesList(Arrays.asList(origin, destination))
                                .profile(DirectionsCriteria.PROFILE_DRIVING)
                                .overview(DirectionsCriteria.OVERVIEW_FULL)
                                .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
                                .build()
                )
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                if (response.body() != null && !response.body().routes().isEmpty()) {
                    DirectionsRoute route = response.body().routes().get(0);
                    int durationInMinutes = (int) (route.duration() / 60); // thời gian tính bằng giây
                    int distanceInKilomet = (int) (route.distance() / 1000);// khoảng cách tính bằng km
                    callback.onRouteFound(durationInMinutes, distanceInKilomet);
                } else {
                    System.out.println("Không tìm thấy route.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }

        });
    }

}
