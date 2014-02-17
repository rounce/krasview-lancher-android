package ru.krasview.launcher;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UpdateActivity extends Activity {
	
	ProgressBar pb;
	TextView tv;
	Button but;
	
	Market market;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.dialog_update);
		 pb = (ProgressBar)findViewById(R.id.progressBar1);
		 tv = (TextView)findViewById(R.id.textView1);
		 but = (Button)findViewById(R.id.button1);
		 Parser.setContext(this);
		 market = new Market(this);
		 new MyAsync().execute();
	 }
	 
	 private Handler h = new Handler();
	 
	 private class MyAsync extends AsyncTask<Object, Object, Node>{

			@Override
			protected Node doInBackground(Object... params) {
				// TODO Auto-generated method stub
				String result =  Parser.getXML("http://tv.kraslan.ru/api/app/android/actual.xml");			
				Document actual;
				actual = Parser.XMLfromString(result);
				actual.normalizeDocument();
				
				Node node0 = actual.getElementsByTagName("application").item(1);
			    if(!market.packageInstall(Parser.getValue(market.KEY_PACKAGE, node0))||market.checkNewVersion(Parser.getValue(market.KEY_PACKAGE, node0), Integer.parseInt(Parser.getValue(market.KEY_VERSION_CODE, node0)))){
			    	
			    	return node0;
				}else{
			    	Node node = actual.getElementsByTagName("application").item(0);
				    if(!market.packageInstall(Parser.getValue(market.KEY_PACKAGE, node))||market.checkNewVersion(Parser.getValue(market.KEY_PACKAGE, node), Integer.parseInt(Parser.getValue(market.KEY_VERSION_CODE, node))))
				    {
				    	return node;
				    }else{
			    	return null;
				    }
				    }
			    }
			
			@Override
			protected void onPostExecute( final Node result){
				super.onPostExecute(result);
				pb.setVisibility(View.GONE);
				if(result == null){
					tv.setText("Обновлений не найдено");
					h.postDelayed(new Runnable(){

						@Override
						public void run() {
							UpdateActivity.this.finish();
						}}, 1000);
				}else{
					String name = Parser.getValue(market.KEY_PACKAGE, result);
					if(name.equals("ru.krasview.tv")){
						name = "Телевидение";
					}else if(name.equals("ru.krasview.launcher")){
						name = "ТВ Ланчер";
					}
					tv.setText("Найдено обновление для пакета " + name);

					but.setVisibility(View.VISIBLE);
					but.requestFocus();
					but.setText("Установить");
					but.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							try {
								market.installApp(Parser.getValue(market.KEY_URL, result));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							UpdateActivity.this.finish();
						}
						
					});
				}
				
				
			}
		}

}
