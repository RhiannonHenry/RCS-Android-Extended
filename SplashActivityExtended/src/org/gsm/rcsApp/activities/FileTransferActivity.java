package org.gsm.rcsApp.activities;

import java.io.UnsupportedEncodingException;

import org.apache.http.auth.AuthScope;
import org.apache.http.entity.StringEntity;
import org.gsm.RCSDemo.R;
import org.gsm.rcsApp.ServiceURL;
import org.gsm.rcsApp.RCS.ChatMessage;
import org.gsm.rcsApp.RCS.Contact;
import org.gsm.rcsApp.RCS.ContactStateManager;
import org.gsm.rcsApp.misc.RCSJsonHttpResponseHandler;
import org.gsm.rcsApp.misc.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FileTransferActivity extends Activity{

	private static Handler closeHandler = null;
	private static Handler errorHandler = null;
	static FileTransferActivity _instance = null;
	String destinationUri;
	String senderUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filetransfer);

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

	public void cancelFileTransfer(View view) {
		finish();
	}

	public void sendFileURL(View view) {
		Context context = getApplicationContext();
		CharSequence text = "Sending File....";
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.show();
		Intent intent = getIntent();
		destinationUri = (String) intent.getExtras().get(destinationUri);
		destinationUri = destinationUri.replace("sip:", "tel:").replace("@rcstestconnect.net", "");
		Log.v("FileTransferActivity", "Destination Address = " + destinationUri);
		senderUri = "tel:" + SplashActivity.userId;
		Log.v("FileTransferActivity", "Sender Address = " + senderUri);
		EditText URLInputBox = (EditText) findViewById(R.id.fileTransferURLTextbox);
		final Editable URL = URLInputBox.getText();
		Thread t = new Thread() {
			public void run() {
				String trimmedURL = URL.toString().trim();

				if (trimmedURL.length() > 0) {
					String jsonData = "{\"fileTransferSessionInformation\": {\"fileInformation\": {\"fileDescription\": \"This is my latest picture\",\"fileDisposition\": \"Render\","
							+ "\"fileSelector\": {\"name\": \"tux.png\",\"size\": 56320,\"type\": \"image/png\"},\"fileURL\":\""
							+ trimmedURL
							+ "\"},"
							+ "\"originatorAddress\": \""
							+ senderUri
							+ "\",\"originatorName\": \"G3\",\"receiverAddress\": \""
							+ destinationUri + "\",\"receiverName\": \"G4\"}}";
					Log.v("FileTransferActivity", "Request Data = " + jsonData);

					sendFile(jsonData);

					finish();
				}
			}
		};
		t.start();
		URLInputBox.setText("");
	}

	private void sendFile(String jsonData) {
		final String url = ServiceURL.sendFileURL(SplashActivity.userId);
		Log.v("FileTransferActivity", "URL = "+url);
		senderUri = "tel:"+SplashActivity.userId;
		AsyncHttpClient client = new AsyncHttpClient();
        AuthScope authscope=new AuthScope(ServiceURL.serverName, ServiceURL.serverPort, AuthScope.ANY_REALM);
        client.setBasicAuth(SplashActivity.userId, SplashActivity.appCredentialPassword, authscope);
		
        try{
        	StringEntity requestData=new StringEntity(jsonData);
	        
	        client.post(_instance.getApplication().getApplicationContext(),
	        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
	        	@Override
	        	public void onSuccess(JSONObject response, int errorCode) {
	        		if(errorCode == 201){
	        		Log.v("FileTransferActivity", "sendFile::success = "+response.toString()+" httpCode="+errorCode);

	        		JSONObject resourceReference = Utils.getJSONObject(response, "resourceReference");
	        		String resourceURL = Utils.getJSONStringElement(resourceReference, "resourceURL");
	        		String[] parts = resourceURL.split("/sessions/");
	        		String senderSessionID = parts[1];
        			Log.v("FileTransferActivity", "sessionID="+senderSessionID+" resourceURL="+resourceURL);
	        		}else{
	        			Log.v("FileTransferActivity", "Response Code = "+errorCode);
	        		}
	        	}
	        	
	        	public void onFailure(Throwable e, String response) {
					Log.d("FileTransferActivity", "No response from " + url);
					Log.d("FileTransferActivity", "HTTP Code = "+response);
				}
	        });
        }catch (UnsupportedEncodingException e) { }
	}
}
