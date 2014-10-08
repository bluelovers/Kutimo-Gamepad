package kco.kutimo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import kco.kutimo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ControllerEditActivity extends Activity 
{
	ArrayList<Control> controls = new ArrayList<Control>();
	AlertDialog openDialog;
	ControllerSurface controllerSurface;
	CountDownTimer longClickTimer;
	Point firstPointerStart = new Point(), firstPointerOffset = new Point(), secondPointerPosition = new Point();
	int screenWidth, screenHeight;
	Control firstPointerTarget;
	Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
		
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        
		retrieveControlsFromFile();
		
		controllerSurface = new ControllerSurface(getBaseContext());
		controllerSurface.parent = this;
		controllerSurface.inEditor = true;
		controllerSurface.setControls(controls);
		controllerSurface.setBackgroundColor(0xFF152326);
		controllerSurface.setOnTouchListener(new controlListener());
		setContentView(controllerSurface);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void showOptionList(String[] _strings, String _title)
	{
		if (openDialog != null)
			openDialog.dismiss();

		ListView optionList = new ListView(this);
		optionList.setBackgroundColor(0xFF152326);

		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, R.layout.listview_item, R.id.list_item_text, _strings);
		optionList.setAdapter(modeAdapter);
		optionList.setOnItemClickListener(new itemClickListener());
		optionList.setSelector(R.drawable.option_list_selector);
		optionList.setDivider(new ColorDrawable(0xFF000000));
		optionList.setDividerHeight(6);
		
		openDialog = new AlertDialog.Builder(this).create();
		
		if (_title != null)
		{
			View customTitle = getLayoutInflater().inflate(R.layout.option_list_title, null);
			((TextView)customTitle.findViewById(R.id.txt_title)).setText(_title);
			openDialog.setCustomTitle(customTitle);
		}
		
		openDialog.setView(optionList, 0, 0, 0, 0);
		openDialog.show();
	}
	
	public void addNewControl(String _type)
	{
		Control newControl = new Control();
		newControl.controlId = controls.size();
		newControl.controlType = _type;
		newControl.setX(100);
		newControl.setY(100);
		newControl.width = 200;
		newControl.height = 200;
		newControl.padInputCount = 4;
		newControl.padInputStartAngle = 45;

		controls.add(newControl);
	}
	
	public class itemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position, long id) 
		{
			if (adapter.getAdapter().getItem(position).toString().equals("Add control"))
			{
				showOptionList(new String[] {"Pad", "Round button", "Square button"}, null);
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Save layout"))
			{
				saveControlsToFile();
				showToast("Layout saved");
				openDialog.dismiss();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Clear all"))
			{
				showOptionList(new String[] {"Yes", "No"}, "Really clear all?");
			}
			
			if (adapter.getAdapter().getItem(position).toString().equals("Yes"))
			{
				controls.clear();
				controllerSurface.invalidate();
				openDialog.dismiss();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("No"))
			{
				openDialog.dismiss();
			}
			
			if (adapter.getAdapter().getItem(position).toString().equals("Pad"))
			{
				addNewControl("pad");
				controllerSurface.invalidate();
				openDialog.dismiss();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Round button"))
			{
				addNewControl("button_round");
				controllerSurface.invalidate();
				openDialog.dismiss();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Square button"))
			{
				addNewControl("button_square");
				controllerSurface.invalidate();
				openDialog.dismiss();
			}
			
			if (adapter.getAdapter().getItem(position).toString().equals("Set directions"))
			{
				showOptionList(new String[] {"Directions +", "Directions -", "Done"}, null);
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Rotate"))
			{
				showOptionList(new String[] {"Rotate CW", "Rotate CCW", "Done"}, null);
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Copy"))
			{
				copyControl(firstPointerTarget);
				openDialog.dismiss();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Remove"))
			{
				controls.remove(controls.indexOf(firstPointerTarget));
				
				for (int i = 0; i < controls.size(); i++)
					controls.get(i).controlId = i;
				
				showToast("Control removed");
				controllerSurface.invalidate();
				openDialog.dismiss();
			}
			
			if (adapter.getAdapter().getItem(position).toString().equals("Directions +"))
			{
				if (firstPointerTarget.padInputCount < 8)
					firstPointerTarget.padInputCount++;
				controllerSurface.invalidate();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Directions -"))
			{
				if (firstPointerTarget.padInputCount > 2)
					firstPointerTarget.padInputCount--;
				controllerSurface.invalidate();
			}
			
			if (adapter.getAdapter().getItem(position).toString().equals("Rotate CW"))
			{
				firstPointerTarget.padInputStartAngle += 5;
				
				if (firstPointerTarget.padInputStartAngle > 360)
					firstPointerTarget.padInputStartAngle -= 360;
				
				controllerSurface.invalidate();
			}
			else if (adapter.getAdapter().getItem(position).toString().equals("Rotate CCW"))
			{
				firstPointerTarget.padInputStartAngle -= 5;
				
				if (firstPointerTarget.padInputStartAngle < 0)
					firstPointerTarget.padInputStartAngle += 360;
				
				controllerSurface.invalidate();
			}
			
			if (adapter.getAdapter().getItem(position).toString().equals("Done"))
			{
				if (openDialog != null)
					openDialog.dismiss();
			}
			
			vibrator.vibrate(40);
		}
	}
	
	public void resizeSelectedControl(int _widthAmount, int _heightAmount)
	{
		if (!firstPointerTarget.controlType.equals("button_square"))
		{
			if ((_widthAmount < 0 && firstPointerTarget.width > 75) || _widthAmount > 0)
			{
				if (firstPointerTarget.width + _widthAmount < 75)
					firstPointerTarget.width = 75;
				else
					firstPointerTarget.width += _widthAmount;
				
				firstPointerTarget.height = firstPointerTarget.width;
			}
		}
		else
		{
			if ((_widthAmount < 0 && firstPointerTarget.width > 75) || _widthAmount > 0)
			{
				if (firstPointerTarget.width + _widthAmount < 75)
					firstPointerTarget.width = 75;
				else
					firstPointerTarget.width += _widthAmount;
			}
			
			if ((_heightAmount < 0 && firstPointerTarget.height > 75) || _heightAmount > 0)
			{
				if (firstPointerTarget.height + _heightAmount < 75)
					firstPointerTarget.height = 75;
				else
					firstPointerTarget.height += _heightAmount;
			}
		}
		
		controllerSurface.invalidate();
	}
	
	public void copyControl(Control _controlToCopy)
	{
		Control newControl = new Control();
		newControl.controlId = controls.size();
		newControl.controlType = _controlToCopy.controlType;
		newControl.setX(100);
		newControl.setY(100);
		newControl.width = _controlToCopy.width;
		newControl.height = _controlToCopy.height;
		newControl.padInputCount = _controlToCopy.padInputCount;
		newControl.padInputStartAngle = _controlToCopy.padInputStartAngle;

		controls.add(newControl);
		controllerSurface.invalidate();
	}
	
	public class controlListener implements OnTouchListener
	{
		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getAction() == MotionEvent.ACTION_MOVE || event.getActionMasked() == MotionEvent.ACTION_MOVE)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
				{
					if (event.getActionIndex() == 0)
					{
						firstPointerStart.x = (int)event.getRawX();
						firstPointerStart.y = (int)event.getRawY();
						firstPointerTarget = checkForControlAt(firstPointerStart.x, firstPointerStart.y);
						
						if (firstPointerTarget != null)
						{
							firstPointerOffset.x = firstPointerTarget.getX() - firstPointerStart.x;
							firstPointerOffset.y = firstPointerTarget.getY() - firstPointerStart.y;
						}
						
						longClickTimer = new CountDownTimer(1000, 1000) 
					    {
							@Override
					        public void onTick(long millisUntilFinished)
					        {}
	
					        @Override
					        public void onFinish() 
					        {
					        	if (firstPointerTarget == null)
					        		showOptionList(new String[] {"Add control", "Save layout", "Clear all"}, null);
					        	else
					        	{
					        		if (firstPointerTarget.controlType.equals("pad"))
					        			showOptionList(new String[] {"Set directions", "Rotate", "Copy", "Remove"}, null);
					        		else
					        			showOptionList(new String[] {"Copy", "Remove"}, null);
					        	}
					        	
					        	vibrator.vibrate(50);
					        }
					    }.start();
					}
					else if (event.getActionIndex() == 1)
					{
						secondPointerPosition.x = (int)event.getX(1);
						secondPointerPosition.y = (int)event.getY(1);
						longClickTimer.cancel();
					}
				}
				
				if (event.getActionIndex() == 0 && getDistance(firstPointerStart.x, firstPointerStart.y, (int)event.getRawX(), (int)event.getRawY()) > 10)
					longClickTimer.cancel();
				
				if (event.getActionIndex() == 0)
				{
					if (firstPointerTarget != null)
					{
						firstPointerTarget.setX((int)event.getRawX() + firstPointerOffset.x);
						firstPointerTarget.setY((int)event.getRawY() + firstPointerOffset.y);
						controllerSurface.invalidate();
					}
				}
					
				if (event.getPointerCount() > 1)
				{
					if (firstPointerTarget != null)
						resizeSelectedControl((int)((int)event.getX(1) - secondPointerPosition.x), (int)((int)event.getY(1) - secondPointerPosition.y));
					
					secondPointerPosition.x = (int)event.getX(1);
					secondPointerPosition.y = (int)event.getY(1);
				}
			}
			else if (event.getAction() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
			{
				longClickTimer.cancel();
			}

			return true;
		}
	}
	
	public Control checkForControlAt(int _xCheck, int _yCheck)
	{
		for (int i = controls.size() - 1; i >= 0; i--)
		{
			if (!controls.get(i).controlType.equals("button_square") && getDistance(_xCheck, _yCheck, controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).width / 2)) < controls.get(i).width / 2)
				return controls.get(i);
			else if (controls.get(i).controlType.equals("button_square") && _xCheck > controls.get(i).getX() && _xCheck < controls.get(i).getX() + controls.get(i).width && _yCheck > controls.get(i).getY() && _yCheck < controls.get(i).getY() + controls.get(i).height)
				return controls.get(i);
		}
		
		return null;
	}
	
	public void saveControlsToFile()
	{
		try 
		{
			FileOutputStream fos = openFileOutput("controller_layout", Context.MODE_PRIVATE);
			
			for (int i = 0; i < controls.size(); i++)
			{
				String output = i + ";" + controls.get(i).controlType + ";" + controls.get(i).getX() + ";" + controls.get(i).getY() + ";" + controls.get(i).width + ";" + controls.get(i).height + ";" + (controls.get(i).controlType.equals("pad") ? controls.get(i).padInputCount : 0) + ";" + (controls.get(i).controlType.equals("pad") ? controls.get(i).padInputStartAngle : 0) + "\n";
				fos.write(output.getBytes());
			}
			
			fos.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void retrieveControlsFromFile()
	{
		int nextChar;
		ArrayList<String> values = new ArrayList<String>();
		String line = "";
		
		try 
		{
			FileInputStream fos = openFileInput("controller_layout");
			
			while((nextChar = fos.read()) != -1)
			{
				if ((char)nextChar == '\n')
				{
					values.add(line);
					line = "";
				}
				else
					line += (char)nextChar;
			}
			
			fos.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		for (int i = 0; i < values.size(); i++)
		{
			Control newControl = new Control();
			String[] controlValues = values.get(i).split(";");

			newControl.controlId = Integer.valueOf(controlValues[0]);
			newControl.controlType = controlValues[1];
			newControl.setX(Integer.valueOf(controlValues[2]));
			newControl.setY(Integer.valueOf(controlValues[3]));
			newControl.width = Integer.valueOf(controlValues[4]);
			newControl.height = Integer.valueOf(controlValues[5]);
			newControl.padInputCount = Integer.valueOf(controlValues[6]);
			newControl.padInputStartAngle = Integer.valueOf(controlValues[7]);

			controls.add(newControl);
		}
	}
	
	public void showToast(String _text)
	{
		Toast toast = Toast.makeText(getApplicationContext(), _text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public double getDistance(int x1, int y1, int x2, int y2)
	{
		return Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
	}
}