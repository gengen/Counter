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
        //�ݒ��ʂ̍쐬
        if(Counter.CHARGE_FLAG){
        	this.addPreferencesFromResource(R.xml.preference_charge);
            //���݂̃G�N�X�|�[�g�p���A�h���T�}���ɕ\��
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
    
    //�T�E���h�ݒ�̎擾
    public static boolean isSound(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
				.getBoolean("sound_setting", true);
    }
    
    //�o�C�u���[�^�ݒ�̎擾
    public static boolean isVibration(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
    			.getBoolean("vib_setting", true);
    }
    
    //GPS�ݒ�̎擾
    /*
    public static boolean isGPS(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
				.getBoolean("gps_setting", false);
    }
    */
    
    //���[���A�h���X�ݒ�̎擾
    public static String getMailAddress(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
				.getString("mail_setting", null);    	
    }
    
    public static String getButtonNum(Context c){
    	return PreferenceManager.getDefaultSharedPreferences(c)
    			.getString("button_set", null);
    }

    //�{�^�����ύX���X�i
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		final CharSequence value = (CharSequence)newValue;
		
		if(pref.getKey().equals("button_set")){
			//�m�F�_�C�A���O�̕\��
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
					//�O��̐ݒ��ݒ肷��
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
