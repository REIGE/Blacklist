package com.reige.blacklist.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.reige.blacklist.db.dao.BlackListDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class BlackListService extends Service {

	private BlackListDao mDao;
	private TelephonyManager mTM;
	private MyPhoneStateListener mPhoneStateListener;
	private MyContentObserver myContentObserver;
	private InnerSmsReceiver innerSmsReceiver;

	@Override
	public void onCreate() {
		mDao = BlackListDao.getInstance(this);
		// 拦截短信
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.setPriority(1000);
		innerSmsReceiver = new InnerSmsReceiver();
		registerReceiver(innerSmsReceiver, intentFilter);

		// 监听电话的状态
		// 1,电话管理者对象
		mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 2,监听电话状态

		mPhoneStateListener = new MyPhoneStateListener();
		mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		super.onCreate();
	}

	class MyPhoneStateListener extends PhoneStateListener {
		// 3,手动重写,电话状态发生改变会触发的方法
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				// 挂断电话 aidl文件中去了
				endCall(incomingNumber);
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	public void endCall(String phone) {
		int mode = mDao.getMode(phone);

		if (mode == 2 || mode == 3) {
			// ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
			// ServiceManager此类android对开发者隐藏,所以不能去直接调用其方法,需要反射调用

			try {
				// 1,获取ServiceManager字节码文件
				Class<?> clazz = Class.forName("android.os.ServiceManager");
				// 2,获取方法
				Method method = clazz.getMethod("getService", String.class);
				// 3,反射调用此方法
				IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
				// 4,调用获取aidl文件对象方法
				ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
				// 5,调用在aidl中隐藏的endCall方法
				iTelephony.endCall();

			} catch (Exception e) {
				e.printStackTrace();
			}

			Handler handler = new Handler();
			myContentObserver = new MyContentObserver(handler, phone);
			getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true,
					myContentObserver);
		}

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	class InnerSmsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取短信内容,获取发送短信电话号码,如果此电话号码在黑名单中,并且拦截模式也为1(短信)或者3(所有),拦截短信
			// 1,获取短信内容
			Object[] objects = (Object[]) intent.getExtras().get("pdus");
			// 2,循环遍历短信过程
			for (Object object : objects) {
				// 3,获取短信对象
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
				// 4,获取短信对象的基本信息
				String originatingAddress = sms.getOriginatingAddress();
				String messageBody = sms.getMessageBody();

				int mode = mDao.getMode(originatingAddress);

				if (mode == 1 || mode == 3) {
					// 拦截短信(android 4.4版本失效 短信数据库,删除)
					abortBroadcast();
				}
			}
		}

	}

	class MyContentObserver extends ContentObserver {
		private String phone;

		public MyContentObserver(Handler handler, String phone) {
			super(handler);
			this.phone = phone;
		}

		// 数据库中指定calls表发生改变的时候会去调用方法
		@Override
		public void onChange(boolean selfChange) {
			// 插入一条数据后,再进行删除
			getContentResolver().delete(Uri.parse("content://call_log/calls"), "number = ?", new String[] { phone });
			super.onChange(selfChange);
		}
	}

	@Override
	public void onDestroy() {
		// 注销广播
		if (innerSmsReceiver != null) {
			unregisterReceiver(innerSmsReceiver);
		}
		// 注销内容观察者
		if (myContentObserver != null) {
			getContentResolver().unregisterContentObserver(myContentObserver);
		}
		// 取消对电话状态的监听
		if (mPhoneStateListener != null) {
			mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}

		super.onDestroy();
	}

}
