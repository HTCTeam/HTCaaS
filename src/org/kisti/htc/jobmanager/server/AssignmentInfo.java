package org.kisti.htc.jobmanager.server;

import java.util.ArrayList;
import java.util.List;

/*
 * AssignmentInfo class: maintains an assignment (matches and values) in the Sweep.
 * Example sweep assignment (optimization JSDL):
    <ns3:Assignment>
      <ns3:DocumentNode>
          <ns3:Match>EXPNUM</ns3:Match>
        </ns3:DocumentNode>
        <ns3:LoopInteger step="1" end="5" start="1"/>
    </ns3:Assignment>
 */
public class AssignmentInfo {

  public List<String> matches;
  public String valueType;
  public List<String> values;
  private int index = 0;
  
  public AssignmentInfo() {
    matches = new ArrayList<String>();
    values = new ArrayList<String>();
  }
  
  public void addMatch(String match) {
    matches.add(match);
  }
  
  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
  
  public void addValue(String value) {
    values.add(value);
  }
  
  /*
   * Get the next value assignment set:
   * e.g., {EXPNUM, 1}, {EXPNUM, 2}, {EXPNUM, 3}, {EXPNUM, 4}, {EXPNUM, 5} in the optimization JSDL script.
   */
  public List<MatchValue> next() {
    if (index + 1 > values.size()) {
      return null;
    }    
    // Get the "next" value
    String value = values.get(index);
    
    // For the "next" value, construct a set of value assignments with (multiple) matches
    List<MatchValue> pairs = new ArrayList<MatchValue>();
    for (String match : matches) {
      pairs.add(new MatchValue(match, value));
    }
    
    index++;
    return pairs;
  }
  
  @Override
  public String toString() {
    return "Matches:" + matches + ", Values:" + values;
  }
  
  public static void main(String[] args) throws Exception {
    AssignmentInfo ai = new AssignmentInfo();
  }
}
