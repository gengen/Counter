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
	//counter�e�[�u����ID���X�g
	Integer[] dbid = null;
	//�ۑ��f�[�^���X�g
	String[] savedata = null;

	//for mediba ab
	private MasAdView mAd = null;
	
	ArrayList<DataList> mDataArray = null; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //��ʍ쐬
        setDataDisplay();
        //�ۑ��f�[�^�\��
        displaySaveData();
    }
    
    private void setDataDisplay(){
		setContentView(R.layout.list);
		
        mAd = (MasAdView)findViewById(R.id.adview);
        mAd.setAuid("112070");
        mAd.start();
    }
    
    //�ۑ��f�[�^�\������
    private void displaySaveData(){
		//DB�擾
    	if(dbhelper == null){
    		dbhelper = new DatabaseHelper(this);
    	}
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	String query = "select * from counter;";
    	Cursor c = db.rawQuery(query, null);
    	
    	mDataArray = new ArrayList<DataList>();
    	
		//�^�C�g�����쐬
		DataList titleList = new DataList();
		titleList.setName(getString(R.string.cnt_save_title));
		titleList.setCount(getString(R.string.dm_count_num));
        titleList.setDate(getString(R.string.dm_savetime));
        titleList.setDBID(-9999);
		mDataArray.add(titleList);

    	//�ۑ��f�[�^�����擾
    	int rowcount = c.getCount();
    	if(rowcount == 0){
        	//�ۑ��f�[�^���Ȃ��ꍇ
    		savedata = new String[1];
    		savedata[0] = new String("No data");

    	}else{
    		dbid = new Integer[rowcount];
    		savedata = new String[rowcount];
    		//�~���ɕ��ׂ�
    		c.moveToLast();

    		//���ׂẴf�[�^��z��ɏ�������
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

				//�\�����郊�X�g���쐬
				DataList list = new DataList();
				list.setName(title);
				list.setCount(countStr);
                list.setDate(dstr[0]);
				mDataArray.add(list);
				
    			//�~���ɕ��ׂ�
    			c.moveToPrevious();
    		}
    	}
    	
    	c.close();

		//�ۑ��f�[�^�̕\��
		ListView lv = (ListView)this.findViewById(R.id.widget42);
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, savedata);
		DataArrayAdapter adapter = new DataArrayAdapter(this, android.R.layout.simple_list_item_1, mDataArray);
		lv.setAdapter(adapter);

		//�f�[�^�I�����̃��X�i��o�^
		lv.setOnItemClickListener(new ClickAdapter());
		lv.setOnItemLongClickListener(new LongClickAdapter());
    }
    
    private class ClickAdapter implements OnItemClickListener{
        public void onItemClick(AdapterView<?> adapter,
                View view, int position, long id) {
        	//�f�[�^���Ȃ��ꍇ�͉������Ȃ�
        	if(savedata == null || savedata[0].equalsIgnoreCase("No data")){
        		return;
        	}

        	//intent�쐬
        	Intent intent = new Intent(DataManager.this, DataState.class);
        	//�I�����ڂ�DB��ID��n��
        	intent.putExtra("id", dbid[position-1]);//position����^�C�g������1������
        	//intent�𔭍s���J�E���g�̏ڍ׉�ʂ�\��
        	startActivityForResult(intent, REQUEST_CODE);
        }
    }
    
    private class LongClickAdapter implements OnItemLongClickListener{
    	int position = -1;
    	
		public boolean onItemLongClick(AdapterView<?> adapter, View view,
				int pos, long id) {
        	//�f�[�^���Ȃ��ꍇ�͉������Ȃ�

			if(savedata == null || savedata[0].equalsIgnoreCase("No data")){
        		return true;
        	}
			
			//�^�C�g����������
			position = pos-1;

			if(Counter.CHARGE_FLAG){
				new AlertDialog.Builder(DataManager.this)
				.setTitle(R.string.dm_dialog_select)
				.setItems(R.array.dm_context_charge, new DialogInterface.OnClickListener() {
				
					public void onClick(DialogInterface dialog, int item) {
						switch(item){
						case 0://�G�N�X�|�[�g
							export(position);
							break;
						case 1://�폜
							delete(position);
							break;
						case 2://�L�����Z��
							//�������Ȃ�
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
						case 0://�폜
							delete(position);
							break;
						case 1://�L�����Z��
							break;
						}
					}
				}).show();				
			}
			
			return true;
		}
    }

    //�������ŃG�N�X�|�[�g���I�����ꂽ�ۂ̏���
    private void export(int position){
    	Pattern pt = Pattern.compile(System.getProperty("line.separator"));
    	String[] str = pt.split(savedata[position]);
    	final String title = str[0];
    	final int pos = position;
    	
    	//CSV�܂��̓��[���ŃG�N�X�|�[�g�\��
		new AlertDialog.Builder(DataManager.this)
		.setTitle("\"" + title + "\"" + getString(R.string.dm_dialog_export))
		.setItems(R.array.dm_howtoexport, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0://���[���ő��M
					exportByMail(title, pos);
					break;
				case 1://SD�ɏ�����
					exportToSD(title, pos);
					break;
				case 2://�L�����Z��
					//�������Ȃ�
					break;
				}
			}
		}).show();    	
    }
    
    String getExportString(String title, int position){
    	//DB���擾
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
    	//�Ƃ肠�����A�����ł̊m�F�_�C�A���O�͍폜
    	/*
    	//�m�F�_�C�A���O�̕\��
    	new AlertDialog.Builder(this)
    	.setTitle("���[�����M")
    	.setMessage("�u" + title + "�v�����[���ő��M���܂����H")
    	.setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�G�N�X�|�[�g����
				exportData(title, pos);
			}
		})
		.setNegativeButton("������", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�������Ȃ�
			}
		})
		.show();    
    	*/

    	String sendstr = getExportString(title, position);
    	
		//�f�[�^�����[���ő��M
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	//���A�h���ݒ��ʂŐݒ肳��Ă���ꍇ�͐ݒ�
    	String ad = CounterPreference.getMailAddress(this);
    	if(ad != null){
    		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ad});
    	}
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send \"" + title + "\" data");
    	intent.putExtra(Intent.EXTRA_TEXT, sendstr);
    	//���ꂪ�Ȃ��Ɨ�����̂Őݒ�
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
    
    private void exportToSD(String title, int position){
        File file = new File(Environment.getExternalStorageDirectory(), "/Counter");

        try{
            file.mkdir();
            File savefile = new File(file.getPath(), getCurrentDate() + ".txt");
        	FileOutputStream fos = new FileOutputStream(savefile);
        	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        	BufferedWriter bw = new BufferedWriter(osw);
        	//SD�ւ̏�����
        	bw.write(getExportString(title, position));
        	bw.flush();
        	bw.close();
        	
        	Toast.makeText(this, R.string.dm_export_sd_ok, Toast.LENGTH_SHORT).show();

        }catch(Exception e){
        	Toast.makeText(this, R.string.dm_export_sd_ng, Toast.LENGTH_SHORT).show();        	
        }
    }
    
    //�������ō폜���I�����ꂽ�ۂ̏���
    private void delete(int position){
    	Pattern pt = Pattern.compile(System.getProperty("line.separator"));
    	String[] str = pt.split(savedata[position]);
    	String title = str[0];
    	
    	final int pos = position;
    	
    	//�m�F�_�C�A���O�̕\��
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.dm_delete)
    	.setMessage("\"" + title + "\" " + getString(R.string.dm_delete_confirm))
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�ۑ��f�[�^�폜����
				removeData(pos);
				displaySaveData();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�������Ȃ�
			}
		})
		.show();    
    }
    
    private void removeData(int position){
    	//DB�擾
    	DatabaseHelper helper = new DatabaseHelper(this);
    	SQLiteDatabase db = helper.getWritableDatabase();
    	//�f�[�^���폜
    	db.delete("counter", "rowid = ?", new String[]{Integer.toString(dbid[position])});
    }
    
    //�Ăяo����intent�I�����̏���
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	//�f�[�^��\��
    	if(requestCode == REQUEST_CODE){
    		displaySaveData();
    	}
    }

    //�I�v�V�������j���[�̍쐬
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(Counter.CHARGE_FLAG){
        	//�I�v�V�������j���[���ڍ쐬(�u�S�G�N�X�|�[�g�v)
        	MenuItem saveItem = menu.add(0, MENU_EXPORT_ALL, 0 ,R.string.option_export_all);
        	saveItem.setIcon(android.R.drawable.ic_menu_upload);
        }

        //�I�v�V�������j���[���ڍ쐬(�u�S�폜�v)
        MenuItem clearItem = menu.add(0, MENU_REMOVE_ALL, 0 ,R.string.option_remove_all);
        clearItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }
    
    //�I�v�V�������j���[�I�����̃��X�i
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
    		//�������Ȃ�
    	}

    	return true;
    }
    
    private void exportAll(){
    	//CSV�܂��̓��[���ŃG�N�X�|�[�g
		new AlertDialog.Builder(DataManager.this)
		.setTitle(R.string.dm_dialog_export_all)
		.setItems(R.array.dm_howtoexport, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				switch(item){
				case 0://���[���ő��M
					exportByMailAll();
					break;
				case 1://SD�ɏ�����
					exportToSDAll();
					break;
				case 2://�L�����Z��
					//�������Ȃ�
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

    	//���[���̃e�L�X�g��ݒ�
    	for(int i = 0; i < dbid.length; i++){
        	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid[i])});
        	c.moveToFirst();
        	String title = c.getString(1);
        	String tmpstr = getExportString(title, i);
    		sendstr += crlf + tmpstr;
    		sendstr += crlf + crlf;
    		c.close();
    	}

    	//intent���쐬
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	//���A�h���ݒ��ʂŐݒ肳��Ă���ꍇ�͐ݒ�
    	String ad = CounterPreference.getMailAddress(this);
    	if(ad != null){
    		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ad});
    	}
    	intent.putExtra(Intent.EXTRA_SUBJECT, "send All data");
    	intent.putExtra(Intent.EXTRA_TEXT, sendstr);
    	//���ꂪ�Ȃ��Ɨ�����̂Œǉ�
    	intent.setType("plain/text");
    	//intent�𔭍s���A���[�����N��
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

    private void exportToSDAll(){
    	String exstr = "";
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	String query = "select * from counter where rowid = ?;";
		String crlf = System.getProperty("line.separator");

    	//SD�ɏ������ޕ������ݒ�
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
        	//SD�ւ̏�����
        	bw.write(exstr);
        	bw.flush();
        	bw.close();
        	
        	Toast.makeText(this, R.string.dm_export_sd_ok, Toast.LENGTH_SHORT).show();

        }catch(Exception e){
        	Toast.makeText(this, R.string.dm_export_sd_ng, Toast.LENGTH_SHORT).show();        	
        }
    }
    
    
    private void removeAll(){
    	//�m�F�_�C�A���O�̕\��
    	new AlertDialog.Builder(this)
    	.setTitle(R.string.dm_delete_all)
    	.setMessage(R.string.dm_delete_confirm_all)
    	.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�ۑ��f�[�^�S�폜����
				removeAllData();
				displaySaveData();
			}
		})
		.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//�������Ȃ�
			}
		})
		.show();
    }
    
    private void removeAllData(){
       	//DB�擾
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
