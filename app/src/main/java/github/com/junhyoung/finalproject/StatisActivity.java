package github.com.junhyoung.finalproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class StatisActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener { //통계 액티비티

    ArrayList<String> arraylist;
    SQLiteDatabase db;
    String dbName = "savedb.db"; // db이름
    String tableName="savetable";
    int dbMode = Context.MODE_PRIVATE;
    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    TextView num1,num2,num3,num4,num5,num6,num7,num8; //시간대별로의 횟수를 위한 텍스트뷰

    TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statis);

        test=(TextView)findViewById(R.id.test);
        mListView = (ListView)findViewById(R.id.log);
        mAdapter=new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        db = openOrCreateDatabase(dbName,dbMode,null);
        num1=(TextView)findViewById(R.id.num1);
        num2=(TextView)findViewById(R.id.num2);
        num3=(TextView)findViewById(R.id.num3);
        num4=(TextView)findViewById(R.id.num4);
        num5=(TextView)findViewById(R.id.num5);
        num6=(TextView)findViewById(R.id.num6);
        num7=(TextView)findViewById(R.id.num7);
        num8=(TextView)findViewById(R.id.num8);
        createTable();
        setCategory();
        readAllDb();


       mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //customListView 의 객체를 클릭시 MapActivity로 연결하여 지도 출력
           @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //커스텀리스트뷰의 아이템 클릭시 지도 호출
                ListData mdata=mAdapter.mListData.get(position);
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra("lat", mdata.lat);
                intent.putExtra("lng", mdata.lng);
                startActivity(intent);
            }
        });
    }

    private class ViewHolder{ //customlistview의 모양 객체
        public TextView mDate;
        public TextView mTime;
        public TextView mLocate;
        public TextView mEvent;
    }
    private class ListViewAdapter extends BaseAdapter {//customlistview 를 위한 객체
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }
        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.customlistview, null);

                holder.mDate = (TextView) convertView.findViewById(R.id.date);
                holder.mTime = (TextView) convertView.findViewById(R.id.time);
                holder.mLocate = (TextView) convertView.findViewById(R.id.locate);
                holder.mEvent = (TextView) convertView.findViewById(R.id.event);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            ListData mData = mListData.get(position);
            holder.mDate.setText(mData.mDate);
            holder.mTime.setText(mData.mTime);
            holder.mLocate.setText(mData.mLocate);
            holder.mEvent.setText(mData.mEvent);
            return convertView;
        }
        public void addItem(String date,String time, String locate,String Event,double latitude, double longtitude){ //리스트뷰 아이템뷰 추가 함수
            ListData addInfo = null;
            addInfo=new ListData();
            addInfo.mDate=date;
            addInfo.mTime=time;
            addInfo.mLocate=locate;
            addInfo.mEvent=Event;
            addInfo.lat=latitude;
            addInfo.lng=longtitude;
            mListData.add(addInfo);
        }
        public void clear(){
            mListData.clear();
        }
    }

    public void createTable(){ //DB의 테이블이 없을시 테이블 생성
        try {
            String sql = "create table " + tableName + "(id integer primary key autoincrement, locate text not null,lat real, lng real, date text not null, time text not null, category text not null, event text not null)";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite", "error: " + e);
        }
    }
    public void readAllDb(){ //분야가 '전체'일때 모든 데이터 읽어오는 함수
        int count[]=new int[8]; //시간대별로 갯수 저장 함수
        String temp="";
        String sql = "select * from " + tableName + " order by id DESC ;";//내림차순으로 최신데이터부터 출력
        Cursor result = db.rawQuery(sql, null);
        result.moveToFirst();
        mAdapter.clear();

        while (!result.isAfterLast()) {
            String locate = result.getString(1);
            String date = result.getString(4);
            String time = result.getString(5);
            String event = result.getString(7);
            String category=result.getString(6);
            Log.d("", locate + date + time + event + category);
            switch(Integer.parseInt(time.substring(0,2))){ //시간별로 횟수 측정
                case 0:case 1:case 2: count[0]++;break;
                case 3:case 4:case 5: count[1]++;break;
                case 6:case 7:case 8: count[2]++;break;
                case 9:case 10:case 11: count[3]++;break;
                case 12:case 13:case 14: count[4]++;break;
                case 15:case 16:case 17: count[5]++;break;
                case 18:case 19:case 20: count[6]++;break;
                case 21:case 22:case 23: count[7]++;break;
            }
            mAdapter.addItem(date, time, locate, event, Double.parseDouble(result.getString(2)), Double.parseDouble(result.getString(3)));
            result.moveToNext();
        }
        num1.setText("    "+count[0]+" 회");
        num2.setText("    "+count[1]+" 회");
        num3.setText("    "+count[2]+" 회");
        num4.setText("    "+count[3]+" 회");
        num5.setText("    "+count[4]+" 회");
        num6.setText("    "+count[5]+" 회");
        num7.setText("    " + count[6]+" 회");
        num8.setText("    " + count[7]+" 회");

        result.close();
    }

    public void readDB(String category){ //분야 선택시 분야에 대한 DB데이터 호출
        int count[]=new int[8];
        String temp="";
        try {
            String sql = "select * from " + tableName + " where category = '" + category + "' order by id DESC ;"; //최신것부터 내림차순으로 정렬
            Cursor result = db.rawQuery(sql, null);

            result.moveToFirst();

            while (!result.isAfterLast()) {
                String locate = result.getString(1);
                String date = result.getString(4);
                String time = result.getString(5);
                String event = result.getString(7);
                switch(Integer.parseInt(time.substring(0,2))){//시간별로 횟수 측정
                    case 0:case 1:case 2: count[0]++;break;
                    case 3:case 4:case 5: count[1]++;break;
                    case 6:case 7:case 8: count[2]++;break;
                    case 9:case 10:case 11: count[3]++;break;
                    case 12:case 13:case 14: count[4]++;break;
                    case 15:case 16:case 17: count[5]++;break;
                    case 18:case 19:case 20: count[6]++;break;
                    case 21:case 22:case 23: count[7]++;break;
                }
                mAdapter.addItem(date, time, locate, event, Double.parseDouble(result.getString(2)), Double.parseDouble(result.getString(3)));// 커스텀 리스트뷰에 아이템 추가
                result.moveToNext();
            }
            num1.setText("    "+count[0]+" 회");
            num2.setText("    "+count[1]+" 회");
            num3.setText("    "+count[2]+" 회");
            num4.setText("    "+count[3]+" 회");
            num5.setText("    "+count[4]+" 회");
            num6.setText("    "+count[5]+" 회");
            num7.setText("    "+count[6]+" 회");
            num8.setText("    "+count[7]+" 회");
            result.close();
        }catch (Exception e){
            Toast toast=Toast.makeText(getApplicationContext(),category+"분야의 데이터가 없습니다.",Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    public void setCategory(){ //스피너 설정
        arraylist = new ArrayList<String>();
        arraylist.add("전체");
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
        Spinner sp = (Spinner)findViewById(R.id.category);
        sp.setPrompt("Category"); // 스피너 제목
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(this);


    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) { //스피너에서 분야 선택시
        // TODO Auto-generated method stub
        Toast.makeText(this, arraylist.get(arg2), Toast.LENGTH_LONG).show();//해당목차눌렸을때

        if(arraylist.get(arg2)=="전체"){ //분야가 전체일경우 모든 데이터 호출
            mAdapter.clear();
            readAllDb();
            mAdapter.notifyDataSetChanged();
        }else { //분야 선택시 분야에 대한 데이터만 호출
            mAdapter.clear();
            readDB(arraylist.get(arg2));
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}
