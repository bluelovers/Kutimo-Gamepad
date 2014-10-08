package kco.kutimo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkThread implements Runnable 
{
	final int PORT = 7787;
    public ControllerActivity parent;
    DatagramSocket socket;
    InetAddress clientAddress;
    DatagramPacket dataPacket = null;
    byte[] keyData = new byte[4];
    boolean sendControlListFlag = false, disconnectFlag = false;
    
    @Override
    public void run() 
    {	
        try 
        {
        	socket = new DatagramSocket(PORT);
        } 
        catch (Exception e) 
        {
            Log.e("Kutimo", "Error opening sockets", e);
        }

        do
        {
        	if (sendControlListFlag)
        		sendControlListToClient(parent.controls);
        	
        	for (int i = 0; i < parent.buttonQueue.length; i++)
        	{
        		if (parent.buttonQueue[i][0] != -1)
        		{
        			send(parent.buttonQueue[i][0], parent.buttonQueue[i][1], parent.buttonQueue[i][2]);
            		parent.buttonQueue[i][0] = -1;
        		}
        	}
        } while (!disconnectFlag);
        
        disconnect();
    }
    
    public void sendControlListToClient(ArrayList<Control> controls)
    {
    	DatagramPacket dp;
    	byte[] controlPacket;
    	
    	sendControlListFlag = false;
    	
    	for (int i = 0; i < controls.size(); i++)
    	{
    		int controlType = 0;
    		
    		if (controls.get(i).controlType.equals("pad"))
    			controlType = 0;
    		else if (controls.get(i).controlType.equals("button_round"))
    			controlType = 1;
    		else if (controls.get(i).controlType.equals("button_square"))
    			controlType = 2;

    		controlPacket = new byte[] {(byte)2, (byte)((i == 0) ? 1 : 0), (byte) controls.get(i).controlId, (byte) controlType, (byte) controls.get(i).padInputCount, (byte) controls.get(i).padInputStartAngle};

    		try 
    		{
				dp = new DatagramPacket(controlPacket, controlPacket.length, clientAddress, 7787);
				socket.send(dp);
			} 
    		catch (Exception e) 
			{
				e.printStackTrace();
			}
    	}
    }
    
    public void send(int controlId, int eventType, int scanCode)
    {
		keyData[0] = 1;
		keyData[1] = (byte)controlId;
		keyData[2] = (byte)eventType;
		keyData[3] = (byte)scanCode;
		
		try 
		{
			if (dataPacket == null)
				dataPacket = new DatagramPacket(keyData, keyData.length, clientAddress, 7787);
			else
			{
				dataPacket.setData(keyData);
				dataPacket.setLength(keyData.length);
			}

			socket.send(dataPacket);
		} 
		catch (Exception e) 
		{
			Log.e("Kutimo", "Error sending key press", e);
		}
    }
    
    public void disconnect()
    {
		try 
		{
			DatagramPacket dp = new DatagramPacket(new byte[] {(byte)3}, 1, clientAddress, 7787);
			socket.send(dp);
		}
		catch (Exception e)
		{
			Log.e("Kutimo", "Error disconnecting", e);
		}
		
		socket.close();
    }
    
    public void setAddress(String _address)
    {
    	try 
    	{
			clientAddress = InetAddress.getByName(_address);
		} 
    	catch (UnknownHostException e) 
    	{
			Log.e("Kutimo", "Error setting client address", e);
		}
    }
    
    public InetAddress getBroadcastAddress()
    {
        WifiManager wifi = (WifiManager) parent.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        
        byte[] quads = new byte[4];
        
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        
        try 
        {
			return InetAddress.getByAddress(quads);
		} 
        catch (UnknownHostException e) 
        {
			e.printStackTrace();
		}
        
        return null;
    }
}