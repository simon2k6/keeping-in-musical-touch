package com.waikato.kimt.server;

import java.util.ArrayList;
import com.waikato.kimt.server.interfaces.IClient;

public class ClientManager 
{
	private ClientManager()
	{
		
	}
	
	public static ClientManager getSingleton()
	{
		if (manager == null)
			manager = new ClientManager();
		return manager;
	}
	
	static ClientManager manager;
	ArrayList<IClient> clients = new ArrayList<IClient>();
	
	public synchronized void insert (IClient c)
	{
		clients.add(c);
	}
	
	public synchronized boolean remove(IClient c)
	{
		return clients.remove(c);
	}
	
	public synchronized boolean remove(String s)
	{
		for(IClient c : clients)
		{
			if (c.getName().equals(s))
			{
				clients.remove(c);
				return true;
			}
		}
		return false;
	}
	
	public synchronized ArrayList<IClient> getClients()
	{
		return clients;
	}
}
