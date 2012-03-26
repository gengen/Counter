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
         * �������s���ɂȂ邩������Ȃ��̂ŁA���̏ꍇ�͎g���܂킷�B
         * �������AVISIBLE�ݒ�������������ق����悢�B
         */
        //if(convertView == null){
            convertView = mInflater.inflate(R.layout.listitem, null);
        //}
        
        //���݂̍s�ɐݒ肷��I�u�W�F�N�g
        DataList data = (DataList)getItem(position);
        
        //���O
        TextView nameView = (TextView)convertView.findViewById(R.id.item_name);
        nameView.setText(data.getName());
        
        //�J�E���g
        TextView countView = (TextView)convertView.findViewById(R.id.item_count);
        countView.setText(data.getCount());

        //����
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
