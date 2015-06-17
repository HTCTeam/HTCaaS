package org.kisti.htc.acmanager.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import org.kisti.htc.acmanager.server.ACManager;

public class ACManagerClientImpl implements ACManagerClient {

	private String ACManagerURL;
	private ACManager acm;
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;

	public ACManagerClientImpl() throws Exception {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			ACManagerURL = prop.getProperty("ACManager.Address");

			if (prop.getProperty("SSL.Authentication").equals("true")) {
				SSL = true;
				ACManagerURL = ACManagerURL.replace("http", "https");
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}

		} catch (Exception e) {
			System.exit(1);
		}

		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(ACManager.class);
		factory.setAddress(ACManagerURL);

		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		acm = (ACManager) factory.create();

		if (SSL) {
			setupTLS(acm);
		}
	}
	
	public static void main(String arg[]){
		try {
			ACManagerClientImpl ac = new ACManagerClientImpl();
			  
	        String userid = "p143ksw";
			String pw = "kisti0568%";
			String cert = "p143ksw.der";
			String certpw = "rlatkddhks";
			ac.HTCaasLogin(userid, pw, cert, certpw);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void setupTLS(ACManager port) throws FileNotFoundException, IOException, GeneralSecurityException {

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
		tlsCP.setSecureSocketProtocol("SSL"); // addme

		httpConduit.setTlsClientParameters(tlsCP);

	}

	private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
		fac.init(trustStore);
		return fac.getTrustManagers();
	}

	private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
		KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
		fac.init(keyStore, keyPass);
		return fac.getKeyManagers();
	}

	public boolean checkPLSICert(String userId, String userPw, String cert, String certPw) {

		X509Certificate uCert;
		PrivateKey uPrivkey;
		boolean status = false;

		try {
			uCert = ReadCert_PEM_FILE(cert);
			String privLocation = makePrivFileLoc(cert);
			uPrivkey = ReadPrivateKey_PEM_FILE(privLocation, certPw);

			// 인증서의 유효 날짜 검사
			if (checkDate(uCert) != true) {
				return false;
			}
			// 읽어온 개인키와 인증서에 읽어온 공개키의 매치 검사
			if (!checkKeyPair(uPrivkey, uCert.getPublicKey())) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		try {

			String extracted_userId = extractUSERID(uCert.getSubjectDN().getName()); // 사용자id추출
			if (!StringUtils.equals(userId, extracted_userId)) { // 입력한 사용자id와
																	// 추출한 id가
																	// 일치하는지 검사
				throw new Exception();
			}

			/* 인증서에서 인증서 일련번호 추출 */
			String seNo = uCert.getSerialNumber().toString();
			/* 아이디 암호화 수행 (개인키 이용) */
			byte[] SigMsgB = generateSignMessage(extracted_userId, uPrivkey);
			String convSign = new String(Base64.encode(SigMsgB));

			// data = UserID, Serial Num, 개인키를 이용해서 암호화한 UserID 로 구성
			String data = URLEncoder.encode("USER", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8") + "&" + URLEncoder.encode("SNUM", "UTF-8") + "=" + URLEncoder.encode(seNo, "UTF-8") + "&"
					+ URLEncoder.encode("SMSG", "UTF-8") + "=" + URLEncoder.encode(convSign, "UTF-8");

			// acmanager 서버 호출 (acm.checkPLSICert(data) )
			// acmanager 서버 호출후 전달받은 메시지(암호화된 사용자 아이디)를 개인키로 복호화
			String SVR_uid = convertSignMessageString(acm.checkPLSICert(data), uPrivkey);

			if (SVR_uid.equals(userId)) { // 사용자 아이디와 서버로부터 전달 받은 아이디가 동일하면 유효
				status = true;
			} else {
				status = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;
	}

	public boolean HTCaasLogin(String userId, String userPw, String cert, String certPw) {

		boolean status = false;
		String flag = acm.checkUserID(userId);
			
	// plsi 사용자인 경우, plsi 인증 검사 후에 서버에 login 요청
//		if (flag.equals("10") || flag.equals("11")) {
//			if (checkPLSICert(userId, userPw, cert, certPw)) { // 인증서가 유효하면
//				status = acm.Login(userId, userPw, flag);
//			} else {
//				status = false;
//			}
//			return status;
//		} else { // flag 가 00 또는 01
//			status = acm.Login(userId, userPw, flag);
//		}
				

		return status;
	}

	 public boolean HTCaasLoginNew(String userId, String userPw, String OTP) {

	    boolean status = false;
	    String flag = acm.checkUserID(userId);

	    if(flag.equals("+10") || flag.equals("+11")){
	      status = acm.Login(userId, userPw, flag, OTP);
	      
	    }else {
	      // logging : not OTP user
	      status = acm.Login(userId, userPw, flag, OTP);
	    }

	    return status;
	  }
	
	// Private 함수들
	// CertUtils

	/**
	 * 전달받은 절대경로(인증서파일)에서 인증서파일을 읽어서 인증서 객체를 생성함
	 * 
	 * @param fileloc
	 *            파일 절대경로
	 * @return X509Certificate
	 * @throws Exception
	 *             Exception
	 */
	private X509Certificate ReadCert_PEM_FILE(String fileloc) throws Exception {
		boolean exist = false;
		for (Provider provider : Security.getProviders()) {
			if (provider instanceof BouncyCastleProvider) {
				exist = true;
			}
		}
		if (!exist) {
			Security.addProvider(new BouncyCastleProvider());
		}

		File file = new File(fileloc);
		FileReader fr = new FileReader(file);
		PEMReader reader = new PEMReader(fr);
		Object ret = reader.readObject();

		reader.close();
		fr.close();

		return (X509Certificate) ret;
	}

	/**
	 * 인증서 파일 경로명에서 확장자 부분을 개인키 확장자로 대체
	 * 
	 * @param filename
	 *            인증서 파일명
	 * @return 개인키 확장자
	 */
	private String makePrivFileLoc(String filename) {
		String filePath = filename;
		int pos = filePath.lastIndexOf(".");
		String fileExt = filePath.substring(0, pos + 1);
		return fileExt.concat("pri");
	}

	/**
	 * 전달받은 절대경로(개인키파일)에서 개인키 파일을 읽어서 개인키 객체를 생성함
	 * 
	 * @param fileloc
	 *            파일 절대경로
	 * @param passwd
	 *            개인키
	 * @return PrivateKey
	 * @throws IOException
	 *             IOException
	 */
	private PrivateKey ReadPrivateKey_PEM_FILE(String fileloc, String passwd) throws IOException {

		FileReader fr = new FileReader(fileloc);
		StringWriter sw = new StringWriter();
		KeyPair kp = null;
		int read = -1;
		while ((read = fr.read()) != -1) {
			sw.append((char) read);
		}

		fr.close();
		PEMReader reader = new PEMReader(new StringReader(sw.toString()), new Password(passwd.toCharArray()));
		Object ret = reader.readObject();
		reader.close();
		sw.close();
		kp = (KeyPair) ret;
		return (PrivateKey) kp.getPrivate();
	}

	private class Password implements PasswordFinder {
		private final char[] password;

		public Password(char[] word) {
			this.password = (char[]) word.clone();
		}

		public char[] getPassword() {
			return (char[]) password.clone();
		}
	}

	// 인증서 만료 여부 확인
	private boolean checkDate(X509Certificate cert) {

		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) {
			return false;
		} catch (CertificateNotYetValidException e) {
			return false;
		}
		return true;
	}

	/**
	 * 키쌍의 유효성을 검사 Check KeyPair (readed privatekey & publickey from
	 * certificate) return 0 : OK matched 1 : Not matched
	 * 
	 * @param priv
	 *            PrivateKey
	 * @param pubkey
	 *            PublicKey
	 * @return return 0 : OK matched 1 : Not matched
	 * @throws Exception
	 *             Exception
	 */
	private boolean checkKeyPair(PrivateKey priv, PublicKey pubkey) throws Exception {
		byte signValue[] = null;
		boolean success = false;

		String SigAlgo = "SHA1withRSA";
		Signature sig = Signature.getInstance(SigAlgo);

		sig.initSign(priv);
		sig.update(SigAlgo.getBytes());
		signValue = sig.sign();

		sig.initVerify(pubkey);
		sig.update(SigAlgo.getBytes());
		success = sig.verify(signValue);

		return success;
	}

	/**
	 * SubjectDN에서 ID 부분만 추출 UID=p123sjk,CN=김성준,Ou=PLSI, OU=KISTI p123sjk추출
	 * 
	 * @param userDN
	 *            사용자 DN
	 * @return ID String
	 */
	private static String extractUSERID(String userDN) {
		StringTokenizer stk = new StringTokenizer(userDN, ",");
		for (; stk.hasMoreElements();) {
			String tmp = stk.nextToken();

			if (tmp.matches("UID.*")) {
				String tmp2[] = tmp.split("=");
				return tmp2[1];
			}
		}
		return null;
	}

	/**
	 * 서버로 전송할 암호문을 작성함
	 * 
	 * @param Seed
	 *            문자 Seed
	 * @param privkey
	 *            PrivateKey
	 * @return Byte Array
	 * @throws Exception
	 */
	private static byte[] generateSignMessage(String Seed, PrivateKey privkey) throws Exception {

		Cipher cipher = Cipher.getInstance("RSA/None/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, privkey);
		byte[] cipherText = cipher.doFinal(Seed.getBytes());

		return Hex.encode(cipherText);
	}

	// 서버로 부터 전송받은 암호문을 복호화 수행
	public static String convertSignMessageString(String Seed, PrivateKey privKey) throws Exception {

		byte[] convBytes = Base64.decode(Seed.getBytes());
		Cipher cipher = Cipher.getInstance("RSA/None/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] plainText = cipher.doFinal(Hex.decode(convBytes));

		return new String(plainText);
	}
}
