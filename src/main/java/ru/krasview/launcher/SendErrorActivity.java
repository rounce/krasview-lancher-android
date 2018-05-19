package ru.krasview.launcher;

import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SendErrorActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_error);
		Intent intent = getIntent();
		final String stacktrace = intent.getStringExtra("stacktrace");
		Log.i("SendErrorActivity", "Активити запущена \n" + stacktrace);
		Parser.setContext(this);
		Thread t = new Thread(new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Parser.getXML("http://tv.kraslan.ru/api/tv/bug", "text=" + URLEncoder.encode(stacktrace));
			}

		});
		t.start();
	}

	public void onClick(View v) {
		Log.i("SendErrorActivity", "Кнопка нажата");
		Intent i = new Intent(getBaseContext(), MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getBaseContext().startActivity(i);
		finish();
	}
}
