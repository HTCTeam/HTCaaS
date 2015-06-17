
package org.kisti.htc.dbmanager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.mLogger; 
import util.mLoggerFactory; 

public class DAOBase {

  final static mLogger logger = mLoggerFactory.getLogger("DB");

  public DAOBase() {
    //logger.set_prefix("[" + this.getClass().getSimpleName() +"] "); // logger message prefix
  }

  // db query abstration
  public ResultSet _query(PreparedStatement stmt) {

    // 아래 방법은 ORACLE jdbc 드라이버만 해당됨
    //try {
    //  Class stmt1 = stmt.getClass();  
    //  java.lang.reflect.Field mem = stmt1.getField("sql");  
    //  String value = (String)mem.get(stmt); 
    //  _debug("value =============== " + value);
    //} catch (Exception e) {
    // e.printStackTrace(); 
    //}

    //System.out.println("---------------- a");
    String statementText = stmt.toString();
    //System.out.println("---------------- b");
    try {
      String query = statementText.substring( statementText.indexOf( ": " ) + 2 );
      //System.out.println("---------------- c");
      logger.info(query);
      //System.out.println("---------------- d");
    } catch (Exception e) { }

    ResultSet rs = null;
    try {

      rs = stmt.executeQuery();

    } catch (Exception e) {
    }
    return rs;
  }

  // db query abstration
  public int _update(PreparedStatement stmt) {

    String statementText = stmt.toString();
    String query = statementText.substring( statementText.indexOf( ": " ) + 2 );

    int rows = 0;
    try {
      rows = stmt.executeUpdate();
    } catch (Exception e) {
    }
    return rows;
  }


}
