package com.zss.test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class GetLocaIp {

	public static void main(String[] args) throws UnknownHostException, SocketException {
        System.out.println(getLocalIP());
	}


	/**
	 * 获取本地IP地址
	 *
	 * @throws SocketException
	 */
	public static boolean getLocalIP() throws UnknownHostException, SocketException {
//		if (isWindowsOS()) {
//			return InetAddress.getLocalHost().getHostAddress();
//		} else {
		String host = "172.33.128.3";
		ArrayList<?> linuxLocalIp = getLinuxLocalIp();
		for (Object string : linuxLocalIp) {
			System.out.println(string);
			if (host.equals(string)) {
				return false;
			}
		}
		return true;
//		}
	}

	/**
	 * 判断操作系统是否是Windows
	 *
	 * @return
	 */
	public static boolean isWindowsOS() {
		boolean isWindowsOS = false;
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWindowsOS = true;
		}
		return isWindowsOS;
	}

	/**
	 * 获取本地Host名称
	 */
	public static String getLocalHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

	/**
	 * 获取Linux下的IP地址
	 *
	 * @return IP地址
	 * @throws SocketException
	 */
	private static ArrayList<?> getLinuxLocalIp() throws SocketException {
		ArrayList<Object> ips = new ArrayList<>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				String name = intf.getName();
				if (!name.contains("docker") && !name.contains("lo")) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							String ipaddress = inetAddress.getHostAddress().toString();
							if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
								ips.add(ipaddress);
							}
						}
					}
				}
			}
		} catch (SocketException ex) {
			ips=null;
			ex.printStackTrace();
		}
		return ips;
	}
}
