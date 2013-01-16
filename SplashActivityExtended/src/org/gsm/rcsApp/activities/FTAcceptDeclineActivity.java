package org.gsm.rcsApp.activities;

import org.apache.http.auth.AuthScope;
import org.apache.http.entity.StringEntity;
import org.gsm.RCSDemo.R;
import org.gsm.rcsApp.ServiceURL;
import org.gsm.rcsApp.misc.RCSJsonHttpResponseHandler;
import org.gsm.rcsApp.misc.Utils;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class FTAcceptDeclineActivity extends Activity{
	private static Handler closeHandler = null;
	private static Handler errorHandler = null;
	static FTAcceptDeclineActivity _instance = null;
	String originator;
	String recipient;
	String sessionID;
	String username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ftacceptdecline);

		_instance = this;

		closeHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				finish();
			}
		};
		errorHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				int code = msg.what;
				String error = (String) msg.obj;
				Log.v("GetCapabilitiesActivity", "Error " + code
						+ " description=" + error);
				Context context = getApplicationContext();
				CharSequence text = "Error " + code
						+ (error != null ? " \"" + error + "\"" : "");
				Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
				toast.show();
			}
		};
	}
	
	public void acceptFileTransferClicked(View view){
		// Display Toast 'File Transfer Accepted'
		Context context = getApplicationContext();
		CharSequence text = "File Transfer Accepted....";
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.show();
		// Get variables passed through...
		Intent intent = getIntent();
		originator = (String)intent.getExtras().getString(originator);
		recipient = (String)intent.getExtras().getString(recipient);
		sessionID = (String)intent.getExtras().getString(sessionID);
		Log.v("FTAcceptDeclineActivity", "[Accept] Originator = "+originator);
		Log.v("FTAcceptDeclineActivity", "[Accept] Recipient = "+recipient);
		Log.v("FTAcceptDeclineActivity", "[Accept] SessionID = "+sessionID);
		username = SplashActivity.userId;
		Log.v("FTAcceptDeclineActivity", "[Accept] Username = "+username);
		final String url = ServiceURL.fileTransferStatusURL(username, sessionID);
		Log.v("FTAcceptDeclineActivity", "[Accept] URL = "+url);
		final String acceptData = "{\"receiverSessionStatus\": {\"status\": \"Connected\"}}";
		Log.v("FTAcceptDeclineActivity", "[Accept] Request Data = "+acceptData);
		AsyncHttpClient client = new AsyncHttpClient();
        AuthScope authscope=new AuthScope(ServiceURL.serverName, ServiceURL.serverPort, AuthScope.ANY_REALM);
        client.setBasicAuth(SplashActivity.userId, SplashActivity.appCredentialPassword, authscope);
		
        try {
        	StringEntity requestData=new StringEntity(acceptData);
			client.post(_instance.getApplication().getApplicationContext(),
	        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
				@Override
	        	public void onSuccess(JSONObject response, int errorCode) {
	        		if(errorCode == 204){
	        		Log.v("FTAcceptDeclineActivity", "acceptFileTransfer::success = "+response.toString()+" httpCode="+errorCode);
	        		}
	        	}
				public void onFailure(Throwable e, String response) {
					Log.d("FTAcceptDeclineActivity", "No response from " + url);
					Log.d("FTAcceptDeclineActivity", "acceptFileTransfer::error = "+response);
				}
			});
		} catch (Exception e) {	}
        finish();
	}
	
	public void declineFileTransferClicked(View view){
		// Display Toast 'File Transfer Declined'
		Context context = getApplicationContext();
		CharSequence text = "File Transfer Declined....";
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.show();
		// send decline...
		Intent intent = getIntent();
		sessionID = (String)intent.getExtras().getString(sessionID);
		Log.v("FTAcceptDeclineActivity", "[Decline] SessionID = "+sessionID);
		
		username = SplashActivity.userId;
		Log.v("FTAcceptDeclineActivity", "[Decline] Username = "+username);
		
		final String url = ServiceURL.fileTransferSessionURL(username, sessionID);
		Log.v("FTAcceptDeclineActivity", "[Decline] URL = "+url);
		AsyncHttpClient client = new AsyncHttpClient();
        AuthScope authscope=new AuthScope(ServiceURL.serverName, ServiceURL.serverPort, AuthScope.ANY_REALM);
        client.setBasicAuth(SplashActivity.userId, SplashActivity.appCredentialPassword, authscope);
        
        try {
        	client.delete(_instance.getApplication().getApplicationContext(),
	        		url, new RCSJsonHttpResponseHandler() {
        		@Override
	        	public void onSuccess(JSONObject response, int errorCode) {
	        		if(errorCode == 201){
	        		Log.v("FTAcceptDeclineActivity", "declineFileTransfer::success = "+response.toString()+" httpCode="+errorCode);
	        		}else{
	        			Log.v("FTAcceptDeclineActivity", "unexpectedResponse = "+errorCode);
	        		}
	        	}
        		public void onFailure(Throwable e, String response) {
					Log.d("FTAcceptDeclineActivity", "No response from " + url);
					Log.d("FTAcceptDeclineActivity", "declineFileTransfer::error = "+response);
				}
        	});
		} catch (Exception e) {}
		// return user to previous screen
		finish();
	}
}
