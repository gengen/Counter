package org.g_okuyama.counter2;

import java.util.*;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
//import android.location.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;

import android.util.Log;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.view.*;

import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.Typeface;

import jp.co.nobot.libAdMaker.libAdMaker;

public class Counter extends Activity {
	public static final boolean CHARGE_FLAG = false;
	public static final String TAG = "Counter";
	public static final String HP_URL = "http://neging01.web.fc2.com/android/counter/top.html";
	
	int count1 = 0;
	//ボタン2個用
	int count2 = 0;
	static final int MENU_SAVE_DATA = 0;
	static final int MENU_CLEAR_DATA = 1;
	static final int MENU_DISP_SAVE_DATA = 2;
	static final int MENU_DISP_PREF = 3;
	static final int MENU_DISP_HELP = 4;
	static final int REQUEST_CODE = 1;
	DatabaseHelper dbhelper;
	
	static LinkedHashMap<String, Integer> timecount1 = null;
	static LinkedHashMap<String, Integer> timecount2 = null;
	
	//ボタンクリック時サウンド
	private SoundPool sound;
	private int soundid;

	//現在のボタン数
	private String currentNum;
	//クロノメータ
	private Chronometer chronometer;
	
	//DB作成時のメモ用オブジェクト
	EditText edittext;
	
	private String pretime1 = "";
	private String pretime2 = "";
	
	//for admaker
	private libAdMaker AdMaker = null;
	
	/*
	//位置情報保存用オブジェクト
	LocationManager locationManager = null;
	//位置情報取得用リスナ
	LocationListener locationListener = new LocationListener(){
		public void onLocationChanged(Location arg0) {
		}
		public void onProviderDisabled(String provider) {
		}
		public void onProviderEnabled(String provider) {
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}    		
	};
	*/
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //DBヘルパの作成
        dbhelper = new DatabaseHelper(this);

        //時刻保存用HashMap
        timecount1 = new LinkedHashMap<String, Integer>();
        timecount2 = new LinkedHashMap<String, Integer>();
        
        //ボタンクリック時サウンドロード
        sound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundid = sound.load(this, R.raw.countsound, 1);
        
        currentNum = CounterPreference.getButtonNum(this);
        if(currentNum == null){
        	//default
        	currentNum = "1";
        }
        
        //メイン画面の設定
        setMainDisplay();
    }
    
    private void setMainDisplay(){
    	if(currentNum.equals("1")){
            setContentView(R.layout.main);    		
    	}else if(currentNum.equals("2")){
    		setContentView(R.layout.main2);
    	}else{
    		Log.v(TAG, currentNum);
    	}

    	//カウンタボタンのリスナ登録
    	ImageButton btn1 = (ImageButton)this.findViewById(R.id.button1);
        btn1.setOnTouchListener(new OnTouchListener(){
            public boolean onTouch(View v, MotionEvent me) {
                if(me.getAction() == MotionEvent.ACTION_DOWN){
                    action();
                    count1();
                }
                return true;
            }
        });
        
        //フォント設定
        TextView tv1 = (TextView)this.findViewById(R.id.count1);
        tv1.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/7barPBd.TTF"));

    	if(currentNum.equals("2")){
    		ImageButton btn2 = (ImageButton)this.findViewById(R.id.button2);
            btn2.setOnTouchListener(new OnTouchListener(){
                public boolean onTouch(View v, MotionEvent me) {
                    if(me.getAction() == MotionEvent.ACTION_DOWN){
                        action();
                        count2();
                    }
                    return true;
                }
            });
            
            //フォント設定
            TextView tv2 = (TextView)this.findViewById(R.id.count2);
            tv2.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/7barPBd.TTF"));
    	}
    	//クロノメータ設定
        chronometer = (Chronometer)findViewById(R.id.chrono);
        
        AdMaker = (libAdMaker)findViewById(R.id.admakerview);
        AdMaker.siteId = "1401";
        AdMaker.zoneId = "5936";
        AdMaker.setUrl("http://images.ad-maker.info/apps/izwc6d3n6s4l.html");
        AdMaker.setBackgroundColor(Color.TRANSPARENT);
        AdMaker.start();
    }
    
    private void action(){
        //サウンド設定が有効の場合は音を鳴らす
        if(CounterPreference.isSound(this)){
            sound.play(soundid, 100, 100, 1, 0, 2);
        }

        //バイブレータ設定が有効の場合はバイブレータを動作させる
        if(CounterPreference.isVibration(this)){
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(30);
        }        
    }
    
    //カウンタを増やす
    private void count1(){
    	//クロノメータ開始
    	if((currentNum.equals("1") && count1 == 0)
    			|| (currentNum.equals("2") && (count1 == 0 && count2 ==0))){
    		//クリアしてからスタートする
    		chronometer.setBase(SystemClock.elapsedRealtime());
        	chronometer.start();
    	}
    	
    	//カウント数を増やす
    	TextView tv = (TextView)this.findViewById(R.id.count1);
    	tv.setText(Integer.toString(++count1));
    	
    	//日時(分単位)とカウント数を紐付けて格納
    	String time = getTime();
    	//if(timecount1.containsKey(time)){
    	if(time.equals(pretime1)){
    		int value = timecount1.get(time);
    		timecount1.put(time, ++value);

    	}else{
    	    while(true){
    	        if(pretime1.equals("")){
    	            break;//while(true)
    	        }
    	        String[] cur = time.split(":");
    	        int h = Integer.parseInt(cur[0]);
    	        int m = Integer.parseInt(cur[1]);
    	        
                String[] pre = pretime1.split(":");
    	        int preh = Integer.parseInt(pre[0]);
    	        int prem = Integer.parseInt(pre[1]);
    	    
    	        if(h == preh && prem - m == 1){
    	            break;//while(true)
    	        }
    	        
    	        if(h == preh && prem - m != 1){
                    //minuteを0で埋める
    	            for(int i = prem + 1; i < m; i++){
    	                timecount1.put(h + ":" + i, 0);
    	            }
    	            break;//while(true)
    	        }
    	        
    	        if(h != preh){
                    //前回hourのminuteを0で埋める
                    for(int i = prem + 1; i < 60; i++){
                        timecount1.put(preh + ":" + i, 0);
                    }
                    
                    //hourをまたぐ場合(1時間)
    	            if(h - preh == 1 || (h + 24) - preh == 1){
                        //現在hourのminuteを0で埋める
                        for(int i = 0; i < m; i++){
                            timecount1.put(h + ":" + i, 0);
                        }
                        break;//while(true)
    	            }
                    //hourをまたぐ場合(2時間以上)←これはいるか疑問	            
    	            else{
    	            	int i = preh + 1;
    	            	if((preh < 24) && (h >= 0)){
    	            		h += 24;
    	            	}
    	                
    	                while(h > i){
                            for(int j = 0; j < 60; j++){
                            	if(i >= 24){
                            		timecount1.put((i-24) + ":" + j, 0); 
                            	}
                            	else{
                            		timecount1.put(i + ":" + j, 0);
                            	}
                            }
                            i++;
    	                }

    	                //現在hourのminuteを0で埋める
                        for(int j = 0; j < m; j++){
                        	if(h >= 24){
                        		timecount1.put((h-24) + ":" + j, 0);
                        	}
                        	else{
                        		timecount1.put(h + ":" + j, 0);
                        	}
                        }
                        break;//while(true)
    	            }
    	        }
    	        break;
    	    }
    	    
    		timecount1.put(time, 1);
    	}
    	pretime1 = time;
    	
    	if(currentNum.equals("2")){
    		if(pretime2.equals("")){
    			timecount2.put(time, 0);
    			pretime2 = time;
    		}
    	}
    }

    //カウンタを増やす
    private void count2(){
    	//クロノメータ開始
    	if(currentNum.equals("2") && (count1 == 0 && count2 ==0)){
    		//クリアしてからスタートする
    		chronometer.setBase(SystemClock.elapsedRealtime());
        	chronometer.start();
    	}
    	
    	//カウント数を増やす
    	TextView tv = (TextView)this.findViewById(R.id.count2);
    	tv.setText(Integer.toString(++count2));
    	
    	//日時(分単位)とカウント数を紐付けて格納
    	String time = getTime();
    	//if(timecount2.containsKey(time)){
    	if(time.equals(pretime2)){
    		int value = timecount2.get(time);
    		timecount2.put(time, ++value);

    	}else{
    	    while(true){
    	        if(pretime2.equals("")){
    	            break;//while(true)
    	        }
    	        String[] cur = time.split(":");
    	        int h = Integer.parseInt(cur[0]);
    	        int m = Integer.parseInt(cur[1]);
    	        
                String[] pre = pretime2.split(":");
    	        int preh = Integer.parseInt(pre[0]);
    	        int prem = Integer.parseInt(pre[1]);
    	    
    	        if(h == preh && prem - m == 1){
    	            break;//while(true)
    	        }
    	        
    	        if(h == preh && prem - m != 1){
                    //minuteを0で埋める
    	            for(int i = prem + 1; i < m; i++){
    	                timecount2.put(h + ":" + i, 0);
    	            }
    	            break;//while(true)
    	        }
    	        
    	        if(h != preh){
                    //前回hourのminuteを0で埋める
                    for(int i = prem + 1; i < 60; i++){
                        timecount2.put(preh + ":" + i, 0);
                    }
                    
                    //hourをまたぐ場合(1時間)
    	            if(h - preh == 1 || (h + 24) - preh == 1){
                        //現在hourのminuteを0で埋める
                        for(int i = 0; i < m; i++){
                            timecount2.put(h + ":" + i, 0);
                        }
                        break;//while(true)
    	            }
                    //hourをまたぐ場合(2時間以上)←これはいるか疑問	            
    	            else{
    	            	int i = preh + 1;
    	            	if((preh < 24) && (h >= 0)){
    	            		h += 24;
    	            	}
    	                
    	                while(h > i){
                            for(int j = 0; j < 60; j++){
                            	if(i >= 24){
                            		timecount2.put((i-24) + ":" + j, 0); 
                            	}
                            	else{
                            		timecount2.put(i + ":" + j, 0);
                            	}
                            }
                            i++;
    	                }

    	                //現在hourのminuteを0で埋める
                        for(int j = 0; j < m; j++){
                        	if(h >= 24){
                        		timecount2.put((h-24) + ":" + j, 0);
                        	}
                        	else{
                        		timecount2.put(h + ":" + j, 0);
                        	}
                        }
                        break;//while(true)
    	            }
    	        }
    	        break;
    	    }
    	    
    		timecount2.put(time, 1);    		
    	}
    	pretime2 = time;
    	
    	if(currentNum.equals("2")){
    		if(pretime1.equals("")){
    			timecount1.put(time, 0);
    			pretime1 = time;
    		}
    	}
    }

    //オプションメニューの作成
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //オプションメニュー項目作成(「保存」)
        MenuItem saveItem = menu.add(0, MENU_SAVE_DATA, 0 ,R.string.option_menu_save);
        saveItem.setIcon(android.R.drawable.ic_menu_save);

        //オプションメニュー項目作成(「カウントクリア」)
        MenuItem clearItem = menu.add(0, MENU_CLEAR_DATA, 0 ,R.string.option_menu_clear);
        clearItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        //オプションメニュー項目作成(「保存データ表示」)
        MenuItem dispItem = menu.add(0, MENU_DISP_SAVE_DATA, 0 ,R.string.option_menu_disp);
        dispItem.setIcon(android.R.drawable.ic_menu_search);

        //オプションメニュー項目作成(「設定」)
        MenuItem prefItem = menu.add(0, MENU_DISP_PREF, 0 ,R.string.option_menu_pref);
        prefItem.setIcon(android.R.drawable.ic_menu_preferences);

        //オプションメニュー項目作成(「ヘルプ」)
        MenuItem helpItem = menu.add(0, MENU_DISP_HELP, 0 ,R.string.option_menu_help);
        helpItem.setIcon(android.R.drawable.ic_menu_help);

        return true;
    }
    
    //オプションメニュー選択時のリスナ
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_SAVE_DATA:
    		save();
    		break;
    		
    	case MENU_CLEAR_DATA:
    		clear();
    		break;
    	
    	case MENU_DISP_SAVE_DATA:
        	//保存データ表示
        	//画面遷移のためintentの発行
        	Intent save_intent = new Intent(this, DataManager.class);
        	startActivity(save_intent);
    		break;
    		
    	case MENU_DISP_PREF:
    		//設定画面表示
    		Intent pref_intent = new Intent(this, CounterPreference.class);
    		startActivityForResult(pref_intent, REQUEST_CODE);
    		break;
    		
    	case MENU_DISP_HELP:
        	//ダイアログを表示(タイトル記載)
        	new AlertDialog.Builder(this)
        	.setIcon(R.drawable.counter)
        	.setTitle(R.string.cnt_info_title)
        	.setMessage(R.string.app_abstract)
        	.setPositiveButton(R.string.cnt_info_web, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {				
   					Uri uri = Uri.parse(HP_URL);
   					Intent i = new Intent(Intent.ACTION_VIEW, uri);
   			    	try{
   	   					startActivity(i);
   			    	}catch(ActivityNotFoundException e){
   			    	}
    			}
    		})
    		.setNegativeButton(R.string.cnt_cancel, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				//キャンセル時は何もせずに戻る
    			}
    		})
    		.show();
    		
    		break;
    		
    	default:
    		//何もしない
    	}

    	return true;
    }    
    //呼び出したintent終了時の処理
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	//データを表示
    	if(data == null){
    		return;
    	}
    	//ボタン数が変更されたときは画面に反映
    	if(requestCode == REQUEST_CODE){
    		String num = data.getStringExtra("button_num");
    		if(!num.equals(currentNum)){
    			clearCounter();
    			//画面変更
    			currentNum = num;
    			setMainDisplay();
    		}
    	}
    }

    //カウンタの保存
    private void save(){
    	saveCounter();
    }
    
    //カウンタ保存処理
    private void saveCounter(){
    	edittext = new EditText(this);    		

    	//ダイアログを表示(タイトル記載)
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.cnt_save_title)
    	.setView(edittext)
    	.setPositiveButton(R.string.cnt_submit, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {				
				String title = edittext.getText().toString();
				if(title.length() == 0){
					new AlertDialog.Builder(Counter.this)
						.setTitle(R.string.cnt_error)
						.setMessage(R.string.cnt_input_req)
						.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								saveCounter();
							}
						}).show();
				}else{
					saveToDB(edittext);
				}
			}
		})
		.setNegativeButton(R.string.cnt_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//キャンセル時は何もせずに戻る
			}
		})
		.show();
    }
    
    private void saveToDB(EditText text){
		//DB取得
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	ContentValues values = new ContentValues();

		//テキストメモ、カウンタ、時刻を設定
    	values.put("title", text.getText().toString());

    	if(currentNum.equals("1")){
    		values.put("count", count1);
    		values.put("button", "1");
    	}else if(currentNum.equals("2")){
    		values.put("count", count1 + "," + count2);
    		values.put("button", "2");
    	}

    	String date = getCurrentDate();
    	values.put("date", date);

    	//hashmapから時刻とカウント数の紐付け情報を取得
    	String setstr = "";
    	Set<String> set = timecount1.keySet();
    	Iterator<String> iterator = set.iterator();

    	while(iterator.hasNext()){
    		String key = (String)iterator.next();
    		String time = null;
        	Pattern pt = Pattern.compile(":");
        	String[] keystr = pt.split(key);
        	//分が1桁の場合は数字の前に0を足して2桁にする
    		if(keystr[1].length() == 1){
    			keystr[1] = "0" + keystr[1];
    			time = keystr[0] + ":" + keystr[1];
    		}else{
    			time = key;
    		}

    		if(setstr.equals("")){
    			setstr = time + " " + timecount1.get(key).toString();
    		}else{
        		setstr = setstr + "," + time + " " + timecount1.get(key);    			
    		}
    	}
    	
    	if(currentNum.equals("2")){
    		String setstr2 = "";
        	Set<String> set2 = timecount2.keySet();
        	Iterator<String> iterator2 = set2.iterator();

        	while(iterator2.hasNext()){
        		String key = (String)iterator2.next();
        		String time = null;
            	Pattern pt = Pattern.compile(":");
            	String[] keystr = pt.split(key);
            	//分が1桁の場合は数字の前に0を足して2桁にする
        		if(keystr[1].length() == 1){
        			keystr[1] = "0" + keystr[1];
        			time = keystr[0] + ":" + keystr[1];
        		}else{
        			time = key;
        		}

        		if(setstr2.equals("")){
        			setstr2 = time + " " + timecount2.get(key).toString();
        		}else{
            		setstr2 = setstr2 + "," + time + " " + timecount2.get(key);    			
        		}
        	}

    		//区切り文字とボタン2分を追加
    		setstr += ":::" + setstr2;
    	}
    	
    	//時刻とカウント数の紐付け情報を格納
    	values.put("timecount", setstr);
    	
    	//位置情報を付与するとき
    	/*
    	if(CounterPreference.isGPS(this) && locationManager != null){
    		String place = getLocationString();
    		if(place == null){
    			Toast.makeText(this, R.string.cnt_fail_location, Toast.LENGTH_SHORT).show();
    			values.put("place", "unknown");    		
    		}else{
    			values.put("place", place);
    		}
    	}else{
    	*/
			values.put("place", "unknown");
		/*
    	}
    	*/

    	db.insert("counter", null, values);

    	//カウンタをクリア
		clearCounter();    	
    }
    
    String getCurrentDate(){
        Calendar cal1 = Calendar.getInstance();
        int year = cal1.get(Calendar.YEAR);
        int month = cal1.get(Calendar.MONTH) + 1;
        int day = cal1.get(Calendar.DATE);
        int hour = cal1.get(Calendar.HOUR_OF_DAY);
        int min = cal1.get(Calendar.MINUTE);
        int sec = cal1.get(Calendar.SECOND);
        
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
        
        return year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second;
    }
    
    String getTime(){
        Calendar cal1 = Calendar.getInstance();
        int hour = cal1.get(Calendar.HOUR_OF_DAY);
        int min = cal1.get(Calendar.MINUTE);
        
        return hour + ":" + min;
    }
    
    /*
    String getLocationString(){
    	//位置情報を設定
    	Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	if(loc == null){
    		return null;
    	}

    	//Geocoder coder = new Geocoder(this, Locale.JAPAN);
    	Geocoder coder = new Geocoder(this);
    	List<Address> ads = null;
    		
    	try{
    		//緯度経度情報から住所を引く。位置情報は1つしか取得しない
    		ads = coder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
    	}catch(IOException e){
    		return null;
    	}

    	String adstr = null;
    	for(Address ad : ads){
    		if(ad.getAddressLine(1) != null){
    			adstr = ad.getAddressLine(1);
    		}else{
    			adstr = ad.getAdminArea() + ad.getLocality();
    		}
    	}

    	return adstr;
    }
    */

    //カウンタのクリア
    private void clear(){
    	
    	if(currentNum.equals("1")){
    		if(count1 == 0){
    			return;
    		}

    	}else if(currentNum.equals("2")){
    		if(count1 == 0 && count2 == 0){
    			return;
    		}
    	}
    	
    	//確認ダイアログの表示
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.cnt_clear)
    	.setMessage(R.string.cnt_clear_confirm)
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//カウンタクリア処理
				clearCounter();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		})
		.show();
    }
    
	//カウンタのクリア
    private void clearCounter(){
    	TextView et = (TextView)this.findViewById(R.id.count1);
    	et.setText(Integer.toString(0));
    	count1 = 0;
    	pretime1 = "";
    	timecount1.clear();

    	if(currentNum.equals("2")){
        	TextView et2 = (TextView)this.findViewById(R.id.count2);
        	et2.setText(Integer.toString(0));
        	count2 = 0;
        	pretime2 = "";
        	timecount2.clear();    	
    	}
    	
    	//クロノメータのクリア。ストップしてからクリアする
    	chronometer.stop();
    	chronometer.setBase(SystemClock.elapsedRealtime());
    }
    
    protected void onResume(){
    	/*位置情報を付与するとき*/
    	/*
    	if(CounterPreference.isGPS(this)){
    		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		//更新頻度は、5分、50mに設定
    		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000000, 50, locationListener);
    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    	}
    	*/
    	super.onResume();
    }
    
    protected void onPause(){
    	//GPSを止める
    	/*
    	if(locationManager != null){
    		locationManager.removeUpdates(locationListener);
    		locationManager = null;
    	}
    	*/
    	super.onPause();
    	if(AdMaker != null){
        	AdMaker.stop();
    	}
    }
    
    protected void onDestroy(){
    	//GPSを止める
    	/*
    	if(locationManager != null){
    		locationManager.removeUpdates(locationListener);
    		locationManager = null;
    	}
    	*/

    	super.onDestroy();
    	if(AdMaker != null){
    		AdMaker.destroy();
        	AdMaker = null;
    	}
    }
    
    protected void onRestart(){
    	super.onRestart();
    	if(AdMaker != null){
    		AdMaker.start();
    	}
    }
    
    public void finish(){
    	//確認ダイアログの表示
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.cnt_finish)
    	.setMessage(R.string.cnt_finish_confirm)
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	//終了処理
				System.exit(RESULT_OK);
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
				return;
			}
		})
		.show();    	
    }
}
