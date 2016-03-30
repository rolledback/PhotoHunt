package mrayer.photohunt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SetChangeLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker m;
    private LatLng currentPos;
    private LatLng originalLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_change_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.set_location_toolbar);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Set Photo Location");

        // Obtain the current location IF there is one from the Bundle
        if(!(getIntent().getParcelableExtra("bundle") == null)) {
            Bundle bundle = getIntent().getParcelableExtra("bundle");
            LatLng defaultPos = bundle.getParcelable("location");
            currentPos = defaultPos;
            originalLocation = defaultPos;
        }
        else {
            currentPos = new LatLng(-1, -1);
        }

        // If the button is clicked, start an intent that sends the location back to the Create New Photo Hunt Activity
        Button b = (Button) findViewById(R.id.setLocation);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(currentPos.equals(originalLocation)) {
                    // if user did not change the location, we can simply treat this as a cancel
                    setResult(Activity.RESULT_CANCELED, intent);
                    finish();
                }
                else {
                    // user moved the location, so return it
                    Bundle latLng = new Bundle();
                    latLng.putParcelable("location", currentPos);
                    intent.putExtra("bundle", latLng);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set a mark at the user's current location if no location from photo
        if(currentPos.latitude == -1 && currentPos.longitude == -1)
        {
            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = service.getBestProvider(criteria, false);
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d(Constants.SetChangeLocation_Tag, "Do not have correct permissions");
            }
            else
            {
                Location location = service.getLastKnownLocation(provider);
                // Cannot retrieve the last known location
                if(location != null)
                {
                    LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    currentPos = userLocation;
                }
                // Set the default to GDC, because -1, -1 is in Africa or something
                else
                {
                    currentPos = new LatLng(30.286315, -97.736669);
                }
            }
        }

        m = mMap.addMarker(new MarkerOptions().position(currentPos).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16));

        // Clicking on the map will set the current position to that
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                m.setPosition(latLng);
                currentPos = latLng;
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // Do nothing
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Do nothing
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                currentPos = marker.getPosition();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
