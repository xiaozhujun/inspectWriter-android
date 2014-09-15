package com.csei.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.hdhe.nfc.NFCcmdManager;
import com.csei.entity.Listable;
import com.csei.exception.ArgumentException;
import com.csei.util.Tools;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera.Area;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class RFIDService extends Service {
	// 区域卡密码
	private static String AREA_PASSWORD = "FFFFFFFFFFFF";
	//private static final String AREA_PASSWORD = "AAAAAAAAAAAA";

	private NFCcmdManager cmdManager = null;
	// 广播接受者
	private MyReceiver myReceiver;
	// 卡类型
	private String cardType = null;
	// 发送请求的activity
	private String activity = null;
	
	private String command=null;

	private String Tag = "RFIDService"; // Debug
	Timer searchCard = null;
	
	private Listable listable;

	byte[] value_array;
	String cardID = "";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getSectorBlock(int sector,int block) {
		int value = sector*4 + block;
		Log.e("value", value + "");
		String value_str;
		if (value > 15) {
			value_str = Integer.toHexString(value);
		} else {
			value_str = "0" + Integer.toHexString(value);
		}
		return value_str;
	}

	// 读取数据并返回给请求端
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if(command.equals("reset")){
				if(resetCard()){
					Intent serviceIntent = new Intent();
					serviceIntent.setAction(activity);
					serviceIntent.putExtra("result","重置成功!");
					serviceIntent.putExtra("command", command);
					sendBroadcast(serviceIntent);
				}else{
					Intent serviceIntent = new Intent();
					serviceIntent.setAction(activity);
					serviceIntent.putExtra("result","重置失败!");
					serviceIntent.putExtra("command", command);
					sendBroadcast(serviceIntent);
				}
			}else{
				if(writeListable(listable)){
					Intent serviceIntent = new Intent();
					serviceIntent.setAction(activity);
					serviceIntent.putExtra("result","写卡成功!");
					serviceIntent.putExtra("command", command);
					sendBroadcast(serviceIntent);
				}else{
					Intent serviceIntent = new Intent();
					serviceIntent.setAction(activity);
					serviceIntent.putExtra("result","写卡失败!");
					serviceIntent.putExtra("command", command);
					sendBroadcast(serviceIntent);
				}
			}
			
		};
		
		private boolean resetCard(){
			AREA_PASSWORD = "152121152121";
			for(int i=5;i<9;i++){
				if(!resetSector(i,AREA_PASSWORD,cardID)){
					return false;
				}
			}
			return true;
		}
		
		private boolean resetSector(int sector,String password,String cardID){
			boolean authFlag = authCard(sector, AREA_PASSWORD, cardID);
//			Log.i(Tag, "" + authFlag);
			if (authFlag) {

				String block = getSectorBlock(sector, 3);
				String data = "FFFFFFFFFFFFFF078069FFFFFFFFFFFF";
				//String data = "AAAAAAAAAAAAFF078069AAAAAAAAAAAA";
				byte[] dataBytes = Tools.HexString2Bytes(data);
				//byte[] dataBytes = data.getBytes();
				// byte[] blocks = Tools.HexString2Bytes(block);
				int bolckInt = Integer.parseInt(block, 16);
				Log.i("", bolckInt + "");
				return cmdManager.writeMifare14443A(bolckInt, dataBytes);
			}	
			return false;
			
		}

		// 认证
		private boolean authCard(int sector, String password, String cardID) {
			boolean flag = false;
			byte[] cardIDBytes = Tools.HexString2Bytes(cardID);
			int cardIDLen = cardIDBytes.length;
			byte[] passwordBytes = Tools.HexString2Bytes(password);
			
			flag = cmdManager.authMifare14443A(0, sector,
					passwordBytes, cardIDLen, cardIDBytes);
			

			return flag;
		}

		// 读数据
		private String readCard(String block) {
			String data = null;
			byte[] dataBytes = null;
			byte[] blockBytes = Tools.HexString2Bytes(block);
			dataBytes = cmdManager.readMifare14443A(blockBytes);
			if (dataBytes != null) {
				data = Tools.Bytes2HexString(dataBytes, dataBytes.length);
				Log.i(Tag, "read card   " + data);
			}
			return data;
		}
		
		private boolean writeSector(int sector,String cardID,String value){
			// 测试数据10扇区
			boolean authFlag = authCard(sector, AREA_PASSWORD, cardID);
//			Log.i(Tag, "" + authFlag);
			if (authFlag) {

				String block = getSectorBlock(sector, 0);
				return writeCard(block, value);
			}			
			return false;
		}
		
		private boolean writeListable(Listable listable){
			AREA_PASSWORD = "FFFFFFFFFFFF";
			if(listable.getPropertyCount()>15){
				try {
					throw new ArgumentException("需要写入rfid卡的属性超过上限15个！");
				} catch (ArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			List<String> list = listable.getParams();
			writeSector(1, cardID, ""+list.size());
			for(int i=1;i<=list.size();i++){
				if(!writeSector(i+1, cardID, list.get(i-1))){
					return false;
				}
			}
			return true;
		}

		// 写数据
		private boolean writeCard(String block, String data) {
			boolean flag = false;
			if(data.length()<15){
				data +="\0";
			}
			//byte[] dataBytes = Tools.HexString2Bytes(data);
			byte[] dataBytes = data.getBytes();
			// byte[] blocks = Tools.HexString2Bytes(block);
			int bolckInt = Integer.parseInt(block, 16);
			Log.i("", bolckInt + "");
			flag = cmdManager.writeMifare14443A(bolckInt, dataBytes);
			return flag;
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(Tag, "service onCreate");
		cmdManager = NFCcmdManager.getNFCcmdManager(13, 115200, 0); // 打开串口，串口13，波特率115200
		if (cmdManager != null) {

			cmdManager.readerPowerOn();// 打开电源
			/* 测试模块是否连接上 */
			String version = cmdManager.getVersion();
			if (version != null) {
				Log.i(Tag, "cmdManager version " + version);
			}
		}
		// 注册Broadcast Receiver，用于关闭Service
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.csei.service.RFIDService");
		registerReceiver(myReceiver, filter);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		cardType = intent.getStringExtra("cardType");
		listable = intent.getParcelableExtra("listable");
		command = intent.getStringExtra("command");
		if (intent.getStringExtra("activity") != null) {
			activity = intent.getStringExtra("activity");
		}
		if (cardType != null) {
			startSearchCard();
		} else {
			Log.e("无卡", "无卡");
		}
		return 0;
	}

	// 寻卡,读取cardID
	private void startSearchCard() {
		searchCard = new Timer();
		searchCard.schedule(new TimerTask() {
			boolean init14443AFlag;
			// 14443AcardID
			byte[] cardID14443A = null;

			// byte[] b
			@Override
			public void run() {
				// 寻卡流程：14443A初始化-->14443A寻卡-->14443A取消初始化
				init14443AFlag = cmdManager.init_14443A();
				if (init14443AFlag) {
					Log.i(Tag, "init14443A flag    " + init14443AFlag);
					// 寻卡
					cardID14443A = cmdManager.inventory_14443A();
					if (cardID14443A != null) {
						
						// 取消14443A初始化
						if (cmdManager.deInit_14443A()) {
							// Mifare初始化
							if (cmdManager.initMifare14443A()) {
								
								cardID = Tools.Bytes2HexString(cardID14443A,
										cardID14443A.length);
								Log.i(Tag, "rfid car cardID " + cardID);
								Message msg = new Message();
								Bundle bundle = new Bundle();
								bundle.putString("cardID", cardID);
								msg.setData(bundle);
								handler.sendMessage(msg);
								Log.i(Tag, "startSearchCard   " + cardID);
								searchCard.cancel();
								return;
							}
						}
						
						
					}

				}

			}
		}, 10, 10);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(Tag, "onDestroy");
		// 卸载广播注册
		if (myReceiver != null) {
			unregisterReceiver(myReceiver);
		}
		// 关闭串口
		if (cmdManager != null) {
			cmdManager.close(13);
			Log.i(Tag, "onDestroy close");
		}
		super.onDestroy();

	}

	/**
	 * 广播接受者
	 * 
	 * @author Jimmy Pang
	 */
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String ac = intent.getStringExtra("activity");
			if (ac != null)
				Log.e("receive activity", ac);
			activity = ac; // 获取activity
			// 关闭服务
			if (intent.getBooleanExtra("stopflag", false)) {
				stopSelf(); // 收到停止服务信号
				Log.e("stop service", intent.getBooleanExtra("stopflag", false)
						+ "");
			}
			// 关闭寻卡
			if (intent.getBooleanExtra("stopSearch", false)) {
				if (searchCard != null) {
					searchCard.cancel();
				}
				Log.e("stop search",
						intent.getBooleanExtra("stopSearch", false) + "");
			}
		}

	}
}
