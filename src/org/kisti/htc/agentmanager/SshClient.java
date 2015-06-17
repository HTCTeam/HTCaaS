package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class SshClient {

  final static Logger logger = LoggerFactory.getLogger(SshClient.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AM");

  long timeout = 5000;
  
  // host, username, password, port
  public Session getSession(String host, String username, String password, int port) throws JSchException {
    JSch jsch = new JSch();
    Session session = jsch.getSession(username, host, port);
    session.setPassword(password);
    session.setDaemonThread(true);
    session.setConfig("StrictHostKeyChecking", "no");
    session.setConfig("PreferredAuthentications", "password");

    if (session.isConnected()) {
      System.out.println("session connect:"+session.isConnected());
      session.disconnect();
    }
    session.connect(5000);

    return session;
  }
  
  // getSession --> getSessionPEM 로 이름 변경
  // host, certificate
  public Session getSessionPEM(String host, String pem) throws JSchException {
    String username = "root";
    int port = 22;
    JSch jsch = new JSch();
    Session session = jsch.getSession(username, host, port);
    session.setDaemonThread(true);
    session.setConfig("StrictHostKeyChecking", "no");
    session.setConfig("PreferredAuthentications", "password");
    
    try {
      JSch.setConfig("StrictHostKeyChecking", "no");
      jsch.addIdentity(pem);
    } catch (JSchException e) {
      e.printStackTrace();
    }
    
    if(session.isConnected()){
      System.out.println("session connect:"+session.isConnected());
      session.disconnect();
    }

    session.connect(5000);

    return session;
  }

  // SshClient::Exec
  public SshExecReturn Exec(String command, Session session, boolean disconn) throws JSchException, IOException {

    SshExecReturn ret = new SshExecReturn();
    StringBuffer stdout = new StringBuffer();
    StringBuffer stderr = new StringBuffer();

    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand("source ~/.bash_profile;" + command);

    channel.setInputStream(null);
    // ((ChannelExec) channel).setErrStream(System.err);
    ((ChannelExec) channel).setErrStream(new FileOutputStream("err"));

    InputStream in = channel.getInputStream();

    channel.connect();

    byte[] tmp = new byte[1024];
    while (true) {
      while (in.available() > 0) {
        int i = in.read(tmp, 0, 1024);
        if (i < 0)
          break;
        stdout.append(new String(tmp, 0, i));
      }

      if (channel.isClosed()) {
        ret.setExitValue(channel.getExitStatus());
        break;
      }

      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }

    ret.setStdOutput(stdout.toString());

    if (ret.getExitValue() != 0) {
      BufferedReader br = new BufferedReader(new FileReader("err"));
      String line = null;
      while ((line = br.readLine()) != null) {
        stderr.append(line);
      }
      br.close();
    }
    ret.setStdError(stderr.toString());

    channel.disconnect();
    if(disconn){
      session.disconnect();
    }
    return ret;
  }
  
//SshClient::Exec
 public SshExecReturn Exec(String command, Session session, boolean disconn, long timeout) throws JSchException, IOException {

   SshExecReturn ret = new SshExecReturn();
   StringBuffer stdout = new StringBuffer();
   StringBuffer stderr = new StringBuffer();

   Channel channel = session.openChannel("exec");
   ((ChannelExec) channel).setCommand("source ~/.bash_profile;" + command);

   channel.setInputStream(null);
   // ((ChannelExec) channel).setErrStream(System.err);
   ((ChannelExec) channel).setErrStream(new FileOutputStream("err"));

   InputStream in = channel.getInputStream();

   channel.connect();
   
   while (true) {
	      if (ready(new InputStreamReader(in), timeout)) {
	    	  byte[] tmp = new byte[1024];
	    	  while (true) {
	    		  while (in.available() > 0) {
	    			  int i = in.read(tmp, 0, 1024);
	    			  if (i < 0)
	    				  break;
	    			  stdout.append(new String(tmp, 0, i));
	    		  }
	    		  
	    		  if (channel.isClosed()) {
	    			  ret.setExitValue(channel.getExitStatus());
	    			  break;
	    		  }
	    		  
	    		  try {
	    			  Thread.sleep(1000);
	    		  } catch (Exception ee) {
	    		  }
	    	  }
	    	  
	    	  ret.setStdOutput(stdout.toString());
	    	  
	    	  if (ret.getExitValue() != 0) {
	    		  BufferedReader br = new BufferedReader(new FileReader("err"));
	    		  String line = null;
	    		  while ((line = br.readLine()) != null) {
	    			  stderr.append(line);
	    		  }
	    		  br.close();
	    	  }
	    	  ret.setStdError(stderr.toString());
	    	  
	    	  channel.disconnect();
	    	  
	    	  break;

	      }else{
	    	  System.out.println("getSSHExec timeout : " + timeout);
	          break;
	      }
	      
   }
   
   if(channel.isConnected()){
	   channel.disconnect();
   }
   
   if(disconn){
	   session.disconnect();
   }
   
   return ret;

 }

  public SshExecReturn Exec2(String command, Session session, boolean disconn) throws JSchException, IOException {

	    SshExecReturn ret = new SshExecReturn();
	    StringBuffer stdout = new StringBuffer();
	    StringBuffer stderr = new StringBuffer();

	    Channel channel = session.openChannel("exec");
	    ((ChannelExec) channel).setCommand("source ~/.bashrc;" + command);

	    channel.setInputStream(null);
	    // ((ChannelExec) channel).setErrStream(System.err);
	    ((ChannelExec) channel).setErrStream(new FileOutputStream("err"));

	    InputStream in = channel.getInputStream();

	    channel.connect();

	    byte[] tmp = new byte[1024];
	    while (true) {
	      while (in.available() > 0) {
	        int i = in.read(tmp, 0, 1024);
	        if (i < 0)
	          break;
	        stdout.append(new String(tmp, 0, i));
	      }

	      if (channel.isClosed()) {
	        ret.setExitValue(channel.getExitStatus());
	        break;
	      }

	      try {
	        Thread.sleep(1000);
	      } catch (Exception ee) {
	      }
	    }

	    ret.setStdOutput(stdout.toString());

	    if (ret.getExitValue() != 0) {
	      BufferedReader br = new BufferedReader(new FileReader("err"));
	      String line = null;
	      while ((line = br.readLine()) != null) {
	        stderr.append(line);
	      }
	      br.close();
	    }
	    ret.setStdError(stderr.toString());

	    channel.disconnect();
	    if(disconn){
	      session.disconnect();
	    }
	    return ret;
	  }
  
  public static boolean ready(Reader in, long timeout) throws IOException {
    while (true) {
      long now = System.currentTimeMillis();
      try {
        while (in.ready() == false && timeout > 0) {
          Thread.sleep(100);
          timeout -= 100;
        }
        return in.ready();
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        // ignore
      } finally {
        // adjust timer by length of last nap
        timeout -= System.currentTimeMillis() - now;
      }
    }
  }

  public List<String> getPLSIInfo(String command, Session session, boolean disconn) throws JSchException, IOException {

    Channel channel = session.openChannel("exec");
    ((ChannelExec) channel).setCommand("source ~/.bash_profile;" + command);

    ((ChannelExec) channel).setErrStream(new FileOutputStream("err"));

    InputStream in = channel.getInputStream();

    channel.connect();

    List<String> plsiList = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String line;
    
    while (true) {
      if (ready(br, timeout)) {
        line = br.readLine();
        Pattern pattern = Pattern.compile("\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
          String cluster = matcher.group(1);
          String nodes = matcher.group(2);
          String availableCPU = matcher.group(3).split("/")[0];
          String totalCPU = matcher.group(4);
          String waitingJobs = matcher.group(5).split("/")[0];
          String pendingJobs = matcher.group(6).split("/")[0];
          String runningJobs = matcher.group(7).split("/")[0];

          logger.info("| cluster:" + cluster + " nodes:" + nodes + " availableCPU:" + availableCPU + " totalCPU:" + totalCPU
              + " waitingJobs:" + waitingJobs + " pendingJobs:" + pendingJobs + " runningJobs:" + runningJobs);
          if (cluster.equals("Total")) {
            continue;
          }
          plsiList.add(cluster + " " + totalCPU + " " + availableCPU + " " + runningJobs + " " + waitingJobs);
        }
      } else {
        System.out.println("getPLSIINFO timeout : " + timeout);
        break;
      }
    }

    // logger.info(plsiList.toString());
    channel.disconnect();
    
    if(disconn){
      session.disconnect();
    }

    return plsiList;
  }

  public void ScpFrom(String rfile, String lfile, Session session, boolean disconn) throws Exception {
    FileOutputStream fos = null;
    try {

      String prefix = null;
      if (new File(lfile).isDirectory()) {
        prefix = lfile + File.separator;
      }

      // exec 'scp -f rfile' remotely
      String command = "scp -f " + rfile;
      Channel channel = session.openChannel("exec");
      ((ChannelExec) channel).setCommand(command);

      // get I/O streams for remote scp
      OutputStream out = channel.getOutputStream();
      InputStream in = channel.getInputStream();

      channel.connect();

      byte[] buf = new byte[1024];

      // send '\0'
      buf[0] = 0;
      out.write(buf, 0, 1);
      out.flush();

      while (true) {
        int c = checkAck(in);
        if (c != 'C') {
          break;
        }

        // read '0644 '
        in.read(buf, 0, 5);

        long filesize = 0L;
        while (true) {
          if (in.read(buf, 0, 1) < 0) {
            // error
            break;
          }
          if (buf[0] == ' ')
            break;
          filesize = filesize * 10L + (buf[0] - '0');
        }

        String file = null;
        for (int i = 0;; i++) {
          in.read(buf, i, 1);
          if (buf[i] == (byte) 0x0a) {
            file = new String(buf, 0, i);
            break;
          }
        }

        // System.out.println("filesize="+filesize+", file="+file);

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        // read a content of lfile
        fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
        int foo;
        while (true) {
          if (buf.length < filesize)
            foo = buf.length;
          else
            foo = (int) filesize;
          foo = in.read(buf, 0, foo);
          if (foo < 0) {
            // error
            break;
          }
          fos.write(buf, 0, foo);
          filesize -= foo;
          if (filesize == 0L)
            break;
        }
        fos.close();
        fos = null;

        if (checkAck(in) != 0) {
          throw new Exception("checkAck Error");
        }

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
      }

      channel.disconnect();
      
      if(disconn){
        session.disconnect();
      }

      logger.debug("Success");
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (fos != null)
          fos.close();
      } catch (Exception ee) {
      }
    }
  }

  public void ScpTo(String lfile, String rfile, Session session, boolean disconn) throws Exception {

    FileInputStream fis = null;
    try {
      
      // exec 'scp -t rfile' remotely
      String command = "scp -p -t " + rfile;
      Channel channel = session.openChannel("exec");
      ((ChannelExec) channel).setCommand(command);

      // get I/O streams for remote scp
      OutputStream out = channel.getOutputStream();
      InputStream in = channel.getInputStream();

      channel.connect();

      if (checkAck(in) != 0) {
        throw new Exception("checkAck Error");
      }

      // send "C0644 filesize filename", where filename should not include
      // '/'
      long filesize = (new File(lfile)).length();
      command = "C0644 " + filesize + " ";
      if (lfile.lastIndexOf('/') > 0) {
        command += lfile.substring(lfile.lastIndexOf('/') + 1);
      } else {
        command += lfile;
      }
      command += "\n";
      out.write(command.getBytes());
      out.flush();
      if (checkAck(in) != 0) {
        throw new Exception("checkAck Error");
      }

      // send a content of lfile
      fis = new FileInputStream(lfile);
      byte[] buf = new byte[1024];
      while (true) {
        int len = fis.read(buf, 0, buf.length);
        if (len <= 0)
          break;
        out.write(buf, 0, len); // out.flush();
      }
      fis.close();
      fis = null;
      // send '\0'
      buf[0] = 0;
      out.write(buf, 0, 1);
      out.flush();
      if (checkAck(in) != 0) {
        throw new Exception("checkAck Error");
      }
      out.close();

      channel.disconnect();
      
      if(disconn){
        session.disconnect();
      }

      logger.debug("Success");

    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (fis != null)
          fis.close();
      } catch (Exception ee) {
      }
    }
  }

  static int checkAck(InputStream in) throws IOException {
    int b = in.read();

    // b may be 0 for success,
    // 1 for error,
    // 2 for fatal error,
    // -1
    if (b == 0)
      return b;
    if (b == -1)
      return b;
    if (b == 1 || b == 2) {
      StringBuffer sb = new StringBuffer();
      int c;
      do {
        c = in.read();
        sb.append((char) c);
      } while (c != '\n');
      if (b == 1) { // error
        System.out.print(sb.toString());
      }
      if (b == 2) { // fatal error
        System.out.print(sb.toString());
      }
    }
    return b;
  }
  
/*
  public static class TestThread extends Thread {
    
    @Override
    public void run(){
      SshClient sc = new SshClient();
      try {
        Session ss = sc.getSession("150.183.158.172", "plsiportal", "zltmxl^^456", 22);
        SshExecReturn ret = sc.Exec("mkdir -p /pwork01/plsiportal/"+Thread.currentThread().getName(), ss, false);
        
        sc.ScpTo("lib/shared/cxf-2.2.8.jar", "/pwork01/plsiportal/"+Thread.currentThread().getName(), ss, true);
//         System.out.println(ret.getStdOutput());
//         System.out.println(ret.getStdError());
//         System.out.println(ret.getExitValue());
      } catch (JSchException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (Exception e){
        e.printStackTrace();
      }
      
    }
  }
*/

  // test code
  public static void main(String[] arg) {
    
    SshClient sc = new SshClient();

    try {
      Session ss = sc.getSession(OpenstackResource.OPENSTACKNAME, OpenstackResource.OPENSTACKID,  OpenstackResource.OPENSTACKID, 6980);

      SshExecReturn ret;

      ret = sc.Exec("hostname", ss, false);
      System.out.println(ret);

//      ret = sc.Exec("date", ss, false);
//      System.out.println(ret);

      ret = sc.Exec("ssh ubuntu@10.1.0.12  hostname", ss, false);
      System.out.println(ret);

//      sc.ScpTo("/root/.bashrc", "/root/.bashrc.tmp", ss, false);
//
//      sc.ScpFrom("/root/.bashrc.tmp", "/root/.bashrc.tmp2", ss, false);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
