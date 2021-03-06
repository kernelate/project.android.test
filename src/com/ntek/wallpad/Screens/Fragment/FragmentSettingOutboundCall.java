package com.ntek.wallpad.Screens.Fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntek.wallpad.R;
import com.ntek.wallpad.Utils.CommonUtilities;
import com.ntek.wallpad.Utils.RingProgressDialogManager;
import com.ntek.wallpad.network.Global;
import com.ntek.wallpad.network.SocClient;

public class FragmentSettingOutboundCall extends Fragment {
	
	private final static String TAG = FragmentSettingOutboundCall.class.getCanonicalName();
	
	private View view;
	private TextView nameTitleHeader;
	private Button btnSaveOutboundCall;
	private Button btnAddOutboundCall;
	private ListView lvOutBoundCall;

	private String deviceList;
	private BroadcastReceiver mBroadCastRecv;
	
	private CustomArrayAdapter adapter;
	private String mDeviceName;
	
	public FragmentSettingOutboundCall(String deviceName)
	{
		Log.i(TAG, "FragmentSettingOutboundCall");
		this.mDeviceName = deviceName;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		String[] priorityList = new String[5];
		
		deviceList = Global.getInstance().getOutboundPriorityList();

		PriorityInfo.getList().clear();
		if (deviceList != null && !deviceList.isEmpty()) 
		{
			try {
				JSONObject json = new JSONObject(deviceList);

				@SuppressWarnings("rawtypes")
				Iterator i = json.keys();
				while (i.hasNext()) 
				{
					String key = i.next().toString();
					if(key.equals("1")) {
						priorityList[0] = json.getString(key);
					}
					if(key.equals("2")) {
						priorityList[1] = json.getString(key);
					}
					if(key.equals("3")) {
						priorityList[2] = json.getString(key);
					}
					if(key.equals("4")) {
						priorityList[3] = json.getString(key);
					}
					if(key.equals("5")) {
						priorityList[4] = json.getString(key);
					}
				}
				
				for(int j =0; j < priorityList.length; j++) 
				{
					if(priorityList[j] != null && priorityList[j].length() > 0)
					{
						String partyUri = priorityList[j].substring(0, priorityList[j].indexOf("@"));
						String displayName = priorityList[j].substring(priorityList[j].indexOf("@") + 1, priorityList[j].length());
						PriorityInfo.getList().add(new PriorityInfo(Integer.toString(j + 1), partyUri, displayName));
					}
				}
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		
		mBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d(TAG, "onReceive() : " + action);
				RingProgressDialogManager.hide();
				if (action.equals("com.smartbean.servertalk.action.SENDING_OUTBOUND_PRIORITYLIST_CALLBACK")) {
					String response = intent.getStringExtra("response");
					Log.d(TAG, "getStringExtra() : " + response);

					if (response.equals("success")) {
						showToast("Outbound Call priority is successfully send to the server");
					}
					else {
						PriorityInfo.getList().clear();
						if (!deviceList.isEmpty()) {
							try {
								JSONObject json = new JSONObject(deviceList);

								@SuppressWarnings("rawtypes")
								Iterator i = json.keys();
								while (i.hasNext()) {
									String key = i.next().toString();
									
								}
							}
							catch (JSONException e) {
								e.printStackTrace();
							}
						}
						
						adapter.notifyDataSetChanged();
						showToast(getString(R.string.string_connection_lost));
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_setting_outbound_call, container, false);
		initializedUI();

		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.smartbean.servertalk.action.SENDING_OUTBOUND_PRIORITYLIST_CALLBACK");
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

	protected void initializedUI() {
		nameTitleHeader = (TextView) view.findViewById(R.id.fragment_setting_outbound_call_unnamed_doortalk_outbound_call);
		btnSaveOutboundCall = (Button) view.findViewById(R.id.setting_button_save_outbound_call);
		btnAddOutboundCall = (Button) view.findViewById(R.id.setting_button_add_outbound_call);
		lvOutBoundCall = (ListView) view.findViewById(R.id.setting_listview_outbound_call);
		adapter = new CustomArrayAdapter(getActivity(), PriorityInfo.getList());
		lvOutBoundCall.setAdapter(adapter);
		
		nameTitleHeader.setText("OUTBOUND CALL LIST");
		
		btnSaveOutboundCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, " outbound call " + PriorityInfo.getList());
				JSONObject json = new JSONObject();
				try {
					for (int i = 0; i < PriorityInfo.getList().size(); i++) {
						json.put(PriorityInfo.getList().get(i).getPriority(), PriorityInfo.getList().get(i).getPartyUri() + "@"+ PriorityInfo.getList().get(i).getDisplayName());
					}
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
				
				Global.getInstance().setOutboundPriorityList(json.toString());
				if(Global.getInstance().getOutboundPriorityList() != null) 
				{
					RingProgressDialogManager.show(getActivity(), "Please wait...", "Sending outbound priority list...");
					new Thread(new SocClient("outbound_prioritylist", CommonUtilities.soc_port, getActivity())).start();
				}
			}
		});
		
		btnAddOutboundCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (PriorityInfo.getList().size() == 5) {
					showToast("Call priority is limited to 5");
					return;
				} 
				showCreateDialog();
			}
		});
	}

	private void showCreateDialog() {
		final Dialog createDialog = new Dialog(getActivity());
		createDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		createDialog.setContentView(R.layout.dialog_add_outbound_calls);

		final EditText callNumberEditText = (EditText) createDialog.findViewById(R.id.dialog_add_outbound_calls_edittext_receive_number);
		final EditText informationEditText = (EditText) createDialog.findViewById(R.id.dialog_add_outbound_calls_edittext_email);
		final EditText priorityEditText = (EditText) createDialog.findViewById(R.id.dialog_add_outbound_calls_edittext_priority);
		final Button saveButton = (Button) createDialog.findViewById(R.id.dialog_add_outbound_calls_button_save);

		priorityEditText.setText(Integer.toString(PriorityInfo.getList().size() + 1));
			
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (informationEditText.getText().toString().length() < 1 || callNumberEditText.getText().toString().length() < 1) {
					showToast("Don't leave any fields empty");
					return;
				}
				
				for(int i = 0; i < PriorityInfo.getList().size(); i++ ) {
					if(callNumberEditText.getText().toString().equals(PriorityInfo.getList().get(i).getPartyUri())) {
						showToast("Callee is already a priority");
						return;
					}
				}
				
				PriorityInfo.getList().add(new PriorityInfo(priorityEditText.getText().toString(), callNumberEditText.getText().toString(), informationEditText.getText().toString()));
				Log.d(TAG, " number " + callNumberEditText.getText().toString() + " info " + informationEditText.getText().toString());
				adapter.notifyDataSetChanged();
				createDialog.dismiss();
			}
		});
		createDialog.show();
	}
	
	public static class PriorityInfo {
		String displayName;
		String partyUri;
		String priority;

		private static List<PriorityInfo> dataList = null;

		public PriorityInfo(String priority, String partyUri, String displayName) {
			super();
			this.displayName = displayName;
			this.partyUri = partyUri;
			this.priority = priority;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getPriority() {
			return priority;
		}

		public void setPriority(String priority) {
			this.priority = priority;
		}

		public String getPartyUri() {
			return partyUri;
		}

		public void setPartyUri(String partyUri) {
			this.partyUri = partyUri;
		}

		public static synchronized List<PriorityInfo> getList() {
			if (null == dataList) {
				dataList = new ArrayList<PriorityInfo>();
			}
			return dataList;
		}
	}

	private class CustomArrayAdapter extends ArrayAdapter<PriorityInfo> {
		private Context context;
		private List<PriorityInfo> serverDataList;

		public CustomArrayAdapter(Context context, List<PriorityInfo> serverDataList) {
			super(context, 0, serverDataList);
			this.context = context;
			this.serverDataList = serverDataList;
		}

		@Override
		public PriorityInfo getItem(int position) {
			return serverDataList.get(position);
		}

		public class ViewHolder {
			TextView displayName;
			TextView partyUri;
			TextView priority;
			Button delete;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder viewHolder = new ViewHolder();
			if (view == null) {
				LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				view = layoutInflater.inflate(R.layout.setting_outbound_call_adapter, null);
				viewHolder.displayName = (TextView) view.findViewById(R.id.setting_textview_outbound_information);
				viewHolder.partyUri = (TextView) view.findViewById(R.id.setting_textview_number_outbound);
				viewHolder.priority = (TextView) view.findViewById(R.id.setting_textview_outbound_information_priority);
				viewHolder.delete = (Button) view.findViewById(R.id.setting_imagebutton_delete_outbound);
				view.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder) view.getTag();
			}
			PriorityInfo serverData = getItem(position);
			if (serverData != null) {
				if (viewHolder.displayName != null) {
					Log.d(TAG, " serverData.getDisplayName() " + serverData.getDisplayName() + serverData.displayName);
					viewHolder.displayName.setText(serverData.getDisplayName());
				}
				if (viewHolder.partyUri != null) {
					viewHolder.partyUri.setText(serverData.getPartyUri());
				}
				if (viewHolder.priority != null) {
					viewHolder.priority.setText(serverData.getPriority());
				}
			}
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					final AlertDialog.Builder removeDeviceDialogBuilder = new AlertDialog.Builder(getActivity());
					removeDeviceDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
					removeDeviceDialogBuilder.setTitle("Delete");
					removeDeviceDialogBuilder.setMessage("Are you sure you want to remove " + PriorityInfo.getList().get(position).getPartyUri() + " from the list?");

					removeDeviceDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int index) {
							String deletedPriority = "0";
							deletedPriority = PriorityInfo.getList().get(position).getPriority();
							PriorityInfo.getList().remove(position);
							
							for(int i = Integer.parseInt(deletedPriority) - 1; i < PriorityInfo.getList().size(); i++) {
								PriorityInfo.getList().get(i).setPriority(Integer.toString(i + 1));
							}
							
							adapter.notifyDataSetChanged();
							dialog.dismiss();
						}
					});

					removeDeviceDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int index) {
							dialog.dismiss();
						}
					});

					removeDeviceDialogBuilder.show();
				}
			});

			return view;
		}
	}
	
	protected void showToast(String message) {
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
	
}
