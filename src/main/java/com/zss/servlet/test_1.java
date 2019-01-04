package com.zss.servlet;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class test_1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test_1 t = new test_1();
		Session session = null;
		ChannelSftp sftp = null;
		String sftpHost = "192.36.2.103";
		String sftpUserName = "zhaoshuai";
		String sftpPassword = "123456";
		String sftpTimeout = "30000";
		String port = "22";
		System.out.println(t.getConnectSftp(session, sftp, sftpHost, sftpUserName, sftpPassword, sftpTimeout, port));
		

	}
	
	
	
	public boolean getConnectSftp(Session session,ChannelSftp sftp,String sftpHost,String sftpUserName,String sftpPassword,String sftpTimeout,String port) {
		boolean flag = false;
		// 默认的端口22 此处我是定义到常量类中；
		int ftpPort = 22;
		try {
			JSch jsch = new JSch(); // 创建JSch对象
			// 判断端口号是否为空，如果不为空，则赋值
			if (port != null && !port.equals("")) {
				ftpPort = Integer.valueOf(port);
				// 按照用户名,主机ip,端口获取一个Session对象
				session = jsch.getSession(sftpUserName, sftpHost, ftpPort);
				if (sftpPassword != null) {
					session.setPassword(sftpPassword); // 设置密码
				}
				if (!(sftpTimeout == null || "".equals(sftpTimeout))) {
					session.setTimeout(Integer.parseInt(sftpTimeout)); // 设置timeout时候
				}
				// 并且一旦计算机的密匙发生了变化，就拒绝连接。
				session.setConfig("StrictHostKeyChecking", "no");
				// 默认值是 “yes” 此处是由于我们SFTP服务器的DNS解析有问题，则把UseDNS设置为“no”
				session.setConfig("UseDNS", "no");
				session.connect(); // 经由过程Session建树链接
				sftp = (ChannelSftp) session.openChannel("sftp"); // 打开SFTP通道
				sftp.connect(); // 建树SFTP通道的连接
				flag = true;
			} else {
				return flag;
			}
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			// TrcLog.log("A00729998.log", "ESB服务器连接失败！" + e.getMessage());
			System.out.println(e.getMessage());
		}
		return flag;
	}

}
