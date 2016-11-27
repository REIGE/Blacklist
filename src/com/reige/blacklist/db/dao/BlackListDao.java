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
	 * 增加一个需要拦截的号码
	 * 
	 * @param phone
	 *            拦截电话号码
	 * @param mode
	 *            拦截模式 1短信 2来电 3全部
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
	 * 删除一个拦截的电话号码
	 * 
	 * @param phone
	 *            需要删除的电话号码
	 */
	public void delete(String phone) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();
		db.delete("blacklist", "phone = ?", new String[] { phone });
		// 开启数据库马上就关闭一下 然后在中间去写逻辑 以免忘记
		db.close();
	}

	/**
	 * 根据电话号码去,更新拦截模式
	 * 
	 * @param phone
	 *            更新拦截模式的电话号码
	 * @param mode
	 *            更新的拦截模式 1短信 2来电 3全部
	 */
	public void update(String phone, String mode) {
		SQLiteDatabase db = mBlackListOpenHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("mode", mode);
		db.update("blacklist", values, "phone = ?", new String[] { phone });
		db.close();
	}

	/**
	 * @return 查询到数据库中所有的号码以及拦截类型所在的集合
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
	 * 每次查询20条数据
	 * 
	 * @param index
	 *            查询的索引值
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
	 * @return 数据库中数据的总条目个数,返回0代表没有数据或异常
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
	 *            作为查询条件的电话号码
	 * @return 传入电话号码的拦截模式 1:短信 2:电话 3:所有 0:没有此条数据
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
