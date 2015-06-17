package org.kisti.htc.acmanager.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.kisti.htc.acmanager.server.JSONUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
//import org.eclipse.core.runtime.IAdaptable;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;


/**
 * 어플리케이션 세션 정보 담고 있는 클래스
 * 
 * @author kocjun
 *
// */
//public class Session implements IAdaptable {
public class Session {
  
  private static final Logger logger = LoggerFactory.getLogger(Session.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AC");
  
  public final static int HTTP_GET = 0;
  
  public final static int HTTP_POST = 1;
  
  public final static String HTTP_PATH_LOGIN = "/jsp/session/login";
  
  public final static String HTTP_PATH_USER_INDEX = "/jsp/user/index.jsp";
  
  public final static String HTTP_PATH_USER_SESSION = "/jsp/user/session";
  
  public final static String HTTP_PATH_USER_JSON = "/jsp/user/json";
  
  public final static String HTTP_PATH_USER_EXEC = "/jsp/user/exec";
  
  public final static String HTTP_PATH_USER_LLVIEW = "/jsp/user/llview";
  
  public final static String HTTP_PATH_USER_ORACLE ="/jsp/user/oracle";
  
  public final static String HTTP_PATH_USER_SCHEDULER = "/jsp/user/scheduler";
  
  public final static String HTTP_PATH_USER_SCHEDULER_PUBLIC = "/jsp/user/scheduler/public";
  
  public final static String HTTP_PATH_USER_SCRIPT = "/jsp/user/script";
  
  public final static String HTTP_PARAM_QUERY = "query";
  
  public final static String HTTP_PARAM_OPER = "oper";
  
  public final static String HTTP_PARAM_OPER_COUNT = "count";
  
  public final static String HTTP_PARAM_OPER_SELECT = "select";
  
  public final static String HTTP_PARAM_OPER_ADD = "add";
  
  public final static String HTTP_PARAM_OPER_EDIT = "edit";
  
  public final static String HTTP_PARAM_OPER_DEL = "del";
  
  public final static String HTTP_PARAM_SEARCH_FIELD = "fld";
  
  public final static String HTTP_PARAM_SEARCH_OPER = "foper";
  
  public final static String HTTP_PARAM_SEARCH_OPER_EQ = "eq";
  
  public final static String HTTP_PARAM_SEARCH_OPER_NE = "ne";
  
  public final static String HTTP_PARAM_SEARCH_OPER_LT = "lt";
  
  public final static String HTTP_PARAM_SEARCH_OPER_LE = "le";
  
  public final static String HTTP_PARAM_SEARCH_OPER_GT = "gt";
  
  public final static String HTTP_PARAM_SEARCH_OPER_GE = "ge";

  public final static String HTTP_PARAM_SEARCH_OPER_BW = "bw";
  
  public final static String HTTP_PARAM_SEARCH_OPER_BN = "bn";
  
  public final static String HTTP_PARAM_SEARCH_OPER_IN = "in";
  
  public final static String HTTP_PARAM_SEARCH_OPER_NI = "ni";
  
  public final static String HTTP_PARAM_SEARCH_OPER_EW = "ew";
  
  public final static String HTTP_PARAM_SEARCH_OPER_EN = "en";
  
  public final static String HTTP_PARAM_SEARCH_OPER_CN = "cn";
  
  public final static String HTTP_PARAM_SEARCH_OPER_NC = "nc";
  
  public final static String HTTP_PARAM_SEARCH_STRING = "fldata";
  
  public final static String HTTP_PARAM_ORDER_FIELD = "sidx";
  
  public final static String HTTP_PARAM_ORDER_SORD = "sord";
  
  public final static String HTTP_PARAM_ORDER_SORD_ASC = "ASC";
  
  public final static String HTTP_PARAM_ORDER_SORD_DESC = "DESC";
  
  public final static String HTTP_VALUE_TRUE = "1";
  
  public final static String HTTP_VALUE_FALSE = "0";
  
  public final static String HTTP_RESULT_OK = "1";
  
  /* HTTP */
  private DefaultHttpClient httpClient;
  private BasicHttpContext httpContext;
  
  private ConnectionDetails connectionDetails;
  
  //private static List<Map<String, Object>> schedulerList;
  
  private static Session INSTANCE;
  
//  private ProgressMonitorDialog progressDialog;
  
  public static Session getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Session();
    }
    return INSTANCE;
  }
  
  private Session() {

    httpClient = new DefaultHttpClient();
    wrapClient(httpClient);
    httpContext = new BasicHttpContext();
  
    //progressDialog = new ProgressMonitorDialog(null);
  }
  
  public static HttpClient wrapClient(HttpClient base) {
    try {
      SSLContext ctx = SSLContext.getInstance("TLS");
      X509TrustManager tm = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException { }
   
        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException { }
   
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
      };
      ctx.init(null, new TrustManager[]{tm}, null);
      SSLSocketFactory ssf = new SSLSocketFactory(ctx);
      ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      ClientConnectionManager ccm = base.getConnectionManager();
      SchemeRegistry sr = ccm.getSchemeRegistry();
      sr.register(new Scheme("https", ssf, 443));
      return new DefaultHttpClient(ccm, base.getParams());
    } catch (Exception ex) {
      return null;
    }
  }
  
  public ConnectionDetails getConnectionDetails() {
    return connectionDetails;
  }

  public void setConnectionDetails(ConnectionDetails connectionDetails) {
    this.connectionDetails = connectionDetails;
  }
  
//  public void connectAndLogin(final IProgressMonitor monitor) {
//    try {
//      monitor.beginTask("Connecting...", IProgressMonitor.UNKNOWN);
//      monitor.subTask("Contacting " + connectionDetails.getServer() + "...");
//    } finally {
////      if (connection != null)
////        connection.removePacketWriterListener(progressPacketListener);
//      monitor.done();
//    }
//  }
  
  public boolean createSession(String httpPath) {    
    if (!createHttpSession(httpPath)) return false;
    return true;
  }
  
  
  // 사용자 인증 부분
  public boolean createHttpSession(String path) {

    logger.debug("path = " + path);

    //this.getHttpResponseBodyAsString(HTTP_GET, HTTP_PATH_USER_INDEX, null);
    
    BasicCookieStore cookieStore = new BasicCookieStore();

    for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
      if (StringUtils.equals(cookie.getName(), "JSESSIONID")) {
        cookieStore.addCookie(cookie);
        logger.debug("JSESSIONID : {}", cookie.getValue());
        break;
      }
    }
        
    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
    Map<String, String> params = new HashMap<String, String>();
    params.put("type", "userSession");
//    params.put("div", "A");
    params.put("div", "W");
    params.put("id", connectionDetails.getUserId());
    params.put("password", connectionDetails.getPassword());
    params.put("otp", connectionDetails.getOtp());
    
    String out = this.getHttpResponseBodyAsString(HTTP_POST, path, params);
    logger.debug("out = " + out);

    if (StringUtils.equals(out, "1")) {
      return true;
    }

    return false;
  }
  
  
  private InputStream getHttpResponseBody(int method, String path, Map<String, String> params) {
    if (httpClient == null)
      return null;
    
    MultipartEntity multipartEntity = null;
      
    List<NameValuePair> qParams = new ArrayList<NameValuePair>();
    if (params != null) {
      Iterator<String> iter = params.keySet().iterator();
      while (iter.hasNext()) {
        String key = iter.next();
        String value = params.get(key);
        if (StringUtils.equals(key, "file")) {
          try {
            multipartEntity = new MultipartEntity();
            byte[] bytes = IOUtils.toByteArray(IOUtils.toInputStream(value, "UTF-8"));
            ByteArrayBody byteArrayBody = new ByteArrayBody(bytes, "file");
            multipartEntity.addPart("file", byteArrayBody);
          } catch (IOException e) {
            //CorePlugin.log(e);
          }
        } else {
          qParams.add(new BasicNameValuePair(key, value));  
        }
      }
    }

    try {      
      URI uri = null;
      if (StringUtils.startsWith(path, "http")) {
        try {
          uri = new URI(new StringBuilder().append(path).append("?").append(URLEncodedUtils.format(qParams, "UTF-8")).toString());
        } catch (URISyntaxException e) {
          //CorePlugin.log(e);
        }
      }
      
      if (!StringUtils.startsWith(path, "http") && connectionDetails != null) {
        try {
          uri = URIUtils.createURI(connectionDetails.getServerscheme(), connectionDetails.getServerHost(), connectionDetails.getServerPort(), path, URLEncodedUtils.format(qParams, "UTF-8"), null);
        } catch (URISyntaxException e) {
          //CorePlugin.log(e);
        }
      }
      
      if (uri == null)
        return null;
      
      HttpResponse httpResponse = null;
      switch (method) {
        case HTTP_GET:
          HttpGet httpGet = new HttpGet(uri);
          httpResponse = httpClient.execute(httpGet, httpContext);
          break;
        case HTTP_POST:
          HttpPost httpPost = new HttpPost(uri);
          if (multipartEntity != null) {
            httpPost.setEntity(multipartEntity);
          }
          httpResponse = httpClient.execute(httpPost, httpContext);
          break;
      }
      
      HttpEntity httpEntity = httpResponse.getEntity();
      InputStream input = new BufferedInputStream(IOUtils.toBufferedInputStream(httpResponse.getEntity().getContent()));
      EntityUtils.consume(httpEntity);
      return input;
    } catch (IOException e) {
//      //CorePlugin.log(e);
//      if (!MessageDialog.openQuestion(null, Messages.getString("Session.connectionErrorTitle"), Messages.getString("Session.connectionErrorMessage"))) {
//        System.exit(0);
//      }
    }
    return null;
    
  }
//  
  public InputStream getHttpResponseBodyAsStream(int method, String path, Map<String, String> params) {
    InputStream input = null;

    int i = 0;
    while (true) {
      input = getHttpResponseBody(method, path, params);
      
      if (input != null) {
        try {
          if (input.available() > 0)
            return input;
          else {
            i++;
            if (i > 5) {
              //MessageDialog.openError(null, Messages.getString("Session.connectionErrorTitle"), new StringBuilder().append(path).append(" : ").append(params.toString()).toString());
            }
          }
        } catch (IOException e) {
          //CorePlugin.log(e);
        }
        
        this.createHttpSession(HTTP_PATH_LOGIN);
        
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  public String getHttpResponseBodyAsString(int method, String path, Map<String, String> params) {
    logger.debug("+ getHttpResponseBodyAsString");
    try {
      InputStream input = getHttpResponseBodyAsStream(method, path, params);
      if (input == null) return null;

      String responseBody = IOUtils.toString(input, "UTF-8");
      //logger.debug("responseBody : "+responseBody);

      return responseBody;

    } catch (IOException e) {
      //CorePlugin.log(e);
    }
    return null;
  }
  
  public Long getHttpResponseBodyAsLong(int method, String path, Map<String, String> params) {
    return NumberUtils.toLong(getHttpResponseBodyAsString(method, path, params));
  }
  
  public Map getHttpResponseBodyAsJSONMap(int method, String path, Map<String, String> params) {
    String input = getHttpResponseBodyAsString(method, path, params);
    return JSONUtils.toMap(input);
  }
  
  public List getHttpResponseBodyAsJSONList(int method, String path, Map<String, String> params) {
    String input = getHttpResponseBodyAsString(method, path, params);
    return JSONUtils.toList(input);
  }

//
//  /**
//   * 스케줄러 리스트 
//   */
//  public List<Map<String, Object>> getSchedulerList() {
//    if (schedulerList == null) {
//      Map<String, String> params = new HashMap<String, String>();
//      params.put(Session.HTTP_PARAM_QUERY, "Code.scheduler");
//      params.put(Session.HTTP_PARAM_OPER, Session.HTTP_PARAM_OPER_SELECT);
//      params.put(Session.HTTP_PARAM_SEARCH_FIELD, "SCHEDULER_USE");
//      params.put(Session.HTTP_PARAM_SEARCH_OPER, Session.HTTP_PARAM_SEARCH_OPER_EQ);
//      params.put(Session.HTTP_PARAM_SEARCH_STRING, Session.HTTP_VALUE_TRUE);
//      params.put(Session.HTTP_PARAM_ORDER_FIELD, "SCHEDULER_SORT");
//      params.put(Session.HTTP_PARAM_ORDER_SORD, Session.HTTP_PARAM_ORDER_SORD_ASC);
//      schedulerList = getHttpResponseBodyAsJSONList(Session.HTTP_POST, Session.HTTP_PATH_USER_JSON, params);
//    }
//    return schedulerList;
//  }
//  
//  @Override
//  public Object getAdapter(Class adapter) {
//    return null;
//  }
}
