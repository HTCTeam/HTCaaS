package org.kisti.htc.acmanager.server;


import java.util.Map;

import org.apache.commons.lang.StringUtils;

import util.DebugMessage; // 디버그 메시지 출력 _debug, _warn, _error

/**
 * 연결 정보를 담고있는 클래스
 * @author kocjun
 *
 */
public class ConnectionDetails extends DebugMessage {
  
  private String server, serverscheme, serverHost;
  
  private int serverPort = -1;
  
  private String userId, password, otp;
  
  private Map<String, Object> httpSessionMap;

  /**
   * ConnectionDetails 생성자
   * 
   * @param server 서버 정보
   * @param userId 사용자 ID
   * @param password 사용자 비밀번호
   */
  public ConnectionDetails(String server, String userId, String password, String otp) {

    set_logger_prefix("[ConnectionDetails.java] "); // debug message prefix

    this.server = server;
    this.userId = userId;
    this.password = password;
    this.otp = otp;
    

    _debug("server address = " + server);

    String[] schemeSplit = StringUtils.splitByWholeSeparator(server, "://");
    this.serverscheme = schemeSplit[0];
    String[] hostSplit = StringUtils.splitByWholeSeparator(schemeSplit[1], ":");
    this.serverHost = hostSplit[0];
    if (hostSplit.length > 1) this.serverPort = Integer.parseInt(hostSplit[1]);
  }
  
  /**
   *  서버 정보 반환
   *  
   * @return 서버 정보
   */
  public String getServer() {
    return server;
  }

  /**
   * 서버 스키마 반환
   * 
   * @return 서버 스키마
   */
  public String getServerscheme() {
    return serverscheme;
  }
  
  /**
   * 서버 호스트 정보 반환
   * 
   * @return 서버 호스트 정보
   */
  public String getServerHost() {
    return serverHost;
  }
  
  /**
   * 서버 포트 정보 반환
   * 
   * @return 서버 포트 정보
   */
  public int getServerPort() {
    return serverPort;
  }
  
  /**
   * 사용자 ID 반환
   * 
   * @return 사용자 ID 
   */
  public String getUserId() {
    return userId;
  }
  
  /**
   * 사용자 비밀번호 반환
   * 
   * @return 사용자 비밀번호
   */
  public String getPassword() {
    return password;
  }
  
  public String getOtp() {
	  return otp;
  }

  /**
   * 현재 시간 반환
   * 
   * @return 현재 시간 
   */
  public String getResource() {
    return String.valueOf(System.currentTimeMillis());
  }
  
  /**
   * HttpSessionMap 설정
   *  
   * @param httpSessionMap 세션맵
   */
  public void setHttpSessionMap(Map<String, Object> httpSessionMap) {
    this.httpSessionMap = httpSessionMap;
  }
  /**
   * HttpSessionMap 반환
   * 
   * @return 세션맵
   */
  public Map<String, Object> getHttpSessionMap() {
    return httpSessionMap;
  }
}

