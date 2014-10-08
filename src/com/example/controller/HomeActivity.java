package com.example.controller;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Vibrator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity 
{
	Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
        
		((ImageView) findViewById(R.id.img_edit)).setOnTouchListener(new touchListener());
		((ImageView) findViewById(R.id.img_help)).setOnTouchListener(new touchListener());
		((ImageView) findViewById(R.id.img_exit)).setOnTouchListener(new touchListener());
		((ImageView) findViewById(R.id.img_refresh)).setOnTouchListener(new touchListener());
		((ListView) findViewById(R.id.list_possible_connections)).setSelector(R.drawable.client_list_selector);

		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		((ListView)findViewById(R.id.list_possible_connections)).setAdapter(new ArrayAdapter<String>(this, R.layout.client_list_item, R.id.list_item_text, new String[] {}));
		((TextView)findViewById(R.id.txt_clients_tag)).setText("Searching for clients...");
        new Thread(new ClientManagerThread(this)).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	public void populateClientList(ArrayList<String> clients)
	{
		ListView clientList = (ListView) findViewById(R.id.list_possible_connections);
		String[] clientArray = new String[clients.size()];
		
		if (clientArray.length == 0)
		{
			((TextView)findViewById(R.id.txt_clients_tag)).setText("No clients found");
			findViewById(R.id.img_refresh).setVisibility(View.VISIBLE);
			findViewById(R.id.txt_refresh_tag).setVisibility(View.VISIBLE);
		}
		else
			((TextView)findViewById(R.id.txt_clients_tag)).setText("Clients found!");
		
		clients.toArray(clientArray);

		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, R.layout.client_list_item, R.id.list_item_text, clientArray);
		clientList.setAdapter(modeAdapter);
		
		clientList.setOnItemClickListener(new itemClickListener());
	}
	
	public class itemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
		{
			vibrator.vibrate(40);
			Intent controllerIntent = new Intent(getBaseContext(), ControllerActivity.class);
			controllerIntent.putExtra("CLIENT_IP", ((TextView)arg1.findViewById(R.id.list_item_text)).getText().toString());
			startActivity(controllerIntent);
		}
	}
	
	public class touchListener implements OnTouchListener
	{
		@SuppressLint("NewApi")
		@Override
		public boolean onTouch(View touchedView, MotionEvent motionEvent) 
		{
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			{
				touchedView.setScaleX(.9F);
				touchedView.setScaleY(.9F);
			}
			else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
			{
				View[] buttons = new View[] {findViewById(R.id.img_edit), findViewById(R.id.img_help), findViewById(R.id.img_exit), findViewById(R.id.img_refresh)};
				
				for (int i = 0; i < buttons.length; i++)
				{
					buttons[i].setScaleX(1);
					buttons[i].setScaleY(1);
					
					if (checkButtonBounds(buttons[i], (int)motionEvent.getRawX(), (int)motionEvent.getRawY()))
					{
						buttons[i].setScaleX(.9F);
						buttons[i].setScaleY(.9F);
					}
				}
			}
			else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
			{
				View[] buttons = new View[] {findViewById(R.id.img_edit), findViewById(R.id.img_help), findViewById(R.id.img_exit), findViewById(R.id.img_refresh)};
				
				for (int i = 0; i < buttons.length; i++)
				{
					buttons[i].setScaleX(1);
					buttons[i].setScaleY(1);
				}
				
				if (checkButtonBounds(findViewById(R.id.img_edit), (int)motionEvent.getRawX(), (int)motionEvent.getRawY()))
				{
					startActivity(new Intent(getBaseContext(), ControllerEditActivity.class));
					vibrator.vibrate(40);
				}
				else if (checkButtonBounds(findViewById(R.id.img_help), (int)motionEvent.getRawX(), (int)motionEvent.getRawY()))
				{
					startActivity(new Intent(getBaseContext(), HelpActivity.class));
					vibrator.vibrate(40);
				}
				else if (checkButtonBounds(findViewById(R.id.img_exit), (int)motionEvent.getRawX(), (int)motionEvent.getRawY()))
				{
					finish();
					vibrator.vibrate(40);
				}
				else if (checkButtonBounds(findViewById(R.id.img_refresh), (int)motionEvent.getRawX(), (int)motionEvent.getRawY()))
				{
					findViewById(R.id.img_refresh).setVisibility(View.INVISIBLE);
					findViewById(R.id.txt_refresh_tag).setVisibility(View.INVISIBLE);
					((TextView)findViewById(R.id.txt_clients_tag)).setText("Searching for clients...");
					new Thread(new ClientManagerThread(HomeActivity.this)).start();
					vibrator.vibrate(40);
				}
			}
			
			return true;
		}	
	}
	
	private boolean checkButtonBounds(View _view, int _x, int _y)
	{
	    Rect boundsRectangle = new Rect();
	    int[] viewLocation = new int[2];
	    _view.getDrawingRect(boundsRectangle);
	    _view.getLocationOnScreen(viewLocation);
	    boundsRectangle.offset(viewLocation[0], viewLocation[1]);
	    return boundsRectangle.contains(_x, _y);
	}
}