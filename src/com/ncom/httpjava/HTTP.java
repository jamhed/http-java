package com.ncom.httpjava;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

class HTTP {

	final int MSG_DONE = 1;

	int socketTimeout;
	int requestTimeout;
	
	BasicHttpContext localContext;
		
	HTTP(int socketTimeout, int requestTimeout) {
		this.socketTimeout = socketTimeout;
		this.requestTimeout = requestTimeout;
		localContext = new BasicHttpContext();
	}
		
	HTTP() {
		this(5000, 25000);
	}
	
	static void Log(String s) {
		Log.d("HTTP", s);
	}

	public interface Callback {
		void apply(String res);
	}
	
	class Param {
		Callback callback;
		String result;
		
		Param(Callback callback, String result) {
			this.callback = callback;
			this.result = result;
		}
	}
	
	static Handler cbHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
            Param r = (Param) m.obj;
            Log("RES: " + r.result);
            r.callback.apply(r.result);
        }
	};

    class TimeOutTask extends TimerTask {   
		String url;
		Thread t;
		
		TimeOutTask(Thread t, String url) {
        	this.t = t;
        	this.url = url;
        }
		
        public void run() {
            if (t.isAlive()) {
                t.interrupt();
            }
        }
    }
    
    String convertInputStreamToString(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line = "";
        try {
			while( (line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return builder.toString();
    }

    void Post(final String url, final String body, final Callback callback) {
        Log("REQ: " + url);
        
        Thread r = new Thread() {
            
        	@Override
            public void run() {
            
                HttpClient client = new DefaultHttpClient();
                HttpParams param = client.getParams();

                HttpConnectionParams.setConnectionTimeout(param, socketTimeout);
                HttpConnectionParams.setSoTimeout(param, socketTimeout);

                HttpPost req = new HttpPost(url);

                String result = "";
                
				if (body.length() > 0) {
                    try {
						req.setEntity(new StringEntity(body));
		                InputStream inputStream = client.execute(req, new BasicHttpContext()).getEntity().getContent();
		                result  = convertInputStreamToString(inputStream);
		                req.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    } catch (Exception e) {
						e.printStackTrace();
					}
                    
                }
                cbHandler.obtainMessage(MSG_DONE, new Param(callback, result)).sendToTarget();
            }
        };

        new Timer(true).schedule(new TimeOutTask(r, url), requestTimeout);
        r.start();
    }
}
