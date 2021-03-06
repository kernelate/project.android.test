package com.ntek.wallpad.Screens.Fragment;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ntek.wallpad.R;
import com.ntek.wallpad.Utils.CommonUtilities;
import com.ntek.wallpad.Utils.OnChangeFragmentListener;
import com.ntek.wallpad.Utils.RingProgressDialogManager;
import com.ntek.wallpad.network.Global;
import com.ntek.wallpad.network.LoginGlobal;
import com.ntek.wallpad.network.SocClient;

public class FragmentSettingManual extends Fragment{

	private final static String TAG = FragmentSettingManual.class.getCanonicalName();
	
	private EditText etIPAddress;
	private EditText etPort;
	private Button btnConnect;
	
	private String loginID, loginPassword;
	private BroadcastReceiver mBroadCastRecv;
	private OnChangeFragmentListener mOnChangeFragment;
	private static FragmentSettingManual mThis;
	private Dialog loginDialog;
	
	View view;
	TextView maunalText;
	TextView ipAddressText;
	TextView portText;
	
	
	
	public FragmentSettingManual()
	{
		Log.d(TAG, TAG);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnChangeFragment = (OnChangeFragmentListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.d(TAG, "onReceive() : " + action);
				
				RingProgressDialogManager.hide();
				if(action.equals(Global.TCP_CONNECT_CALLBACK)) {
					final String response = intent.getStringExtra("connect");
					Log.d(TAG, "getStringExtra() : " + response);
					if (response.equals("success")) {
						if(FragmentCommonSetting.selectType == FragmentCommonSetting.SERVER_CONFIG) {
							loginDialog.show();
						}
						else if(FragmentCommonSetting.selectType == FragmentCommonSetting.EVENT_SETTING) {
							FragmentTransaction ft = getFragmentManager().beginTransaction();
							ft.replace(R.id.setting_screen_right, new FragmentSettingEventNotificationForm());
							ft.addToBackStack(null);
							ft.commit();
						}
					}
					else {
						showToast(getString(R.string.string_connection_fail));
					}
				}
				else if(action.equals(LoginGlobal.TCP_LOGIN_CALLBACK)) {
					final String response = intent.getStringExtra("login");
					Log.d(TAG, "getStringExtra() : " + response);

					if (response.equals("true") || response.equals("false") || response.equals("success")) {
						new Thread(new SocClient("doortalk", CommonUtilities.soc_port, getActivity())).start();
						FragmentCommonSetting.isDoorDeviceApMode = Boolean.parseBoolean(response);
					}
					else {
						RingProgressDialogManager.hide();
						if (response.equals("invalid_id"))
							showToast(getString(R.string.string_invalid_login_id));
						else if (response.equals("invalid_password"))
							showToast(getString(R.string.string_invalid_login_password));
						else if (response.equals("connection_lost"))
							showToast(getString(R.string.string_connection_lost));
					}
				}
				else if (action.equals(Global.TCP_GET_DOORTALK_DATA_CALLBACK)) {
					final String response = intent.getStringExtra("doortalk");
					Log.d(TAG, "getStringExtra() : " + response);

					RingProgressDialogManager.hide();
					if (response.equals("success")) {
						if (mOnChangeFragment != null)
						{
							mOnChangeFragment.changeFragment(null, new FragmentSetUpDoorTalkSelectUser(), true);
						}
					}
					else
						showToast(getString(R.string.string_connection_lost));
				}
			}
		};
		
		loginDialog = new Dialog(getActivity());
		loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		loginDialog.setContentView(R.layout.dialog_login);

		final EditText loginIDEditText = (EditText) loginDialog.findViewById(R.id.loginIDEditText);
		final EditText loginPasswordEditText = (EditText) loginDialog.findViewById(R.id.loginPasswordEditText);
		final Button loginButton = (Button) loginDialog.findViewById(R.id.loginButton);

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginID = loginIDEditText.getText().toString();
				loginPassword = loginPasswordEditText.getText().toString();

				if (!loginID.trim().isEmpty()) {
					if (!loginPassword.trim().isEmpty()) {
						LoginGlobal.getInstance().setLoginID(loginID);
						LoginGlobal.getInstance().setLoginPassword(loginPassword);

						loginDialog.dismiss();
						RingProgressDialogManager.show(getActivity(), "Please wait...", "Trying to Login...");
						new Thread(new SocClient("login", CommonUtilities.soc_port, getActivity())).start();
					}
					else {
						showToast(getString(R.string.string_password_empty));
					}
				}
				else {
					showToast(getString(R.string.string_id_empty));
				}
			}
		});
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.fragment_setting_manual, container, false);
//		initializeUI();
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Global.TCP_CONNECT_CALLBACK);
		intentFilter.addAction(LoginGlobal.TCP_LOGIN_CALLBACK);
		intentFilter.addAction(Global.TCP_GET_DOORTALK_DATA_CALLBACK);
		getActivity().registerReceiver(mBroadCastRecv, intentFilter);
	}
	
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mBroadCastRecv != null)
		{
			getActivity().unregisterReceiver(mBroadCastRecv);
		}
	}
	
	private void showToast(String message) {
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
	
	private static boolean isValidIp(final String ip) {
	    return InetAddressUtils.isIPv4Address(ip) || InetAddressUtils.isIPv6Address(ip);
	}
	
	private void initializeUI()
	{
		etIPAddress = (EditText) view.findViewById(R.id.fragment_setting_manual_edittext_ip_address);
		etPort = (EditText) view.findViewById(R.id.fragment_setting_manual_edittext_port);
		btnConnect = (Button) view.findViewById(R.id.fragment_setting_manual_button_connect);
		
		maunalText = (TextView) view.findViewById(R.id.automation_setting_manual);
		ipAddressText = (TextView) view.findViewById(R.id.automation_setting_manual_ip);
		portText = (TextView) view.findViewById(R.id.automation_setting_manual_port);
		
		Typeface fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSansRegular.ttf");
		Typeface fontSemiBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSansSemibold.ttf");
		
//		etIPAddress = (EditText) view.findViewById(R.id.login_manual_ipadd_et);
//		etPort = (EditText) view.findViewById(R.id.login_manual_pw_et);
//		btnConnect = (Button) view.findViewById(R.id.login_manual_connect);
		
		etIPAddress.setTypeface(fontRegular);
		etPort.setTypeface(fontRegular);
		maunalText.setTypeface(fontSemiBold);
		ipAddressText.setTypeface(fontSemiBold);
		portText.setTypeface(fontSemiBold);
		btnConnect.setTypeface(fontSemiBold);
		
		
		
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isValidIp(etIPAddress.getText().toString())) {
					Global.getInstance().setData(etIPAddress.getText().toString());
					RingProgressDialogManager.show(getActivity(), getString(R.string.string_please_wait),
							getString(R.string.string_connecting_device));
					
					new Thread(new SocClient("connect", CommonUtilities.soc_port, getActivity())).start();
				} else {
					showToast("Not a valid Ip Address");
				}
			}
		});
	}
	
	public static FragmentSettingManual getInstance()
	{
		if (mThis == null)
		{
			mThis = new FragmentSettingManual();
		}
		
		return mThis;
	}

}
