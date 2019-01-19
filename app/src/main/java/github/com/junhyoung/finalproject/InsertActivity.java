package github.com.junhyoung.finalproject;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsertActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener { //데이터 저장 액티비티

    TextView logView;
    TextView contents;
    ArrayList<String> arraylist;
    SQLiteDatabase db;
    String dbName = "savedb.db"; // db이름
    String tableName="savetable"; // table 이름
    int dbMode=Context.MODE_PRIVATE;

    String date;
    String time;
    String category;
    String event;
    String locate;
    double latitude=-1,longtitude=-1; // 좌표가 아직 안잡혔을때 -1값으로 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        db = openOrCreateDatabase(dbName,dbMode,null); // db생성, 오픈
        createTable();
        logView = (TextView) findViewById(R.id.log);
        logView.setText("GPS 찾는 중!");
        contents =(TextView)findViewById(R.id.contents);
        findlocate(); //위치 찾는 함수 호출
        findDay(); //날짜 찾는 함수 호출
        setCategory(); // 분야 스피너 설정


    }

    public void map(View v){ // 지도보기 버튼 클릭시
        if(latitude==-1){ //gps 가 아직 잡히지 않았을때
            Toast toast=Toast.makeText(getApplicationContext(),"위치를 검색중 입니다!",Toast.LENGTH_SHORT);
            toast.show();
        }
        else { // 위도 경도를 넘겨주어 MapActivity 실행
            Intent intent = new Intent(getApplicationContext(), MapActivity.class); //INTENT로 MapActivity에 위도경도 전달
            intent.putExtra("lat", latitude);
            intent.putExtra("lng", longtitude);
            startActivity(intent);
        }
    }
    public boolean insertData(){ // insert SQLite 실행
        if(category=="선택해주세요") { //분야 미선택시
            Toast toast=Toast.makeText(getApplicationContext(),"분야를 선택해 주세요",Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        if(latitude==-1){ // GPS 좌표가 잡히지 않았을시
            Toast toast=Toast.makeText(getApplicationContext(),"GPS 찾는중입니다.",Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }


        if(contents.getText().toString().equals("")) //세부 사건이 입력되지 않았을때 카테고리를 세부사건으로 입력
            event=category;
        else
            event=contents.getText().toString();;

        String sql = "insert into " + tableName + " values(NULL, '" + locate+"'"+", "+latitude+", " +longtitude+", "+"'" +date+"'"+", "+"'" +time+"'"+", " +"'"+category+"'"+", " +"'"+event+ "');"; //db 저장
        db.execSQL(sql);
        return true;
    }


    public void createTable(){ //DB의 Table이 없을때 table 생성
        try {
            String sql = "create table " + tableName + "(id integer primary key autoincrement, " + "locate text not null,lat real, lng real, date text not null, time text not null, category text not null, event text not null)";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite","error: "+ e);
        }
    }
    public void save(View v){ //저장 버튼이 눌렸을때 OnclickHandler
        if(insertData()) { // 저장이 성공적으로 되었을때 종료
            Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
            finish();
        }else{
        }
    }
    public void cancle(View v){//BACK 버튼 클릭
       Toast.makeText(this,"저장 안함",Toast.LENGTH_SHORT).show();
        finish();
    }

    public void setCategory(){//스피너 설정
        arraylist = new ArrayList<String>();
        arraylist.add("선택해주세요");
        arraylist.add("공부");
        arraylist.add("과제");
        arraylist.add("식사");
        arraylist.add("여가");
        arraylist.add("친구");
        arraylist.add("음주");
        arraylist.add("취침");
        arraylist.add("★SPECIAL★");


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, arraylist);
        //스피너 속성
        Spinner sp = (Spinner) this.findViewById(R.id.category);
        sp.setPrompt("Category"); // 스피너 제목
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(this);


    }

    public void findDay() { //날짜 찾는 함수
        TextView day = (TextView) findViewById(R.id.day);
        long now = System.currentTimeMillis();// 현재 시간을 msec으로 구한다.
        Date date = new Date(now);// 현재 시간을 저장 한다.

        SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy"); //yyyy형식으로 년도
        SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM"); //MM형식으로 월
        SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd"); //dd형식으로 일
        SimpleDateFormat CurHourFormat = new SimpleDateFormat("HH"); //HH형식으로 시간
        SimpleDateFormat CurMinuteFormat = new SimpleDateFormat("mm"); //mm형식으로 분
        day.setText(CurYearFormat.format(date) + "년 " + CurMonthFormat.format(date) + "월 " + CurDayFormat.format(date) +"일\n"
                +CurHourFormat.format(date)+"시 "+CurMinuteFormat.format(date)+"분");

        time=CurHourFormat.format(date)+"시"+CurMinuteFormat.format(date)+"분";
        this.date=CurYearFormat.format(date) + "년" + CurMonthFormat.format(date) + "월" + CurDayFormat.format(date) +"일";
    }

    private String findAddress(double lat, double lng) { //위도경도로 주소값 반환
        StringBuffer bf = new StringBuffer();
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> address;
        String currentLocationAddress;
        try {
            if (geocoder != null) {
                // 세번째 인수는 최대결과값인데 하나만 리턴받도록 설정했다
                address = geocoder.getFromLocation(lat, lng, 3);
                // 설정한 데이터로 주소가 리턴된 데이터가 있으면
                if (address != null && address.size() > 0) {
                    // 주소
                    currentLocationAddress = address.get(0).getAddressLine(0).toString();

                    // 전송할 주소 데이터 (위도/경도 포함 편집)
                    bf.append(currentLocationAddress);
                    locate=bf.toString();
                    bf.append("\n").append("lat:").append(lat).append("\n");
                    bf.append("lng:").append(lng);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bf.toString();
    }

    public void findlocate(){ //위도 경도 구해주는 함수
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                longtitude=lng;
                latitude=lat;

                logView.setText(findAddress(lat,lng));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                logView.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                logView.setText("onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
                logView.setText("onProviderDisabled");
            }
        };
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, //스피너가 설정되었을때 함수
                               long arg3) {
        // TODO Auto-generated method stub
        Toast.makeText(this, arraylist.get(arg2), Toast.LENGTH_LONG).show();//해당목차눌렸을때
        category=arraylist.get(arg2);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

}
