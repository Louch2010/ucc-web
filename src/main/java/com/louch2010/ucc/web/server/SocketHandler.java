package com.louch2010.ucc.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.louch2010.ucc.web.constants.Constants;

/** 
  * @Description: socket请求处理器
  * @author: luocihang
  * @date: 2016年11月2日 下午3:09:00
  * @version: V1.0 
  * @see：
  */
public class SocketHandler{
	
	private Log logger = LogFactory.getLog(SocketHandler.class);
	
	private ThreadPoolExecutor executor;
	
	public SocketHandler(){
		this(3, 5, 30);
	}
	
	public SocketHandler(int corePoolSize, int maximumPoolSize, int keepAliveTime){
		this.initPool(corePoolSize, maximumPoolSize, keepAliveTime);
	}
	
	/**
	  *description : 初始化线程池
	  *@param      : @param corePoolSize
	  *@param      : @param maximumPoolSize
	  *@param      : @param keepAliveTime
	  *@return     : void
	  *modified    : 1、2016年11月2日 下午3:11:32 由 luocihang 创建 	   
	  */ 
	private void initPool(int corePoolSize, int maximumPoolSize, int keepAliveTime){
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(50);
		this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
	}

	/**
	  *description : 处理请求
	  *@param      : @param input
	  *@param      : @param output
	  *@return     : void
	  *modified    : 1、2016年11月2日 下午3:09:54 由 luocihang 创建 	   
	  */ 
	public void handle(InputStream input, OutputStream output){
		Worker worker = new Worker(input, output);
		executor.submit(worker);
	}
	
	/**
	  *description : 关闭线程
	  *@param      : 
	  *@return     : void
	  *modified    : 1、2016年11月2日 下午3:13:31 由 luocihang 创建 	   
	  */ 
	public void shutdown(){
		if(logger.isDebugEnabled()){
			logger.debug("shutdonw server...");
		}
		if(!executor.isShutdown()){
			executor.shutdown();
		}
	}
	
	/** 
	  * @Description: 工人
	  * @author: luocihang
	  * @date: 2016年11月2日 下午3:08:00
	  * @version: V1.0 
	  * @see：
	  */
	class Worker implements Runnable{
		private InputStream input;
		private OutputStream output;
		public Worker(InputStream input, OutputStream output) {
			this.input = input;
			this.output = output;
		}
		public void run() {
			doWork(input, output);
		}
	}
	
	/**
	  *description : 处理请求
	  *@param      : @param input
	  *@param      : @param output
	  *@return     : void
	  *modified    : 1、2016年11月2日 下午3:08:08 由 luocihang 创建 	   
	  */ 
	private void doWork(InputStream input, OutputStream output){
		try {
			//读取请求
			List<String> filesName = parseReq(input);
			//响应
			PrintWriter pw = new PrintWriter(output);
			String resp = getResp(filesName);
			pw.println(resp);
			pw.flush();
			//IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(pw);
			//socket.close();
		} catch (Exception e) {
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
	private String getResp(List<String> filesName) throws IOException{
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
		if(logger.isDebugEnabled()){
			logger.debug("handle request finished, response ：" + sb.toString());
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
	private List<String> parseReq(InputStream input) throws IOException{
		List<String> files = new ArrayList<String>();
		InputStreamReader reader = new InputStreamReader(input);
		StringBuffer sb = new StringBuffer();
		char chars[] = new char[1024 * 10];
		int len = reader.read(chars);
		sb.append(new String(chars, 0, len));
		String content = sb.toString().trim();
		if(logger.isDebugEnabled()){
			logger.debug("client request content：" + content);
		}
		//请求文件
		if(content.startsWith(Constants.Protocol.REQ_CONFIG_COMMAND)){
			String req = content.substring(Constants.Protocol.REQ_CONFIG_COMMAND.length());
			files.addAll(Arrays.asList(req.split(Constants.Protocol.REQ_CONFIG_NAME_SPLIT)));
		}
		return files;
	}
}
