package kco.kutimo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import kco.kutimo.R;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class ControllerActivity extends Activity 
{
	int[][] buttonQueue = new int[15][3];
	ArrayList<Control> controls = new ArrayList<Control>();
	ArrayList<ControlTouchEvent> controlTouchEvents = new ArrayList<ControlTouchEvent>();
	
	Thread networkContainerThread;
	NetworkThread clientThread;
	ControllerSurface controllerSurface;
	Vibrator vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		retrieveControlsFromFile();
		intializeKeyQueue();
		
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
		clientThread = new NetworkThread();
	    clientThread.parent = this;
	    clientThread.setAddress(getIntent().getExtras().getString("CLIENT_IP"));
	    networkContainerThread = new Thread(clientThread);
	    networkContainerThread.start();
	    clientThread.sendControlListFlag = true;
	    
	    controllerSurface = new ControllerSurface(getBaseContext());
	    controllerSurface.init(controls, this, false);
	    controllerSurface.setBackgroundColor(0xFF152326);
	    controllerSurface.setOnTouchListener(new controlListener());
	 
	    setContentView(controllerSurface);
	    
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		clientThread.disconnectFlag = true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void intializeKeyQueue()
	{
		for (int i = 0; i < buttonQueue.length; i++)
			buttonQueue[i][0] = -1;
	}
	
	public void addKeyPress(int _controlId, int _action, int _sectionPressed)
	{
		for (int i = 0; i < buttonQueue.length; i++)
		{
			if (buttonQueue[i][0] == -1)
			{
				buttonQueue[i][0] = _controlId;
				buttonQueue[i][1] = _action;
				buttonQueue[i][2] = _sectionPressed;
				break;
			}
		}
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
					ControlTouchEvent newTouchEvent = new ControlTouchEvent();
	
					int xPress = (int)event.getX(event.getActionIndex());
					int yPress = (int)event.getY(event.getActionIndex());
	
					newTouchEvent.pointerId = event.getPointerId(event.getActionIndex());
					newTouchEvent.xPress = xPress;
					newTouchEvent.yPress = yPress;
					controlTouchEvents.add(newTouchEvent);
				}
				
				for (int i = 0; i < event.getPointerCount(); i++) 
			    {
					for (int n = 0; n < controlTouchEvents.size(); n++)
					{
						if (controlTouchEvents.get(n).pointerId == event.getPointerId(i))
						{
							controlTouchEvents.get(n).xPress = (int)event.getX(i);
							controlTouchEvents.get(n).yPress = (int)event.getY(i);
						}
					}
			    }
				
				for (int i = 0; i < controls.size(); i++)
				{
					boolean pressed = false;
					
					for (int n = 0; n < controlTouchEvents.size(); n++)
					{	
						if (controlTouchEvents.get(n).lockedToPad && controlTouchEvents.get(n).lockedPad == controls.get(i))
						{
							int oldSectionPressed = controlTouchEvents.get(n).lockedPad.padSectionPressed;
							controlTouchEvents.get(n).lockedPad.padPressed(getAngle(controlTouchEvents.get(n).lockedPad.getX() + (controlTouchEvents.get(n).lockedPad.width / 2), controlTouchEvents.get(n).lockedPad.getY() + (controlTouchEvents.get(n).lockedPad.height / 2), controlTouchEvents.get(n).xPress, controlTouchEvents.get(n).yPress), (int)getDistance(controlTouchEvents.get(n).lockedPad.getX() + (controlTouchEvents.get(n).lockedPad.width / 2), controlTouchEvents.get(n).lockedPad.getY() + (controlTouchEvents.get(n).lockedPad.height / 2), controlTouchEvents.get(n).xPress, controlTouchEvents.get(n).yPress));
							
							if (oldSectionPressed != controlTouchEvents.get(n).lockedPad.padSectionPressed)
								vibrator.vibrate(15);
							
							pressed = true;
						}
						else if (!controlTouchEvents.get(n).lockedToPad)
						{
							if (controls.get(i).controlType.equals("pad") && !controls.get(i).padIsLocked)
							{
								if (getDistance(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), controlTouchEvents.get(n).xPress, controlTouchEvents.get(n).yPress) < controls.get(i).width / 2)
								{
									controlTouchEvents.get(n).lockedToPad = true;
									controlTouchEvents.get(n).lockedPad = controls.get(i);
									controlTouchEvents.get(n).lockedPad.padPressed(getAngle(controlTouchEvents.get(n).lockedPad.getX() + (controlTouchEvents.get(n).lockedPad.width / 2), controlTouchEvents.get(n).lockedPad.getY() + (controlTouchEvents.get(n).lockedPad.height / 2), controlTouchEvents.get(n).xPress, controlTouchEvents.get(n).yPress), (int)getDistance(controlTouchEvents.get(n).lockedPad.getX() + (controlTouchEvents.get(n).lockedPad.width / 2), controlTouchEvents.get(n).lockedPad.getY() + (controlTouchEvents.get(n).lockedPad.height / 2), controlTouchEvents.get(n).xPress, controlTouchEvents.get(n).yPress));
									controls.get(i).padIsLocked = true;
									vibrator.vibrate(50);
									pressed = true;
								}
							}
							else if (controls.get(i).controlType.equals("button_round"))
							{
								if (getDistance(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), controlTouchEvents.get(n).xPress, controlTouchEvents.get(n).yPress) < controls.get(i).width / 2)
								{
									if (!controls.get(i).isPressed)
									{
										controls.get(i).roundButtonPressed();
										vibrator.vibrate(75);
									}
									
									pressed = true;
								}
							}
							else if (controls.get(i).controlType.equals("button_square"))
							{
								if (controlTouchEvents.get(n).xPress < controls.get(i).getX() + controls.get(i).width && controlTouchEvents.get(n).xPress > controls.get(i).getX() && controlTouchEvents.get(n).yPress < controls.get(i).getY() + controls.get(i).height && controlTouchEvents.get(n).yPress > controls.get(i).getY())
								{
									if (!controls.get(i).isPressed)
									{
										controls.get(i).squareButtonPressed();
										vibrator.vibrate(75);
									}
	
									pressed = true;
								}
							}
						}
					}
					
					if (!pressed)
					{
						if (controls.get(i).isPressed)
							controls.get(i).release();
					}
				}
			}
			else if (event.getAction() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
			{
				for (int i = 0; i < controlTouchEvents.size(); i++)
				{
					if (event.getPointerId(event.getActionIndex()) == controlTouchEvents.get(i).pointerId)
					{
						ArrayList<Control> controlsToRelease = getAllControlsAt(controlTouchEvents.get(i).xPress, controlTouchEvents.get(i).yPress);
						
						for (Control c : controlsToRelease)
							c.release();

						if (controlTouchEvents.get(i).lockedToPad)
						{
							controlTouchEvents.get(i).lockedPad.padIsLocked = false;
							if (controlTouchEvents.get(i).lockedPad.isPressed)
								controlTouchEvents.get(i).lockedPad.release();
						}
						
						controlTouchEvents.remove(i);
						break;
					}
				}
			}

			return true;
		}
	}
	
	public ArrayList<Control> getAllControlsAt(int _xCheck, int _yCheck)
	{
		ArrayList<Control> returnList = new ArrayList<Control>();
		
		for (int i = controls.size() - 1; i >= 0; i--)
		{
			if (!controls.get(i).controlType.equals("button_square") && getDistance(_xCheck, _yCheck, controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).width / 2)) < controls.get(i).width / 2)
				returnList.add(controls.get(i));
			else if (controls.get(i).controlType.equals("button_square") && _xCheck > controls.get(i).getX() && _xCheck < controls.get(i).getX() + controls.get(i).width && _yCheck > controls.get(i).getY() && _yCheck < controls.get(i).getY() + controls.get(i).height)
				returnList.add(controls.get(i));
		}
		
		return returnList;
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

			newControl.parent = this;
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

	public double getDistance(int _x1, int _y1, int _x2, int _y2)
	{
		return Math.sqrt(((_x1 - _x2) * (_x1 - _x2)) + ((_y1 - _y2) * (_y1 - _y2)));
	}

	public double[] getVector(int _startX, int _startY, int _endX, int _endY)
	{
		double angle = getAngle(_startX, _startY, _endX, _endY);
		double[] returnArray = new double[2];
		returnArray[0] = Math.cos(angle);
		returnArray[1] = Math.sin(angle);
		return returnArray;
	}

	public double getAngle(int _startX, int _startY, int _endX, int _endY)
	{
	    double rads = Math.atan2(_endY - _startY, _endX - _startX);
	    rads = -rads;
	    
	    if (rads < 0)
	    	rads = Math.abs(rads);
	    else
	    	rads = 2*Math.PI - rads;
	    
	    return rads;
	}
}