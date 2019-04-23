package com.project.ecs.ecarsavegalu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private EditText getLocationText;
    private EditText dropLocationText;
    private Button getLocationButton;
    private Button findRideButton;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context mContext;
    private Geocoder geocoder;
    List<Address> addressList;
    List<Address> address;
    double latitude, longitude;
    double drop_lat,drop_long;
    private static final String BASE_URL="https://book.olacabs.com/";
    private Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationButton = findViewById(R.id.getLocation);
        getLocationText = findViewById(R.id.pickupLocation);
        dropLocationText = findViewById(R.id.dropLocation);
        findRideButton = findViewById(R.id.findRide);

        geocoder = new Geocoder(this, Locale.getDefault());
        mContext = this;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getLocationText.setText(location.getLatitude()+" "+location.getLongitude());
                Log.d("TAG", "onLocationChanged: "+location.getLatitude()+" "+location.getLongitude());
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    String addressStr = addressList.get(0).getAddressLine(0);
                    String areaStr = addressList.get(0).getLocality();
                    String cityStr = addressList.get(0).getAdminArea();
                    String countryStr = addressList.get(0).getCountryName();
                    String postalcodeStr = addressList.get(0).getPostalCode();

                    String fullAddress = addressStr + ", " + areaStr + ", " + cityStr + ", " + countryStr + ", " + postalcodeStr;

                    getLocationText.setText(addressStr);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return;
            }
        }
        else {
            configureButton();
        }

        findRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dropAddress=dropLocationText.getText().toString();
                try{
                    address = geocoder.getFromLocationName(dropAddress,5);
                    Address location= address.get(0);
                    Log.d("TAG", "onClick: "+ location.getLatitude()+ " "+location.getLongitude());
                    drop_lat=location.getLatitude();
                    drop_long=location.getLongitude();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                Retrofit retrofit= new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(new OkHttpClient())
                        .build();
                api= retrofit.create(Api.class);

                Call<ResponseBody> call=api.FindRide(latitude,longitude,"compact",12345,drop_lat,drop_long,"yes");
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("TAG", "onResponse: Server Response: "+response.toString());
                        if(!response.isSuccessful()) {
                            Log.d("TAG", "Code: "+response.code());
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

                    }
                });

                Intent intent= new Intent(mContext,SecondActivity.class);
                String URL= "https://book.olacabs.com/?lat="+latitude+"&lng="+longitude+"&category=compact&utm=12345&drop_lat="+drop_lat+"&drop_lng="+drop_long+"&dsw=yes";
                intent.putExtra("URL",URL);
                startActivity(intent);

            }
        });

    }

    private void configureButton() {

        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates("gps", 0, 5, locationListener);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    configureButton();
                break;
                default:
                    break;
        }
    }
}
