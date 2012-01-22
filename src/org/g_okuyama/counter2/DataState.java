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
        
        //intent����I�����ڂ����o��
        Bundle extras = getIntent().getExtras();
        dbid = extras.getInt("id");

        //�J�E���g���̏ڍ׉�ʕ\��
        setDetailDisplay();
    }

    private void setDetailDisplay(){
    	setContentView(R.layout.tabs);

    	// TabHost�̃C���X�^���X���擾
    	TabHost tabs = getTabHost();
    	tabs.setOnTabChangedListener(this);
        
    	TabSpec tab1 = tabs.newTabSpec("tab1");
    	tab1.setIndicator(getString(R.string.ds_counter), getResources().getDrawable(R.drawable.tab_circle));
    	tab1.setContent(R.id.table1_1);
    	tabs.addTab(tab1);
    	// �����\���̃^�u�ݒ�
    	tabs.setCurrentTab(0);

    	//�f�[�^�擾
    	getDetailData();

    	if(button.equals("1")){
    		if(count.equals("0")){
    			timecount = "Unknown 0";
    		}
    		
    		Pattern pt = Pattern.compile(",");
    		String[] str = pt.split(timecount);
    	
    		//TableLayout�`���Ŏ����ƃJ�E���g����\��
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
        	//�{�^��B�p�̃^�u�ݒ�
        	TabSpec tab2 = tabs.newTabSpec("tab2");
        	tab2.setIndicator(getString(R.string.ds_counter), getResources().getDrawable(R.drawable.tab_cross));
        	tab2.setContent(R.id.table2_1);
        	tabs.addTab(tab2);
        	
    		if(count.equals("0,0")){
    			timecount = "Unknown 0:::Unknown 0";
    		}
    		
    		//�܂��{�^��A�AB�̕ۑ��f�[�^�𕪂���
    		String[] button_str = timecount.split(":::");

    		if(button_str[0].equals("")){
    			button_str[0] = "Unknown 0";
    		}
    		//button_str[1].equals("")���Ɨ�O�������������߁A�ȉ��Ƃ���
    		else if(button_str.length == 1){
    			timecount = button_str[0] + ":::" + "Unknown 0";
    			button_str = timecount.split(":::");
    		}
    		
    		//���Ƀ{�^��A�AB���ꂼ��̃f�[�^��\������
    		String[] str_a = button_str[0].split(",");
    		//TableLayout�`���Ŏ����ƃJ�E���g����\��
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

    		//�{�^��B
    		String[] str_b = button_str[1].split(",");
    		//TableLayout�`���Ŏ����ƃJ�E���g����\��
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
    	//DB���擾
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

    //�I�v�V�������j���[�̍쐬
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(Counter.CHARGE_FLAG){
        	//�I�v�V�������j���[���ڍ쐬(�u�G�N�X�|�[�g�v)
        	MenuItem saveItem = menu.add(0, MENU_EXPORT, 0 ,R.string.option_export);
        	saveItem.setIcon(android.R.drawable.ic_menu_upload);
        }

        //�I�v�V�������j���[���ڍ쐬(�u�폜�v)
        MenuItem clearItem = menu.add(0, MENU_REMOVE, 0 ,R.string.option_remove);
        clearItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }
    
    //�I�v�V�������j���[�I�����̃��X�i
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_EXPORT:
    		export();
    		break;
    		
    	case MENU_REMOVE:
    		remove();
    		break;

    	default:
    		//�������Ȃ�
    	}

    	return true;
    }
    
    private void export(){
    	//CSV�܂��̓��[���ŃG�N�X�|�[�g
		new AlertDialog.Builder(DataState.this)
		.setTitle("\"" + title + "\"" + getString(R.string.dm_dialog_export))
		.setItems(R.array.dm_howtoexport, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0://���[���ő��M
					exportByMail();
					break;
				case 1://SD�ɏ�����
					exportToSD();
					break;
				case 2://�L�����Z��
					//�������Ȃ�
					break;
				}
			}
		}).show();    	
    }

    
    private void exportByMail(){
		/*������CSV��\�肵�Ă������AGmail��CSV�t�@�C���̓Y�t�ɑΉ����Ă��炸�A
		 * ���M���ɓY�t����Ȃ����ۂ������������߁A�f�O�B
    	FileOutputStream fos = null;
    	BufferedWriter out = null;
    	
    	try{
    		//CSV�t�@�C���̍쐬
    		fos = this.openFileOutput("data.csv", 0);
    		out = new BufferedWriter(new OutputStreamWriter(fos));
    		out.write("�薼,�J�E���g��,����");
    		out.write(title + "," + count + "," + date);
    		out.write(System.getProperty("line.separator"));
    		out.flush();

    	}catch(FileNotFoundException fe){
    		//�Ƃ肠�����ȗ�
        	new AlertDialog.Builder(this)
        	.setTitle("�t�@�C���쐬���s")
        	.setMessage("�t�@�C���쐬�Ɏ��s���܂���")
        	.setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			//�������Ȃ�
        		}
        	})
    		.show();        		

    	}catch(IOException ie){
    		//�������ȗ�
        	new AlertDialog.Builder(this)
        	.setTitle("�t�@�C���쐬���s")
        	.setMessage("�t�@�C���쐬�Ɏ��s���܂���")
        	.setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			//�������Ȃ�
        		}
        	})
    		.show();        		
    	}
    	
    	//CSV�t�@�C�������[���ő��M
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send CSV file");
    	intent.putExtra(Intent.EXTRA_TEXT, "send " + "[" + title + "]");
    	intent.setType("text/csv");
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///data/data/com.sample.counter/files/data.csv"));
    	startActivity(intent);
    	*/
    	
    	String sendstr = getExportString();
		
		//�f�[�^�����[���ő��M
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	//���A�h���ݒ��ʂŐݒ肳��Ă���ꍇ�͐ݒ�
    	String ad = CounterPreference.getMailAddress(this);
    	if(ad != null){
    		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ad});
    	}
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send \"" + title + "\" data");
    	intent.putExtra(Intent.EXTRA_TEXT, sendstr);
    	//���ꂪ�Ȃ��Ɨ�����
    	intent.setType("plain/text");
    	try{
    		startActivity(intent);
    	}catch(ActivityNotFoundException e){
    		new AlertDialog.Builder(this)
    		.setTitle(R.string.dm_error)
    		.setMessage(R.string.dm_missing_mailer)
    		.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				//�������Ȃ�
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
        	//SD�ւ̏�����
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

			//�{�^�����̋�؂�
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
    	//�m�F�_�C�A���O�̕\��
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.dm_delete)
    	.setMessage("\"" + title + "\" " + getString(R.string.dm_delete_confirm))
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�ۑ��f�[�^�폜����
				removeData();

				//Activity���I�����A�O��ʂɖ߂�
				finish();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�������Ȃ�
			}
		})
		.show();    
    }
    
    private void removeData(){
    	//DB�擾
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	//�f�[�^���폜
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
        //1�����̏ꍇ��0������
        if(month.length() == 1){
        	month = "0" + month;
        }

        String day = Integer.toString(d);
        //1�����̏ꍇ��0������
        if(day.length() == 1){
        	day = "0" + day;
        }

        String hour = Integer.toString(h);
        //1�����̏ꍇ��0������
        if(hour.length() == 1){
        	hour = "0" + hour;
        }
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
