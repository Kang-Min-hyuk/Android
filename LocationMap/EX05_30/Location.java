package com.test.exlocationmap;

/**
 * - Play Service 라이브러리 사용 설정하기
 * - XML 레이아웃에 맵프래그먼트 추가하기
 * - 소스 코드에서 내 위치로 지도 이동시키기
 * - 메니페스트에 설정 추가하기
 * - 지도 API 키 (구글 API 콘솔 사이트 http://console.developers.google.com)
 */


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends AppCompatActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML 레이아웃에 정의한 지도 객체(MapFragment) 참조
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        MapsInitializer.initialize(this); // 구글맵 생성이후 호출하면 안전

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMyLocation();
            }
        });
    }

    public void requestMyLocation() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        long mintim = 3000;
        float minDistance = 1.5f;

        GPSListener gpsListener = new GPSListener();

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mintim, minDistance, gpsListener);

        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude = location.getLatitude(); // 위도
        double longitude = location.getLongitude(); // 경도
        showCurrentLocation(latitude, longitude);
    }

    // 좌표정보를 이용해서 구글맵으로 보여주기
    private void showCurrentLocation(double latitude, double longitude) {
        // Zoom 지도의 축척(멀리)1 ~ 19,21(가까이)
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15);


        map.animateCamera(cameraUpdate); // 현재위치를 지도를 중심으로 표시
    }

    class GPSListener implements LocationListener {


        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude(); // 위도
            double longitude = location.getLongitude(); // 경도

            showCurrentLocation(latitude, longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


}
