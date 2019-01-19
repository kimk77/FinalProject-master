package github.com.junhyoung.finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DeleteActivity extends AppCompatActivity {

    SQLiteDatabase db;
    String dbName = "savedb.db"; // db이름
    String tableName="savetable";
    int dbMode = Context.MODE_PRIVATE;
    private ListView mListView = null;
    private ListViewAdapter mAdapter = null;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        mListView = (ListView)findViewById(R.id.log);
        mAdapter=new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        db = openOrCreateDatabase(dbName,dbMode,null);
        createTable();
        builder=new AlertDialog.Builder(this); //확인창 을 위한 변수
        readAllDb(); //데이터를 모두 읽어옴

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //customListView 의 객체를 클릭시 MapActivity로 연결하여 지도 출력
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//커스텀리스트뷰의 아이템이 클릭되면
                ListData mdata = mAdapter.mListData.get(position);
                checkDelete(mdata); //선택된 데이터를 지우기위한 함수 호출
            }
        });
    }
    public void checkDelete(final ListData mdata){ //선택된 데이터 지우기위해 확인창을 띄우는 함수
        // 여기서 부터는 알림창의 속성 설정
        builder.setTitle("정말 삭제하시겠습니까?")        // 제목 설정
                .setMessage(mdata.mDate+mdata.mTime+"에\n"+mdata.mLocate+"에서\n"+mdata.mEvent+"한 일을 삭제 하시겠습니까?")        // 메세지 설정
                .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) { //확인 클릭시
                        deleteData(mdata); // 삭제 함수 호출
                        mAdapter.clear(); // 리스트뷰 초기화
                        readAllDb(); //삭제후 DB 출력
                        mAdapter.notifyDataSetChanged(); // 리스트뷰 출력
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel(); //삭제 취소
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }

    public void deleteData(ListData mdata){ //삭제함수
        String sql = "delete from " + tableName + " where id = " + mdata.id + ";";
        db.execSQL(sql);

    }

    private class ViewHolder{ //customlistview의 내용
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
        public View getView(int position, View convertView, ViewGroup parent) { //각 리스트의 내용설정
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
        public void addItem(String date,String time, String locate,String Event,double latitude, double longtitude,int id){ //리스트뷰에 데이터 추가
            ListData addInfo = null;
            addInfo=new ListData();
            addInfo.mDate=date;
            addInfo.mTime=time;
            addInfo.mLocate=locate;
            addInfo.mEvent=Event;
            addInfo.lat=latitude;
            addInfo.lng=longtitude;
            addInfo.id=id;
            mListData.add(addInfo);
        }
        public void clear(){
            mListData.clear();
        } // 리스트 초기화
    }

    public void createTable(){ // DB의 테이블이 없을시 호출
        try {
            String sql = "create table " + tableName + "(id integer primary key autoincrement, locate text not null,lat real, lng real, date text not null, time text not null, category text not null, event text not null)";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite", "error: " + e);
        }
    }
    public void readAllDb(){ // Db에서 데이터 읽어옴
        String sql = "select * from " + tableName + " order by id DESC ;";
        Cursor result = db.rawQuery(sql, null);
        result.moveToFirst();
        mAdapter.clear();

        while (!result.isAfterLast()) {
            String locate = result.getString(1);
            String date = result.getString(4);
            String time = result.getString(5);
            String event = result.getString(7);
            String category=result.getString(6);
            int id=result.getInt(0);
            Log.d("", locate + date + time + event + category);
            mAdapter.addItem(date, time, locate, event, Double.parseDouble(result.getString(2)), Double.parseDouble(result.getString(3)),id);
            result.moveToNext();
        }

        result.close();
    }
}
