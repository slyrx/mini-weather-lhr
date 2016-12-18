package cn.edu.pku.slyrx.miniweatherlhr;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.slyrx.bean.City;
import pku.ss.slyrx.app.MyApplication;

/**
 * Created by slyrx on 16/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener, SearchView.OnQueryTextListener {
    private ImageView mBackBtn;
    private ListView mListView;
    private SearchView mSearchView;
    private City mSelectCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);


        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        String[] data = {"1", "2", "3"};
        List<City> citylist = MyApplication.getmCityList();
        //遍历List,取出City名,放入新string数组
        List<String> cityName = new ArrayList<String>();
        for (City temp: citylist
             ) {
            cityName.add(temp.getCity());
        }


        mListView = (ListView)findViewById(R.id.city_listView);
        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cityName));
        //mListView.setOnClickListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //响应ListView中某一项的点击
                //记下选择的城市，供后续主画面的显示使用, 此处需要把找到的城市信息传回给主Activity
                //遍历城市list，确认匹配的城市名称
                for(int i = 0; i < MyApplication.getmCityList().size(); i++){
                    if(MyApplication.getmCityList().get(i).getCity().equals(((TextView)view).getText())){
                        mSelectCity = MyApplication.getmCityList().get(i);
                        break;
                    }
                }

                //传递City信息
                Bundle bundle = new Bundle();
                bundle.putString("City", mSelectCity.getNumber());
                Intent intent = new Intent();
                intent.putExtra("GetSelectCityNumber", bundle);
                setResult(101, intent);

                finish();
            }
        });
        mListView.setTextFilterEnabled(true);

        mSearchView = (SearchView)findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(this);

    }



    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {//该分支目前没有走到
        Toast.makeText(this, "您选择的是"+query, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText)){
            mListView.clearTextFilter();
        }else {
            mListView.setFilterText(newText);
        }
        return true;
    }
}
