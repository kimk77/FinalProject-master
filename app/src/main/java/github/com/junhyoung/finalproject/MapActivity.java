package github.com.junhyoung.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity { //구글 맵 API 액티비티

    LatLng locate ;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent=getIntent();
        Drawable alpha = ((ImageView)findViewById(R.id.back)).getDrawable();
        alpha.setAlpha(180);
        locate=new LatLng(intent.getExtras().getDouble("lat"),intent.getExtras().getDouble("lng"));
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        Marker marker = map.addMarker(new MarkerOptions().position(locate).title("Here I am!"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locate, 15));

        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }
    public void back(View v){
        finish();
    } // BACK버튼 클릭

}