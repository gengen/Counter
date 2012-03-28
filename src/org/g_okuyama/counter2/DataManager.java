package org.g_okuyama.counter2;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import mediba.ad.sdk.android.openx.MasAdView;

public class DataManager extends Activity {
	private static final int MENU_EXPORT_ALL = 0;
	private static final int MENU_REMOVE_ALL = 1;
	private static final int REQUEST_CODE = 1;
		
	DatabaseHelper dbhelper = null;
	//counterテーブルのIDリスト
	Integer[] dbid = null;
	//保存データリスト
	String[] savedata = null;

	//for mediba ab
	private MasAdView mAd = null;
	
	ArrayList<DataList> mDataArray = null; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //画面作成
        setDataDisplay();
        //保存データ表示
        displaySaveData();
    }
    
    private void setDataDisplay(){
		setContentView(R.layout.list);
		
        mAd = (MasAdView)findViewById(R.id.adview);
        mAd.setAuid("112070");
        mAd.start();
    }
    
    //保存データ表示処理
    private void displaySaveData(){
		//DB取得
    	if(dbhelper == null){
    		dbhelper = new DatabaseHelper(this);
    	}
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	String query = "select * from counter;";
    	Cursor c = db.rawQuery(query, null);
    	
    	mDataArray = new ArrayList<DataList>();
    	
		//タイトルを作成
		DataList titleList = new DataList();
		titleList.setName(getString(R.string.cnt_save_title));
		titleList.setCount(getString(R.string.dm_count_num));
        titleList.setDate(getString(R.string.dm_savetime));
        titleList.setDBID(-9999);
		mDataArray.add(titleList);

    	//保存データ数を取得
    	int rowcount = c.getCount();
    	if(rowcount == 0){
        	//保存データがない場合
    		savedata = new String[1];
    		savedata[0] = new String("No data");

    	}else{
    		dbid = new Integer[rowcount];
    		savedata = new String[rowcount];
    		//降順に並べる
    		c.moveToLast();

    		//すべてのデータを配列に書き込む
    		String crlf = System.getProperty("line.separator");
    		for(int i = 0; i < rowcount; i++){
    			String title = c.getString(1);
    			String cnt = c.getString(2);
    			String date = c.getString(3);    			
    			//String place = c.getString(4);

    			dbid[i] = c.getInt(0);

    			String countStr = "";

    			if(c.getString(6).equals("2")){
    				String[] cstr = cnt.split(",");
    				savedata[i] = new String(title + crlf
							+ getString(R.string.dm_count_num)
							+ cstr[0] + " " + cstr[1] + crlf
							+ getString(R.string.dm_savetime) + date);
    						/*
							+ crlf
							+ getString(R.string.dm_place) + place);
							*/
    				
    				countStr = cstr[0] + ", " + cstr[1];

    			}else{
    				savedata[i] = new String(title + crlf
							+ getString(R.string.dm_count_num) + cnt + crlf
							+ getString(R.string.dm_savetime) + date);
    						/*
							+ crlf
							+ getString(R.string.dm_place) + place);
							*/
    				
    				countStr = cnt;
    			}

    			String[] dstr = date.split(" ");

				//表示するリストを作成
				DataList list = new DataList();
				list.setName(title);
				list.setCount(countStr);
                list.setDate(dstr[0]);
				mDataArray.add(list);
				
    			//降順に並べる
    			c.moveToPrevious();
    		}
    	}
    	
    	c.close();

		//保存データの表示
		ListView lv = (ListView)this.findViewById(R.id.widget42);
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, savedata);
		DataArrayAdapter adapter = new DataArrayAdapter(this, android.R.layout.simple_list_item_1, mDataArray);
		lv.setAdapter(adapter);

		//データ選択時のリスナを登録
		lv.setOnItemClickListener(new ClickAdapter());
		lv.setOnItemLongClickListener(new LongClickAdapter());
    }
    
    private class ClickAdapter implements OnItemClickListener{
        public void onItemClick(AdapterView<?> adapter,
                View view, int position, long id) {
        	//データがない場合は何もしない
        	if(savedata == null || savedata[0].equalsIgnoreCase("No data")){
        		return;
        	}

        	//intent作成
        	Intent intent = new Intent(DataManager.this, DataState.class);
        	//選択項目のDBのIDを渡す
        	intent.putExtra("id", dbid[position-1]);//positionからタイトル分の1を引く
        	//intentを発行しカウントの詳細画面を表示
        	startActivityForResult(intent, REQUEST_CODE);
        }
    }
    
    private class LongClickAdapter implements OnItemLongClickListener{
    	int position = -1;
    	
		public boolean onItemLongClick(AdapterView<?> adapter, View view,
				int pos, long id) {
        	//データがない場合は何もしない

			if(savedata == null || savedata[0].equalsIgnoreCase("No data")){
        		return true;
        	}
			
			//タイトル分を引く
			position = pos-1;

			if(Counter.CHARGE_FLAG){
				new AlertDialog.Builder(DataManager.this)
				.setTitle(R.string.dm_dialog_select)
				.setItems(R.array.dm_context_charge, new DialogInterface.OnClickListener() {
				
					public void onClick(DialogInterface dialog, int item) {
						switch(item){
						case 0://エクスポート
							export(position);
							break;
						case 1://削除
							delete(position);
							break;
						case 2://キャンセル
							//何もしない
							break;
						}
					}
				}).show();

			}else{
				new AlertDialog.Builder(DataManager.this)
				.setTitle(R.string.dm_dialog_select)
				.setItems(R.array.dm_context_free, new DialogInterface.OnClickListener() {
				
					public void onClick(DialogInterface dialog, int item) {
						switch(item){
						case 0://削除
							delete(position);
							break;
						case 1://キャンセル
							break;
						}
					}
				}).show();				
			}
			
			return true;
		}
    }

    //長押しでエクスポートが選択された際の処理
    private void export(int position){
    	Pattern pt = Pattern.compile(System.getProperty("line.separator"));
    	String[] str = pt.split(savedata[position]);
    	final String title = str[0];
    	final int pos = position;
    	
    	//CSVまたはメールでエクスポート予定
		new AlertDialog.Builder(DataManager.this)
		.setTitle("\"" + title + "\"" + getString(R.string.dm_dialog_export))
		.setItems(R.array.dm_howtoexport, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0://メールで送信
					exportByMail(title, pos);
					break;
				case 1://SDに書込み
					exportToSD(title, pos);
					break;
				case 2://キャンセル
					//何もしない
					break;
				}
			}
		}).show();    	
    }
    
    String getExportString(String title, int position){
    	//DBを取得
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	String query = "select * from counter where rowid = ?;";
    	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid[position])});
    	//db.close();

    	c.moveToFirst();
		//String title = c.getString(1);
		String count = c.getString(2);
		String date = c.getString(3);
		//String place = c.getString(4);
		String timecount = c.getString(5);

		String crlf = System.getProperty("line.separator");

		String exstr;
		if(c.getString(6).equals("2")){
			String[] cstr = count.split(",");
			exstr = getString(R.string.dm_export_format) + crlf
					+ title + "," 
					+ cstr[0] + " " + cstr[1] + ","
					+ date;
					/*
					+ ","
					+ place;
					*/

			exstr += crlf + crlf;

			//ボタン数の区切り
			String[] bstr = timecount.split(":::");
			for(int i = 0; i < bstr.length; i++){

				exstr += getString(R.string.dm_button) + Integer.toString(i+1) + crlf;
				exstr += getString(R.string.dm_time_count) + crlf;

				Pattern pt = Pattern.compile(",");
				String[] str = pt.split(bstr[i]);

				for(String s: str){
					String tmp = s.replace(" ", ",");
					exstr += tmp + crlf;
				}
			}
			
		}else{
			exstr = getString(R.string.dm_export_format) + crlf
			+ title + "," + count + "," + date;
			//+ "," + place;

			exstr += crlf + crlf;
			exstr += getString(R.string.dm_time_count) + crlf;

			Pattern pt = Pattern.compile(",");
			String[] str = pt.split(timecount);

			for(String s: str){
				String tmp = s.replace(" ", ",");
				exstr += tmp + crlf;
			}
		}
		
		c.close();

		return exstr;
    }
    
    private void exportByMail(String title, int position){
    	//とりあえず、ここでの確認ダイアログは削除
    	/*
    	//確認ダイアログの表示
    	new AlertDialog.Builder(this)
    	.setTitle("メール送信")
    	.setMessage("「" + title + "」をメールで送信しますか？")
    	.setPositiveButton("はい", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//エクスポート処理
				exportData(title, pos);
			}
		})
		.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		})
		.show();    
    	*/

    	String sendstr = getExportString(title, position);
    	
		//データをメールで送信
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	//メアドが設定画面で設定されている場合は設定
    	String ad = CounterPreference.getMailAddress(this);
    	if(ad != null){
    		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ad});
    	}
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send \"" + title + "\" data");
    	intent.putExtra(Intent.EXTRA_TEXT, sendstr);
    	//これがないと落ちるので設定
    	intent.setType("plain/text");
    	
    	try{
    		startActivity(intent);
    	}catch(ActivityNotFoundException e){
    		new AlertDialog.Builder(this)
    		.setTitle(R.string.dm_error)
    		.setMessage(R.string.dm_missing_mailer)
    		.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				//何もしない
    			}
    		}).show();
    	}
    }
    
    private void exportToSD(String title, int position){
        File file = new File(Environment.getExternalStorageDirectory(), "/Counter");

        try{
            file.mkdir();
            File savefile = new File(file.getPath(), getCurrentDate() + ".txt");
        	FileOutputStream fos = new FileOutputStream(savefile);
        	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        	BufferedWriter bw = new BufferedWriter(osw);
        	//SDへの書込み
        	bw.write(getExportString(title, position));
        	bw.flush();
        	bw.close();
        	
        	Toast.makeText(this, R.string.dm_export_sd_ok, Toast.LENGTH_SHORT).show();

        }catch(Exception e){
        	Toast.makeText(this, R.string.dm_export_sd_ng, Toast.LENGTH_SHORT).show();        	
        }
    }
    
    //長押しで削除が選択された際の処理
    private void delete(int position){
    	Pattern pt = Pattern.compile(System.getProperty("line.separator"));
    	String[] str = pt.split(savedata[position]);
    	String title = str[0];
    	
    	final int pos = position;
    	
    	//確認ダイアログの表示
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.dm_delete)
    	.setMessage("\"" + title + "\" " + getString(R.string.dm_delete_confirm))
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//保存データ削除処理
				removeData(pos);
				displaySaveData();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		})
		.show();    
    }
    
    private void removeData(int position){
    	//DB取得
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	//データを削除
    	db.delete("counter", "rowid = ?", new String[]{Integer.toString(dbid[position])});
    }
    
    //呼び出したintent終了時の処理
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	//データを表示
    	if(requestCode == REQUEST_CODE){
    		displaySaveData();
    	}
    }

    //オプションメニューの作成
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(Counter.CHARGE_FLAG){
        	//オプションメニュー項目作成(「全エクスポート」)
        	MenuItem saveItem = menu.add(0, MENU_EXPORT_ALL, 0 ,R.string.option_export_all);
        	saveItem.setIcon(android.R.drawable.ic_menu_upload);
        }

        //オプションメニュー項目作成(「全削除」)
        MenuItem clearItem = menu.add(0, MENU_REMOVE_ALL, 0 ,R.string.option_remove_all);
        clearItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {

		if(savedata == null || savedata[0].equalsIgnoreCase("No data")){
    		return true;
    	}
    	
    	switch (item.getItemId()) {
    	case MENU_EXPORT_ALL:
    		exportAll();
    		break;
    		
    	case MENU_REMOVE_ALL:
    		removeAll();
    		break;

    	default:
    		//何もしない
    	}

    	return true;
    }
    
    private void exportAll(){
    	//CSVまたはメールでエクスポート
		new AlertDialog.Builder(DataManager.this)
		.setTitle(R.string.dm_dialog_export_all)
		.setItems(R.array.dm_howtoexport, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0://メールで送信
					exportByMailAll();
					break;
				case 1://SDに書込み
					exportToSDAll();
					break;
				case 2://キャンセル
					//何もしない
					break;
				}
			}
		}).show();    	
    }
    
    private void exportByMailAll(){
    	String sendstr = "";
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	String query = "select * from counter where rowid = ?;";
		String crlf = System.getProperty("line.separator");

    	//メールのテキストを設定
    	for(int i = 0; i < dbid.length; i++){
        	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid[i])});
        	c.moveToFirst();
        	String title = c.getString(1);
        	String tmpstr = getExportString(title, i);
    		sendstr += crlf + tmpstr;
    		sendstr += crlf + crlf;
    		c.close();
    	}

    	//intentを作成
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	//メアドが設定画面で設定されている場合は設定
    	String ad = CounterPreference.getMailAddress(this);
    	if(ad != null){
    		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ad});
    	}
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send All data");
    	intent.putExtra(Intent.EXTRA_TEXT, sendstr);
    	//これがないと落ちるので追加
    	intent.setType("plain/text");
    	//intentを発行し、メーラを起動
    	try{
    		startActivity(intent);
    	}catch(ActivityNotFoundException e){
    		new AlertDialog.Builder(this)
    		.setTitle(R.string.dm_error)
    		.setMessage(R.string.dm_missing_mailer)
    		.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				//何もしない
    			}
    		}).show();
    	}    	
    }

    private void exportToSDAll(){
    	String exstr = "";
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	String query = "select * from counter where rowid = ?;";
		String crlf = System.getProperty("line.separator");

    	//SDに書き込む文字列を設定
    	for(int i = 0; i < dbid.length; i++){
        	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid[i])});
        	c.moveToFirst();
        	String title = c.getString(1);
        	String tmpstr = getExportString(title, i);
    		exstr += crlf + tmpstr;
    		exstr += crlf + crlf;
    		c.close();
    	}

        File file = new File(Environment.getExternalStorageDirectory(), "/Counter");

        try{
            file.mkdir();
            File savefile = new File(file.getPath(), getCurrentDate() + ".txt");
        	FileOutputStream fos = new FileOutputStream(savefile);
        	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        	BufferedWriter bw = new BufferedWriter(osw);
        	//SDへの書込み
        	bw.write(exstr);
        	bw.flush();
        	bw.close();
        	
        	Toast.makeText(this, R.string.dm_export_sd_ok, Toast.LENGTH_SHORT).show();

        }catch(Exception e){
        	Toast.makeText(this, R.string.dm_export_sd_ng, Toast.LENGTH_SHORT).show();        	
        }
    }
    
    
    private void removeAll(){
    	//確認ダイアログの表示
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.dm_delete_all)
    	.setMessage(R.string.dm_delete_confirm_all)
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//保存データ全削除処理
				removeAllData();
				displaySaveData();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		})
		.show();
    }
    
    private void removeAllData(){
       	//DB取得
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	db.delete("counter", null, null);
    }
    
    String getCurrentDate(){
    	Calendar cal1 = Calendar.getInstance();
        int year = cal1.get(Calendar.YEAR);
        int mon = cal1.get(Calendar.MONTH) + 1;
        int d = cal1.get(Calendar.DATE);
        int h = cal1.get(Calendar.HOUR_OF_DAY);
        int min = cal1.get(Calendar.MINUTE);
        int sec = cal1.get(Calendar.SECOND);
        
        String month = Integer.toString(mon);
        //1桁月の場合は0を入れる
        if(month.length() == 1){
        	month = "0" + month;
        }

        String day = Integer.toString(d);
        //1桁日の場合は0を入れる
        if(day.length() == 1){
        	day = "0" + day;
        }

        String hour = Integer.toString(h);
        //1桁時の場合は0を入れる
        if(hour.length() == 1){
        	hour = "0" + hour;
        }
        String minute = Integer.toString(min); 
        //1桁分の場合は0を入れる
        if(minute.length() == 1){
        	minute = "0" + minute;
        }
        
        String second = Integer.toString(sec);
        //1桁秒の場合は0を入れる
        if(second.length() == 1){
        	second = "0" + second;
        }        

        return Integer.toString(year) + month + day + hour + minute + second;
    }
    
    protected void onResume(){
        mAd.start();
    	super.onResume();
    }
    
    protected void onPause(){
    	mAd.stop();
    	super.onPause();
    }
    
    protected void onRestart(){
    	super.onRestart();
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    }
    
}
