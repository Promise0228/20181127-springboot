/**   
  * @Title: pushfile 
  * @Package com.zss.servlet
  * @Description: TODO(SFTP远程文件上传下载) 
  * @author 赵帅  
  * @date 20192019年1月4日上午10:39:21
  * @version V1.0   
  */
package com.zss.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * 
  * @Title: 文件推送
  * @Description: TODO(用一句话描述该文件做什么) 
  * @author 赵帅
  * @date 2019年1月4日
 */
public class PushFile {
	private static final String NO_SUCH_FILE = "no such file";
	private long ESBfilesize;
	/** 
	* @return 
	 * @Title: 文件推送主方法入口
	* @Description: TODO(文件推送方法参数获取与配置) 
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午10:45:05 
	*/
	private boolean sftpPushFile() {
		SimpleDateFormat sdfDate1 = new SimpleDateFormat("YYYYMMdd");
		// 源推送文件目录directoryesb、directoryesb1
        
		String directoryesb = "/home/zhaoshuai/file/pub/esb/mds/wecp/EBAOLIFE/SKXFRDZ/" + sdfDate1.format(new Date())
				+ "/";
		String directoryesb1 = "/home/zhaoshuai/file/pub/esb/mds/wecp/EBAOLIFE/CB/" + sdfDate1.format(new Date()) + "/";
		// 目的推送文件目录directoryeb、directoryeb1
		String directoryeb = "/home/zhaoshuai/SKXFRDZ/" + sdfDate1.format(new Date()) + "/";
		String directoryeb1 = "/home/zhaoshuai/CB/" + sdfDate1.format(new Date()) + "/";
		// 源SFTP连接参数
		String sftpHost = "192.36.3.199";
		String sftpUserName = "zhaoshuai";
		String sftpPassword = "123456";
		String sftpTimeout = "3000";
		String port = "22";
		// 目的SFTP连接参数
		String sftpHost1 = "192.36.2.103";
		String sftpHost2 = "192.36.1.244";
		// 获取源SFTP连接
		ChannelSftp esbsftp = getConnectSftp(sftpHost, sftpUserName, sftpPassword, sftpTimeout, port);
		// 获取目的SFTP连接
		ChannelSftp ebsftp = getConnectSftp(sftpHost1, sftpUserName, sftpPassword, sftpTimeout, port);
		// 调起推送文件的方法
		boolean downFile = downFile(esbsftp, ebsftp, directoryesb, directoryeb);
		// 获取源SFTP连接
		ChannelSftp esbsftp1 = getConnectSftp(sftpHost, sftpUserName, sftpPassword, sftpTimeout, port);
		// 获取目的SFTP连接
		ChannelSftp eb1sftp = getConnectSftp(sftpHost2, sftpUserName, sftpPassword, sftpTimeout, port);
		// 调起推送文件的方法
		boolean downFile2 = downFile(esbsftp1, eb1sftp, directoryesb1, directoryeb1);
		if (downFile && downFile2) {
			return true;
		}
		return false;
	

	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param esbsftp
	* @param ebsftp
	* @param directoryesb
	* @param directoryeb
	* @return
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:00:53 
	*/
	private boolean downFile(ChannelSftp ssftp, ChannelSftp osftp, String directoryesb, String directoryeb) {

		boolean flag = false;
		InputStream is = null;
		try {
			if (ssftp == null) {// 源SFTP连接状态
				System.out.println("源SFTP连接失败");
				return false;
			}
			// 切换目录到源SFTP路径
			System.out.println(ssftp.pwd());
			ssftp.cd(directoryesb);
			System.out.println(ssftp.pwd());
			@SuppressWarnings("unchecked")
			// 获取源SFTP路径下的所有文件及目录详情
			Vector<ChannelSftp.LsEntry> ls = ssftp.ls(ssftp.pwd());
			String filename;
			// 迭代遍历源目录下的所有信息
			Iterator<LsEntry> iterator = ls.iterator();
			for (LsEntry lsEntry : ls) {
				if (iterator.hasNext()) {
					// 获取目录或文件的名称
					filename = lsEntry.getFilename();
					// 获取文件的大小
					ESBfilesize = getSftpFileSize(ssftp, directoryesb + filename);
					// 是否是目录
					if (isDirExist(ssftp, directoryesb + filename)) {
						System.out.println(filename + ":不是推送文件");
						continue;
					}
					System.out.println("当前推送文件为：" + filename);
					// 切换目录到源SFTP路径
					ssftp.cd(directoryesb);
					// 下载当前推送文件
					is = ssftp.get(filename);
					if (osftp == null) {// 目的SFTP连接状态
						System.out.println("目的SFTP连接失败");
						return false;
					}
					// 调起推送文件的方法
					flag = upLoadFile(osftp, directoryeb, filename + "temp", filename, is);
					if (flag) {
						try {// 调遣备份文件的方法
							flag = copyFile(ssftp, directoryesb + filename, directoryesb + "backup/" + filename);
							if (flag) {
								// 删除源推送文件
								ssftp.rm(directoryesb + filename);
							} else {
								System.out.println("文件移动到backup目录失败");
							}
						} catch (Exception e) {
							System.out.println("文件移动异常：" + e.getMessage());
						}
					} else {
						System.out.println("文件上传失败");
					}

				}
			}

		} catch (SftpException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return true;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				closeChannel(ssftp);
				closeChannel(osftp);
			} catch (Exception e) {
				// TrcLog.log(getClass().getSimpleName() +
				// ".log","关闭链接、通道、session异常"+e.getMessage());
				e.printStackTrace();
			}
		}
		return flag;
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param ssftp
	 * @throws Exception 
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:08:41 
	*/
	private void closeChannel(ChannelSftp sftp) throws Exception {

		try {
			if (sftp != null) {
				sftp.disconnect();
				sftp.getSession().disconnect();
			}
		} catch (Exception e) {
			throw new Exception("close esbftp error.");
		}
	
		      
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param ssftp
	* @param string
	* @param string2
	* @return
	 * @throws IOException 
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:06:36 
	*/
	private boolean copyFile(ChannelSftp sftp, String src, String dst) throws IOException {

		ByteArrayInputStream bStreams = null;
		InputStream esbis = null;
		try {
			if (isDirExist(sftp, src)) {
				// 是目录直接返回.
				return false;
			}
			String psth = dst.substring(0, dst.lastIndexOf("/"));
			validatePath(sftp, psth);
			esbis = sftp.get(src);
			byte[] srcFtpFileByte = inputStreamToByte(esbis);
			bStreams = new ByteArrayInputStream(srcFtpFileByte);
			// 二进制流写文件
			sftp.put(bStreams, dst);
			sftp.chmod(Integer.parseInt("775", 8), dst);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (bStreams != null) {
				bStreams.close();
			}
			if (esbis != null) {
				esbis.close();
			}
		}
	  
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param esbis
	* @return
	 * @throws IOException 
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:07:26 
	*/
	private byte[] inputStreamToByte(InputStream iStrm) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = iStrm.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata; 
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param osftp
	* @param directoryeb
	* @param string
	* @param filename
	* @param is
	* @return
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:04:29 
	*/
	private boolean upLoadFile(ChannelSftp sftp, String path, String endfilename, String filename, InputStream is) {

		try {
			// 切换到目的SFTP推送路径
			validatePath(sftp, path);
			// 推送文件
			sftp.put(is, filename);
			// 获取推送文件大小
			long EBfilesize = getSftpFileSize(sftp, path+filename);
			System.out.println("EBfilesize:" + EBfilesize);
			System.out.println("ESBfilesize:" + ESBfilesize);
			if (ESBfilesize == EBfilesize) {
				// 修改推送文件名
				sftp.rename(filename, endfilename);
				// 修改推送文件权限
				sftp.chmod(Integer.parseInt("775", 8), path + endfilename);
			} else {
				return false;
			}
		} catch (SftpException e) {
			return false;
		}
		return true;
	  
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param sftp
	* @param path
	 * @throws SftpException 
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:05:39 
	*/
	private void validatePath(ChannelSftp sftp, String path) throws SftpException {

		try {
			sftp.cd("/");
			sftp.cd(path);
		} catch (SftpException e) {
			if (NO_SUCH_FILE.equals(e.getMessage()) || "".equals(e.getMessage())) {
				String[] paths = path.split("/");
				for (String p : paths) {
					if (!"".equals(p)) {
						try {
							sftp.cd(p);
						} catch (SftpException e1) {
							sftp.mkdir(p);
							sftp.chmod(Integer.parseInt("775", 8), path);
							sftp.cd(p);
						}
					}
				}
			} else {
				throw e;
			}
		}

	 
		      
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param ssftp
	* @param string
	* @return
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:03:28 
	*/
	private boolean isDirExist(ChannelSftp sftp, String directory) {

		boolean isDirExistFlag = false;
		try {
			SftpATTRS sftpATTRS = sftp.lstat(directory);
			isDirExistFlag = true;
			return sftpATTRS.isDir();
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().equals("no such file")) {
				isDirExistFlag = false;
			}
		}
		return isDirExistFlag;
	
	}
	/** 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param ssftp
	* @param string
	* @return
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:02:30 
	*/
	private long getSftpFileSize(ChannelSftp sftp, String path) {

		long filesize = 0;// 文件大于等于0则存在
		try {
			SftpATTRS sftpATTRS = sftp.lstat(path);
			filesize = sftpATTRS.getSize();
		} catch (Exception e) {
			filesize = -1;// 获取文件大小异常
			if (e.getMessage().toLowerCase().equals("no such file")) {
				filesize = -2;// 文件不存在
			}
		}
		return filesize;
	}
	/**
	 * 
	* @Title:  
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param sftpHost
	* @param sftpUserName
	* @param sftpPassword
	* @param sftpTimeout
	* @param port
	* @return
	* @throws 
	* @author 赵帅 
	* @date 2019年1月4日 上午11:00:41
	 */
	public ChannelSftp getConnectSftp(String sftpHost, String sftpUserName, String sftpPassword, String sftpTimeout,
			String port) {
		Session session = null;
		ChannelSftp sftp = null;
		int ftpPort = 22;
		try {
			JSch jsch = new JSch();
			if (port != null && !port.equals("")) {
				ftpPort = Integer.valueOf(port);
				session = jsch.getSession(sftpUserName, sftpHost, ftpPort);
				if (sftpPassword != null) {
					session.setPassword(sftpPassword);
				}
				if (!(sftpTimeout == null || "".equals(sftpTimeout))) {
					session.setTimeout(Integer.parseInt(sftpTimeout));
				}
				session.setConfig("StrictHostKeyChecking", "no");
				// 默认值是 “yes” 此处是由于我们SFTP服务器的DNS解析有问题，则把UseDNS设置为“no”
				session.setConfig("UseDNS", "no");
				session.connect(); // 经由过程Session建树链接
				sftp = (ChannelSftp) session.openChannel("sftp"); // 打开SFTP通道
				sftp.connect(); // 建树SFTP通道的连接
			} else {
				return null;
			}
		} catch (JSchException e) {
			System.out.println(e.getMessage());
			return null;
		}
		return sftp;
	}

}
