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

/**
 * 
 * 服务名称：A00729996(文件推送)
 * 作       者：赵帅
 * 时       间：20192019年1月4日上午9:44:21
 * 详       情：{A服务器~/xx/。目录下的文件推送到B服务器~/xx/。目录下（连接方式SFTP）}
 */
public class A00729996 {
	private final String NO_SUCH_FILE = "No such file";
	private long ESBfilesize;

	public static void main(String[] args) throws SftpException {
		A00729996 a = new A00729996();
		System.out.println(a.sftpUpLoadFile());
	}

	/**
	 * 文件上传下载主方法
	 * 
	 * @return 文件传输状态
	 */
	public boolean sftpUpLoadFile() {
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

    /**
     * 服务名称： 文件推送
     * @param ssftp 源SFTP
     * @param osftp 目的SFTP
     * @param directoryesb 源文件推送路径（绝对路径）
     * @param directoryeb 目的文件推送路径（绝对路径）
     * @return
     */
	public boolean downFile(ChannelSftp ssftp, ChannelSftp osftp, String directoryesb, String directoryeb) {
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
     * @param sftp 目的SFTP
     * @param path 目的SFTP推送路径（觉得路径）
     * @param filename 推送文件名称（临时文件名）
     * @param endfilename 推送文件名称（最终文件名称）
     * @param is 源SFTP推送文件InputStream流
     * @return 返回推送状态
     */
	public boolean upLoadFile(ChannelSftp sftp, String path, String filename, String endfilename, InputStream is) {
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
	 * 验证服务器文件夹路径，如不存在则新建
	 * @param path 上传EB目录绝对路径
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
     * 
     * @param sftp SFTP连接
     * @param path SFTP路径下的文件
     * @return 文件或路径大小
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
	 * 关闭sftp连接
	 * @param sftp SFTP对象
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
	 * @param sftp 当前SFTP对象
	 * @param srcSftpFilePath 下载文件的绝对路径
	 * @return 存在返回true/不存在返回false
	 * @throws SftpException
	 */
	public boolean isFileExist(ChannelSftp sftp, String srcSftpFilePath) throws SftpException {
		boolean isExitFlag = false;
		// 文件大于等于0则存在文件
		if (getFileSize(sftp, srcSftpFilePath) >= 0) {
			isExitFlag = true;
		}
		return isExitFlag;
	}

	/**
	 * 得到远程文件大小
	 * @param sftp 当前SFTP对象
	 * @param srcSftpFilePath 下载文件的绝对路径
	 * @return 返回文件大小,如返回-2 文件不存在,-1文件读取异常
	 * @throws SftpException
	 */
	public long getFileSize(ChannelSftp sftp, String srcSftpFilePath) throws SftpException {
		long filesize = 0;// 文件大于等于0则存在
		try {
			SftpATTRS sftpATTRS = sftp.lstat(srcSftpFilePath);
			filesize = sftpATTRS.getSize();
			System.out.println("文件是否存在：" + filesize);
		} catch (Exception e) {
			filesize = -1;// 获取文件大小异常
		}
		return filesize;
	}

	/**
	 * 服务名称：备份文件
	 * @param sftp 源SFTP对象
	 * @param src 备份源文件的绝对路径(含文件名称)
	 * @param dst 备份成功后文件存放的绝对路径(含文件名称)
	 * @return 成功返回true/失败返回false
	 * @throws Exception
	 */
	public boolean copyFile(ChannelSftp sftp, String src, String dst) throws Exception {
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
	 * inputStream类型转换为byte类型
	 * @param iStrm 需要转换类型的InputStream流
	 * @return byte类型数组
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
	 * @param sftp SFTP
	 * @param directory 校验目录的路径（绝对路径）
	 * @return 是文件返回false/是目录返回true
	 * @throws SftpException
	 */
	public boolean isDirExist(ChannelSftp sftp, String directory) throws SftpException {
		try {
			SftpATTRS sftpATTRS = sftp.lstat(directory);
			return sftpATTRS.isDir();
		} catch (Exception e) {
			return false;
		}
	}

}