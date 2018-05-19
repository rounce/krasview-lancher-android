package ru.krasview.launcher;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	final static String TAG = "MainActivity";
	Market market;
	Activity context = this;

	private IntentFilter mNetworkStateChangedFilter;
	private BroadcastReceiver mNetworkStateIntentReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parser.setContext(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Toast.makeText(context, "onCreate", Toast.LENGTH_LONG).show();

		updateScreen();

		market = new Market(this);

		mNetworkStateChangedFilter = new IntentFilter();
		mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		mNetworkStateIntentReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					//NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					if (isOnline()) {
						new MyAsync().execute();
					}
				}
			}
		};
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class MyAsync extends AsyncTask<Object, Object, String> {
		@Override
		protected String doInBackground(Object... params) {
			String result = Parser.getXML("http://tv.kraslan.ru/api/app/android/actual2.xml");
			Log.i(TAG, result);
			return result;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Document actual;
			actual = Parser.XMLfromString(result);
			actual.normalizeDocument();
			Node node0 = actual.getElementsByTagName("application").item(1);
			Log.i(TAG, "" + Parser.getValue(market.KEY_PACKAGE, node0) + " "
					+ Parser.getValue(market.KEY_VERSION_CODE, node0));
			if (!market.packageInstall(Parser.getValue(market.KEY_PACKAGE, node0)) || market.checkNewVersion(Parser.getValue(market.KEY_PACKAGE, node0), Integer.parseInt(Parser.getValue(market.KEY_VERSION_CODE, node0)))) {
				try {
					market.installApp(Parser.getValue(market.KEY_URL, node0));
					Log.i(TAG, "Ланчер установлен");
					//context.finish();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Node node = actual.getElementsByTagName("application").item(0);
				if (!market.packageInstall(Parser.getValue(market.KEY_PACKAGE, node)) || market.checkNewVersion(Parser.getValue(market.KEY_PACKAGE, node), Integer.parseInt(Parser.getValue(market.KEY_VERSION_CODE, node)))) {
					try {
						market.installApp(Parser.getValue(market.KEY_URL, node));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Intent intent = new Intent("tv.intent.action.LAUNCH");
					startActivity(intent);
					Log.i("MainActivity", "запустить приложение");
					//context.finish();
				}
			}
		}
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null) {
			Toast.makeText(context, netInfo.toString(), Toast.LENGTH_LONG).show();
		}
		if (netInfo != null && netInfo.isConnected()) {
			Toast.makeText(context, "Подключено!", Toast.LENGTH_LONG).show();
			return true;
		}
		if (netInfo.isConnectedOrConnecting()) {
			Toast.makeText(context, "Подключаемся!", Toast.LENGTH_LONG).show();
			return false;
		}
		Toast.makeText(context, "Подключение отсутствует!", Toast.LENGTH_LONG).show();
		return false;
	}

	private void updateScreen() {
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
		unregisterReceiver(mNetworkStateIntentReceiver);
		registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
		isOnline();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		unregisterReceiver(mNetworkStateIntentReceiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Операции для выбранного пункта меню
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent wifi = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
				startActivity(wifi);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
