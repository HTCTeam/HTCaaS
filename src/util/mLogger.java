
package util;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.OutputStreamWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Locale;


public final class mLogger {

  public String prefix = "[log] ";

  //private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  // assume language = English, country = US for consistency with StdIn
  private static final Locale US_LOCALE = new Locale("en", "US");

  // send output here
  private static PrintWriter out = null;

  private static PrintWriter fout = null;
  private static File fileLog = null;
  private static String logpath = "";


  public mLogger (String prefix, String path) {

    this.prefix = prefix;

    // import java.io.PrintWriter;
    try {
      out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);
    } catch (UnsupportedEncodingException e) { System.out.println(e); }
    //System.out.println("logger initialized....... out : " + out);

    boolean enable_filelogger = false;
    if (enable_filelogger) {
      logpath = path;
      try {
        //System.out.println("logger initialized....... logpath : " + logpath);

        File fileLog = new File(logpath);
        //System.out.println("logger initialized....... fileLog : " + fileLog );

        //if (fileLog.exists()) {
        //  fileLog.delete();
        //  fileLog.createNewFile();
        //}

        fout = new PrintWriter(fileLog);
        System.out.println("logger initialized....... fout : " + fout);
      } catch (Exception e) { System.out.println(e); }
    }

  }

  // set message prefix
  public void set_prefix(String prefix) {
    prefix = prefix;
  }

  /**
   * Print a formatted string to standard output using the specified
   * format string and arguments, and flush standard output.
   */
  public static void printf(String format, Object... args) {
    out.printf(US_LOCALE, format, args);
    out.flush();
  }

  public String _time() {
    Calendar cal = Calendar.getInstance( );
        
    String s = String.format("[%04d-%02d-%02d %02d:%02d:%02d] ",
                // 오늘 날짜를 구한다.
                cal.get(Calendar.YEAR),
                (cal.get(Calendar.MONTH) + 1),
                cal.get(Calendar.DAY_OF_MONTH),
                // 현재 시간을 구한다.
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    return s;
  }

  public void __log(Object x) {
    out.print(_time());
    out.print(prefix);
    out.println(x);
    out.flush();

    if (fout != null) {
      fout.print(_time());
      fout.print(prefix);
      fout.println(x);
      fout.flush();
    }
  }

  public void __log2(String format, Object ... args) {
    out.print(_time());
    out.print(prefix);
    out.printf(US_LOCALE, format, args);
    out.println();
    out.flush();

    if (fout != null) {
      fout.print(_time());
      fout.print(prefix);
      fout.printf(US_LOCALE, format, args);
      fout.println();
      fout.flush();
    }
  }

  // info
  public void info(Object x) {
    __log(x);
  }
  public void info(String format, Object ... args) {
    __log2(format, args);
  }


  // debug
  public void debug(Object x) {
    __log(x);
  }
  public void debug(String format, Object ... args) {
    __log2(format, args);
  }

  // warn
  public void warn(Object x) {
    __log(x);
  }
  public void warn(String format, Object ... args) {
    __log2(format, args);
  }


  // error
  public void error(Object x) {
    __log(x);
  }
  public void error(Object ... args) {
    for (int i = 0; i < args.length; i++) {
      __log(args[i]);
    }
  }

  // fatal
  public void fatal(Object x) {
    __log(x);
  }
  public void fatal(String format, Object ... args) {
    __log2(format, args);
  }


}
