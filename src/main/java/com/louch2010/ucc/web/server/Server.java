package com.louch2010.ucc.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) throws Exception {
		SocketHandler handler = new SocketHandler();
		try {
			ServerSocket server = new ServerSocket(9527);
			while(true){				
				Socket socket = server.accept();
				InputStream input = socket.getInputStream();
				OutputStream output = socket.getOutputStream();
				handler.handle(input, output);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
