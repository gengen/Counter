package org.g_okuyama.counter2;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DataArrayAdapter extends ArrayAdapter<DataList> {
    public static final String TAG = "Counter"; 
    private LayoutInflater mInflater;

    public DataArrayAdapter(
            Context context, int textViewResourceId, List<DataList> objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
         * メモリ不足になるかもしれないので、その場合は使いまわす。
         * ただし、VISIBLE設定を初期化したほうがよい。
         */
        //if(convertView == null){
            convertView = mInflater.inflate(R.layout.listitem, null);
        //}
        
        //現在の行に設定するオブジェクト
        DataList data = (DataList)getItem(position);
        
        //名前
        TextView nameView = (TextView)convertView.findViewById(R.id.item_name);
        nameView.setText(data.getName());
        
        //カウント
        TextView countView = (TextView)convertView.findViewById(R.id.item_count);
        countView.setText(data.getCount());

        //日時
        TextView dateView = (TextView)convertView.findViewById(R.id.item_date);
        dateView.setText(data.getDate());
        
        return convertView;
    }
    
    @Override  
    public boolean isEnabled(int position) {
        if(getItem(position).getName().equals("No data") 
                && getItem(position).getCount().equals("")){
            return false;
        }
        else{
            return true;
        }
    }  
}
