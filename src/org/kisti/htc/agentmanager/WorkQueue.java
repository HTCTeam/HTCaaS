package org.kisti.htc.agentmanager;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import util.mLogger;
import util.mLoggerFactory;

// WorkQueue 작업큐를 위한 기본 클래스
// --> CheckWorkQueue : PLSI 작업 (file base)
// --> MatchWorkQueue : gLite 작업
// --> SubmitWorkQueue
public abstract class WorkQueue {

  protected Logger logger = Logger.getLogger(this.getClass());
//  final static mLogger logger = mLoggerFactory.getLogger("AM");

  protected final String queueName;

  private final ArrayList<Object> queue;

  // WorkerThread 는 내부 클래스
  private final WorkerThread[] workerThreads;

  private int runningThreads = 0;

  protected int sleepPeriod = 0;

  // 큐에서 꺼낸 작업의 개수 (아직 처리되지 않은것)
  private int takenJobs = 0;

  // 종료를 위한 플래그
  private boolean end = false;

  // 큐를 중지하기 위한 플래그
  // private boolean paused = false;

  // constructor
  // 큐이름을 지정, 주어진 개수만큼 쓰레드를 생성
  public WorkQueue(String queueName, int nThreads) {

    // name of the queue
    this.queueName = queueName;

    // the queue
    queue = new ArrayList<Object>();

    // 주어진 개수만큼 쓰레드를 생성
    workerThreads = new WorkerThread[nThreads];
    logger.info("| " + nThreads + " threads are ready in " + queueName);
  }

  // 워커 쓰레드를 시작한다.
  protected void startWorkers() {
    for (int i = 0; i < workerThreads.length; i++) {
      workerThreads[i] = new WorkerThread("" + i);
      workerThreads[i].setDaemon(true);
      workerThreads[i].start();
      runningThreads++;
    }
  }

  // 현재 정보를 리턴 (큐의 크기, taken job 개수, running job 개수)
  public String currentInfo() {
    return "Queue:" + queue.size() + " Taken:" + takenJobs + " Running:" + runningThreads;
  }

  public int nbRunningThreads() {
    return runningThreads;
  }

  public int nbTakenJobs() {
    return takenJobs;
  }

  public int size() {
    return queue.size();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public boolean finished() {
    return queue.isEmpty() && (runningThreads == 0);
  }

  public boolean doNothing() {
    return queue.isEmpty() && (takenJobs == 0);
  }

/* 사용되지 않는듯
  public void setSleep(int sleepPeriod) {
    this.sleepPeriod = sleepPeriod;
  }
*/

  // 종료를 위한 플래그를 설정
  public void end() {
    end = true;
    synchronized (queue) {
      queue.notify();
    }
  }

/* 사용되지 않는듯
  public void pause() {
    paused = true;
    synchronized (queue) {
      queue.notify();
    }
  }

  public void resume() {
    paused = false;
    synchronized (queue) {
      queue.notify();
    }
  }
*/

  // 큐에 작업을 추가한다.
  public void addJob(Object job) {
    synchronized (queue) {
      queue.add(job);
      queue.notify();
    }
  }

  public void removeJob(Object job) {
    synchronized (queue) {
      queue.remove(job);
      queue.notify();
    }
  }
  
  public void removeJobAll() {
	synchronized (queue) {
	  queue.clear();
	  queue.notify();
	}
  }

  

  // 상속받는 클래스에서 정의될 것임
  public abstract void doWork(String workerName, Object job);


  /// class WorkerThread {{{
  private class WorkerThread extends Thread {

    private Object job;
    private String name;

    // constructor
    public WorkerThread(String name) {
      // 쓰레드의 이름
      this.name = name;
    }

    @Override
    public void run() {
      while (!end) {
        job = null;
        boolean get = false;

        synchronized (queue) {

       /* 사용되지 않는듯
          // paused 가 true 인 동안은 queue.wait() 를 한다.
          while (paused) {
            // log.info(queueName + " Thread_" + name + " Paused");
            try {
              queue.wait();
            } catch (InterruptedException ignored) {
            }
            if (!paused) {
            // log.info(queueName + " Thread_" + name + " Resumed");
            }
          }
       */

          // 큐가 비어있지 않음
          if (!queue.isEmpty()) {
            // log.debug(queueName + " Thread_" + name + " Taking a Job Out of Queue");
            job = queue.remove(0);
            get = true;
            takenJobs++;

          } else { // 큐가 비어 있으면 기다린다.
            if (end) { // 쓰레드를 종료한다.
              break;
            }

            // log.debug(queueName + " Thread_" + name + " Sleeping");
            try {
              queue.wait();
            } catch (InterruptedException ignored) {
            }
            // log.debug(queueName + " Thread_" + name + " Waking up");
          }
        } // synchronized

        if (get) { // 큐에서 꺼낸 것이 있을 경우
          doWork(name, job); // job 을 처리한다.
       //   logger.info(queueName +"job: " + job+ "job type? //JE.");
          takenJobs--;
        }

        synchronized (queue) {
          queue.notify();
        }
      }

      logger.info(queueName + " Thread_" + name + " End");
      runningThreads--;
    }

  }
  /// class WorkerThread }}}

}

