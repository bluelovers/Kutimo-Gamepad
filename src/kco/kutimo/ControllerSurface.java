package kco.kutimo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.view.View;

public class ControllerSurface extends View
{
	Paint paint = new Paint();
	Activity parent;
	ArrayList<RectF> padArcs;
	ArrayList<Control> controls;
	boolean inEditor = false;
	
	int controlBorderColor = 0xFF152326, controlInnerColor = 0xFF22778B, controlPressedColor = 0xFF214047, controlIdColor = 0xFF268399;
	
	public ControllerSurface(Context context)
	{
		super(context);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}
	
	public void setControls(ArrayList<Control> _controls)
	{
		controls = _controls;
		
		getPadArcs();
	}
	
	@Override
	public void onDraw(Canvas canvas) 
	{
		if (controls.size() == 0)
		{
			paint.setColor(0xFFFFFFFF);
			paint.setTextSize(40);
			paint.setTextAlign(Align.CENTER);
		    canvas.drawText("Touch and hold anywhere on the screen", ((ControllerEditActivity)parent).screenWidth / 2, ((ControllerEditActivity)parent).screenWidth / 4, paint);
		    canvas.drawText("to bring up the editor menu", ((ControllerEditActivity)parent).screenWidth / 2, ((ControllerEditActivity)parent).screenWidth / 4 + 40, paint);
		}
		
		for (int i = 0; i < controls.size(); i++)
		{
			if (controls.get(i).controlType.equals("button_square"))
			{
				 paint.setStyle(Paint.Style.FILL);
				 paint.setStrokeWidth(3);
				 
				 paint.setColor(controlBorderColor);   
			     canvas.drawRect(controls.get(i).getX(), controls.get(i).getY(), controls.get(i).getX() + controls.get(i).width, controls.get(i).getY() + controls.get(i).height, paint);
			    
			     paint.setColor(controlInnerColor);   
			     canvas.drawRect(controls.get(i).getX() + 3, controls.get(i).getY() + 3, controls.get(i).getX() + controls.get(i).width - 3, controls.get(i).getY() + controls.get(i).height - 3, paint);
			     
			     paint.setColor(controlIdColor);
			     paint.setTextSize(controls.get(i).height / 2);
			     paint.setTextAlign(Align.CENTER);
			     canvas.drawText(Integer.toString(controls.get(i).controlId), controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2) + (((paint.descent() - paint.ascent()) / 2) - paint.descent()), paint);
			     
			     if (controls.get(i).isPressed)
			     {
					 paint.setColor(controlPressedColor);
				     canvas.drawRect(controls.get(i).getX() + 3, controls.get(i).getY() + 3, controls.get(i).getX() + controls.get(i).width - 3, controls.get(i).getY() + controls.get(i).height - 3, paint);
			     }
			}
			else if (controls.get(i).controlType.equals("button_round"))
			{
				 paint.setStyle(Paint.Style.FILL);
				 paint.setStrokeWidth(3);
				 
				 paint.setColor(controlBorderColor);
			     canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), (controls.get(i).width / 2), paint);
			     paint.setColor(controlInnerColor);
			     canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), (controls.get(i).width / 2) - 3, paint);

			     paint.setColor(controlIdColor);
			     paint.setTextSize(controls.get(i).height / 2);
			     paint.setTextAlign(Align.CENTER);
			     canvas.drawText(Integer.toString(controls.get(i).controlId), controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2) + (((paint.descent() - paint.ascent()) / 2) - paint.descent()), paint);
			     
			     if (controls.get(i).isPressed)
			     {
					 paint.setColor(controlPressedColor);
				     canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), (controls.get(i).width / 2) - 3, paint);
			     }
			}
			else if (controls.get(i).controlType.equals("pad"))
			{
				paint.setStyle(Paint.Style.FILL);
			    paint.setStrokeWidth(3);
			     
			    paint.setColor(controlBorderColor);
			    canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), (controls.get(i).width / 2), paint);
			     
			    paint.setColor(controlInnerColor);
			    canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), (controls.get(i).width / 2) - 3, paint);
			     
			    paint.setStyle(Paint.Style.STROKE);
			    paint.setColor(controlBorderColor);
			    paint.setStrokeWidth(1);
			    	    
			    for (int l = 0; l < controls.get(i).padInputCount; l++)
			    {
			    	int endX = (int)(Math.cos(Math.toRadians(controls.get(i).padInputStartAngle + (l * (360 / controls.get(i).padInputCount)))) * (controls.get(i).width / 2));
			    	int endY = (int)(Math.sin(Math.toRadians(controls.get(i).padInputStartAngle + (l * (360 / controls.get(i).padInputCount)))) * (controls.get(i).width / 2));
			    	 
			    	canvas.drawLine(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), controls.get(i).getX() + (controls.get(i).width / 2) + endX, controls.get(i).getY() + (controls.get(i).height / 2) + endY, paint);
			    }
			    
			    paint.setStyle(Paint.Style.FILL);
			    paint.setColor(controlIdColor);
			    paint.setTextSize(controls.get(i).height / 3);
			    paint.setTextAlign(Align.CENTER);
			    canvas.drawText(Integer.toString(controls.get(i).controlId), controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (((paint.descent() - paint.ascent()) / 2) - paint.descent()) * 3, paint);
    
			    if (controls.get(i).isPressed)
			    {
				    paint.setColor(controlPressedColor);
				    paint.setStrokeWidth(3);
				     
				    canvas.drawArc(padArcs.get(i), controls.get(i).padInputStartAngle + (controls.get(i).padSectionPressed * (360 / controls.get(i).padInputCount)), 360 / controls.get(i).padInputCount, true, paint);

				    paint.setColor(controlBorderColor);
				    paint.setStrokeWidth(3);
				     
				    canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2) + controls.get(i).padStickLocation[0], controls.get(i).getY() + (controls.get(i).height / 2) + controls.get(i).padStickLocation[1], (controls.get(i).width / 8), paint);
			    }
			    else
			    {
			    	paint.setColor(controlBorderColor);
			     	paint.setStrokeWidth(3);
			     
			     	canvas.drawCircle(controls.get(i).getX() + (controls.get(i).width / 2), controls.get(i).getY() + (controls.get(i).height / 2), (controls.get(i).width / 8), paint);
			    }  
			}
		}
	}
	
	public void getPadArcs()
	{
		padArcs = new ArrayList<RectF>();
		
		for (int i = 0; i < controls.size(); i++)
		{
			if (controls.get(i).controlType.equals("pad"))
			    padArcs.add(new RectF(controls.get(i).getX(), controls.get(i).getY(), controls.get(i).getX() + controls.get(i).width, controls.get(i).getY() + controls.get(i).height));
			else
				padArcs.add(null);
		}
	}
}