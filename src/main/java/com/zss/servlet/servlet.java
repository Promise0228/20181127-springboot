package com.zss.servlet;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class servlet{

	@RequestMapping("/test")
	@ResponseBody
	public Map<String, Object> test() throws UnknownHostException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "张三");
		map.put("age", "22");
		map.put("sex", "男");
		map.put("11", 12343);
		map.put("ip:", Inet4Address.getLocalHost());
		return map;
	}
}
