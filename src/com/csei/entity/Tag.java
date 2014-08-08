package com.csei.entity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.csei.exception.ArgumentException;

public class Tag implements Listable{
	private final String cardType = "0x02";
	private String deviceType;
	private String deviceTypeNum;
	private String deviceNum;
	private String tagArea;
	private String tagAreaNum;
	private String number;
	
	public final int PROPERTY_COUNT = 4;
	
	public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() { 
		
        public Tag createFromParcel(Parcel p) { 
            return new Tag(p); 
        } 
 
        public Tag[] newArray(int size) { 
            return new Tag[size]; 
        } 
    }; 
    
    public Tag(){}
	
	public Tag(String data) throws ArgumentException{
		if(data!=null){
			String[] d = data.split(",");
			tagArea = d[1];
			deviceNum = d[3];
			tagAreaNum = d[4];
		}else{
			throw new ArgumentException("参数不正确");
		}
	}

	public Tag(Parcel p){
		this.tagArea = p.readString();
		this.deviceNum = p.readString();
		this.tagAreaNum = p.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(tagArea);
		dest.writeString(deviceNum);
		dest.writeString(tagAreaNum);
	}
	
	@Override
	public void setByList(List<String> params) throws ArgumentException{
		if(params.size()>3){
			this.tagArea = params.get(1);
			this.tagAreaNum = params.get(2);
			this.deviceNum = params.get(3);
			for(int i=4;i<params.size();i++){
				this.deviceNum+=params.get(i);
			}
		}else{
			throw new ArgumentException("构造参数正确");
		}
	}
	
	@Override
	public int getPropertyCount(){
		return PROPERTY_COUNT;
	}
	
	public List<String> getParams(){
		
		ArrayList<String> params = new ArrayList<String>();
		params.add(cardType);
		params.add(tagArea);
		params.add(tagAreaNum);
		
		if(deviceNum.length()<16){
			params.add(deviceNum);
		}else{
			int count=0;
			for(;count<deviceNum.length();){
				if(count+15<=deviceNum.length()){
					params.add(deviceNum.substring(count, count+15));
					count+=15;
				}else{
					params.add(deviceNum.substring(count, deviceNum.length()));
					count=deviceNum.length();
				}
				
			}
		}
		 
		
		return params;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	public String getCardType() {
		return cardType;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceTypeNum() {
		return deviceTypeNum;
	}
	public void setDeviceTypeNum(String deviceTypeNum) {
		this.deviceTypeNum = deviceTypeNum;
	}
	public String getDeviceNum() {
		return deviceNum;
	}
	public void setDeviceNum(String deviceNum) {
		this.deviceNum = deviceNum;
	}
	public String getTagArea() {
		return tagArea;
	}
	public void setTagArea(String tagArea) {
		this.tagArea = tagArea;
	}
	public String getTagAreaNum() {
		return tagAreaNum;
	}
	public void setTagAreaNum(String tagAreaNum) {
		this.tagAreaNum = tagAreaNum;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	
}
