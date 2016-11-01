package com.louch2010.ucc.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.louch2010.ucc.web.constants.Constants;

public class Server {
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(9527);
			while(true){				
				Socket socket = server.accept();
				InputStream input = socket.getInputStream();
				OutputStream output = socket.getOutputStream();
				//读取请求
				List<String> filesName = parseReq(input);
				//响应
				PrintWriter pw = new PrintWriter(output);
				String resp = getResp(filesName);
				System.out.println("响应内容：" + resp);
				pw.println(resp);
				pw.flush();
				//IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(pw);
				//socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	  *description : 获取响应内容
	  *@param      : @param filesName
	  *@param      : @return
	  *@param      : @throws IOException
	  *@return     : String
	  *modified    : 1、2016年10月26日 上午11:07:00 由 luocihang 创建 	   
	  */ 
	private static String getResp(List<String> filesName) throws IOException{
		StringBuffer sb = new StringBuffer();
		for (String name:filesName) {
			if(name == null || name.trim().length() == 0){
				continue;
			}
			File file = new File("e:/ucc" + name);
			if(!file.exists()){
				continue;
			}
			//写入头标识
			sb.append(Constants.Protocol.RESP_CONFIG_START + "\r\n");
			//写入文件名
			sb.append(name + "\r\n");
			//读取文件内容并写入
			FileInputStream input = new FileInputStream(file);
			List<String> lines = IOUtils.readLines(input, "UTF-8");
			IOUtils.closeQuietly(input);
			for(String line:lines){
				sb.append(line + "\r\n");
			}
			//写入尾标识
			sb.append(Constants.Protocol.RESP_CONFIG_END + "\r\n");
		}
		return sb.toString();
	}
	
	/**
	  *description : 解析请求头
	  *@param      : @param input
	  *@param      : @return
	  *@param      : @throws IOException
	  *@return     : List<String>
	  *modified    : 1、2016年10月26日 上午11:06:46 由 luocihang 创建 	   
	  */ 
	private static List<String> parseReq(InputStream input) throws IOException{
		List<String> files = new ArrayList<String>();
		InputStreamReader reader = new InputStreamReader(input);
		StringBuffer sb = new StringBuffer();
		char chars[] = new char[1024 * 10];
		int len = reader.read(chars);
		sb.append(new String(chars, 0, len));
		String content = sb.toString().trim();
		//请求文件
		if(content.startsWith(Constants.Protocol.REQ_CONFIG_COMMAND)){
			String req = content.substring(Constants.Protocol.REQ_CONFIG_COMMAND.length());
			files.addAll(Arrays.asList(req.split(Constants.Protocol.REQ_CONFIG_NAME_SPLIT)));
		}
		return files;
	}
}
