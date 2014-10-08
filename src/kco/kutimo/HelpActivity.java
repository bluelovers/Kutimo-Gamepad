package kco.kutimo;

import java.util.ArrayList;

import kco.kutimo.R;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

public class HelpActivity extends Activity 
{
	ArrayList<String> headerItems = new ArrayList<String>(), subItems = new ArrayList<String>();
	ExpandableListView helpList;
	Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		headerItems.add("Where do I download the PC client?");
		headerItems.add("Is there a Mac client?");
		headerItems.add("How do I bind a key to a control?");
		headerItems.add("What keys can I bind?");
		headerItems.add("How do I edit the controller layout?");
		headerItems.add("How do I resize the controls in the editor?");
		headerItems.add("How can I customize the directional pads?");
		
		subItems.add("Head to www.bit.ly/1rRnHcX on your computer to download the client for Windows");
		subItems.add("Unfortunately there is no Mac version of the controller client. Sorry!");
		subItems.add("First select the control from the list on the PC client. If you're binding a key to a directional pad, then click on the pad image to select the direction you want to bind.\n\nNext simply enter the key or keys you want to bind to the control in the keybind textbox, and press the \"Set keybind\" button.\n\nYou can have up to 10 keys bound to one control.");
		subItems.add("Bindable keys are A-Z, 1-9, Arrow keys, Space, Escape, Control, Tab, and Enter");
		subItems.add("Press the edit button on the home screen to go to the controller editor");
		subItems.add("Touch and hold the control you want to resize, and then use a pinching gesture to adjust the width and height of a control");
		subItems.add("You can select anywhere from 2-8 directions for the pads and rotate them to your liking. Simply touch and hold a pad to bring up the edit menu and then select the option you want from there.");

		helpList = ((ExpandableListView)findViewById(R.id.explist_help_contents));
		helpList.setGroupIndicator(null);
		helpList.setSelector(R.drawable.option_list_selector);
		helpList.setAdapter(new HelpListAdapter(headerItems, subItems, (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)));
		helpList.setOnGroupClickListener(new itemClickListener());

		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_help, menu);
		return true;
	}
	
	public class itemClickListener implements OnGroupClickListener
	{
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) 
		{
			vibrator.vibrate(40);
			return false;
		}
	}
}