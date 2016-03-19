package com.example.valdio.mapboxintegration;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.DirectionsResponse;
import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    private MapView mapView = null;
    private String MAPBOX_ACCESS_TOKEN = "";

    private DirectionsRoute currentRoute = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MAPBOX_ACCESS_TOKEN = getResources().getString(R.string.accessToken);

        // Set up a standard MapBox map
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setAccessToken(MAPBOX_ACCESS_TOKEN);
        mapView.setStyleUrl(Style.MAPBOX_STREETS); // specify the map style
        mapView.setZoom(14); // zoom level
        mapView.onCreate(savedInstanceState);


        myLocation();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(41.327752, 19.818666)) // Sets the center of the map to the specified location
                        .zoom(13)                            // Sets the zoom level
                        .build();

                //set the user's viewpoint as specified in the cameraPosition object
                mapView.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                //Add a marker to the map in the specified location
                mapView.addMarker(new MarkerOptions()
                        .position(new LatLng(41.327752, 19.818666))
                        .title("MapBox Marker!")
                        .snippet("Welcome to my marker."));

            }
        });


        mapView.setOnMapLongClickListener(new MapView.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {

                //Remove previously added markers
                //Marker is an annotation that shows an icon image at a geographical location
                //so all markers can be removed with the removeAllAnnotations() method.
                mapView.removeAllAnnotations();

                // Set the origin waypoint to the devices location
                Waypoint origin = new Waypoint(mapView.getMyLocation().getLongitude(), mapView.getMyLocation().getLatitude());

                // Set the destination waypoint to the location point long clicked by the user
                Waypoint destination = new Waypoint(point.getLongitude(), point.getLatitude());

                // Add marker to the destination waypoint
                mapView.addMarker(new MarkerOptions()
                        .position(new LatLng(point))
                        .title("Destination Marker")
                        .snippet("My destination"));

                // Get route from API
                getRoute(origin, destination);

            }
        });

    }

    private void myLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mapView.setMyLocationEnabled(true);
        mapView.setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
        mapView.getMyLocation();
    }

    private void getRoute(Waypoint origin, Waypoint destination) {
        MapboxDirections directions = new MapboxDirections.Builder()
                .setAccessToken(MAPBOX_ACCESS_TOKEN)
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_WALKING)
                .build();

        directions.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Response<DirectionsResponse> response, Retrofit retrofit) {

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                showToastMessage(String.format("You are %d meters \nfrom your destination", currentRoute.getDistance()));

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Throwable t) {
                showToastMessage("Error: " + t.getMessage());
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert List<Waypoint> into LatLng[]
        List<Waypoint> waypoints = route.getGeometry().getWaypoints();
        LatLng[] point = new LatLng[waypoints.size()];
        for (int i = 0; i < waypoints.size(); i++) {
            point[i] = new LatLng(
                    waypoints.get(i).getLatitude(),
                    waypoints.get(i).getLongitude());
        }

        // Draw Points on MapView
        mapView.addPolyline(new PolylineOptions()
                .add(point)
                .color(Color.parseColor("#38afea"))
                .width(5));
    }

    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
