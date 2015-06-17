package org.kisti.htc.jobmanager.server;

import java.util.ArrayList;
import java.util.List;

/*
 * AssignmentInfoList class:
 * It maintains a list of AssignmentInfo which corresponds to an assignment in the JSDL script, e.g., Values, Directory, LoopInteger, etc.
 * An assignment element associates one Function substituent with one or more Parameter substituents.
 * An AssignmentInfoList basically represents the list of assignments in "one" Sweep.  
 * The Sweep Element in the JSDL: describes how to modify an existing JSDL Job Template in order to get a Parameter Sweep JSDL Job Template
    - The Sweep element defines the coordination of Assignment element evaluation.
  - The Sweep element MUST contain at least one Assignment element. It MAY contain nested Sweep elements.
  - Sibling Assignment child elements MUST have the same cardinality. Otherwise the containing JSDL Job Template MUST be rejected.
  - All contained Assignment elements MUST be evaluated in parallel: all the nth value assignments of each contained Assignment element 
    form a “Value Assignment Set”.
 */
public class AssignmentInfoList {

  private List<AssignmentInfo> list;
  
  public AssignmentInfoList() {
    list = new ArrayList<AssignmentInfo>();
  }
  
  // Adding a new AssignmentInfo (assignment in the Sweep)
  public void addInfo(AssignmentInfo info) {
    list.add(info);
  }
  
  /*
   * Get the next i-th value assignments of each contained Assignment element to form a "Value Assignment Set"
   * ai.next() returns pairs of (match, value) in the assignment with the "next" value 
   */
  public List<MatchValue> next() {
    List<MatchValue> pairs = new ArrayList<MatchValue>();
    
    for (AssignmentInfo ai : list) {
      List<MatchValue> subPairs = ai.next();
      /*
       * Sibling Assignment child elements MUST have the same cardinality:
       * If one assignment of the list is empty, this should guarantee that all available value assignment set are applied.
       * Otherwise, sibling assignments in a Sweep are having different cardinalities which means we should reject the JSDL.
       * TODO: check the cardinalities of sibling assignments in this function or other places. 
       */
      if (subPairs == null) {
        // No more data
        return null;
      }
      pairs.addAll(subPairs);
    }
    
    return pairs;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (AssignmentInfo ai : list) {
      sb.append(ai.toString() + "\n");
    }
    return sb.toString();
  }
}
