package org.kisti.htc.dbmanager.dao;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger; // logger


public final class DAOUtil {
  
  private static final Logger logger = LoggerFactory.getLogger(DAOUtil.class);
  //private static final mLogger logger = new mLogger();
  
  private static String driver;
  private static String url;
  private static String user;
  private static String password;
  private static String plsi_driver;
  private static String plsi_url;
  private static String plsi_user;
  private static String plsi_password;
  private static int initialSize;
  private static int maxSize;
  static BasicDataSource htcaas_ds;
  static BasicDataSource plsi_ds;
  static boolean usePLSI = true;
  
  static {
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));

      driver = prop.getProperty("htcaas_db.driver");
      logger.info("Database Driver: {}", driver);
      
      url = prop.getProperty("htcaas_db.url");
      logger.info("Database URL: {}", url);
      
      user = prop.getProperty("htcaas_db.user");
      password = prop.getProperty("htcaas_db.password");
      
      if(usePLSI){
        plsi_driver = prop.getProperty("plsi_db.driver");
        logger.info("PLSI Database Driver: {}", plsi_driver);
        
        plsi_url = prop.getProperty("plsi_db.url");
        logger.info("PLSI Database URL: {}", plsi_url);
        
        plsi_user = prop.getProperty("plsi_db.user");
        plsi_password = prop.getProperty("plsi_db.password");
      }
            
      initialSize = Integer.parseInt(prop.getProperty("dbpool.initialSize"));
      logger.info("Database Pool initial size: {}", initialSize);
      
      maxSize = Integer.parseInt(prop.getProperty("dbpool.maxSize"));
      logger.info("Database Pool max size: {}", maxSize);
      
    } catch (Exception e) {
      logger.error("Failed to load config file: " + e.getMessage());
      System.exit(1);
    }
  }

  public DAOUtil() {
    //logger.set_prefix("[" + this.getClass().getSimpleName() +"] "); // logger message prefix
  }

  
  public static void main(String arg[]){
    Connection conn = DAOUtil.getConnection();
    String sql = "select * from Agent where id=1";
    ResultSet rs = null;
    PreparedStatement stmtSelect = null;
    try {
      stmtSelect = conn.prepareStatement(sql);
      rs = stmtSelect.executeQuery();
      while(rs.next()){
        System.out.println(rs.getString(4));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }finally{
      DAOUtil.closeStatement(stmtSelect);
      DAOUtil.closeResultSet(rs);
      DAOUtil.closeJDBCConnection(conn);
    }
  }
  
  
  public static DataSource setupDataSource() {
    logger.debug("setupDataSource");
        htcaas_ds = new BasicDataSource();
        htcaas_ds.setDriverClassName(driver);
        htcaas_ds.setUsername(user);
        htcaas_ds.setPassword(password);
        htcaas_ds.setUrl(url);
        htcaas_ds.setInitialSize(initialSize);
        htcaas_ds.setMaxActive(maxSize);
        htcaas_ds.setDefaultAutoCommit(false);
//        htcaas_ds.setMaxWait(60000);
        htcaas_ds.setValidationQuery("select 1");
        htcaas_ds.setMaxIdle(-1);
//        htcaas_ds.setTestOnBorrow(true);
        htcaas_ds.setTestWhileIdle(true);
        
        return htcaas_ds;
    }
  
  public static DataSource setupPLSIDataSource() {
    logger.debug("setupPLSIDataSource");
        plsi_ds = new BasicDataSource();
        plsi_ds.setDriverClassName(plsi_driver);
        plsi_ds.setUsername(plsi_user);
        plsi_ds.setPassword(plsi_password);
        plsi_ds.setUrl(plsi_url);
        plsi_ds.setInitialSize(initialSize);
        plsi_ds.setMaxActive(maxSize);
//        plsi_ds.setDefaultAutoCommit(false);
        return plsi_ds;
    }

  public static void printDataSourceStats(DataSource ds) {
    logger.debug("printDataSourceStats");
        BasicDataSource bds = (BasicDataSource) ds;
        System.out.println("NumActive: " + bds.getNumActive());
        System.out.println("NumIdle: " + bds.getNumIdle());
    }
  
    public static void shutdownDataSource() throws SQLException {
      logger.debug("shutdownDataSource");
        if(htcaas_ds !=null){
          htcaas_ds.close();
        }
    }
    
    public static void shutdownPLSIDataSource() throws SQLException {
      logger.debug("shutdownPLSIDataSource");
        if(plsi_ds !=null){
          plsi_ds.close();
        }
    }
  
  public static Connection getConnection() {
    //logger.debug("getConnection");
    Connection conn = null;
     try {
      if(htcaas_ds != null){
        conn = htcaas_ds.getConnection();
      } else {
      // synchronized (ds) {
        conn = setupDataSource().getConnection(); ///unsafe thread
      // }
      }
    } catch (SQLException e) {
      logger.error("getConnection Error: {}, {}", conn, e);
    }
    return conn;
  }
  
  public static Connection getPLSIConnection() {
    logger.debug("getPLSIConnection");
    Connection conn = null;
     try {
      if(plsi_ds != null){
        conn = plsi_ds.getConnection();
      }else{
        conn = setupPLSIDataSource().getConnection(); ///unsafe thread
      }
    } catch (SQLException e) {
      logger.error("getPLSIConnection Error: {}, {}", conn, e);
    }
    return conn;
  }
  
  public static void doCommit(Connection conn) {
    //logger.debug("doCommit");
    try {
      conn.commit();
    } catch (SQLException e) {
      logger.error("doCommit Error: {}, {}", conn, e);
    } finally {
      closeJDBCConnection(conn);
    }
  }
  
  public static void onlyCommit(Connection conn) {
    //logger.debug("onlyCommit");
    try {
      conn.commit();
    } catch (SQLException e) {
      logger.error("onlyCommit Error: {}, {}", conn, e);
    }
  }
  
  public static void doRollback(Connection conn) {
    //logger.debug("doRollback");
    try {
      conn.rollback();
    } catch (SQLException e) {
      logger.error("doRollback Error: {}, {}",conn, e);
    } finally {
      closeJDBCConnection(conn);
    }
  }
  
  public static void closeJDBCConnection(final Connection conn) {
    //logger.debug("closeConnection");  
    if (conn != null) {
       try {
         conn.close();
       } catch (SQLException ex) {
         logger.error("close Connection Error: {}, {}",conn, ex);
       }
    }
  }

  public static void closeStatement(final Statement stmt) {
    //logger.debug("closeStatement");
    if (stmt != null) {
       try {
         stmt.close();
       } catch (SQLException ex) {
         logger.error("closeStatement Error: {}, {}",stmt, ex);
       }
    }
  }

  public static void closeResultSet(final ResultSet rs) {
    //logger.debug("closeResultSet");
    if (rs != null) {
       try {
         rs.close();
       } catch (SQLException ex) {
         logger.error("closeResultSet Error: {}, {}",rs, ex);
       }
    }
  }

}
