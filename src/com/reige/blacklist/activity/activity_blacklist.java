package com.reige.blacklist.activity;

import java.util.List;

import com.reige.blacklist.R;
import com.reige.blacklist.db.dao.BlackListDao;
import com.reige.blacklist.db.domain.BlackListInfo;
import com.reige.blacklist.service.BlackListService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class activity_blacklist extends Activity {
	private BlackListDao mDao;
	private List<BlackListInfo> mBlackList;
	private int mCount;
	private int mode = 1;
	MyAdapter mAdapter;
	private boolean mIsLoad = false;
	private Button bt_add;
	private ListView lv_blacknumber;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mAdapter == null) {
				mAdapter = new MyAdapter();
				lv_blacknumber.setAdapter(mAdapter);
			} else {
				mAdapter.notifyDataSetChanged();
			}

		};
	};

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mBlackList.size();
		}

		@Override
		public Object getItem(int position) {
			return mBlackList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(), R.layout.listview_blacklist_item, null);
				holder = new ViewHolder();
				holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
				holder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
				holder.tv_mode = (TextView) convertView.findViewById(R.id.tv_mode);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// ���ݿ���ɾ��
					mDao.delete(mBlackList.get(position).phone);
					// ������ɾ��
					mBlackList.remove(position);
					// ֪ͨ��������������
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
				}
			});
			holder.tv_phone.setText(mBlackList.get(position).phone);
			int mode = Integer.parseInt(mBlackList.get(position).mode);
			switch (mode) {
			case 1:
				holder.tv_mode.setText("���ض���");
				break;
			case 2:
				holder.tv_mode.setText("���ص绰");
				break;
			case 3:
				holder.tv_mode.setText("��������");
				break;
			}
			return convertView;
		}
	}

	// ����viewHolder�����
	static class ViewHolder {
		TextView tv_phone;
		TextView tv_mode;
		ImageView iv_delete;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blacklist);

		initUI();
		initData();

	}

	private void initData() {
		// ��ȡ���ݿ��еĺ���
		new Thread() {

			public void run() {
				mDao = BlackListDao.getInstance(getApplicationContext());
				mBlackList = mDao.find(0);
				mCount = mDao.getCount();

				// 3,ͨ����Ϣ���Ƹ�֪���߳̿���ȥʹ�ð������ݵļ���
				mHandler.sendEmptyMessage(0);
			}
		}.start();
	}

	private void initUI() {
		bt_add = (Button) findViewById(R.id.bt_add);
		lv_blacknumber = (ListView) findViewById(R.id.lv_blacknumber);
		
		Button bt_start = (Button) findViewById(R.id.bt_start);
		Button bt_end = (Button) findViewById(R.id.bt_end);
		bt_start.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				startService(new Intent(getApplicationContext(), BlackListService.class));
			}
		});
		bt_end.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				stopService(new Intent(getApplicationContext(), BlackListService.class));
			}
		});
		
		
		bt_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});
		lv_blacknumber.setOnScrollListener(new OnScrollListener() {
			// ����״̬�����ı���õķ���
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				// OnScrollListener.SCROLL_STATE_FLING ���ٹ���
				// OnScrollListener.SCROLL_STATE_IDLE ����״̬
				// OnScrollListener.SCROLL_STATE_TOUCH_SCROLL ���ִ�����ȥ����״̬
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
						&& lv_blacknumber.getLastVisiblePosition() >= mBlackList.size() - 1 && !mIsLoad) {

					if (mCount >= mBlackList.size()) {
						// ������һҳ����
						new Thread() {
							public void run() {
								// 1,��ȡ�������������ݿ�Ķ���
								mDao = BlackListDao.getInstance(getApplicationContext());
								// 2,��ѯ��������
								List<BlackListInfo> moreData = mDao.find(mBlackList.size());
								// 3,�����һҳ���ݵĹ���
								mBlackList.addAll(moreData);
								// 4,֪ͨ����������ˢ��
								mHandler.sendEmptyMessage(0);
							}
						}.start();
					}
				}

			}

			// ���������е��õķ���
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}

	private void showDialog() {
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(getApplicationContext(), R.layout.dialog_add_blacklist, null);
		dialog.setView(view, 0, 0, 0, 0);

		final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);
		RadioGroup rg_group = (RadioGroup) view.findViewById(R.id.rg_group);

		Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
		Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

		rg_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_sms:
					mode = 1;
					break;
				case R.id.rb_phone:
					mode = 2;

					break;
				case R.id.rb_all:
					mode = 3;
					break;
				}
			}
		});

		bt_submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = et_phone.getText().toString();
				if (!TextUtils.isEmpty(phone)) {
					mDao.insert(phone, mode + "");
					BlackListInfo blackListInfo = new BlackListInfo();
					blackListInfo.phone = phone;
					blackListInfo.mode = mode + "";
					mBlackList.add(0, blackListInfo);
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					// dialog.dismiss();

				} else {
					Toast.makeText(getApplicationContext(), "������Ҫ���εĺ���", Toast.LENGTH_SHORT).show();
				}
			}
		});

		bt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

}
