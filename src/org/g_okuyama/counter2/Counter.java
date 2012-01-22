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
	//�{�^��2�p
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
	
	//�{�^���N���b�N���T�E���h
	private SoundPool sound;
	private int soundid;

	//���݂̃{�^����
	private String currentNum;
	//�N���m���[�^
	private Chronometer chronometer;
	
	//DB�쐬���̃����p�I�u�W�F�N�g
	EditText edittext;
	
	private String pretime1 = "";
	private String pretime2 = "";
	
	//for admaker
	private libAdMaker AdMaker = null;
	
	/*
	//�ʒu���ۑ��p�I�u�W�F�N�g
	LocationManager locationManager = null;
	//�ʒu���擾�p���X�i
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

        //DB�w���p�̍쐬
        dbhelper = new DatabaseHelper(this);

        //�����ۑ��pHashMap
        timecount1 = new LinkedHashMap<String, Integer>();
        timecount2 = new LinkedHashMap<String, Integer>();
        
        //�{�^���N���b�N���T�E���h���[�h
        sound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundid = sound.load(this, R.raw.countsound, 1);
        
        currentNum = CounterPreference.getButtonNum(this);
        if(currentNum == null){
        	//default
        	currentNum = "1";
        }
        
        //���C����ʂ̐ݒ�
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

    	//�J�E���^�{�^���̃��X�i�o�^
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
        
        //�t�H���g�ݒ�
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
            
            //�t�H���g�ݒ�
            TextView tv2 = (TextView)this.findViewById(R.id.count2);
            tv2.setTypeface(Typeface.createFromAsset(this.getAssets(), "font/7barPBd.TTF"));
    	}
    	//�N���m���[�^�ݒ�
        chronometer = (Chronometer)findViewById(R.id.chrono);
        
        AdMaker = (libAdMaker)findViewById(R.id.admakerview);
        AdMaker.siteId = "1401";
        AdMaker.zoneId = "5936";
        AdMaker.setUrl("http://images.ad-maker.info/apps/izwc6d3n6s4l.html");
        AdMaker.setBackgroundColor(Color.TRANSPARENT);
        AdMaker.start();
    }
    
    private void action(){
        //�T�E���h�ݒ肪�L���̏ꍇ�͉���炷
        if(CounterPreference.isSound(this)){
            sound.play(soundid, 100, 100, 1, 0, 2);
        }

        //�o�C�u���[�^�ݒ肪�L���̏ꍇ�̓o�C�u���[�^�𓮍삳����
        if(CounterPreference.isVibration(this)){
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(30);
        }        
    }
    
    //�J�E���^�𑝂₷
    private void count1(){
    	//�N���m���[�^�J�n
    	if((currentNum.equals("1") && count1 == 0)
    			|| (currentNum.equals("2") && (count1 == 0 && count2 ==0))){
    		//�N���A���Ă���X�^�[�g����
    		chronometer.setBase(SystemClock.elapsedRealtime());
        	chronometer.start();
    	}
    	
    	//�J�E���g���𑝂₷
    	TextView tv = (TextView)this.findViewById(R.id.count1);
    	tv.setText(Integer.toString(++count1));
    	
    	//����(���P��)�ƃJ�E���g����R�t���Ċi�[
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
                    //minute��0�Ŗ��߂�
    	            for(int i = prem + 1; i < m; i++){
    	                timecount1.put(h + ":" + i, 0);
    	            }
    	            break;//while(true)
    	        }
    	        
    	        if(h != preh){
                    //�O��hour��minute��0�Ŗ��߂�
                    for(int i = prem + 1; i < 60; i++){
                        timecount1.put(preh + ":" + i, 0);
                    }
                    
                    //hour���܂����ꍇ(1����)
    	            if(h - preh == 1 || (h + 24) - preh == 1){
                        //����hour��minute��0�Ŗ��߂�
                        for(int i = 0; i < m; i++){
                            timecount1.put(h + ":" + i, 0);
                        }
                        break;//while(true)
    	            }
                    //hour���܂����ꍇ(2���Ԉȏ�)������͂��邩�^��	            
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

    	                //����hour��minute��0�Ŗ��߂�
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

    //�J�E���^�𑝂₷
    private void count2(){
    	//�N���m���[�^�J�n
    	if(currentNum.equals("2") && (count1 == 0 && count2 ==0)){
    		//�N���A���Ă���X�^�[�g����
    		chronometer.setBase(SystemClock.elapsedRealtime());
        	chronometer.start();
    	}
    	
    	//�J�E���g���𑝂₷
    	TextView tv = (TextView)this.findViewById(R.id.count2);
    	tv.setText(Integer.toString(++count2));
    	
    	//����(���P��)�ƃJ�E���g����R�t���Ċi�[
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
                    //minute��0�Ŗ��߂�
    	            for(int i = prem + 1; i < m; i++){
    	                timecount2.put(h + ":" + i, 0);
    	            }
    	            break;//while(true)
    	        }
    	        
    	        if(h != preh){
                    //�O��hour��minute��0�Ŗ��߂�
                    for(int i = prem + 1; i < 60; i++){
                        timecount2.put(preh + ":" + i, 0);
                    }
                    
                    //hour���܂����ꍇ(1����)
    	            if(h - preh == 1 || (h + 24) - preh == 1){
                        //����hour��minute��0�Ŗ��߂�
                        for(int i = 0; i < m; i++){
                            timecount2.put(h + ":" + i, 0);
                        }
                        break;//while(true)
    	            }
                    //hour���܂����ꍇ(2���Ԉȏ�)������͂��邩�^��	            
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

    	                //����hour��minute��0�Ŗ��߂�
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

    //�I�v�V�������j���[�̍쐬
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //�I�v�V�������j���[���ڍ쐬(�u�ۑ��v)
        MenuItem saveItem = menu.add(0, MENU_SAVE_DATA, 0 ,R.string.option_menu_save);
        saveItem.setIcon(android.R.drawable.ic_menu_save);

        //�I�v�V�������j���[���ڍ쐬(�u�J�E���g�N���A�v)
        MenuItem clearItem = menu.add(0, MENU_CLEAR_DATA, 0 ,R.string.option_menu_clear);
        clearItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        //�I�v�V�������j���[���ڍ쐬(�u�ۑ��f�[�^�\���v)
        MenuItem dispItem = menu.add(0, MENU_DISP_SAVE_DATA, 0 ,R.string.option_menu_disp);
        dispItem.setIcon(android.R.drawable.ic_menu_search);

        //�I�v�V�������j���[���ڍ쐬(�u�ݒ�v)
        MenuItem prefItem = menu.add(0, MENU_DISP_PREF, 0 ,R.string.option_menu_pref);
        prefItem.setIcon(android.R.drawable.ic_menu_preferences);

        //�I�v�V�������j���[���ڍ쐬(�u�w���v�v)
        MenuItem helpItem = menu.add(0, MENU_DISP_HELP, 0 ,R.string.option_menu_help);
        helpItem.setIcon(android.R.drawable.ic_menu_help);

        return true;
    }
    
    //�I�v�V�������j���[�I�����̃��X�i
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_SAVE_DATA:
    		save();
    		break;
    		
    	case MENU_CLEAR_DATA:
    		clear();
    		break;
    	
    	case MENU_DISP_SAVE_DATA:
        	//�ۑ��f�[�^�\��
        	//��ʑJ�ڂ̂���intent�̔��s
        	Intent save_intent = new Intent(this, DataManager.class);
        	startActivity(save_intent);
    		break;
    		
    	case MENU_DISP_PREF:
    		//�ݒ��ʕ\��
    		Intent pref_intent = new Intent(this, CounterPreference.class);
    		startActivityForResult(pref_intent, REQUEST_CODE);
    		break;
    		
    	case MENU_DISP_HELP:
        	//�_�C�A���O��\��(�^�C�g���L��)
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
    				//�L�����Z�����͉��������ɖ߂�
    			}
    		})
    		.show();
    		
    		break;
    		
    	default:
    		//�������Ȃ�
    	}

    	return true;
    }    
    //�Ăяo����intent�I�����̏���
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	//�f�[�^��\��
    	if(data == null){
    		return;
    	}
    	//�{�^�������ύX���ꂽ�Ƃ��͉�ʂɔ��f
    	if(requestCode == REQUEST_CODE){
    		String num = data.getStringExtra("button_num");
    		if(!num.equals(currentNum)){
    			clearCounter();
    			//��ʕύX
    			currentNum = num;
    			setMainDisplay();
    		}
    	}
    }

    //�J�E���^�̕ۑ�
    private void save(){
    	saveCounter();
    }
    
    //�J�E���^�ۑ�����
    private void saveCounter(){
    	edittext = new EditText(this);    		

    	//�_�C�A���O��\��(�^�C�g���L��)
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
				//�L�����Z�����͉��������ɖ߂�
			}
		})
		.show();
    }
    
    private void saveToDB(EditText text){
		//DB�擾
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	ContentValues values = new ContentValues();

		//�e�L�X�g�����A�J�E���^�A������ݒ�
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

    	//hashmap���玞���ƃJ�E���g���̕R�t�������擾
    	String setstr = "";
    	Set<String> set = timecount1.keySet();
    	Iterator<String> iterator = set.iterator();

    	while(iterator.hasNext()){
    		String key = (String)iterator.next();
    		String time = null;
        	Pattern pt = Pattern.compile(":");
        	String[] keystr = pt.split(key);
        	//����1���̏ꍇ�͐����̑O��0�𑫂���2���ɂ���
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
            	//����1���̏ꍇ�͐����̑O��0�𑫂���2���ɂ���
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

    		//��؂蕶���ƃ{�^��2����ǉ�
    		setstr += ":::" + setstr2;
    	}
    	
    	//�����ƃJ�E���g���̕R�t�������i�[
    	values.put("timecount", setstr);
    	
    	//�ʒu����t�^����Ƃ�
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

    	//�J�E���^���N���A
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
        //1�����̏ꍇ��0������
        if(minute.length() == 1){
        	minute = "0" + minute;
        }
        
        String second = Integer.toString(sec);
        //1���b�̏ꍇ��0������
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
    	//�ʒu����ݒ�
    	Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	if(loc == null){
    		return null;
    	}

    	//Geocoder coder = new Geocoder(this, Locale.JAPAN);
    	Geocoder coder = new Geocoder(this);
    	List<Address> ads = null;
    		
    	try{
    		//�ܓx�o�x��񂩂�Z���������B�ʒu����1�����擾���Ȃ�
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

    //�J�E���^�̃N���A
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
    	
    	//�m�F�_�C�A���O�̕\��
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.cnt_clear)
    	.setMessage(R.string.cnt_clear_confirm)
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�J�E���^�N���A����
				clearCounter();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�������Ȃ�
			}
		})
		.show();
    }
    
	//�J�E���^�̃N���A
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
    	
    	//�N���m���[�^�̃N���A�B�X�g�b�v���Ă���N���A����
    	chronometer.stop();
    	chronometer.setBase(SystemClock.elapsedRealtime());
    }
    
    protected void onResume(){
    	/*�ʒu����t�^����Ƃ�*/
    	/*
    	if(CounterPreference.isGPS(this)){
    		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		//�X�V�p�x�́A5���A50m�ɐݒ�
    		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000000, 50, locationListener);
    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    	}
    	*/
    	super.onResume();
    }
    
    protected void onPause(){
    	//GPS���~�߂�
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
    	//GPS���~�߂�
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
    	//�m�F�_�C�A���O�̕\��
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.cnt_finish)
    	.setMessage(R.string.cnt_finish_confirm)
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	//�I������
				System.exit(RESULT_OK);
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�������Ȃ�
				return;
			}
		})
		.show();    	
    }
}
