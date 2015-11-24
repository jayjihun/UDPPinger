import java.io.*;
import java.lang.Thread;
import java.net.*;
import java.util.*;

public class PingServer
{
	private static final double LOSS_RATE=0.3;
	private static final int AVERAGE_DELAY = 1000;//ms

	public static void main(String[] args) throws Exception
	{		
//		if (args.length != 1)
//		{
//			System.out.println("Required arguments: port");
//			return;
//		}
		//int port = Integer.parseInt(args[0]);
		int port = 11557;
		Random random = new Random();
		DatagramSocket socket = new DatagramSocket(port);
		int thread_num=1;
		while (true)
		{
			DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
			socket.receive(request);
			ReplyThread replier = new ReplyThread(request,random,LOSS_RATE,AVERAGE_DELAY, port + thread_num++);
			replier.run();
			if(thread_num == 100)
				thread_num=1;
		}
	}	
}

class ReplyThread extends Thread
{
	private DatagramPacket request;
	private Random random;
	private double LOSS_RATE;
	private int AVERAGE_DELAY;
	private int portnum;
	DatagramSocket socket;
	public ReplyThread(DatagramPacket req, Random ran, double lr, int ad, int port) throws Exception
	{
		request =req;
		random = ran;
		LOSS_RATE = lr;
		AVERAGE_DELAY=ad;
		portnum = port;
		socket = new DatagramSocket(portnum);
	}
	
	public void run()
	{
		try
		{
			printData(request);
			if (random.nextDouble() < LOSS_RATE)
			{
				System.out.println(" Reply not sent.");
				throw new Exception();
			}
			Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
			InetAddress clientHost = request.getAddress();
			int clientPort = request.getPort();
			byte[] buf = request.getData();
			DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost,clientPort);
			socket.send(reply);
			System.out.println(" Reply sent.");
		}
		catch(Exception e)
		{
			
		}
		finally
		{
			socket.close();
		}
		
	}
	
	private static void printData(DatagramPacket request) throws Exception
	{
		byte[] buf = request.getData();
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
		System.out.println("Received from " + request.getAddress().getHostAddress() + ": " +new String(line));
	}
	
}