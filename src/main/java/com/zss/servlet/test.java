package com.zss.servlet;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class test {
	static private Session esbsession = null;
	static private ChannelSftp esbsftp = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getConnectESB();
		try {
			esbsftp.rename("/home/zhaoshuai/file/test.xml1", "/home/zhaoshuai/file/test.xml");
			System.out.println("ok");
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			System.out.println("errer");
			e.printStackTrace();
		} finally {
			try {
				closeChannelESB();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean getConnectESB() {
		boolean flag = false;

		String sftpHost = "192.36.1.150";
		String port = "22";
		String sftpUserName = "zhaoshuai";
		String sftpPassword = "123456";
		String sftpTimeout = "3000";

		// 默认的端口22 此处我是定义到常量类中；
		int ftpPort = 22;
		try {
			JSch jsch = new JSch(); // 创建JSch对象
			// 判断端口号是否为空，如果不为空，则赋值
			if (port != null && !port.equals("")) {
				ftpPort = Integer.valueOf(port);
				// 按照用户名,主机ip,端口获取一个Session对象
				esbsession = jsch.getSession(sftpUserName, sftpHost, ftpPort);
				if (sftpPassword != null) {
					esbsession.setPassword(sftpPassword); // 设置密码
				}
				if (!(sftpTimeout == null || "".equals(sftpTimeout))) {
					esbsession.setTimeout(Integer.parseInt(sftpTimeout)); // 设置timeout时候
				}
				// 并且一旦计算机的密匙发生了变化，就拒绝连接。
				esbsession.setConfig("StrictHostKeyChecking", "no");
				// 默认值是 “yes” 此处是由于我们SFTP服务器的DNS解析有问题，则把UseDNS设置为“no”
				esbsession.setConfig("UseDNS", "no");
				esbsession.connect(); // 经由过程Session建树链接
				esbsftp = (ChannelSftp) esbsession.openChannel("sftp"); // 打开SFTP通道
				esbsftp.connect(); // 建树SFTP通道的连接
				flag = true;
			} else {
				return flag;
			}
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return flag;
	}
	public static void closeChannelESB() throws Exception {
		try {
			if (esbsftp != null) {
				esbsftp.disconnect();
				esbsftp.getSession().disconnect();
			}
		} catch (Exception e) {
			throw new Exception("close esbftp error.");
		}
	}

}
