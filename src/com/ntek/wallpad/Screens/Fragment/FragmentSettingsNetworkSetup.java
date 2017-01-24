package com.ntek.wallpad.Screens.Fragment;

import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ntek.wallpad.R;
import com.ntek.wallpad.Utils.CommonUtilities;
import com.ntek.wallpad.Utils.RingProgressDialogManager;
import com.ntek.wallpad.Utils.WifiArrayAdapter;
import com.ntek.wallpad.network.NetworkGlobal;
import com.ntek.wallpad.network.NetworkManager;
import com.ntek.wallpad.network.SocClient;

public class FragmentSettingsNetworkSetup extends Fragment {

	private final static String TAG = FragmentSettingsNetworkSetup.class.getCanonicalName();
	
	private View view;
//	private TextView tvNetworkType;
	private TextView tvIpSettins;
	private Button btnScan;
	private Button btnSave;
	private Button btnNetworkType;
	private Button btnIpSetting;
	private EditText etWifiNetwork;
	private EditText etPassword;
	private EditText etIpAddress;
	private EditText etGatewayPort;
	private EditText etSubnetMask;
	private EditText etDns1;
	private EditText etDns2;
	private LinearLayout advanceOptionLinearLayout;
	private LinearLayout wifiNetworkSetupLinearLayout;
	private ToggleButton networktypeTB;
	private ToggleButton ipsettingTB;
	private Button pwShow;
	
	private ScanResult selectedNetwork;
	private List<ScanResult> results;
	
	private Dialog networkIpSettingsDialog;
	private Dialog networkTypeDialog;

	private String networkType = "", networkIpSettings = "", networkSSID = "",
			networkPassword = "", networkSecurity = "", networkIpAddress = "",
			networkNetmask = "", networkGateway = "", networkDNS1 = "",
			networkDNS2 = "";
	
	private BroadcastReceiver mBroadCastRecv;
	private WifiManager wifiManager;
	private WifiArrayAdapter adapter;
	
	
	private TextView sipAccount;
	private Button settingButtonSaveSip;
	private TextView screenIdentityTextviewDisplay;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		networkType = NetworkGlobal.getInstance().getType();
		networkIpSettings = NetworkGlobal.getInstance().getIpSettings();
		
		if(networkType == null)
			networkType = "wifi";
		if(networkIpSettings == null)
			networkIpSettings = "dhcp";
		
		mBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				final String response = intent.getStringExtra("network");
				Log.d(TAG, "onReceive() : " + action);
				Log.d(TAG, "getStringExtra() : " + response);

				RingProgressDialogManager.hide();
				if (response.equals("success")) {
					if(FragmentCommonSetting.isDoorDeviceApMode) {
//						mScreenService.show(ScreenHome.class);
						Log.d(TAG, "Success");
						showToast("Data Successfully Sent to Server");
					}
					else {
//						back();
					}
				}
				else {
					showToast(getString(R.string.string_connection_fail));
				}
			}
		};
		
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NetworkGlobal.TCP_SETUP_NETWORK_CALLBACK);
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_setting_network_setup, container, false);
		initializeUi();
		return view;
	}

	private void initializeUi() {
//		tvNetworkType = (TextView) view.findViewById(R.id.phone_setting_textview_network_type);
		tvIpSettins = (TextView) view.findViewById(R.id.phone_setting_textview_ip_setting);
		btnScan = (Button) view.findViewById(R.id.phone_setting_button_scan_ssid);
		btnSave = (Button) view.findViewById(R.id.phone_setting_button_add);
		btnNetworkType = (Button) view.findViewById(R.id.networkTypeButton);
		btnIpSetting = (Button) view.findViewById(R.id.ipsetting_button);
		etWifiNetwork = (EditText) view.findViewById(R.id.phone_setting_edittext_wifi_network);
		etPassword = (EditText) view.findViewById(R.id.phone_setting_edittext_password);
		etIpAddress = (EditText) view.findViewById(R.id.phone_setting_edittext_ip_address);
		etGatewayPort = (EditText) view.findViewById(R.id.phone_setting_edittext_gateway);
		advanceOptionLinearLayout = (LinearLayout) view.findViewById(R.id.setting_linear_layout_to_hide);
		wifiNetworkSetupLinearLayout = (LinearLayout) view.findViewById(R.id.setting_layout_hide_ethernet);
		etSubnetMask = (EditText) view.findViewById(R.id.phone_setting_edittext_subnetmask);
		etDns1 = (EditText) view.findViewById(R.id.phone_setting_edittext_dns_one);
		etDns2 = (EditText) view.findViewById(R.id.phone_setting_edittext_dns_two);
		pwShow = (Button) view.findViewById(R.id.pwShow);
		pwShow.setBackgroundResource(R.drawable.ic_visibility_off);
		
		sipAccount = (TextView) view.findViewById(R.id.setting_identity_textview_myprofile);
		settingButtonSaveSip = (Button) view.findViewById(R.id.setting_button_save_sip);
		screenIdentityTextviewDisplay = (TextView) view.findViewById(R.id.screen_identity_textView_displayname);
		
		networktypeTB = (ToggleButton) view.findViewById(R.id.phone_setting_network_type_tb);
		ipsettingTB = (ToggleButton) view.findViewById(R.id.phone_setting_ip_setting_tb);
		
		Typeface fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSansRegular.ttf");
		Typeface fontSemiBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSansSemibold.ttf");
		
//		settingButtonSaveSip.setTypeface(fontSemiBold);
		
//		etWifiNetwork.setTypeface(fontRegular);
//		etPassword.setTypeface(fontRegular);
//		etIpAddress.setTypeface(fontRegular);
//		etGatewayPort.setTypeface(fontRegular);
//		etSubnetMask.setTypeface(fontRegular);
//		etDns1.setTypeface(fontRegular);
//		etDns2.setTypeface(fontRegular);
//		
//		sipAccount.setTypeface(fontSemiBold);
//		screenIdentityTextviewDisplay.setTypeface(fontSemiBold);
		
		pwShow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) 
			{

				if(String.valueOf(pwShow.getTag()) == "unhidden"){
					etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());					
					pwShow.setTag("hidden");	
					pwShow.setBackgroundResource(R.drawable.ic_visibility_off);
				}
				else
				{
					etPassword.setTransformationMethod(null);					
					pwShow.setTag("unhidden");	
					pwShow.setBackgroundResource(R.drawable.ic_visibility_on);
				}
			}
		});
		
		
		networktypeTB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (isChecked) 
				{
					Log.d(TAG, "ethernet");
					networkType = "ethernet";
					wifiNetworkSetupLinearLayout.setVisibility(View.GONE);
				}
				else
				{
					Log.d(TAG, "wifi");
					networkType = "wifi";
					wifiNetworkSetupLinearLayout.setVisibility(View.VISIBLE);
				}
				
			}
		});
		
		
		ipsettingTB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (isChecked) 
				{
					Log.d(TAG, "static");
					networkIpSettings = "static";
					advanceOptionLinearLayout.setVisibility(View.VISIBLE);
				}
				else
				{
					Log.d(TAG, "DHCP");
					networkIpSettings = "dhcp";
					advanceOptionLinearLayout.setVisibility(View.GONE);
					
				}
			}
		});
		
		
		
		
		
		
		
		
		
		
		etWifiNetwork.setText(NetworkGlobal.getInstance().getSsid());
		etPassword.setText(NetworkGlobal.getInstance().getPw());
		etIpAddress.setText(NetworkGlobal.getInstance().getIp());
		etSubnetMask.setText(NetworkGlobal.getInstance().getNetmask());
		etGatewayPort.setText(NetworkGlobal.getInstance().getGateway());
		etDns1.setText(NetworkGlobal.getInstance().getDns1());
		etDns2.setText(NetworkGlobal.getInstance().getDns2());
		
		
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (networkType.equals("wifi")) {
					networkSSID = etWifiNetwork.getText().toString();
					networkPassword = etPassword.getText().toString();
					networkSecurity = NetworkManager.getInstance(getActivity()).getScanResultSecurity(selectedNetwork);
				}

				if (networkIpSettings.equals("static")) {
					networkDNS1 = etDns1.getText().toString();
					networkDNS2 = etDns2.getText().toString();
					networkGateway = etGatewayPort.getText().toString();
					networkIpAddress = etIpAddress.getText().toString();
					networkNetmask = etSubnetMask.getText().toString();
				}

				NetworkGlobal.getInstance().setType(networkType);

				// For wifi mode
				NetworkGlobal.getInstance().setSsid(networkSSID);
				NetworkGlobal.getInstance().setPw(networkPassword);
				NetworkGlobal.getInstance().setSecurity(networkSecurity);

				// Advance Option
				NetworkGlobal.getInstance().setIpSettings(networkIpSettings);
				NetworkGlobal.getInstance().setIp(networkIpAddress);
				NetworkGlobal.getInstance().setNetmask(networkNetmask);
				NetworkGlobal.getInstance().setGateway(networkGateway);
				NetworkGlobal.getInstance().setDns1(networkDNS1);
				NetworkGlobal.getInstance().setDns2(networkDNS2);

				RingProgressDialogManager.show(getActivity(), getString(R.string.string_please_wait), getString(R.string.string_send_data));
				Thread nThread = new Thread(new SocClient("network", CommonUtilities.soc_port, getActivity()));
				nThread.start();

				Log.d(TAG, "Type:" + networkType);
				Log.d(TAG, "SSID:" + networkSSID);
				Log.d(TAG, "Password:" + networkPassword);
				Log.d(TAG, "Security:" + networkSecurity);

				Log.d(TAG, "Ip Settings:" + networkIpSettings);
				Log.d(TAG, "Ip Address:" + networkIpAddress);
				Log.d(TAG, "Subnet Netmask:" + networkNetmask);
				Log.d(TAG, "Gateway:" + networkGateway);
				Log.d(TAG, "DNS1:" + networkDNS1);
				Log.d(TAG, "DNS2:" + networkDNS2);

			}
		});
		
		btnNetworkType.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				networkTypeDialog.show();
			}
		});
		
		btnIpSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				networkIpSettingsDialog.show();
			}
		});
		
		btnScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showWifiSearchDialog();
			}
		});
		
		setUpNetworkIpSettingsDialog();
		setUpNetworkTypeDialog();
	}
	
	private void setUpNetworkIpSettingsDialog() {

		networkIpSettingsDialog = new Dialog(getActivity());
		networkIpSettingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		networkIpSettingsDialog.setContentView(R.layout.dialog_ip_settings);

		final Button cancelButton = (Button) networkIpSettingsDialog.findViewById(R.id.dialog_ip_setting_button_cancel);
		final RadioGroup groupRadioIpSettingType = (RadioGroup) networkIpSettingsDialog.findViewById(R.id.dialog_radiogroup_ip_setting);
		final RadioButton dhcpRadioButton = (RadioButton) networkIpSettingsDialog.findViewById(R.id.dialog_ip_setting_radiobutton_dhcp);
		final RadioButton staticRadioButton = (RadioButton) networkIpSettingsDialog.findViewById(R.id.dialog_ip_setting_radiobutton_static);
		
		groupRadioIpSettingType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedID) {
				// TODO Auto-generated method stub
				switch (checkedID) {
				case R.id.dialog_ip_setting_radiobutton_dhcp:
					networkIpSettingsDialog.dismiss();
					tvIpSettins.setText(getString(R.string.DHCP));
					advanceOptionLinearLayout.setVisibility(View.GONE);
					networkIpSettings = "dhcp";
					break;
				case R.id.dialog_ip_setting_radiobutton_static:
					networkIpSettingsDialog.dismiss();
					tvIpSettins.setText(getString(R.string.Static));
					advanceOptionLinearLayout.setVisibility(View.VISIBLE);
					networkIpSettings = "static";
					break;
				default:
					break;
				}
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				networkIpSettingsDialog.dismiss();
			}
		});
		
		if(networkIpSettings != null && networkIpSettings.equals("static")) {
			staticRadioButton.setChecked(true);
			tvIpSettins.setText(getString(R.string.Static));
			advanceOptionLinearLayout.setVisibility(View.VISIBLE);
		} 
		else {
			dhcpRadioButton.setChecked(true);
			tvIpSettins.setText(getString(R.string.DHCP));
			advanceOptionLinearLayout.setVisibility(View.GONE);
		}
	}
	
	private void setUpNetworkTypeDialog() {
		networkTypeDialog = new Dialog(getActivity());
		networkTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		networkTypeDialog.setContentView(R.layout.dialog_network_type);

		final Button cancelButton = (Button) networkTypeDialog.findViewById(R.id.dialog_network_type_button_cancel);
		final RadioButton wifiRadioButton = (RadioButton) networkTypeDialog.findViewById(R.id.dialog_network_type_wifi);
		final RadioButton ethernetRadioButton = (RadioButton) networkTypeDialog.findViewById(R.id.dialog_network_type_ethernet_cable);

		
		final RadioGroup groupRadioNetworkType = (RadioGroup) networkTypeDialog.findViewById(R.id.dialog_radiogroup);
		
		groupRadioNetworkType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedID) {
				// TODO Auto-generated method stub
				switch (checkedID) {
				case R.id.dialog_network_type_ethernet_cable:
					networkTypeDialog.dismiss();
//					tvNetworkType.setText(getString(R.string.Ethernet_cable));
					wifiNetworkSetupLinearLayout.setVisibility(View.GONE);
					networkType = "ethernet";
					break;
				case R.id.dialog_network_type_wifi:
					networkTypeDialog.dismiss();
//					tvNetworkType.setText(getString(R.string.WIFI));
					wifiNetworkSetupLinearLayout.setVisibility(View.VISIBLE);
					networkType = "wifi";
					break;
				default:
					break;
				}
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				networkTypeDialog.dismiss();
			}
		});
		
		if(networkType != null && networkType.equals("ethernet")) {
			ethernetRadioButton.setChecked(true);
//			tvNetworkType.setText(getString(R.string.Ethernet_cable));
			wifiNetworkSetupLinearLayout.setVisibility(View.GONE);
		}
		else {
			wifiRadioButton.setChecked(true);
//			tvNetworkType.setText(getString(R.string.WIFI));
			wifiNetworkSetupLinearLayout.setVisibility(View.VISIBLE);
		}

	}
	
	protected void showWifiSearchDialog() {
		final Dialog wifiSearchDialog = new Dialog(getActivity());
		wifiSearchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		wifiSearchDialog.setContentView(R.layout.dialog_select_wifi_network);

		final ListView wifiSearchListView = (ListView) wifiSearchDialog.findViewById(R.id.dialog_select_wifi_network_listview);
		final Button wifiScanButton = (Button) wifiSearchDialog.findViewById(R.id.dialog_select_wifi_network_button_scan);

		// Check for wifi is disabled, If wifi disabled then enable it
		if (wifiManager.isWifiEnabled() == false) {
			wifiManager.setWifiEnabled(true);
		}

		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			// start WiFi Scan
			wifiManager.startScan();
		}

		results = wifiManager.getScanResults();
		adapter = new WifiArrayAdapter(getActivity(), R.layout.list_single, results);
		wifiSearchListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		wifiScanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
					// start WiFi Scan
					wifiManager.startScan();
				}

				results = wifiManager.getScanResults();
				adapter = new WifiArrayAdapter(getActivity(), R.layout.list_single, results);
				wifiSearchListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		});

		wifiSearchListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedNetwork = results.get(position);
				etWifiNetwork.setText(results.get(position).SSID);
				wifiSearchDialog.dismiss();
			}
		});

		wifiSearchDialog.show();
	}

	
	protected void showToast(String message) {
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
}