package org.kisti.htc.jobmanager.server;

/*
 * MatchValue class: maintains a single pair of match and value in the parameter sweep.
 * e.g., {TARGET, 2QMJ_new} in the autodock, {EXPNUM, 1} in the optimization.
 */
public class MatchValue {

  private String match;
  private String value;
  
  public MatchValue(String match, String value) {
    this.match = match;
    this.value = value;
  }
  
  public String getMatch() {
    return match;
  }
  
  public void setMatch(String match) {
    this.match = match;
  }
  
  public String getValue() {
    return value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  @Override
  public String toString() {
    return "[" + match + "," + value + "]";
  }
  
}
