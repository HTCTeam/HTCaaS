package org.kisti.htc.cli.client;

//import kr.re.kisti.htcaascli.main.HTCaasCLI;

/**
 * ProgressBar.java
 * 
 * <pre>
 * Progress Bar를 구현하기 위한 Class
 * Copyright(C) 2012 ChironSoft Co,. Ltd.
 * </pre>
 * 
 * @author YeonSuk Lee
 * @date 2012. 8. 7.
 */
public class ProgressBar extends Thread {

  /**
   * Progress Bar 구현
   * 
   * @author YeonSuk Lee
   * @param percent
   *          넘어온 percent 값
   * @param fileName
   *          파일의 이름
   */
  public void printProgBar(int percent, String fileName, String rate) {

    StringBuilder bar = new StringBuilder("[");

    for (int i = 0; i < 50; i++) {
      if (i < (percent / 2)) {
        bar.append("=");
      } else if (i == (percent / 2)) {
        bar.append(">");
      } else {
        bar.append(" ");
      }
    }

    bar.append("] " + percent + "% " + rate + " ");
    if (percent != 100) {
      if (System.getProperty("os.name").split(" ")[0].equals("Windows")) {
        bar.append((fileName.length() < 20 - (rate.length() + 1) ? fileName
            : fileName.substring(0, 18 - (rate.length() + 1)) + "..."));
      } else {
        bar.append(fileName);
      }
    }else {
      if (System.getProperty("os.name").split(" ")[0].equals("Linux")) {
        bar.append("                ");
      } else {
        for(int j=0; j< fileName.length(); j++){
          bar.append(" ");
        }
      }
    }
    System.out.print("\r" + bar.toString());
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
