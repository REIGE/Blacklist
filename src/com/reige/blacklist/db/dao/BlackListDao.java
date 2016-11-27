package com.reige.blacklist.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.reige.blacklist.db.BlackListOpenHelper;
import com.reige.blacklist.db.domain.BlackListInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BlackListDao {
	private BlackListOpenHelper mBlackListOpenHelper;

	private BlackListDao(Context context) {
		mBlackListOpenHelper = new BlackListOpenHelper(context);
	}

	private static BlackListDao blacklistdao = null;

	public static BlackListDao getInstance(Context context) {
		if (blacklistdao == null) {
			blacklistdao = new BlackListDao(context);
		}
		return blacklistdao;
	}

	/**
	 * ����һ����Ҫ���صĺ���
	 * 
	 * @param phone
	 *            ���ص绰����
	 * @param mode
	 *            ����ģʽ 1���� 2���� 3ȫ��
	 */
	public void insert(String phone, String mode) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("phone", phone);
		values.put("mode", mode);
		db.insert("blacklist", null, values);
		db.close();
	}

	/**
	 * ɾ��һ�����صĵ绰����
	 * 
	 * @param phone
	 *            ��Ҫɾ���ĵ绰����
	 */
	public void delete(String phone) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		db.delete("blacklist", "phone = ?", new String[] { phone });
		// �������ݿ����Ͼ͹ر�һ�� Ȼ�����м�ȥд�߼� ��������
		db.close();
	}

	/**
	 * ���ݵ绰����ȥ,��������ģʽ
	 * 
	 * @param phone
	 *            ��������ģʽ�ĵ绰����
	 * @param mode
	 *            ���µ�����ģʽ 1���� 2���� 3ȫ��
	 */
	public void update(String phone, String mode) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("mode", mode);
		db.update("blacklist", values, "phone = ?", new String[] { phone });
		db.close();
	}

	/**
	 * @return ��ѯ�����ݿ������еĺ����Լ������������ڵļ���
	 */
	public List<BlackListInfo> findAll() {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select phone,mode from blacknumber order by _id desc;", null);
		List<BlackListInfo> list = new ArrayList<BlackListInfo>();
		while (cursor.moveToNext()) {
			BlackListInfo blackListInfo = new BlackListInfo();
			blackListInfo.phone = cursor.getString(0);
			blackListInfo.mode = cursor.getString(1);
			list.add(blackListInfo);
		}
		cursor.close();
		db.close();

		return list;
	}

	/**
	 * ÿ�β�ѯ20������
	 * 
	 * @param index
	 *            ��ѯ������ֵ
	 * @return
	 */
	public List<BlackListInfo> find(int index) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select phone,mode from blacklist order by _id desc limit ?,20;",
				new String[] { index + "" });
		List<BlackListInfo> list = new ArrayList<BlackListInfo>();
		while (cursor.moveToNext()) {
			BlackListInfo blackListInfo = new BlackListInfo();
			blackListInfo.phone = cursor.getString(0);
			blackListInfo.mode = cursor.getString(1);
			list.add(blackListInfo);
		}
		cursor.close();
		db.close();

		return list;
	}

	/**
	 * @return ���ݿ������ݵ�����Ŀ����,����0����û�����ݻ��쳣
	 */
	public int getCount() {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		int count = 0;
		Cursor cursor = db.rawQuery("select count(*) from blacklist;", null);
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}

		cursor.close();
		db.close();
		return count;
	}

	/**
	 * @param phone
	 *            ��Ϊ��ѯ�����ĵ绰����
	 * @return ����绰���������ģʽ 1:���� 2:�绰 3:���� 0:û�д�������
	 */
	public int getMode(String phone) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		int mode = 0;
		Cursor cursor = db.query("blacklist", new String[] { "mode" }, "phone = ?", new String[] { phone }, null, null,
				null);
		if (cursor.moveToNext()) {
			mode = cursor.getInt(0);
		}

		cursor.close();
		db.close();
		return mode;
	}

}
