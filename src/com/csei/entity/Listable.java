package com.csei.entity;

import java.util.List;

import android.os.Parcelable;

import com.csei.exception.ArgumentException;

public interface Listable extends Parcelable{
	public List<String> getParams();
	public void setByList(List<String> params) throws ArgumentException;
	public int getPropertyCount();

}
