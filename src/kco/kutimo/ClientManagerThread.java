package kco.kutimo;

import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ClientManagerThread implements Runnable 
{
	final int PORT = 7787;
    public HomeActivity parent;
    ArrayList<String> clients;   
    
    public ClientManagerThread(HomeActivity _parent)
    {
    	parent = _parent;
    }
    
    @Override
    public void run() 
    {	
		findClients();
		
		parent.runOnUiThread(
			new Runnable()
			{
				@Override
				public void run()
				{
					parent.populateClientList(clients);
				}	
			}
		);
    }
    
    public void findClients()
    {
    	DatagramSocket socket = null;
    	clients = new ArrayList<String>();

    	try 
		{
			String[] ipAddress = getIpAddress(parent.getApplicationContext()).split("\\.");
	    	DatagramPacket packet = new DatagramPacket(new byte[] {0, (byte)((int)Integer.valueOf(ipAddress[0])), (byte)((int)Integer.valueOf(ipAddress[1])), (byte)((int)Integer.valueOf(ipAddress[2])), (byte)((int)Integer.valueOf(ipAddress[3]))}, 5, getBroadcastAddress(), PORT);
	    	
			socket = new DatagramSocket(PORT);
			socket.setBroadcast(true);
	    	socket.send(packet);

	    	socket.close();
	    	
	    	socket = new DatagramSocket(PORT);
	    	socket.setSoTimeout(1000);
	    	
	    	for (int n = 0; n < 10; n++)
	    	{
	    		packet = new DatagramPacket(new byte[15], 15);
		    	socket.receive(packet);
		    	String clientIp = "";
		    	
		    	for (int i = 0; i < packet.getData().length; i++)
		    	{
		    		if (packet.getData()[i] != 0)
		    			clientIp += (char)packet.getData()[i];
		    	}
		    	
		    	clients.add(clientIp);
	    	}
	    	
	    	socket.close();
		} 
		catch (InterruptedIOException e) 
		{
			socket.close();
		} 
    	catch (Exception e) 
		{
			Log.e("Kutimo", "Client manager error", e);
		}
    	finally
    	{
    		if (socket != null && !socket.isClosed())
    			socket.close();
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
			Log.e("Kutimo", "Error getting broadcast address", e);
		}
        
        return null;
    }
    
    public String getIpAddress(Context context) 
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
            ipAddress = Integer.reverseBytes(ipAddress);

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        
        try 
        {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } 
        catch (UnknownHostException e) 
        {
        	Log.e("Kutimo", "Error getting device IP address", e);
            ipAddressString = null;
        }

        return ipAddressString;
    }
}