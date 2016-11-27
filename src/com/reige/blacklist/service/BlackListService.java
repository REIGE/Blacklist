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
		// ���ض���
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.setPriority(1000);
		innerSmsReceiver = new InnerSmsReceiver();
		registerReceiver(innerSmsReceiver, intentFilter);

		// �����绰��״̬
		// 1,�绰�����߶���
		mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 2,�����绰״̬

		mPhoneStateListener = new MyPhoneStateListener();
		mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		super.onCreate();
	}

	class MyPhoneStateListener extends PhoneStateListener {
		// 3,�ֶ���д,�绰״̬�����ı�ᴥ���ķ���
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				// �Ҷϵ绰 aidl�ļ���ȥ��
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
			// ServiceManager����android�Կ���������,���Բ���ȥֱ�ӵ����䷽��,��Ҫ�������

			try {
				// 1,��ȡServiceManager�ֽ����ļ�
				Class<?> clazz = Class.forName("android.os.ServiceManager");
				// 2,��ȡ����
				Method method = clazz.getMethod("getService", String.class);
				// 3,������ô˷���
				IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
				// 4,���û�ȡaidl�ļ����󷽷�
				ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
				// 5,������aidl�����ص�endCall����
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
			// ��ȡ��������,��ȡ���Ͷ��ŵ绰����,����˵绰�����ں�������,��������ģʽҲΪ1(����)����3(����),���ض���
			// 1,��ȡ��������
			Object[] objects = (Object[]) intent.getExtras().get("pdus");
			// 2,ѭ���������Ź���
			for (Object object : objects) {
				// 3,��ȡ���Ŷ���
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
				// 4,��ȡ���Ŷ���Ļ�����Ϣ
				String originatingAddress = sms.getOriginatingAddress();
				String messageBody = sms.getMessageBody();

				int mode = mDao.getMode(originatingAddress);

				if (mode == 1 || mode == 3) {
					// ���ض���(android 4.4�汾ʧЧ �������ݿ�,ɾ��)
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

		// ���ݿ���ָ��calls�����ı��ʱ���ȥ���÷���
		@Override
		public void onChange(boolean selfChange) {
			// ����һ�����ݺ�,�ٽ���ɾ��
			getContentResolver().delete(Uri.parse("content://call_log/calls"), "number = ?", new String[] { phone });
			super.onChange(selfChange);
		}
	}

	@Override
	public void onDestroy() {
		// ע���㲥
		if (innerSmsReceiver != null) {
			unregisterReceiver(innerSmsReceiver);
		}
		// ע�����ݹ۲���
		if (myContentObserver != null) {
			getContentResolver().unregisterContentObserver(myContentObserver);
		}
		// ȡ���Ե绰״̬�ļ���
		if (mPhoneStateListener != null) {
			mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}

		super.onDestroy();
	}

}
