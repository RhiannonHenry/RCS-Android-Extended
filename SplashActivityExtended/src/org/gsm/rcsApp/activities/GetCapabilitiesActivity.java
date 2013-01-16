package org.gsm.rcsApp.activities;

import java.io.UnsupportedEncodingException;
import org.apache.http.auth.AuthScope;
import org.apache.http.entity.StringEntity;
import org.gsm.RCSDemo.R;
import org.gsm.rcsApp.ServiceURL;
import org.gsm.rcsApp.misc.RCSJsonHttpResponseHandler;
import org.gsm.rcsApp.misc.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GetCapabilitiesActivity extends Activity {
	private static Handler closeHandler = null;
	private static Handler errorHandler = null;
	private String address;
	private boolean im_session = false;
	private boolean file_transfer = false;
	private boolean image_share = false;
	private boolean video_share = false;
	private boolean social_presence = false;
	private boolean discovery_presence = false;
	ToggleButton im_sessionToggle;
	ToggleButton file_transferToggle;
	ToggleButton image_shareToggle;
	ToggleButton video_shareToggle;
	ToggleButton social_presenceToggle;
	ToggleButton discovery_presenceToggle;

	static GetCapabilitiesActivity _instance = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capabilities);

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

		im_sessionToggle = (ToggleButton) findViewById(R.id.im_sessionToggle);
		file_transferToggle = (ToggleButton) findViewById(R.id.file_transferToggle);
		image_shareToggle = (ToggleButton) findViewById(R.id.image_shareToggle);
		video_shareToggle = (ToggleButton) findViewById(R.id.video_shareToggle);
		social_presenceToggle = (ToggleButton) findViewById(R.id.social_presenceToggle);
		discovery_presenceToggle = (ToggleButton) findViewById(R.id.discovery_presenceToggle);

		im_sessionToggle.setChecked(false);
		file_transferToggle.setChecked(false);
		image_shareToggle.setChecked(false);
		video_shareToggle.setChecked(false);
		social_presenceToggle.setChecked(false);
		discovery_presenceToggle.setChecked(false);
	}

	public void onStart() {
		super.onStart();

		getCapabilities();
	}

	private void getCapabilities() {
		final String url = ServiceURL.capabilitiesURL(SplashActivity.userId);
		im_sessionToggle = (ToggleButton) findViewById(R.id.im_sessionToggle);
		file_transferToggle = (ToggleButton) findViewById(R.id.file_transferToggle);
		image_shareToggle = (ToggleButton) findViewById(R.id.image_shareToggle);
		video_shareToggle = (ToggleButton) findViewById(R.id.video_shareToggle);
		social_presenceToggle = (ToggleButton) findViewById(R.id.social_presenceToggle);
		discovery_presenceToggle = (ToggleButton) findViewById(R.id.discovery_presenceToggle);

		if (SplashActivity.userId != null) {
			AsyncHttpClient client = new AsyncHttpClient();
			AuthScope authscope = new AuthScope(ServiceURL.serverName,
					ServiceURL.serverPort, AuthScope.ANY_REALM);
			client.setBasicAuth(SplashActivity.userId,
					SplashActivity.appCredentialPassword, authscope);

			client.get(url, new RCSJsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject response, int errorCode) {
					Log.i("GetCapabilitiesActivity",
							"getCapabilities httpCode="
									+ errorCode
									+ " response="
									+ (response != null ? response.toString()
											: null));
					if (response != null) {
						try {
							if (errorCode == 200) {
								JSONObject capabilities = Utils.getJSONObject(
										response, "capabilities");
								Log.i("GetCapabilitiesActivity",
										"getJSONObject    "
												+ capabilities
														.getBoolean("im_session"));
								address = capabilities.getString("address");
								im_session = capabilities
										.getBoolean("im_session");
								im_sessionToggle.setChecked(im_session);
								file_transfer = capabilities
										.getBoolean("file_transfer");
								file_transferToggle.setChecked(file_transfer);
								image_share = capabilities
										.getBoolean("image_share");
								image_shareToggle.setChecked(image_share);
								video_share = capabilities
										.getBoolean("video_share");
								video_shareToggle.setChecked(video_share);
								social_presence = capabilities
										.getBoolean("social_presence");
								social_presenceToggle
										.setChecked(social_presence);
								discovery_presence = capabilities
										.getBoolean("discovery_presence");
								discovery_presenceToggle
										.setChecked(discovery_presence);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				public void onFailure(Throwable e, String response) {
					Log.d("GetCapabilitiesActivity", "No response from " + url);
				}
			});
		}

	}

	public void setCapabilities(View view) {
		final String url = ServiceURL.capabilitiesURL(SplashActivity.userId);
		address = "tel:"+SplashActivity.userId;
		Log.v("GetCapabilitiesActivity",
				"address = "+address);
		Log.v("GetCapabilitiesActivity",
				"URL = "+url);
		AsyncHttpClient client = new AsyncHttpClient();
		AuthScope authscope = new AuthScope(ServiceURL.serverName,
				ServiceURL.serverPort, AuthScope.ANY_REALM);
		client.setBasicAuth(SplashActivity.userId,
				SplashActivity.appCredentialPassword, authscope);
		int toggle = ((ToggleButton) view).getId();

		if (toggle == R.id.im_sessionToggle) {
			im_session = !im_session;
		} else if (toggle == R.id.file_transferToggle) {
			file_transfer = !file_transfer;
		} else if (toggle == R.id.image_shareToggle) {
			image_share = !image_share;
		} else if (toggle == R.id.video_shareToggle) {
			video_share = !video_share;
		} else if (toggle == R.id.social_presenceToggle) {
			social_presence = !social_presence;
		} else if (toggle == R.id.discovery_presenceToggle) {
			discovery_presence = !discovery_presence;
		}
		String jsonData = "{\"capabilities\":{\"address\":\"" + address
				+ "\",\"imSession\":" + im_session + ",\"fileTransfer\":"
				+ file_transfer + ",\"imageShare\":" + image_share
				+ ",\"videoShare\":" + video_share + ",\"socialPresence\":"
				+ social_presence + ",\"discoveryPresence\":"
				+ discovery_presence + "}}";
		Log.v("GetCapabilitiesActivity",
				"RequestData = "+jsonData);
		StringEntity requestData;
		try {
			requestData = new StringEntity(jsonData);

			client.put(_instance.getApplication().getApplicationContext(),
					url, requestData, "application/json",
					new RCSJsonHttpResponseHandler() {
						@Override
						public void onSuccess(JSONObject response,
								int statusCode) {
							Log.i("GetCapabilitiesActivity",
									"setCapabilities response = "
											+ response.toString()
											+ " statusCode=" + statusCode);

							if (statusCode == 204) {
								Log.i("GetCapabilitiesActivity",
										"SUCCESS - Capabilities have been set...");
							}
						}
					});
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getCapabilities(View view){
		final String url = ServiceURL.capabilitiesURL(SplashActivity.userId);
		if (SplashActivity.userId != null) {
			AsyncHttpClient client = new AsyncHttpClient();
			AuthScope authscope = new AuthScope(ServiceURL.serverName,
					ServiceURL.serverPort, AuthScope.ANY_REALM);
			client.setBasicAuth(SplashActivity.userId,
					SplashActivity.appCredentialPassword, authscope);

			client.get(url, new RCSJsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject response, int errorCode) {
					Log.i("GetCapabilitiesActivity",
							"getCapabilities httpCode="
									+ errorCode
									+ " response="
									+ (response != null ? response.toString()
											: null));
					if (response != null) {
						try {
							if (errorCode == 200) {
								JSONObject capabilities = Utils.getJSONObject(
										response, "capabilities");
								Log.i("GetCapabilitiesActivity",
										"getJSONObject    "
												+ capabilities
														.getBoolean("im_session"));
								address = capabilities.getString("address");
								im_session = capabilities
										.getBoolean("im_session");
								im_sessionToggle.setChecked(im_session);
								file_transfer = capabilities
										.getBoolean("file_transfer");
								file_transferToggle.setChecked(file_transfer);
								image_share = capabilities
										.getBoolean("image_share");
								image_shareToggle.setChecked(image_share);
								video_share = capabilities
										.getBoolean("video_share");
								video_shareToggle.setChecked(video_share);
								social_presence = capabilities
										.getBoolean("social_presence");
								social_presenceToggle
										.setChecked(social_presence);
								discovery_presence = capabilities
										.getBoolean("discovery_presence");
								discovery_presenceToggle
										.setChecked(discovery_presence);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				public void onFailure(Throwable e, String response) {
					Log.d("GetCapabilitiesActivity", "No response from " + url);
				}
			});
		}
	}
}
