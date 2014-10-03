package com.example.controller;

public class Control 
{
	ControllerActivity parent;
	String controlType;
	
	int[] position = new int[2];
	int width, height;
	
	boolean isPressed;

	boolean padIsLocked = false;
	int[] padStickLocation = new int[2];
	int padInputCount, padInputStartAngle, padSectionPressed = -1;

	int controlId;
	
	public void padPressed(double _angle, int _distance)
	{
		int adjustedDistance = _distance;
		
		if (adjustedDistance > (width / 2) - (width / 8))
			adjustedDistance = (width / 2) - (width / 8);
		
		padStickLocation[0] = (int) (Math.cos(_angle) * adjustedDistance);
		padStickLocation[1] = (int) (Math.sin(_angle) * adjustedDistance);
		
		boolean adjustAngle;
		boolean shouldSendKey = true;
		
		for (int i = 0; i < padInputCount; i++)
		{
			adjustAngle = (padInputStartAngle + ((i + 1) * (360 / padInputCount)) > 360);

			if (Math.toDegrees(_angle) + (adjustAngle ? 360 : 0) > padInputStartAngle + (i * (360 / padInputCount)) && Math.toDegrees(_angle) < padInputStartAngle + (adjustAngle ? 360 : 0) + ((i + 1) * (360 / padInputCount)))
			{
				if (padSectionPressed != i && padSectionPressed != -1)
					parent.addKeyPress(controlId, 1, padSectionPressed);
				else if (padSectionPressed == i)
					shouldSendKey = false;
				
				padSectionPressed = i;
				break;
			}
		}

		if (shouldSendKey)
			parent.addKeyPress(controlId, 0, padSectionPressed);
		
		isPressed = true;
		parent.controllerSurface.invalidate();
	}
	
	public void roundButtonPressed()
	{
		parent.addKeyPress(controlId, 0, 0);
		isPressed = true;
		parent.controllerSurface.invalidate();
	}
	
	public void squareButtonPressed()
	{
		parent.addKeyPress(controlId, 0, 0);
		isPressed = true;
		parent.controllerSurface.invalidate();
	}
	
	public void release()
	{
		if (controlType.equals("pad"))
		{
			parent.addKeyPress(controlId, 1, padSectionPressed);
			padSectionPressed = -1;
		}
		else
		{
			parent.addKeyPress(controlId, 1, 0);
		}
		
		isPressed = false;
		parent.controllerSurface.invalidate();
	}
	
	public void setX(int _newX)
	{
		position[0] = _newX;
	}
	
	public int getX()
	{
		return position[0];
	}
	
	public void setY(int _newY)
	{
		position[1] = _newY;
	}
	
	public int getY()
	{
		return position[1];
	}
}