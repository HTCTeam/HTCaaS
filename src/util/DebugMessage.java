
package util;

/*
Usage:

import util.DebugMessage; // output debug message _debug, _warn, _error

public class MyClass extends DebugMessage {

  public MyClass() {
    set_logger_prefix("[MyClass] ");  // debug message prefix
    ...
  }
}
*/

public class DebugMessage {

  public String debug_prefix = "[debug] ";

  public DebugMessage() {}

  // 메시지 앞에 출력할 prefix 를 설정
  public void set_logger_prefix(String prefix) {
    debug_prefix = prefix;
  }

  // 디버깅메시지 출력을 위한 메서드
  public void _debug(Object x) {
    StdOut.print(debug_prefix);
    StdOut.println(x);
  }
  public void _debug(String format, Object ... args) {
    StdOut.print(debug_prefix);
    StdOut.printf(format, args);
    StdOut.println();
  }

  // warn
  public void _warn(Object x) {
    StdOut.print(debug_prefix);
    StdOut.println(x);
  }


  // error
  public void _error(Object x) {
    StdOut.print(debug_prefix);
    StdOut.println(x);
  }
  public void _error(Object ... args) {
    for (int i = 0; i < args.length; i++) {
      StdOut.println(args[i]);
    }
  }

  // 사용자 메시지 출력 (print msg)
  public void pmsg(String msg) {
    StdOut.println(msg);
  }

}
