package com.ncom.httpjava;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    void request() {
    	HTTP http = new HTTP();
     	final TextView v = (TextView) findViewById(R.id.textview);
     	
        http.Post("http://mnp.tele2.ru/gateway.php?9273193358", "",  new HTTP.Callback() {
        	public void apply(String result) {
        		v.setText(result);
        	}
        });
    }

}
