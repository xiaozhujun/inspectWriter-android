package com.csei.entity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.csei.exception.ArgumentException;

public class Employer implements Listable{
	private final String cardType = "0x01";
	private String role;
	private String roleNum;
	private String name;
	private String number;
	
	private final int PROPERTY_COUNT = 5;
	
	public static final Parcelable.Creator<Employer> CREATOR = new Parcelable.Creator<Employer>() { 
        public Employer createFromParcel(Parcel p) { 
            return new Employer(p); 
        } 
 
        public Employer[] newArray(int size) { 
            return new Employer[size]; 
        } 
    }; 
    
    public Employer(){}
	

	public Employer(Parcel p){
		this.role = p.readString();
		this.roleNum = p.readString();
		this.name = p.readString();
		this.number = p.readString();
	}
	

	public Employer(String data) throws ArgumentException{
		if(data!=null){
			String[] d = data.split(",");
			name = d[1];
			number = d[2];
			role = d[3];
			roleNum = d[4];
				
		}else{
			throw new ArgumentException("参数不正确");
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(role);
		dest.writeString(roleNum);
		dest.writeString(name);
		dest.writeString(number);
	}
	
	@Override
	public void setByList(List<String> params) throws ArgumentException{
		if(params.size()==PROPERTY_COUNT){
			this.role = params.get(1);
			this.roleNum = params.get(2);
			this.name = params.get(3);
			this.number = params.get(4);
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
		params.add(role);
		params.add(roleNum); 
		params.add(name);
		params.add(number);
		
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

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getRoleNum() {
		return roleNum;
	}
	public void setRoleNum(String roleNum) {
		this.roleNum = roleNum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}

	

		

}
