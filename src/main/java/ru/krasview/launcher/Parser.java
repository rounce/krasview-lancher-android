package ru.krasview.launcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class Parser {
	private static final String TAG = "Parser";
	
	static Context mContext = null;
	
	public static void setContext(Context context){
		mContext = context;
	}
	
	public static boolean isOnline() { 
	    ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo nInfo = cm.getActiveNetworkInfo(); 
	    if (nInfo != null && nInfo.isConnected()) {
	        return true; 
	    } else {  
	        return false;
	    }
		//return true;
	 }
	
	public static String getXML(String address){
		return getXML(address, "");
	}
    public static String getXML(String address, String params) {
    	if(!isOnline()){
    		return "";
    	}
    	
    	String line = null;
        
       // address=auth(address);
    	//address = address + "?" + "login=" + TVActivity.login + "&" + "password=" + TVActivity.password + "&" + params;
    	address = address + "?" + params;
    	//Log.i(TAG, "Login:" +TVActivity.login);
    	Log.i(TAG,address);
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(address);
 
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            line = EntityUtils.toString(httpEntity, "UTF-8");
 
        } catch (UnsupportedEncodingException e) {
            line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
        } catch (MalformedURLException e) {
            line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
        } catch (IOException e) {
            line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
        }
 
        return line;
    }

    public static String getXMLFromFile(String addres, Context context) {
    	String xmlString = null;
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(addres);
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            xmlString = new String(data);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Log.i("getXMLFromFile", xmlString);
        return xmlString;
    }
    
	public static Document XMLfromString(String xml) {
		Document doc = null;
	 
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        try {
	 
	        DocumentBuilder db = dbf.newDocumentBuilder();
	 
	       // InputSource is = new InputSource();
	        //    is.setCharacterStream(new StringReader(xml));
	        
	        ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF8"));

	            doc = db.parse(is);	 
	        } catch (ParserConfigurationException e) {
	            System.out.println("XML parse error: " + e.getMessage());
	            return null;
	        } catch (SAXException e) {
	            System.out.println("Wrong XML file structure: " + e.getMessage());
	            return null;
	        } catch (IOException e) {
	            System.out.println("I/O exeption: " + e.getMessage());
	            return null;
	        }	 
	        return doc;
	}
	
	public static String getValue(String tag, Node node) {
		NodeList nlList  = ((Element)node).getElementsByTagName(tag);
		/*if(tag.equals(KVConstants.KEY_RATIO)){
			if(nlList.item(0).getFirstChild() == null)
			return "default";
		}
		if(tag.equals(KVConstants.KEY_ORDER)){
			if(nlList.item(0).getFirstChild() == null)
			return "0";
		}*/
		return nlList.item(0).getFirstChild().getNodeValue();
	}
	
	public static Bitmap getImage(String adress) {
		URL url = null;
		Bitmap bmp;
		try {
			url = new URL(adress);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		conn.setDoInput(true);

		try {
			conn.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//int length = conn.getContentLength();

		InputStream is = null;
		try {
			is = conn.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		bmp = BitmapFactory.decodeStream(is);
		return bmp;
	}
	
	static String auth(final String address, final String login, final String password) {
        if (login.equals("")) return address;
        if (password.equals("")) return address;
        Uri uri = Uri.parse(address);
        Uri.Builder b = uri.buildUpon();
        b.encodedAuthority(login + ":" + password + "@" + uri.getHost() + ":" + uri.getPort());
        String url = b.build().toString();
        return url;
    }
	   
	public static String auth(final String address) {
	   return "";//auth(address, TVActivity.login, TVActivity.password);
	}
}