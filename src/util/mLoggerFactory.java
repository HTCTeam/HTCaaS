
package util;

public final class mLoggerFactory {

  private static mLogger _lg_db = new mLogger("[DB] ", "log/db.log");
  private static mLogger _lg_am = new mLogger("[AM] ", "log/am.log");
  private static mLogger _lg_jm = new mLogger("[JM] ", "log/jm.log");
  private static mLogger _lg_mn = new mLogger("[MN] ", "log/mn.log");
  private static mLogger _lg_ud = new mLogger("[UD] ", "log/ud.log");
  private static mLogger _lg_ac = new mLogger("[AC] ", "log/ac.log");

  public static mLogger getLogger(String name) {
    if (name == null) {
      System.err.println("logger name is null");
      System.exit(1);
      //throw new IllegalArgumentException("name is null");
    }

    //System.out.println("logger name : " + name);
    
         if (name.equals("DB")) return _lg_db;
    else if (name.equals("AM")) return _lg_am;
    else if (name.equals("JM")) return _lg_jm;
    else if (name.equals("MN")) return _lg_mn;
    else if (name.equals("UD")) return _lg_ud;
    else if (name.equals("AC")) return _lg_ac;
    else {
      System.err.println("unknown logger name : " + name);
      System.exit(1);
      return null;
    }

  }

}
