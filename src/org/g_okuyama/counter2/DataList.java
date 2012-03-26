package org.g_okuyama.counter2;

public class DataList {
    private int mDBID;
    private String mName = null;
    private String mCount = null;
    private String mDate = null;
    
    public void setDBID(int id){
        mDBID = id;
    }
    
    public int getDBID(){
        return mDBID;
    }
    
    public void setName(String name){
        mName = name;
    }
    
    public String getName(){
        return mName;
    }
    
    public void setCount(String count){
        mCount = count;
    }
    
    public String getCount(){
        return mCount;
    }
    
    public void setDate(String date){
        mDate = date;
    }
    
    public String getDate(){
        return mDate;
    }
}
