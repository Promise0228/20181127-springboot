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
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class A00729996 {
	private final String NO_SUCH_FILE = "No such file";
	private long ESBfilesize;

	public static void main(String[] args) throws SftpException {
		A00729996 a = new A00729996();
	    System.out.println(a.sftpUpLoadFile());
	}

	public boolean sftpUpLoadFile() {
		SimpleDateFormat sdfDate1 = new SimpleDateFormat("YYYYMMdd");
		String directoryesb = "/home/zhaoshuai/file/pub/esb/mds/wecp/EBAOLIFE/SKXFRDZ/" + sdfDate1.format(new Date())+ "/";
		String directoryesb1 = "/home/zhaoshuai/file/pub/esb/mds/wecp/EBAOLIFE/CB/" + sdfDate1.format(new Date())+ "/";
		String directoryeb = "/home/zhaoshuai/SKXFRDZ/" + sdfDate1.format(new Date()) + "/";
		String directoryeb1 = "/home/zhaoshuai/CB/" + sdfDate1.format(new Date()) + "/";
		String sftpHost = "192.36.3.199";
		String sftpUserName = "zhaoshuai";
		String sftpPassword = "123456";
		String sftpTimeout = "3000";
		String port = "22";
		String sftpHost1 = "192.36.2.103";
		String sftpHost2 = "192.36.1.244";
		ChannelSftp	esbsftp = getConnectSftp(sftpHost, sftpUserName, sftpPassword, sftpTimeout, port);
		ChannelSftp ebsftp = getConnectSftp(sftpHost1, sftpUserName, sftpPassword, sftpTimeout, port);
		boolean downFile = downFile(esbsftp,ebsftp,directoryesb, directoryeb);
		ChannelSftp esbsftp1 = getConnectSftp(sftpHost, sftpUserName, sftpPassword, sftpTimeout, port);
		ChannelSftp eb1sftp = getConnectSftp(sftpHost2, sftpUserName, sftpPassword, sftpTimeout, port);
		boolean downFile2 = downFile(esbsftp1,eb1sftp,directoryesb1, directoryeb1);
		if (downFile&&downFile2) {
			return true;
		} 
		return false;
	}
	
	
	public ChannelSftp getConnectSftp(String sftpHost,String sftpUserName,String sftpPassword,String sftpTimeout,String port) {
			Session session = null;
			ChannelSftp sftp = null;
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
			} else {
				return null;
			}
		} catch (JSchException e) {
			// TrcLog.log("A00729998.log", "ESB服务器连接失败！" + e.getMessage());
			System.out.println(e.getMessage());
			return null;
		}
		return sftp;
	}


	/**
	 * 文件上传
	 * 
	 * @param path
	 *            上传EB文件的绝对路径
	 * @param filename
	 *            上传EB文件的临时文件名称
	 * @param filename
	 *            上传EB文件成功后的最终文件名称
	 * @param is
	 *            从ESB获取文件的InputStream流
	 * @return 成功返回true/失败返回false
	 */

	public boolean upLoadFile(ChannelSftp sftp,String path, String filename, String endfilename, InputStream is) {
		// 检查sftp连接是否存在
		try {
			// EB上传目录是否存在，不存在则创建
			validatePath(sftp,path);
			// 开始临时文件上传
			sftp.put(is, filename);
			// 获取上传到EB的文件属性
			SftpATTRS stat = sftp.stat(filename);
			// 获取文件属性中文件大小
			long EBfilesize = stat.getSize();
			// 如果文件大小不为0或者EB服务器文件与ESB服务器文件大小相同则把临时文件名改为目标文件名
			// TrcLog.log(getClass().getSimpleName() +
			// ".log","下载上传完成后ESB服务器文件大小:" + ESBfilesize);
			// TrcLog.log(getClass().getSimpleName() +
			// ".log","下载上传完成后EB服务器文件大小:" + EBfilesize);
			System.out.println("EBfilesize:" + EBfilesize);
			System.out.println("ESBfilesize:" + ESBfilesize);
			if (ESBfilesize == EBfilesize) {
				// 修改文件名称
				sftp.rename(filename, endfilename);
				sftp.chmod(Integer.parseInt("775", 8), path + endfilename);
			} else {
				return false;
			}
		} catch (SftpException e) {
			// TrcLog.log(getClass().getSimpleName() + ".log",
			// endfilename+"EB文件上传失败"+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * 验证服务器文件夹路径，如不存在则新建
	 * 
	 * @param path
	 *            上传EB目录绝对路径
	 */
	private void validatePath(ChannelSftp sftp,String path) throws SftpException {
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
				// TrcLog.log(getClass().getSimpleName() + ".log", path +
				// "目录切换或创建失败"+e.getMessage());
				throw e;
			}
		}

	}

	/**
	 * ESB下载文件
	 * @param sftp 
	 * @param sftp 
	 * 
	 * @param path
	 *            下载ESB文件绝对路径
	 * @param filename
	 *            下载ESB文件的名称
	 * @return is 返回下载ESB文件的InputStream流
	 * @throws IOException
	 */
	public boolean downFile(ChannelSftp ssftp, ChannelSftp osftp, String directoryesb, String directoryeb) {
		boolean flag = false;
		InputStream is=null;
		try {
			if (ssftp==null) {
				System.out.println("源SFTP连接失败");
				return false;
			}
			// 切换目录到ESBpath路径
			System.out.println(ssftp.pwd());
			ssftp.cd(directoryesb);
			System.out.println(ssftp.pwd());
			@SuppressWarnings("unchecked")
			Vector<ChannelSftp.LsEntry> ls = ssftp.ls(ssftp.pwd());
			String filename;
			Iterator<LsEntry> iterator = ls.iterator();
			for (LsEntry lsEntry : ls) {
				if (iterator.hasNext()) {
					filename = lsEntry.getFilename();
					ESBfilesize = getSftpFileSize(ssftp,directoryesb + filename);
					if (isFileExist(ssftp,directoryesb + filename) && !"backup".equals(filename)) {
						if (".".equals(filename) || "..".equals(filename)) {
							continue;
						}
						// 用sftp对象以InputStream流的形式获取要下载文件
						System.out.println(filename);
						ssftp.cd(directoryesb);
						is = ssftp.get(filename);
						if (osftp==null) {
							System.out.println("目的SFTP连接失败");
							return false;
						}
						flag = upLoadFile(osftp,directoryeb, filename + "temp", filename, is);
						if (flag) {
							try {
								flag = copyFile(ssftp,directoryesb + filename, directoryesb + "backup/" + filename);
								if (flag) {
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
			}

		} catch (SftpException e) {
			// TrcLog.log(getClass().getSimpleName() +
			// ".log","从E宝服务器获取文件："+filename+"失败"+e.getMessage());
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

	private long getSftpFileSize(ChannelSftp sftp,String path) {
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
	 * 关闭sftp连接
	 * @param sftp 
	 * 
	 * @throws Exception
	 */
	public void closeChannel(ChannelSftp sftp) throws Exception {
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
	 * 判断远程文件是否存在
	 * @param sftp 
	 * 
	 * @param srcSftpFilePath
	 *            下载ESB文件的绝对路径
	 * @return 存在返回true/不存在返回false
	 * @throws SftpException
	 */
	public boolean isFileExist(ChannelSftp sftp, String srcSftpFilePath) throws SftpException {
		boolean isExitFlag = false;
		// 文件大于等于0则存在文件
		if (getFileSize(sftp,srcSftpFilePath) >= 0) {
			isExitFlag = true;
		}
		return isExitFlag;
	}

	/**
	 * 得到远程文件大小
	 * @param sftp 
	 * 
	 * @param srcSftpFilePath
	 *            下载ESB文件的绝对路径
	 * @return 返回文件大小,如返回-2 文件不存在,-1文件读取异常
	 * @throws SftpException
	 */
	public long getFileSize(ChannelSftp sftp, String srcSftpFilePath) throws SftpException {
		long filesize = 0;// 文件大于等于0则存在
		try {
			SftpATTRS sftpATTRS = sftp.lstat(srcSftpFilePath);
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
	 * 复制文件
	 * @param sftp 
	 * 
	 * @param src
	 *            ESB复制源文件的绝对路径(含文件名称)
	 * @param dst
	 *            ESB复制成功后文件存放的绝对路径(含文件名称)
	 * @return boolean 成功返回true/失败返回false
	 * @throws Exception
	 */
	public boolean copyFile(ChannelSftp sftp, String src, String dst) throws Exception {
		ByteArrayInputStream bStreams = null;
		InputStream esbis = null;
		try {
			if (isDirExist(sftp,src)) {
				// 是目录直接反回.
				return false;
			}
			String psth = dst.substring(0, dst.lastIndexOf("/"));
			validatePath(sftp,psth);;
			esbis = sftp.get(src);
			byte[] srcFtpFileByte = inputStreamToByte(esbis);
			bStreams = new ByteArrayInputStream(srcFtpFileByte);
			// 二进制流写文件
			sftp.put(bStreams, dst);
			sftp.chmod(Integer.parseInt("775", 8), dst);
			return true;
		} catch (Exception e) {
			// TrcLog.log(getClass().getSimpleName() +
			// ".log","从ESB服务器原路径移动文件到backup目录失败！"+e.getMessage());
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
	 * inputStream类型转换为byte类型
	 * 
	 * @param iStrm
	 * @return
	 * @throws IOException
	 */
	public byte[] inputStreamToByte(InputStream iStrm) throws IOException {
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
	 * 判断目录是否存在
	 * @param sftp 
	 * 
	 * @param directory
	 * @return
	 * @throws SftpException
	 */
	public boolean isDirExist(ChannelSftp sftp, String directory) throws SftpException {
		boolean isDirExistFlag = false;
		try {
			SftpATTRS sftpATTRS = sftp.lstat(directory);
			isDirExistFlag = true;
			return sftpATTRS.isDir();
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().equals("no such file")) {
				// TrcLog.log(getClass().getSimpleName() +
				// ".log",directory+"目录不存在！"+e.getMessage());
				isDirExistFlag = false;
			}
		}
		return isDirExistFlag;
	}



}