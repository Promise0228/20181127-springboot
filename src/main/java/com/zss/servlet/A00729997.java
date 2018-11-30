package com.zss.servlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
/**
 * 服务名称：智慧E保（EB）对账文件下载
 * 作        者：赵帅
 * 日        期：2018/11/7
 * 详        情：从EB获取对账文件*_SOA_SEND.xlsx,通过外联平台sftp传给ESB
 */
public class A00729997 {
	private Session ebsession = null;
	private Session esbsession = null;
	private ChannelSftp ebsftp = null;
	private ChannelSftp esbsftp = null;
	private final String NO_SUCH_FILE = "No such file";
	private long EBfilesize;
	private long ESBfilesize;
	private InputStream downFile;
	static Date date = new Date();
	SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat sdfDate2 = new SimpleDateFormat("yyyyMM");
	static String ECP_NODEID = "01";
	public static void main(String[] args) throws InterruptedException {
		A00729997 a = new A00729997();
		for (int i = 0; i <9; i++) {
			if (i%2==0) {
				Thread.sleep(10 * 1000);
				/*System.out.println(date);
				System.out.println(a.generatePlyEdr());
				System.out.println(date);
				System.out.println(a.generatePlyEdr());
				System.out.println(date);*/
				if (a.generatePlyEdr()) {
					System.out.println("第"+i+"次成功！");
				} else {
					System.out.println("第"+i+"次失败！");
				}
			}
		
		}
	}

	/**
     * 主方法：自动任务调用执行主要逻辑流程
     * @return 返回执行结果  成功/失败
     */
	public boolean generatePlyEdr() {
		String nodeid = ECP_NODEID;
		if (!"01".equals(nodeid)) {
			//TrcLog.log(getClass().getSimpleName() + ".log","此节点不是01主节点，不执行此自动任务");
			return true;
		}
		boolean flag = false;
		// 上传ESB的最终文件名称
		String endFileName = "YB_JX_" + sdfDate1.format(date) + "_SOA_SEND.xlsx";
		System.out.println("上传ESB的最终文件名称"+endFileName);
		// 上传ESB的最终文件临时名称
		String sftpFileName = "YB_JX_" + sdfDate1.format(date) + "_SOA_SEND.xlsxtemp";
		System.out.println("上传ESB的最终文件临时名称"+sftpFileName);
		// 下载EB文件绝对路径
		String directoryeb = "/home/zhaoshuai/SKXFRDZ/" + sdfDate2.format(date) + "/";
		System.out.println("下载EB文件绝对路径"+directoryeb);
		// 上传ESB文件保存绝对路径
		String directoryesb = "/home/zhaoshuai/file/pub/esb/mds/wecp/EBAOLIFE/SKXFRDZ/" + sdfDate2.format(date) + "/";
		System.out.println("上传ESB文件保存绝对路径"+directoryesb);
		//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器开始下载文件" + endFileName);
		downFile = downFile(directoryeb, endFileName);
		// 如果文件下载正常则开始文件上传
		try {
			if (downFile != null) {
				System.out.println("下载成功");
				//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器下载文件完成" + endFileName);
				//TrcLog.log(getClass().getSimpleName() + ".log", "ESB服务器开始上传文件" + endFileName);
				boolean upLoadFile = upLoadFile(directoryesb, sftpFileName, endFileName, downFile);
				if (upLoadFile == true) {
					System.out.println("上传成功");
					//TrcLog.log(getClass().getSimpleName() + ".log", "ESB服务器上传文件成功：" + endFileName);
					//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器开始移动文件到backup目录" + endFileName);
					boolean copyFile = copyFile(directoryeb + endFileName, directoryeb + "backup/" + endFileName);
					if (copyFile) {
						System.out.println("移动成功");
						//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器移动文件到backup目录完成" + endFileName);
						ebsftp.rm(directoryeb + endFileName);
						System.out.println("删除源文件成功！");
						//TrcLog.log(getClass().getSimpleName() + ".log", "EB源文件" + directoryeb + endFileName + "删除成功：");
						flag = true;
					}
				} else {
					//TrcLog.log(getClass().getSimpleName() + ".log", "ESB服务器上传文件失败：");
					return flag;
				}
			} else {
				//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器下载文件失败");
				return flag;
			}
		} catch (Exception e) {
			//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器获取文件异常" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				closeChannelESB();
				closeChannelEB();
			} catch (Exception e) {
				//TrcLog.log(getClass().getSimpleName() + ".log", "关闭链接、通道、session异常" + e.getMessage());
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * 创建ESBsftp连接
	 * 
	 * @return
	 */
	public boolean getConnectESB() {
		boolean flag = false;

		/*String sftpHost = ResPool.configMap.get("ESBSftpHost");
		String port = ResPool.configMap.get("ESBSftpHostPort");
		String sftpUserName = ResPool.configMap.get("ESBSftpUserName");
		String sftpPassword = ResPool.configMap.get("ESBSftpPassword");
		String sftpTimeout = ResPool.configMap.get("ESBSftpTimeout");*/
		/*String sftpHost = "171.33.34.116";
		String sftpUserName = "esbapp";
		String sftpPassword = "EsbApp01";*/
		String sftpHost = "192.36.2.112";
		String sftpUserName = "zhaoshuai";
		String sftpPassword = "123456";
		String sftpTimeout = "3000";
		String port = "22";

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
			//TrcLog.log("A00729997.log", "ESB服务器连接失败！" + e.getMessage());
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 创建亿保sftp连接
	 * 
	 * @return
	 */
	public boolean getConnectEB() {
		boolean flag = false;

		String sftpHost = "192.36.2.18";
		String port = "22";
		String sftpUserName = "zhaoshuai";
		String sftpPassword = "123456";
		String sftpTimeout = "3000";

		try {
			JSch jsch = new JSch(); // 创建JSch对象
			// 默认的端口22 此处我是定义到常量类中；
			int ftpPort = 22;
			// 判断端口号是否为空，如果不为空，则赋值
			if (port != null && !port.equals("")) {
				ftpPort = Integer.valueOf(port);
			}
			// 按照用户名,主机ip,端口获取一个Session对象
			ebsession = jsch.getSession(sftpUserName, sftpHost, ftpPort);
			if (sftpPassword != null) {
				ebsession.setPassword(sftpPassword); // 设置密码
			}
			if (!(sftpTimeout == null || "".equals(sftpTimeout))) {
				ebsession.setTimeout(Integer.parseInt(sftpTimeout)); // 设置timeout时候
			}
			// 并且一旦计算机的密匙发生了变化，就拒绝连接。
			ebsession.setConfig("StrictHostKeyChecking", "no");
			// 默认值是 “yes” 此处是由于我们SFTP服务器的DNS解析有问题，则把UseDNS设置为“no”
			ebsession.setConfig("UseDNS", "no");
			ebsession.connect(); // 经由过程Session建树链接
			ebsftp = (ChannelSftp) ebsession.openChannel("sftp"); // 打开SFTP通道
			ebsftp.connect(); // 建树SFTP通道的连接
			flag = true;
		} catch (JSchException e) {
			//TrcLog.log("A00729997.log", "EB服务器连接失败！" + e.getMessage());
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 文件上传
	 * 
	 * @param path 上传ESB文件的绝对路径
	 * @param filename 上传ESB的临时文件名称
	 * @param endfilename 上传ESB的成功后最终文件名称
	 * @param is EB上传到ESB文件的InputStream流
	 * @return 成功返回true/失败返回false
	 * @throws UnsupportedEncodingException
	 */
	public boolean upLoadFile(String path, String filename, String endfilename, InputStream is) {
		// 检查sftp连接是否存在
		try {
			//TrcLog.log(getClass().getSimpleName() + ".log", "初始化esbsftp连接");
			System.out.println("远程连接ESB："+getConnectESB());
			//TrcLog.log(getClass().getSimpleName() + ".log", "esbsftp连接初始化完成！");
		} catch (Exception e) {
			e.printStackTrace();
			//TrcLog.log(getClass().getSimpleName() + ".log", "esbsftp连接初始化失败" + e.getMessage());
		}
		try {
			// ESB上传目录是否存在，不存在则创建
			validatePath(path+"backup/");
			System.out.println("创建~/backup目录："+esbsftp.pwd());
			validatePath(path);
			System.out.println("切换到上传ESB文件目录："+esbsftp.pwd());
			// 开始临时文件上传
			esbsftp.put(is, filename);
			// 获取上传到ESB的文件属性
			SftpATTRS stat = esbsftp.stat(filename);
			// 获取文件属性中文件大小
			ESBfilesize = stat.getSize();
			System.out.println("ESBfilesize:"+ESBfilesize);
			System.out.println("EBfilesize:"+EBfilesize);
			//TrcLog.log(getClass().getSimpleName() + ".log", "下载上传完成后ESB服务器文件大小:" + ESBfilesize);
			//TrcLog.log(getClass().getSimpleName() + ".log", "下载上传完成后EB服务器文件大小:" + ESBfilesize);
			if (ESBfilesize != 0 && EBfilesize != 0 && ESBfilesize == EBfilesize) {
				// 修改文件名称
				esbsftp.rename(filename, endfilename);
				esbsftp.chmod(Integer.parseInt("775", 8), path+endfilename);
			} else {
				return false;
			}
		} catch (SftpException e) {
			//TrcLog.log(getClass().getSimpleName() + ".log", endfilename + "文件上传ESB失败：" + e.getMessage());
			System.out.println(e.getMessage());
			return false;
			
		} finally {
			/*if (is != null) {
				try {
					//is.close();
				} catch (IOException e) {
					//TrcLog.log(getClass().getSimpleName() + ".log", "上传文件流关闭失败：" + e.getMessage());
					e.printStackTrace();
				}
			}*/
		}
		return true;
	}

	/**
	 * 断开连接
	 * 
	 * @throws Exception
	 */
	private void closeChannelEB() throws Exception {
		try {
			if (ebsftp != null) {
				ebsftp.disconnect();
				ebsftp.getSession().disconnect();
			}
		} catch (Exception e) {
			throw new Exception("close esbftp error.");
		}
	}

	/**
	 * 验证服务器文件夹路径，如不存在则新建
	 * @param path EB的backup目录的绝对路径
	 */
	private void validatePathEB(String path) throws SftpException {
		try {
			ebsftp.cd("/");
			ebsftp.cd(path);
		} catch (SftpException e) {
			if (NO_SUCH_FILE.equals(e.getMessage()) || "".equals(e.getMessage())) {
				String[] paths = path.split("/");
				for (String p : paths) {
					if (!"".equals(p)) {
						try {
							ebsftp.cd(p);
						} catch (SftpException e1) {
							ebsftp.mkdir(p);
							ebsftp.chmod(Integer.parseInt("775",8), path);
							ebsftp.cd(p);
						}
					}
				}
			} else {
				//TrcLog.log(getClass().getSimpleName() + ".log", path + "EB目录切换或创建失败" + e.getMessage());
				throw e;
			}
		}

	}

	/**
	 * 验证服务器文件夹路径，如不存在则新建
	 * 
	 * @param path ESB上传文件的绝对路径
	 */
	private void validatePath(String path) throws SftpException {
		try {
			esbsftp.cd("/");
			esbsftp.cd(path);
		} catch (SftpException e) {
			if (NO_SUCH_FILE.equals(e.getMessage()) || "".equals(e.getMessage())) {
				String[] paths = path.split("/");
				for (String p : paths) {
					if (!"".equals(p)) {
						try {
							esbsftp.cd(p);
						} catch (SftpException e1) {
							esbsftp.mkdir(p);
							System.out.println("bef:"+esbsftp.pwd()+"/"+p);
							esbsftp.chmod(Integer.parseInt("775",8), esbsftp.pwd()+"/"+p);
							System.out.println("sft:"+esbsftp.pwd()+"/"+p+"--------");
							esbsftp.cd(p);
						}
					}
				}
			} else {
				//TrcLog.log(getClass().getSimpleName() + ".log", path + "ESB目录切换或创建失败" + e.getMessage());
				throw e;
			}
		}

	}

	/**
	 * 下载文件
	 * @param path 下载EB文件绝对路径
	 * @param filename 下载EB文件名称
	 * @return 返回下载文件InputStream流
	 */
	public InputStream downFile(String path, String filename) {
		try {
			//TrcLog.log(getClass().getSimpleName() + ".log", "初始化ebsftp连接");
			System.out.println("远程连接EB： "+getConnectEB());
			//TrcLog.log(getClass().getSimpleName() + ".log", "ebsftp连接初始化完成");
			// 切换目录到path路径
			ebsftp.cd("/");
			ebsftp.cd(path);
			System.out.println("切换到EB文件存放目录："+ebsftp.pwd());
			if (isFileExist(path + filename)) {
				// 用sftp对象以InputStream流的形式获取要下载文件
				InputStream is = ebsftp.get(filename);
				// 用SftpATTRS获取远程文件的属性信息
				SftpATTRS stat = ebsftp.stat(filename);
				// 获取文件信息中文件的大小
				EBfilesize = stat.getSize();
				//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器下载文件" + path + filename + "的大小为：" + EBfilesize + "B");
				return is;
			} else {
				//TrcLog.log(getClass().getSimpleName() + ".log", "EB服务器下载文件不存在！");
				return null;
			}
		} catch (SftpException e) {
			//TrcLog.log(getClass().getSimpleName() + ".log", "从EB服务器获取文件：" + filename + "异常" + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 关闭sftp连接
	 * @throws Exception
	 */
	public void closeChannelESB() throws Exception {
		try {
			if (esbsftp != null) {
				esbsftp.disconnect();
				esbsftp.getSession().disconnect();
			}
		} catch (Exception e) {
			throw new Exception("close esbftp error.");
		}
	}

	/**
	 * 复制文件
	 * @param src 复制源文件的绝对路径(含文件名称)
	 * @param dst 复制目的文件的绝对路径(含文件名称)
	 * @throws Exception
	 * @return boolean 成功/失败
	 */
	public boolean copyFile(String src, String dst) throws Exception {
		ByteArrayInputStream bStreams = null;
		InputStream ebis = null;
		try {
			if (isDirExist(src)) {
				// 文件不存在直接反回.
				return false;
			}
			String path = dst.substring(0, dst.lastIndexOf("/"));
			validatePathEB(path);
			ebis = ebsftp.get(src);
			byte[] srcFtpFileByte = inputStreamToByte(ebis);
			bStreams = new ByteArrayInputStream(srcFtpFileByte);
			// 二进制流写文件
			ebsftp.put(bStreams, dst);
			ebsftp.chmod(Integer.parseInt("775",8), dst);
			return true;
		} catch (Exception e) {
			//TrcLog.log(getClass().getSimpleName() + ".log", "从E宝服务器移动文件到backup目录失败！" + e.getMessage());
			throw e;
		} finally {
			if (bStreams != null) {
				bStreams.close();
			}
			if (ebis != null) {
				ebis.close();
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
	 * @param directory 
	 * @return
	 * @throws SftpException
	 */
	public boolean isDirExist(String directory) throws SftpException {
		boolean isDirExistFlag = false;
		try {
			SftpATTRS sftpATTRS = ebsftp.lstat(directory);
			isDirExistFlag = true;
			return sftpATTRS.isDir();
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().equals("no such file")) {
				//TrcLog.log(getClass().getSimpleName() + ".log", directory + "文件不存在：" + e.getMessage());
				isDirExistFlag = false;
			}
		}
		return isDirExistFlag;
	}

	/**
	 * 判断远程文件是否存在
	 * 
	 * @param srcSftpFilePath 文件的绝对路径
	 * @return 存在返回true/失败false
	 * @throws SftpException
	 */
	public boolean isFileExist(String srcSftpFilePath) throws SftpException {
		boolean isExitFlag = false;
		// 文件大于等于0则存在文件
		if (getFileSize(srcSftpFilePath) >= 0) {
			isExitFlag = true;
		}
		return isExitFlag;
	}

	/**
	 * 得到远程文件大小
	 * @param srcSftpFilePath 文件的绝对路径
	 * @return 返回文件大小,如返回-2 文件不存在,-1文件读取异常
	 * @throws SftpException
	 */
	public long getFileSize(String srcSftpFilePath) throws SftpException {
		long filesize = 0;// 文件大于等于0则存在
		try {
			SftpATTRS sftpATTRS = ebsftp.lstat(srcSftpFilePath);
			filesize = sftpATTRS.getSize();
		} catch (Exception e) {
			filesize = -1;// 获取文件大小异常
			if (e.getMessage().toLowerCase().equals("no such file")) {
				filesize = -2;// 文件不存在
			}
		}
		return filesize;
	}

}