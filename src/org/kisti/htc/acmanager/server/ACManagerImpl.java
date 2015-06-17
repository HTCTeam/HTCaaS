package org.kisti.htc.acmanager.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.commons.lang.StringUtils;
import org.kisti.htc.dbmanager.beans.User;
import org.kisti.htc.dbmanager.server.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uib.client.UIB;
import util.mLogger; 
import util.mLoggerFactory;


public class ACManagerImpl implements ACManager {

  private static final Logger logger = LoggerFactory.getLogger(ACManagerImpl.class);

  private String DBManagerURL;
  private String FTPaddress;
  private Database dbClient;
  private static String SSLClientPath;
  private static String SSLClientPassword;
  private static String SSLCAPath;
  private static String SSLCAPassword;
  private static boolean SSL = false;
  private ConnectionDetails connectionDetails; 
  
  protected static Integer AUTH_TRUE = 1;
  protected static Integer AUTH_FALSE = 0;
  
  // ERROR_CODE
  public static final Integer NOERR = -1;
  public static final Integer ERR01 = 1; // (Need to register)on HTCaaS or PLSI
  public static final Integer ERR02 = 2; // HTCaaS ID/PW  is incorrect 
  public static final Integer ERR03 = 3; // (PLSI cert user) PLSI passwd is incorrect
  public static final Integer ERR04 = 4; // PLSI passwd is incorrect (PLSI CERT USER) 
  public static final Integer ERR05 = 5; // (PLSI OTP User) PLSI passwd is incorrect
  public static final Integer ERR06 = 6; //PLSI passwd is incorrect (PLSI OTP USER)
  public static final Integer ERR07 = 7; // Failed to add user
  public static final Integer ERR08 = 8; // Failed to update pw
  public static final Integer ERR09 = 9; // Unpermitted Flag
  
  public static final String USER = "00";
  public static final String GENERAL_USER = "01";
  public static final String PLSI_USER = "10";
  public static final String ALL_USER = "11";
  public static final String OTP_PLSI_USER = "+10";
  public static final String OTP_ALL_USER = "+11";

  public ACManagerImpl() {

    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));
      DBManagerURL = prop.getProperty("DBManager.Address");

      if(prop.getProperty("SSL.Authentication").equals("true")){
        SSL = true;
        DBManagerURL = DBManagerURL.replace("http", "https");
        SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
        SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
        SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
        SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
      }

      logger.info("DBManagerURL: {}", DBManagerURL);

      FTPaddress = prop.getProperty("FTP.Address");
      logger.info("FTP.Address: {}", FTPaddress);

    } catch (Exception e) {
      logger.error("Failed to load config file: {} " , e.getMessage());
      System.exit(1);
    }

    ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
    factory.setServiceClass(Database.class);
    factory.setAddress(DBManagerURL);

    factory.getServiceFactory().setDataBinding(new AegisDatabinding());
    dbClient = (Database) factory.create();

    if(SSL){
          try {
        setupTLS(dbClient);
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (GeneralSecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
        }

  }

  // ACManagerImpl::setupTLS
  private static void setupTLS(Database port) throws FileNotFoundException, IOException, GeneralSecurityException {

    HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

    TLSClientParameters tlsCP = new TLSClientParameters();
    KeyStore keyStore = KeyStore.getInstance("JKS");
    String keyStoreLoc = SSLClientPath;
    keyStore.load(new FileInputStream(keyStoreLoc), SSLClientPassword.toCharArray());
    KeyManager[] myKeyManagers = getKeyManagers(keyStore, SSLClientPassword);
    tlsCP.setKeyManagers(myKeyManagers);

    KeyStore trustStore = KeyStore.getInstance("JKS");
    String trustStoreLoc = SSLCAPath;
    trustStore.load(new FileInputStream(trustStoreLoc), SSLCAPassword.toCharArray());
    TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
    tlsCP.setTrustManagers(myTrustStoreKeyManagers);

    // The following is not recommended and would not be done in a
    // prodcution environment,
    // this is just for illustrative purpose
    tlsCP.setDisableCNCheck(true);
    tlsCP.setSecureSocketProtocol("SSL");   // addme

    httpConduit.setTlsClientParameters(tlsCP);

  }

  // ACManagerImpl::getTrustManagers
  private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
    fac.init(trustStore);
    return fac.getTrustManagers();
  }

  // ACManagerImpl::getKeyManagers
  private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
    KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
    fac.init(keyStore, keyPass);
    return fac.getKeyManagers();
  }


  // ACManagerImpl::Login
  //  사용자 로그인 수행(인증 결과 반환)
  //  flag 가 10 또는 11인 경우에는(PLSI에 등록된 사용자),
  //  client 측에서 PLSI 인증서 및 OTP 검사를 하여 True인 경우만 본 함수를 호출하도록 함
  @Override
  public boolean Login(String userId, String userPw, String flag, String otp) {
    logger.debug("Login userId = " + userId);
    logger.debug("Login flag = " + flag);
    logger.debug("Login userPw = " + userPw);

    boolean ret = false;

    if (flag.equals(USER)){   // HTCaaS & PLSI 등록 안된 사용자
       ret = false;
       logger.info("Need to register on HTCaaS or PLSI- {}", userId);
      return  ret;

    } else if(flag.equals(GENERAL_USER)){  // HTCaaS에만 등록된 사용자
      ret = checkHTCaasUserValid(userId, userPw);  // HTCaaS User DB에 저장된 id/pw 일치 검사
      logger.info("Only registered on HTCaaS, not PLSI- {}", userId);
      return  ret;

    } else if(flag.equals(PLSI_USER)){    // PLSI에만 등록된 사용자
	      if(checkPLSIUserValid(userId, userPw, otp)){
	          ret = HTCaasUserAdd("dn", getUserName(userId, userPw), userId, userPw, "4" ); // HTCaaS User DB에 사용자 추가
	          if(!ret){
	            logger.info("Failed to register your id. Contact your administrator ");
	          }
	      }else {
	    	  logger.info("PLSI_Cert is valid, but User password is incorrect-{}", userId);
	    	  return false;
	      } 
      return ret;

    } else if (flag.equals(ALL_USER)){   // HTCaaS & PLSI  모두 등록된 사용자

      ret = checkPLSIUserValid(userId, userPw, otp); // PLSI User Validity Check
      if (ret) {
          ret = updateUserPasswd(userId, userPw);
          if(!ret){
            logger.info("Failed to update your password. Contact your administrator ");
          }
          
    	} else {
    	  logger.info("PLSI Password is incorrect-{}", userId);	
    	}
      // true 이면, HTCaaS DB Update
      // false이면 false 리턴
      return ret;
      
    } else if (flag.equals(OTP_PLSI_USER)){
    	
    	if(checkPLSIUserValid(userId, userPw, otp)){
    		ret = HTCaasUserAdd("dn", "OTP_user", userId, userPw, "4" ); //XXX Add user name from OTP info
	          if(!ret){
		            logger.info("Failed to register your id. Contact your administrator ");
	                  }
    	} else {
	    	  logger.info("PLSI_OTP is valid, but User password is incorrect-{}", userId);
	    	  return false;
	      } 
    	return ret;
  
    } else if (flag.equals(OTP_ALL_USER)){ 
     
      ret = checkPLSIUserValid(userId, userPw, otp); // PLSI User Validity Check
        if (ret) {
            ret = updateUserPasswd(userId, userPw);
            if(!ret){
              logger.info("Failed to update your password. Contact your administrator ");
            }
            
      	} else {
      	  logger.info("PLSI Password is incorrect-{}", userId);	
      	}       
        	return ret;
        	
    }else {
      logger.error("ACManager- not permitted flag:  {}", flag);
      return ret;
    }
  }
  
  
 ////////BEGIN OF Login2(..)//////////
  @Override
  public Map<Integer, Integer> Login2(String userId, String userPw, String flag, String otp) {
    logger.debug("Login userId = " + userId);
    logger.debug("Login flag = " + flag);
    logger.debug("Login userPw = " + userPw);

    boolean temp = false;
    Map<Integer, Integer> result = new HashMap<Integer, Integer>(); 
    // Map <{RESULT}, {ERROR_CODE}> 
    // {RESULT} = 0: false, 1: true 
    // {ERROR_CODE} = {-1||1||2||3||4||5||6||7||8||9}

	if (flag.equals(USER)){   // HTCaaS & PLSI 등록 안된 사용자
		
		logger.info("Need to register on HTCaaS or PLSI- {}", userId);
		result.put(AUTH_FALSE, ERR01); 
		return  result;

	} else if(flag.equals(GENERAL_USER)){ 
		
		temp = checkHTCaasUserValid(userId, userPw);  // HTCaaS User DB에 저장된 id/pw 일치 검사
		logger.info("Only registered on HTCaaS, not PLSI- {}", userId);
		
		if(temp){  result.put(AUTH_TRUE, NOERR ); 
		} else  {  result.put(AUTH_FALSE, ERR02); }
		
		return  result;

	} else if(flag.equals(PLSI_USER)){ 
    	
		if(checkPLSIUserValid(userId, userPw, otp)){
	          temp = HTCaasUserAdd("dn", getUserName(userId, userPw), userId, userPw, "4" ); // HTCaaS User DB에 사용자 추가
		          if(!temp){
		            logger.info("Failed to register your id. Contact your administrator ");
		            result.put(AUTH_TRUE, ERR07);
		          } else { result.put(AUTH_TRUE, NOERR); }
		      return result;    
		} else {
	    	  logger.info("PLSI_Cert is valid, but PLSI password is incorrect-{}", userId);
	    	  result.put(AUTH_FALSE, ERR03); 
	    	  return result;
		} 

	} else if (flag.equals(ALL_USER)){  

		temp = checkPLSIUserValid(userId, userPw, otp); // PLSI User Validity Check
		if (temp) { //PLSI is VALID
			temp = updateUserPasswd(userId, userPw);
	          if(!temp){
	        	  logger.info("Failed to update your password. Contact your administrator ");
	        	  result.put(AUTH_TRUE, ERR08);
	          } else { result.put(AUTH_TRUE, NOERR); }
          return result;
		} else { 
    	  logger.info("PLSI Password is incorrect-{}", userId);	
    	  result.put(AUTH_FALSE, ERR04);
    	  return result;
    	}

    } else if (flag.equals(OTP_PLSI_USER)){ 
    	
    	if(checkPLSIUserValid(userId, userPw, otp)){
    		temp = HTCaasUserAdd("dn", "OTP_user", userId, userPw, "4" ); //XXX Add user name from OTP info
	          if(!temp){
		            logger.info("Failed to register your id. Contact your administrator ");
		            result.put(AUTH_TRUE, ERR07);
	           } else { result.put(AUTH_TRUE, NOERR); }
		      return result;    
    	} else {
	    	  logger.info("PLSI_OTP is valid, but PLSI password is incorrect-{}", userId);
	    	  result.put(AUTH_FALSE, ERR05); 
	    	  return result;
    	} 

    } else if (flag.equals(OTP_ALL_USER)){  
     
    	temp = checkPLSIUserValid(userId, userPw, otp); // PLSI User Validity Check
        if (temp) { // PLSI OTP is valid
            temp = updateUserPasswd(userId, userPw);
            if(!temp){
              logger.info("Failed to update your password. Contact your administrator ");
        	  result.put(AUTH_TRUE, ERR08);
            } else { result.put(AUTH_TRUE, NOERR);  }
            return result;
        } else {
        	  logger.info("PLSI Password is incorrect-{}", userId);	
	    	  result.put(AUTH_FALSE, ERR06); 
        	  return result;
      	}       

    } else { 
    	logger.error("ACManager- Unpermitted flag:  {}", flag);
    	result.put(AUTH_FALSE, ERR09);
      return result;
      
    }
	
	
  }
  /////////END OF Login2(..)////////////
  
  
  // ACManagerImpl::checkUserID
  //  사용자가 HTCaaS / PLSI 에 등록되어 있는지 검사
  //  검사 수행이 안되면 -00 이 반환됨(DBManager server 연결이 제대로 안되는 경우)
  @Override
  public String checkUserID(String userId) {
    logger.info("checkUserID: {}", userId);

    String flag="-00";
    
    boolean avail = dbClient.getServiceInfraAvail(LoginConstants.PLSI) || dbClient.getServiceInfraAvail(LoginConstants.CONDOR);
    
    if(avail){
    	// checkPlsiUser 함수 호출
    	// ACManagerImpl::checkPlsiUser -> CertUtils::Usercheck
    	try {
    		if (!checkPlsiUser(userId)) {
    			
    			if (!checkHTCaasUser(userId)) {
    				flag="00";
			} else {
    				flag="01";
    			}
    			
    		} else if (!checkHTCaasUser(userId)){
    			flag="10";
    			if (checkOTPUser(userId)){ // XXX 
    				flag ="+10";
    			}
    		} else {
    			flag="11";
    			if (checkOTPUser(userId)){ // XXX 
    				flag ="+11";
    			}
    		}
    		
    	} catch (Exception e) {
    		logger.error("Failed to check userID: {}", e.toString());
    	}
    }else{
    	if (!checkHTCaasUser(userId)) {
			flag="00";
		} else {
			flag="01";
		}
    }
    return flag;
  }


  // ACManagerImpl::checkPlsiUser
  //  해당 userid 가 PLSI 사용자 인지 체크
  //   PLSI 의 웹서비스 (NEW_CA) / UserChecker.jsp  로 userid를 넘김
  //     (UserChecke.jsp에서) userid를   PLSI DB('cert' table)에서 찾아
  //     레코드 개수를 반환 받고, 그 개수를 통해 사용자의 plsi 등록 여부를 확인
  @Override
  public boolean checkPlsiUser(String userId) {
    logger.info("ACManagerImpl::checkPlsiUser- {}", userId);

    boolean result = false;
    try {
    	// OTP 사용자인지 확인
    	if(checkOTPUser(userId)){
          logger.info("ACManagerImpl::checkPlsiUser_OTP userid valid");
          result = true;	
         	
      // 인증서 해당 사용자 
      // 해당 uid 가 유효한지 검사한다. UserChecker.jsp 를 호출함
	    }else  if (CertUtils.Usercheck(userId, false)) {
	        logger.info("ACManagerImpl::checkPlsiUser userid valid");
	        result = true;
	      } else {
	        logger.info("ACManagerImpl::checkPlsiUser userid invalid");
	        result = false;
	      }
      return result;

    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error in chekPlsiUser : {}", e.getMessage());
      return false;
    }
  }

//ACManagerImpl::checkPLSIUserValid
  // PLSI 서버로  userid 와 userPw 를 채크함
  @Override
  public boolean checkPLSIUserValid(String userId, String userPw, String otp) {
//  public boolean checkPLSIUserValid(String userId, String userPw) { 

	  connectionDetails = new ConnectionDetails(LoginConstants.SERVER, userId, userPw, otp);
	
	  Session session = Session.getInstance();
    logger.debug("session = " + session);

	  session.setConnectionDetails(connectionDetails);
	
	  if (!session.createSession(Session.HTTP_PATH_LOGIN)) {
		  logger.error("Error in CreateSession of checkPLSIUserValid()");
		  return false;
	  }
    
    logger.debug("checkPLSIUserValid return true");
	  return true;
  }
  
  // ACManagerImpl::checkHTCaasUser
  // 해당 userid 가 HTCaaS 사용자 인지 채크
  @Override
  public boolean checkHTCaasUser(String userId) {
    logger.info("ACManagerImpl::checkHTCaaSUser");

    boolean result = false;
    // db manager 를 호출
    result = dbClient.checkUser(userId);
    return result;
  }

  // ACManagerImpl::checkHTCaasUserValid
  // userid 와 userPw 를 채크함
  @Override
  public boolean checkHTCaasUserValid(String userId, String userPw) {
    logger.info("checkHTCaaSUserValid");

    String pw = dbClient.getUserPasswd(userId);
    // 단순 문자열 비교
    if (!StringUtils.equals(userPw, pw)) {
      return false;
    }
    return true;
  }

  // ACManagerImpl::checkPLSICert
  // plsi 인증서의 유효성 체크를 위해 Client 에서 전달한 메시지(UserID, Serial Num, 개인키를 이용해서 암호화한 UserID 로 구성)를
  // CertChecker.jsp 로 보냄
  @Override
  public String checkPLSICert(String data) {
    logger.info("checkPLSICert");

    String result= null;
    try {
      result = CertUtils.sendSignMessage2(data, false);
    } catch (UnsupportedEncodingException e) {
      logger.error("Error(Encoding problem) in checkPLSICert - {}", e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      logger.error("Error in checkPLSICert - {}", e.getMessage());
      e.printStackTrace();
    }

    return result;
  }

  // ACManagerImpl::HTCaasUserAdd
  // HTCaaS DB (User)에 사용자 레코드 추가 
  // { ftpserver에 사용자 계정 생성 --> 삭제}
  @Override
  public boolean HTCaasUserAdd(String dn, String name, String userId,String userPw, String serviceInfra ) {
    logger.info("HTCaaSUserAdd");

    boolean result = false;
    // Insert user record into HTCaaS DB (DBManager 사용)
    int id = dbClient.addUser(dn, name, userId, userPw, serviceInfra);  // 사용자 추가

    if(id>0){
//		 Create user account on FTP server 
//		      try {
//		        if(!System.getProperty("os.name").startsWith("Window"))  // acmanager (server)가 window상에서 실행될 때는 진행X
//		        { adduser(userId,userPw); }
//		      } catch (Exception e) {
//		        e.printStackTrace();
//		      }
		      result = true; 
    }

    return result;
  }

  // ACManagerImpl::HTCaasUserDel
  //HTCaaS DB (User)에 사용자 레코드 삭제 
  // (ftpserver에 사용자 계정 제거 --> 삭제 )
  @Override
  public void HTCaasUserDel(String userId) {
    logger.info("HTCaaSUserDel");

    /* delete User DB record  (DBManager 사용) */
    dbClient.deleteUser(dbClient.getId(userId));

//    try {     // delete user account from FTP server
//    if(!System.getProperty("os.name").startsWith("Window")) // acmanager (server)가 window상에서 실행될 때에는 진행X
//      { deluser(userId); }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  // ACManagerImpl::HTCaasUserList
  // HTCaaS 사용자 목록 반환 (각각의 사용자가 줄 단위로 구분)
  @Override
  public List<User>  HTCaasUserList() {     //public List<String>  HTCaasUserList() {
    logger.info("HTCaaSUserList");
    List<User> usrList = null;
    usrList = dbClient.getUserObjectList();

    return usrList;
  }

  // ACManagerImpl::HTCaasUserUpdate
  //  사용자 정보 업데이트
  //  index : 수정하려는 항목의 번호
  //  change :  변경 내용
  @Override
  public boolean HTCaasUserUpdate(String userId, User user ) {
    logger.info("HTCaaSUserUpdate");

    boolean result = false;

    result = dbClient.setUserInfo(userId, user);

    if(result){
      logger.error("Error in HTCaasUserUpdate - {} ", userId);
    }

    return result;
  }

  // ACManagerImpl::HTCaasUserInfo
   //  특정 사용자에 대한 정보를 반환
  @Override
  public User HTCaasUserInfo(String userId) {
    logger.info("HTCaasUserInfo- {}", userId);

    return dbClient.getUserInfo(userId);
  }

  // ACManagerImpl::getPLSICertInfo
  //  특정 사용자의 PLSI 인증 정보 반환
  @Override
  public String getPLSICertInfo(String userId, String userPw) {
    logger.info("getPLSICertInfo-{}", userId);

    String info = null;
   if ( checkPlsiUser(userId)){    //plsi 사용자인지 재검사
      try {
        info = CertUtils.getCertInfo(userId, false);    //Show_info.jsp 호출(userId 입력)
 // System.out.println("info:"+info);
       } catch (Exception e) {
         logger.error("getPLSICertInfo - {}", e.getMessage());
        e.printStackTrace();
      }
    }
  return info;
  }
  
  // To get a group id for given user
  @Override
  public int getUserGroup(String userId) {
	  logger.info("getUserGroupId- {}", userId);

	  int u_gid=-1;
	  User user = null; 
	  user = dbClient.getUserInfo(userId);
	  
	  u_gid = user.getUsergroupId();
	  return u_gid;
  }

  // ACManagerImpl::adduser
  //  FTP Server에 사용자 계정 생성
  private void adduser(String userId, String userPw) throws Exception {

    logger.info("+ Try to add New user: {} "+ userId);

    String command = "";
    String line="";

    // acmanager server가 실행되는 곳에서 FTP server로 ssh 접속 후, pipe로
    // 사용자 추가(password 동시 설정)  명령 넘겨줌
     command = "sudo ssh root@"+ FTPaddress +" /usr/sbin/useradd "+ userId ;
    //    + userId + "; echo " + userPw + " | passwd --stdin " + userId + "'";
     logger.info("Command: {}", command);

    Process process = Runtime.getRuntime().exec(command);

    logger.info("| [OutputStream]");

    BufferedReader br = new BufferedReader(new InputStreamReader(
        process.getInputStream()));

    while ((line = br.readLine()) != null) {
      logger.info("| " + line);
    }

    br.close();

    logger.info("| [ErrorStream]");
    br = new BufferedReader(new InputStreamReader(
        process.getErrorStream()));

    while ((line = br.readLine()) != null) {
      logger.info("| " + line);
    }

        command = "sudo ssh root@"+ FTPaddress +" echo "+ userPw + " | /usr/bin/passwd --stdin " +userId;
        logger.info("Command: {}", command);
        process = Runtime.getRuntime().exec(command);
        logger.info("| [OutputStream]");

        br = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

        while ((line = br.readLine()) != null) {
                logger.info("| " + line);
        }

        br.close();

        logger.info("| [ErrorStream]");
        br = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));

        while ((line = br.readLine()) != null) {
                logger.info("| " + line);
        }


  }

  // ACManagerImpl::deluser
  //  FTP Server에 사용자 계정 삭제
  private void deluser(String userId) throws Exception {

    logger.info("+ Try to delete user: {}"+ userId);

    String command = "";
    String line="";

    // acmanager server가 실행되는 곳에서 FTP server로 ssh 접속 후, pipe로
    // 사용자 삭제(-r : 홈디렉터리 함께 삭제)  명령 넘겨줌
    command= "sudo ssh root@"+ FTPaddress +" /usr/sbin/userdel -r  "+ userId;

    Process process = Runtime.getRuntime().exec(command);

    logger.info("| [OutputStream]");

    BufferedReader br = new BufferedReader(new InputStreamReader(
        process.getInputStream()));

    while ((line = br.readLine()) != null) {
      logger.info("| " + line);
    }

    br.close();

    logger.info("| [ErrorStream]");
    br = new BufferedReader(new InputStreamReader(
        process.getErrorStream()));

    while ((line = br.readLine()) != null) {
      logger.info("| " + line);
    }

//    command = "sudo ssh root@"+ FTPaddress +" rm -rf /home/" + userId;
//        logger.info("Command: {}", command);
//        process = Runtime.getRuntime().exec(command);
//        logger.info("| [OutputStream]");
//
//        br = new BufferedReader(new InputStreamReader(
//                        process.getInputStream()));
//
//        while ((line = br.readLine()) != null) {
//                logger.info("| " + line);
//        }
//
//        br.close();
//
//        logger.info("| [ErrorStream]");
//        br = new BufferedReader(new InputStreamReader(
//                        process.getErrorStream()));
//
//        while ((line = br.readLine()) != null) {
//                logger.info("| " + line);
//        }

  }

  // ACManagerImpl::getUserName
  // plsi 인증서 정보로 부터 사용자의 실제 이름을 반환
  // plsi 인증 정보를 getPLSICertInfo(userid, pw) 로 부터 얻음
  private String getUserName(String userId, String userPw){

    String name = null;
    //String [] info ;
    String temp = null;
    
    try {
    	temp = getPLSICertInfo(userId , userPw);
    //	System.out.println(temp);
    	temp= temp.toString();
    	name = temp.split("\n")[1];
      //name = info[1];   //넘겨받은 인증 정보를 line으로 구분하여,  사용자 이름인 2번째 정보(0부터 시작) 를 받아옴.
      return name;
    } catch (Exception e) {
      e.printStackTrace();
      return name;
    }
  }
  
  //ACManagerImpl::updateUserPasswd
  //plsi 인증정보로 HTCaaS DB 업데이트
  //param : userId(사용자 ID), userPw(사용자비밀번호)
  private boolean updateUserPasswd(String userId, String userPw) {

	  logger.debug("updateUserPasswd %s / %s", userId, userPw);

	  boolean result = false;

	  try {
	    dbClient.setUserPasswd(userId, userPw);
	    result = true;

	  } catch (Exception e) {
      logger.error("Cannot update database");
		  //e.printStackTrace();
		  //logger.error(e.getMessage());
	  }

	  return result;
  }

  // ACManagerImpl::OTPCheck
  private boolean checkOTPUser(String userId){
    logger.info("checkOTPUser :" + userId);
	  
	  boolean result = false;
	  int flag =0;
	  
	  if(checkHTCaasUser(userId)){
		  flag = dbClient.getUserInfo(userId).getOtpflag();
		  logger.debug("The DB Flag :" + flag);
	  }else {
		  flag = -1;
	  }
	  if(flag != 1){
	    //logger.info("ID " + userId + "  is not the OTP user");
			  UIB uib = new UIB();
			  uib.init(LoginConstants.UIB_URL);
			  
			  Map param = new HashMap();
			  param.put("id", userId);
			  param.put("otpValue", "000000");
			  
			  Map checkOTP = (Map) uib.get("OTPService", param);
			  //System.out.println(":::"+checkOTP.get("message").toString());
			  String code = checkOTP.get("message").toString().trim().substring(1, 5);
			  logger.info("Code: "+code);
			  
			  if("6024".equals(code) || "6023".equals(code) || "6022".equals(code)){
				  // get the message
				  logger.debug("Not OTP user %s ", userId);
				  result = false;
			  }else {
				  if(flag != -1){ 
					  result = dbClient.updateOTPflag(userId, 1);
				  } else{
					  result = true;
					  logger.info("ID:"+userId + "  is OTP user, but not a HTCaaS user");
//				  } else{
//					  // if flag == -1
					  result = HTCaasUserAdd("dn", "new user", userId, "", "4" ); 
					  result = dbClient.updateOTPflag(userId, 1);
				  }
				  
//				  result = true;
			  }
	  }else { // 1이면
	    logger.info("ID " + userId + " is the OTP user");
		  result = true;
	  }
	  
	 return result; 
  }
  
  // ACManagerImpl::main
  public static void main(String[] args) {

    ACManagerImpl ac = new ACManagerImpl();

      System.out.println(ac.checkPLSIUserValid("p260ksy", "rlatjdud2!","517608"));

   }


}
