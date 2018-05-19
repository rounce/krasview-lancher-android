package ru.krasview.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewActivity extends Activity {
	Market market;
	boolean alrUpdate;
	boolean waitNetwork;

	String[] names = {"Телевидение",
			"Сериалы",
			"Аниме",
			"Фильмы",
			"Настройки"};
	String[] intents = {"krasview.intent.action.LAUNCH",
			"krasview.intent.action.LAUNCH",
			"krasview.intent.action.LAUNCH",
			"krasview.intent.action.LAUNCH",
			Settings.ACTION_SETTINGS};
	int[] images = {R.drawable.tv,
			R.drawable.series,
			R.drawable.anime,
			R.drawable.series,
			R.drawable.settings};
	String[] addresses = {
			"http://tv.kraslan.ru/api/tv/get.xml",
			"http://tv.kraslan.ru/api/series/submenu.xml",
			"http://tv.kraslan.ru/api/anime/submenu.xml",
			"http://tv.kraslan.ru/api/movie/submenu.xml"
	};

	private IntentFilter mNetworkStateChangedFilter;
	private BroadcastReceiver mNetworkStateIntentReceiver;

	GradientDrawable grad;
	View view;

	LinearLayout mainLayout;

	/**
	 * Called when the activity is first created.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_new);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Log.i("Debug", "density " + metrics.density);

		mainLayout = (LinearLayout) findViewById(R.id.main);
		grad = new GradientDrawable(Orientation.TL_BR, new int[]{getResources().getColor(R.color.black_2), //светлый в центре
				getResources().getColor(R.color.black_1)});//темный с краю
		grad.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		grad.setGradientRadius(100);
		grad.setGradientCenter(0.5f, 0.5f);
		mainLayout.setBackgroundDrawable(grad);

		LayoutInflater ltInflater = getLayoutInflater();
		for (int i = 0; i < 4; i++) {
			view = ltInflater.inflate(R.layout.new_item, mainLayout, false);
			mainLayout.addView(view);
			TextView tv = (TextView) view.findViewById(R.id.text);
			tv.setFocusable(true);
			Log.i("Debug", "focus " + tv.isFocusable());
			tv.setText(names[i]);
			Log.i("Debug", "Color: " + Color.green(tv.getTextColors().getDefaultColor()));
			ImageView image = (ImageView) view.findViewById(R.id.image);
			//image.setImageResource(images[i]);
			image.setImageDrawable(this.getResources().getDrawable(images[i]));
			view.setTag(i);
			view.setOnClickListener(listener);
			if (i == 0) {
				view.requestFocus();
			}
		}

		alrUpdate = false;
		waitNetwork = false;
		Parser.setContext(this);
		market = new Market(this);

		mNetworkStateChangedFilter = new IntentFilter();
		mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		mNetworkStateIntentReceiver = new WiFiReceiver();

		boolean a = checkWiFi();
		if (a && !alrUpdate) {
			alrUpdate = true;
			Intent i = new Intent(this, UpdateActivity.class);
			startActivity(i);
		} else if (!a) {
			waitNetwork = true;
		}
	}

	public View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			int a = (Integer) view.getTag();
			if (a < 4 && !market.packageInstall("ru.krasview.tv")) {
				Toast.makeText(NewActivity.this, "Приложение Телевидение не установлено \nПроверьте наличие обновлений(Меню->Обновление)", Toast.LENGTH_LONG).show();
				return;
			}
			Intent intent = new Intent();
			intent.setAction(intents[a]);
			if (a < 4) {
				intent.putExtra("address", addresses[a]);
			}
			startActivity(intent);
		}
	};

	public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
		if ((arg2 == 0 || arg2 == 1) && !market.packageInstall("ru.krasview.tv")) {
			Toast.makeText(NewActivity.this, "Приложение Телевидение не установлено \nПроверьте наличие обновлений(Меню->Обновление)", Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(intents[arg2]);
		startActivity(intent);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
		unregisterReceiver(mNetworkStateIntentReceiver);
		registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);

		final ViewTreeObserver observer = mainLayout.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				//по ширине
				grad.setGradientRadius(mainLayout.getWidth());
			}

		});

	}

	private class WiFiReceiver extends BroadcastReceiver {
		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (checkWiFi(info) && !alrUpdate && waitNetwork) {
					alrUpdate = true;
					waitNetwork = false;
					Intent i = new Intent(NewActivity.this, UpdateActivity.class);
					startActivity(i);
				}
			}
		}
	}

	private boolean checkWiFi() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return checkWiFi(netInfo);
	}

	private boolean checkWiFi(NetworkInfo info) {

		if (info == null) {
			//data.get(2).put("title", WIFI + "\n(" + WF_STAT_DISCONNECT + ")");
			//adapter.notifyDataSetChanged();
			return false;
		}
		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			if (info.isConnected()) {
				return true;
			}
			if (info.isConnectedOrConnecting()) {
				return true;
			}
			if (!info.isAvailable()) {
				return false;
			}
			return false;
		} else {
			if (info.isConnectedOrConnecting()) {
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_new, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Операции для выбранного пункта меню
		switch (item.getItemId()) {
			case R.id.update:
				if (!checkWiFi()) {
					Toast.makeText(NewActivity.this, "Невозможно проверить обновления. \nНет подключения к сети.", Toast.LENGTH_LONG).show();
					return true;
				}
				Intent i = new Intent(this, UpdateActivity.class);
				startActivity(i);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_UP) {
			listener.onClick(this.getCurrentFocus());
		}
		return super.dispatchKeyEvent(event);
	}
}
