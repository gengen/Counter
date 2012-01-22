package org.g_okuyama.counter2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;

public class CounterPreference extends PreferenceActivity 
		implements OnPreferenceChangeListener{
	
	String precount;
	static final int RESULT_CODE = 1;
	ListPreference listpref;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //設定画面の作成
        if(Counter.CHARGE_FLAG){
        	this.addPreferencesFromResource(R.xml.preference_charge);
            //現在のエクスポート用メアドをサマリに表示
            String mail = CounterPreference.getMailAddress(this);
            Preference mailpref = this.findPreference("mail_setting");
            mailpref.setOnPreferenceChangeListener(this);
            if(mail != null){
            	mailpref.setSummary((CharSequence)mail);
            }
        }else{
        	this.addPreferencesFromResource(R.xml.preference);
        }
        
        CharSequence cs = getText(R.string.button_set);
        listpref = (ListPreference)findPreference(cs);
        listpref.setOnPreferenceChangeListener(this);

        precount = CounterPreference.getButtonNum(this);
        if(precount != null){
        	listpref.setSummary(precount);
        }        
    }
    
    //サウンド設定の取得
    public static boolean isSound(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
				.getBoolean("sound_setting", true);
    }
    
    //バイブレータ設定の取得
    public static boolean isVibration(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
    			.getBoolean("vib_setting", true);
    }
    
    //GPS設定の取得
    /*
    public static boolean isGPS(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
				.getBoolean("gps_setting", false);
    }
    */
    
    //メールアドレス設定の取得
    public static String getMailAddress(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
				.getString("mail_setting", null);    	
    }
    
    public static String getButtonNum(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
    			.getString("button_set", null);
    }

    //ボタン数変更リスナ
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		final CharSequence value = (CharSequence)newValue;
		
		if(pref.getKey().equals("button_set")){
			//確認ダイアログの表示
			new AlertDialog.Builder(this)
			.setTitle(R.string.cp_change_button)
			.setMessage(R.string.cp_change_confirm)
			.setPositiveButton(R.string.cnt_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.putExtra("button_num", value);
					CounterPreference.this.setResult(RESULT_CODE, intent);
					finish();
				}
			})
			.setNegativeButton(R.string.cnt_ng, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//前回の設定を設定する
					listpref.setValue(precount);
					//return;
				}
			})
			.show();

		}else if(pref.getKey().equals("mail_setting")){
	        pref.setSummary((CharSequence)newValue);
		}
		
		return true;
	}
}
