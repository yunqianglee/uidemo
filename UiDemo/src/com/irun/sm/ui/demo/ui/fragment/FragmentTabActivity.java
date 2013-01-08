package com.irun.sm.ui.demo.ui.fragment;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.irun.sm.ui.demo.ui.R;
import com.irun.sm.ui.demo.utils.ActivityUtils;

/***
 * @author huangsm
 * @date 2013-1-4
 * @email huangsanm@gmail.com
 * @desc fragment Activity
 */
public class FragmentTabActivity extends FragmentActivity {
	
	private TabHost mTabHost;
	private TabManager mTabManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActivityUtils.requestNotTitleBar(this);
		
		setContentView(R.layout.fragment_tabs);
		
		//继承tabactivity.getTabHost()不需要setup()
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		
		mTabManager = new TabManager(this, mTabHost, R.id.containertabcontent);
		
		RelativeLayout app = (RelativeLayout) getLayoutInflater().inflate(R.layout.app_tab_layout, null);
		mTabManager.addTab(mTabHost.newTabSpec("Apps").setIndicator(app), AppsFragment.class, null);
		
		RelativeLayout contacts = (RelativeLayout) getLayoutInflater().inflate(R.layout.contacts_tab_layout, null);
		mTabManager.addTab(mTabHost.newTabSpec("Contact").setIndicator(contacts), ContactsFragment.class, null);
		
		RelativeLayout message = (RelativeLayout) getLayoutInflater().inflate(R.layout.message_tab_layout, null);
		mTabManager.addTab(mTabHost.newTabSpec("Message").setIndicator(message), MessageFragment.class, null);
		
		if(savedInstanceState != null){
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tag"));
		}
	}
	
	/**
	 * 销毁之前
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tag", mTabHost.getCurrentTabTag());
	}
	
	public static class TabManager implements TabHost.OnTabChangeListener {
		private final FragmentTabActivity mActivity;
		//保存tab
		private final Map<String, TabInfo> mTabs = new HashMap<String, FragmentTabActivity.TabManager.TabInfo>();
		private final TabHost mTabHost;
		private final int mContainerID;
		private TabInfo mLastTab;
		
		public TabManager(FragmentTabActivity activity, TabHost tabHost, int containerID){
			mActivity = activity;
			mTabHost = tabHost;
			mContainerID = containerID;
			mTabHost.setOnTabChangedListener(this);
		}
		
		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private Fragment fragment;
			
			TabInfo(String _tag, Class<?> _clss, Bundle _args){
				tag = _tag;
				clss = _clss;
				args = _args;
			}
		}
		
		static class TabFactory implements TabHost.TabContentFactory {
			private Context mContext;
			TabFactory(Context context){
				mContext = context;
			}
			
			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumHeight(0);
				v.setMinimumWidth(0);
				return v;
			}
		}
		
		//加入tab
		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args){
			tabSpec.setContent(new TabFactory(mActivity));
			String tag = tabSpec.getTag();
			
			TabInfo info = new TabInfo(tag, clss, args);
			final FragmentManager fm = mActivity.getSupportFragmentManager();
			info.fragment = fm.findFragmentByTag(tag);
			//isDetached分离状态
			if(info.fragment != null && !info.fragment.isDetached()){
				FragmentTransaction ft = fm.beginTransaction();
				ft.detach(info.fragment);
				ft.commit();
			}
			mTabs.put(tag, info);
			mTabHost.addTab(tabSpec);
		}
		
		@Override
		public void onTabChanged(String tabId) {
			TabInfo newTab = mTabs.get(tabId);
			if(mLastTab != newTab){
				FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				//脱离之前的tab
				if(mLastTab != null && mLastTab.fragment != null){
					fragmentTransaction.detach(mLastTab.fragment);
				}
				if(newTab != null){
					if(newTab.fragment == null){
						newTab.fragment = Fragment.instantiate(mActivity, newTab.clss.getName(), newTab.args);
						fragmentTransaction.add(mContainerID, newTab.fragment, newTab.tag);
					}else{
						//激活
						fragmentTransaction.attach(newTab.fragment);
					}
				}
				mLastTab = newTab;
				fragmentTransaction.commit();
				//会在进程的主线程中，用异步的方式来执行,如果想要立即执行这个等待中的操作，就要调用这个方法
				//所有的回调和相关的行为都会在这个调用中被执行完成，因此要仔细确认这个方法的调用位置。
				fragmentManager.executePendingTransactions();
			}
		}
	}
}
