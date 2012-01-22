package org.g_okuyama.counter2;

import java.io.*;
import java.util.Calendar;
import java.util.regex.Pattern;

import jp.co.nobot.libAdMaker.libAdMaker;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import jp.co.nobot.libAdMaker.libAdMaker;

public class DataState extends TabActivity implements OnTabChangeListener{
	private static final int MENU_EXPORT = 0;
	private static final int MENU_REMOVE = 1;
	
	static int dbid = -1;
	
	String title = "";
	String count = "";
	String date = "";
	//String place = "";
	String timecount = "";
	String button = "";
	
	//for admaker
	private libAdMaker AdMaker = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //intentから選択項目を取り出す
        Bundle extras = getIntent().getExtras();
        dbid = extras.getInt("id");

        //カウント情報の詳細画面表示
        setDetailDisplay();
    }

    private void setDetailDisplay(){
    	setContentView(R.layout.tabs);

    	// TabHostのインスタンスを取得
    	TabHost tabs = getTabHost();
    	tabs.setOnTabChangedListener(this);
        
    	TabSpec tab1 = tabs.newTabSpec("tab1");
    	tab1.setIndicator(getString(R.string.ds_counter), getResources().getDrawable(R.drawable.tab_circle));
    	tab1.setContent(R.id.table1_1);
    	tabs.addTab(tab1);
    	// 初期表示のタブ設定
    	tabs.setCurrentTab(0);

    	//データ取得
    	getDetailData();

    	if(button.equals("1")){
    		if(count.equals("0")){
    			timecount = "Unknown 0";
    		}
    		
    		Pattern pt = Pattern.compile(",");
    		String[] str = pt.split(timecount);
    	
    		//TableLayout形式で時刻とカウント数を表示
    		for(String s: str){
    			Pattern pt2 = Pattern.compile(" ");
    			String[] str2 = pt2.split(s);
    		
    			TableLayout tl = (TableLayout)findViewById(R.id.table1_1);
    			TableRow tr = new TableRow(this);
    			tr.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    			//tr.setGravity(Gravity.CENTER);
    			
    			/*
    			LinearLayout ll = new LinearLayout(this);
    			ll.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
    			ll.setOrientation(LinearLayout.HORIZONTAL);
    			*/
    			
    			TextView tv1 = new TextView(this);
    			tv1.setText(str2[0]);
    			TextView tv2 = new TextView(this);
    			tv2.setText(str2[1]);
    			tr.addView(tv1);
    			tr.addView(tv2);
    			tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
    		}

    	}else{	/*button=2*/
        	//ボタンB用のタブ設定
        	TabSpec tab2 = tabs.newTabSpec("tab2");
        	tab2.setIndicator(getString(R.string.ds_counter), getResources().getDrawable(R.drawable.tab_cross));
        	tab2.setContent(R.id.table2_1);
        	tabs.addTab(tab2);
        	
    		if(count.equals("0,0")){
    			timecount = "Unknown 0:::Unknown 0";
    		}
    		
    		//まずボタンA、Bの保存データを分ける
    		String[] button_str = timecount.split(":::");

    		if(button_str[0].equals("")){
    			button_str[0] = "Unknown 0";
    		}
    		//button_str[1].equals("")だと例外が発生したため、以下とする
    		else if(button_str.length == 1){
    			timecount = button_str[0] + ":::" + "Unknown 0";
    			button_str = timecount.split(":::");
    		}
    		
    		//次にボタンA、Bそれぞれのデータを表示する
    		String[] str_a = button_str[0].split(",");
    		//TableLayout形式で時刻とカウント数を表示
    		for(String s: str_a){
    			Pattern pt2 = Pattern.compile(" ");
    			String[] str_a2 = pt2.split(s);

    			TableLayout tl = (TableLayout)findViewById(R.id.table1_1);
    			TableRow tr = new TableRow(this);
    			TextView tv1 = new TextView(this);
    			tv1.setText(str_a2[0]);
    			TextView tv2 = new TextView(this);
    			tv2.setText(str_a2[1]);
    			tr.addView(tv1);
    			tr.addView(tv2);
    			tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    														LayoutParams.WRAP_CONTENT));
    		}

    		//ボタンB
    		String[] str_b = button_str[1].split(",");
    		//TableLayout形式で時刻とカウント数を表示
    		for(String s: str_b){
    			Pattern pt2 = Pattern.compile(" ");
    			String[] str_b2 = pt2.split(s);
    		
    			TableLayout tl = (TableLayout)findViewById(R.id.table2_1);
    			TableRow tr = new TableRow(this);
    			TextView tv1 = new TextView(this);
    			tv1.setText(str_b2[0]);
    			TextView tv2 = new TextView(this);
    			tv2.setText(str_b2[1]);
    			tr.addView(tv1);
    			tr.addView(tv2);
    			tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    														LayoutParams.WRAP_CONTENT));
    		}
    	}

    	AdMaker = (libAdMaker)findViewById(R.id.admakerview);
        AdMaker.siteId = "1401";
        AdMaker.zoneId = "5936";
        AdMaker.setUrl("http://images.ad-maker.info/apps/izwc6d3n6s4l.html");
        AdMaker.setBackgroundColor(Color.TRANSPARENT);
        AdMaker.start();
    }
    
    private void getDetailData(){
    	//DBを取得
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	String query = "select * from counter where rowid = ?;";
    	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid)});

    	c.moveToFirst();
		title = c.getString(1);
		count = c.getString(2);
		date = c.getString(3);
		//place = c.getString(4);
		timecount = c.getString(5);
		button = c.getString(6);
		
		c.close();
    }

    //オプションメニューの作成
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(Counter.CHARGE_FLAG){
        	//オプションメニュー項目作成(「エクスポート」)
        	MenuItem saveItem = menu.add(0, MENU_EXPORT, 0 ,R.string.option_export);
        	saveItem.setIcon(android.R.drawable.ic_menu_upload);
        }

        //オプションメニュー項目作成(「削除」)
        MenuItem clearItem = menu.add(0, MENU_REMOVE, 0 ,R.string.option_remove);
        clearItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_EXPORT:
    		export();
    		break;
    		
    	case MENU_REMOVE:
    		remove();
    		break;

    	default:
    		//何もしない
    	}

    	return true;
    }
    
    private void export(){
    	//CSVまたはメールでエクスポート
		new AlertDialog.Builder(DataState.this)
		.setTitle("\"" + title + "\"" + getString(R.string.dm_dialog_export))
		.setItems(R.array.dm_howtoexport, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0://メールで送信
					exportByMail();
					break;
				case 1://SDに書込み
					exportToSD();
					break;
				case 2://キャンセル
					//何もしない
					break;
				}
			}
		}).show();    	
    }

    
    private void exportByMail(){
		/*当初はCSVを予定していたが、GmailがCSVファイルの添付に対応しておらず、
		 * 送信時に添付されない現象が発生したため、断念。
    	FileOutputStream fos = null;
    	BufferedWriter out = null;
    	
    	try{
    		//CSVファイルの作成
    		fos = this.openFileOutput("data.csv", 0);
    		out = new BufferedWriter(new OutputStreamWriter(fos));
    		out.write("題名,カウント数,日時");
    		out.write(title + "," + count + "," + date);
    		out.write(System.getProperty("line.separator"));
    		out.flush();

    	}catch(FileNotFoundException fe){
    		//とりあえず省略
        	new AlertDialog.Builder(this)
        	.setTitle("ファイル作成失敗")
        	.setMessage("ファイル作成に失敗しました")
        	.setPositiveButton("はい", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			//何もしない
        		}
        	})
    		.show();        		

    	}catch(IOException ie){
    		//同じく省略
        	new AlertDialog.Builder(this)
        	.setTitle("ファイル作成失敗")
        	.setMessage("ファイル作成に失敗しました")
        	.setPositiveButton("はい", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			//何もしない
        		}
        	})
    		.show();        		
    	}
    	
    	//CSVファイルをメールで送信
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send CSV file");
    	intent.putExtra(Intent.EXTRA_TEXT, "send " + "[" + title + "]");
    	intent.setType("text/csv");
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///data/data/com.sample.counter/files/data.csv"));
    	startActivity(intent);
    	*/
    	
    	String sendstr = getExportString();
		
		//データをメールで送信
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	//メアドが設定画面で設定されている場合は設定
    	String ad = CounterPreference.getMailAddress(this);
    	if(ad != null){
    		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ad});
    	}
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send \"" + title + "\" data");
    	intent.putExtra(Intent.EXTRA_TEXT, sendstr);
    	//これがないと落ちる
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
    
    private void exportToSD(){
    	String exstr = getExportString();

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
    
    private String getExportString(){
		String crlf = System.getProperty("line.separator");

		String exstr;
		if(button.equals("2")){
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
			/*
			+ "," + place;
			*/

			exstr += crlf + crlf;
			exstr += getString(R.string.dm_time_count) + crlf;

			Pattern pt = Pattern.compile(",");
			String[] str = pt.split(timecount);

			for(String s: str){
				String tmp = s.replace(" ", ",");
				exstr += tmp + crlf;
			}
		}

		return exstr;
    }
    
    private void remove(){
    	//確認ダイアログの表示
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.dm_delete)
    	.setMessage("\"" + title + "\" " + getString(R.string.dm_delete_confirm))
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//保存データ削除処理
				removeData();

				//Activityを終了し、前画面に戻る
				finish();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		})
		.show();    
    }
    
    private void removeData(){
    	//DB取得
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	//データを削除
    	db.delete("counter", "rowid = ?", new String[]{Integer.toString(dbid)});
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

	public void onTabChanged(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
    protected void onPause(){
    	super.onPause();
    	if(AdMaker != null){
        	AdMaker.stop();
    	}
    }
    
    protected void onRestart(){
    	super.onRestart();
    	if(AdMaker != null){
    		AdMaker.start();
    	}
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    	if(AdMaker != null){
    		AdMaker.destroy();
        	AdMaker = null;
    	}
    }
}
