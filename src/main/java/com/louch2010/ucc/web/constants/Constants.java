package com.louch2010.ucc.web.constants;

public class Constants {
	/** 
	  * @Description: ucc客户端与服务器端的通讯
	  * @author: luocihang
	  * @date: 2016年10月25日 下午5:13:54
	  * @version: V1.0 
	  * @see：
	  */
	public interface Protocol{
		String REQ_CONFIG_COMMAND = "gconf://";
		String REQ_CONFIG_NAME_SPLIT = "-\\|\\|-";
		String RESP_CONFIG_START = "<!-||--||--||--||--||--||--||";
		String RESP_CONFIG_END = "-||--||--||--||--||--||--||-!>";
	}
}
