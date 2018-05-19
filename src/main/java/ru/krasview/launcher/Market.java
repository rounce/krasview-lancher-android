package ru.krasview.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Market {
	
	final String KEY_NAME = "name";
	final String KEY_PACKAGE = "package";
	final String KEY_VERSION_CODE = "versionCode";
	final String KEY_VERSION_NAME = "versionName";
	final String KEY_URL = "url";
	
	Activity mContext;
	
	Market(Activity context){
		mContext = context;
	}
	
	 public boolean checkNewVersion(String packageName, int versionCodeNew) {
	        List<ApplicationInfo> apps = mContext.getPackageManager()
	                .getInstalledApplications(0);
	        for (int i = 0; i < apps.size(); i++) {
	            ApplicationInfo app = apps.get(i);
	            if (packageName.equals(app.packageName)) {
	                PackageManager manager = mContext.getPackageManager();
	                PackageInfo info;
	                try {
	                    info = manager.getPackageInfo(app.packageName, 0);
	                    int versionCode = info.versionCode;
	                    if (versionCodeNew > versionCode) {
	                      //  Toast.makeText(mContext, "New Version!", Toast.LENGTH_LONG)
	                       //         .show();
	                        return true;
	                    }
	                } catch (NameNotFoundException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return false;
	    }
	 
	 public boolean packageInstall(String packageName){
		 List<ApplicationInfo> apps = mContext.getPackageManager()
	                .getInstalledApplications(0);
	        for (int i = 0; i < apps.size(); i++) {
	            ApplicationInfo app = apps.get(i);
	            if (packageName.equals(app.packageName)) {
	            	return true;
	            }
	        }
	     //   Toast.makeText(mContext, "Not installed!", Toast.LENGTH_LONG).show();
		return false;
	 };
	 
	 public void installApp(final String addr) throws IOException
		{
			Thread t;
			
			t=new Thread(new Runnable(){

				public void run() {
					// TODO Auto-generated method stub
					URL url=null;
					try {
						url = new URL(addr);
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					HttpURLConnection c=null;
					try {
						c = (HttpURLConnection) url.openConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					c.setDoInput(true);
					try {
						c.setRequestMethod("GET");
						Log.i("Market", "Успешно установлен протокол запроса " + c.getRequestMethod() );
					} catch (ProtocolException e1) {
						// TODO Auto-generated catch block

						Log.e("Market", "Ошибка установления протокола запроса");
						e1.printStackTrace();
					}
					//c.setDoOutput(true);
					String path = Environment.getExternalStorageDirectory() + "/download/";
					Log.i("MainActivity", path);
					File file = new File(path);
					if(file.mkdirs()){
						Log.i("MainActivity", "Создание папок. Успешно");
					}else{
						Log.i("MainActivity", "Создание папок. Ошибка либо папки уже существуют");
					}
					File outputFile = new File(file, "main.apk");
					FileOutputStream fos=null;
					try {
						fos = new FileOutputStream(outputFile);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InputStream is=null;
					try {
						is = c.getInputStream();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("Market", "Ошибка");
					}
					byte[] buffer = new byte[1024];
					int len1 = 0;
					try {
						while ((len1 = is.read(buffer)) != -1) {
						        fos.write(buffer, 0, len1);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "main.apk")), "application/vnd.android.package-archive");
					mContext.startActivity(intent);
				}});
			Log.i("MainActivity", "Выбран пункт Загрузить программу");
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
