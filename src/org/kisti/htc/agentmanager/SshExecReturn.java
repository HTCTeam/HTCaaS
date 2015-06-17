package org.kisti.htc.agentmanager;

public class SshExecReturn {
  
  private int exitValue;
  private String stdOutput;
  private String stdError;
  
  public SshExecReturn() {  
  }
  
  public int getExitValue() {
    return exitValue;
  }
  public void setExitValue(int exitValue) {
    this.exitValue = exitValue;
  }
  public String getStdOutput() {
    return stdOutput;
  }
  public void setStdOutput(String stdOutput) {
    this.stdOutput = stdOutput;
  }
  public String getStdError() {
    return stdError;
  }
  public void setStdError(String stdError) {
    this.stdError = stdError;
  }

  public String toString() {
    String str = "";
    str += String.format("exit value : %d\n", this.exitValue);
    str += String.format("std output : %s\n", this.stdOutput);
    str += String.format("std error : %s\n",  this.stdError);
    return str;
  }
}
