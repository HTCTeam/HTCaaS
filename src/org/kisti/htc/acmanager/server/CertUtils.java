package org.kisti.htc.acmanager.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
//import javax.crypto.Cipher;
//import org.bouncycastle.util.encoders.Base64;
//import org.bouncycastle.util.encoders.Hex;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.kisti.htc.acmanager.server.LoginConstants;

public class CertUtils {
  
  boolean debugFlag = false;

  /**
  * CertUtils::sendSignMessage2
  * 생성한 서명문을 서버로 전송 uid is userid , sno is user certificate's serial no, sign_msg is sign message
  * @param data (uid  &   user certificate's serial no &  sign_msg converted with privKey  )
  * @param debug_mode Debug mode flag
  * @return 암호문 
  * @throws UnsupportedEncodingException UnsupportedEncodingException
  * @throws Exception Exception
  */
  //output : received message (인증 결과) 
  public static String sendSignMessage2(String data, boolean debug_mode) throws UnsupportedEncodingException, Exception {

    //String svrurl = "http://localhost:8080/NEW_CA/CertChecker.jsp";
    String svrurl = LoginConstants.CASERVER + "/CertChecker.jsp";
    String SVR_MSG = null;    
    String sRet = "";

    /* data = Userid || Serial Number of Certificate || Signed Message [ Userid] */
    //String data = URLEncoder.encode("USER", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" 
    // + URLEncoder.encode("SNUM", "UTF-8") + "=" + URLEncoder.encode(sno, "UTF-8") + "&" + URLEncoder.encode("SMSG", "UTF-8") + "=" + URLEncoder.encode(sign_msg, "UTF-8");

    URL url = new URL(svrurl);
    URLConnection conn = url.openConnection();

    conn.setDoOutput(true);
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    wr.write(data);
    wr.flush();

    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
    String line;
    while ((line = rd.readLine()) != null) {
      sRet = line.replace(" ", "");

      if (sRet.length() == 0)
        continue;

      if (debug_mode)
        System.out.println("[in send msg]" + sRet);

      String HEADER = "<SERVER_SIGN>";
      String TAILER = "</SERVER_SIGN>";

      if (sRet.matches(HEADER + ".*")) {

        /** 서버가 사용자의 공개키로 암호화해서 보낸 사용자 ID를 복호화해서 
         * 사용자 ID와의 일치성을 재검사 맞으면 인증 OK, 다르면 인증 실패  */

        // 암호문 추출 (서버로 부터 전달받음) 
         SVR_MSG = sRet.substring(sRet.indexOf(HEADER) + HEADER.length(), sRet.indexOf(TAILER));

        // 복호화 (Client 부분에서 진행)
        //        String SVR_UID = convertSignMessageString(SVR_MSG, privKey);
        //        if (SVR_UID.equals(uid)) {
        //          return_status = true;
        //        } else {
        //          return_status = false;
        //        }
         
      }
    }
    wr.close();
    rd.close();

    return SVR_MSG;
  }

  /**
  *  해당 uid 가 유효한지 검사한다.
  * UserChecker.jsp 를 호출함
  * LoginConstants.CASERVER + "/UserChecker.jsp";
  * 유효하면 true 를 리턴, 그렇지 않으면 false 를 리턴
  */
  // ACManagerImpl::checkPlsiUser -> CertUtils::Usercheck
  public static boolean Usercheck(String uid, boolean debug_mode) throws  Exception {

    // portal.plsi.or.kr 에 요청
    String svrurl = LoginConstants.CASERVER + "/UserChecker.jsp";

    // sangwan
    //System.out.println(svrurl);

    String sRet = "";
    boolean return_status = false;

    String data = URLEncoder.encode("USER", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") ;
    URL url = new URL(svrurl);
    URLConnection conn = url.openConnection();

    // 서버로 메시지를 보낸다.
    conn.setDoOutput(true);
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    //System.out.println(data);

    wr.write(data);
    wr.flush();

    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
    String line;
    while ((line = rd.readLine()) != null) {
      if (line.length() == 0)
        continue;

      if (debug_mode) System.out.println("[in send msg]" + sRet);

      // line에서 공백을 뺌
      line = line.trim();

    // sangwan
    System.out.println(line);

        if (!line.matches("CNT = 0")) {
          return_status = true; // success
        } else {
          return_status = false; // fail
        }
    }
    wr.close();
    rd.close();

    return return_status;
  }

    /**
     *  해당 uid 의 plsi 인증 정보 요청.
     *   Show_info.jsp 를 호출함
     *   LoginConstants.CASERVER + "/Show_info.jsp";
     *   plsi 인증서 정보를 string으로 리턴( 인증 정보 각각이 '/' 로 구분되어 있으므로 '\n'  로 구분되도록 변경)
         * # ACManagerImpl.getPLSICertInfo()
     */
    public static String getCertInfo(String uid, boolean debug_mode) throws  Exception {
      
      String svrurl = LoginConstants.CASERVER + "/Show_info.jsp"; // NEW_CA/Show_info.jsp
      String sRet = null;
      String data = URLEncoder.encode("USER", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") ;
      

      URL url = new URL(svrurl);
      URLConnection conn = url.openConnection();

      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();

      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      String line;
      String results=null;
      while ((line = rd.readLine()) != null) {
        
          sRet = line.replace("/", "\n");

        if (line.length() == 0){continue;}
        results=sRet;
        
        if (debug_mode)
        { System.out.println("[in send msg]\n" + results);  }
        
         //   { /*System.out.println("[in send msg]" + sRet); */ return sRet;}        
      	}
      wr.close();
      rd.close();
      return results;
    }  
    
    /**
     *  (수정버전) 해당 uid 의 plsi 인증 정보 요청.
     *   show_cert_info.jsp 를 호출함
     *   LoginConstants.CASERVER + "/show_cert_info.jsp"; 
     *   plsi 인증서 정보를 string으로 리턴( 인증 정보 각각이 '/' 로 구분되어 있으므로 '\n'  로 구분되도록 변경)
         * # ACManagerImpl.getPLSICertInfo()
     */
    public static String getCertInfo2(String uid, boolean debug_mode) throws  Exception {
        
        String svrurl = LoginConstants.CASERVER + "/show_cert_info.jsp"; // NEW_CA/Show_info.jsp
        String sRet = null;
        String data = URLEncoder.encode("userid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") ;
 System.out.println(data);
        
        URL url = new URL(svrurl);
        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line;
      
        while ((line = rd.readLine()) != null) {
          
//            sRet = line.replace("/", "\n");
        	  sRet+= line;

          if (line.length() == 0)
            continue;

          if (debug_mode)
          { System.out.println("[in send msg]" + sRet); }
           //   { /*System.out.println("[in send msg]" + sRet); */ return sRet;}
          
        }
        wr.close();
        rd.close();
        return sRet;
      }  
    
    public static void main(String[] args) {
    	CertUtils cu = new CertUtils();
    	String message=null;
    	try {
			message= cu.getCertInfo2("p260ksy", true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(message);
    }
    
    
}
