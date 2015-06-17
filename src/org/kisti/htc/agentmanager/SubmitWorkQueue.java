
package org.kisti.htc.agentmanager;

import org.apache.log4j.Logger;

import util.mLogger;
import util.mLoggerFactory;

// 작업 제출을 위한 큐
public class SubmitWorkQueue extends WorkQueue {

  //  final static mLogger logger = mLoggerFactory.getLogger("AM");
  private Logger logger = Logger.getLogger(this.getClass());
  private AgentManager am;
    
  public SubmitWorkQueue(AgentManager am, String queueName, int nThreads) {
    super(queueName, nThreads);
    this.am = am;
    startWorkers();
  }  
  
  // object 에 대해 처리함
  @Override
  public void doWork(String workerName, Object object) {
    logger.info("| " + queueName + " Thread_" + workerName + " Submitting a new Agent");
    
    // g-Lite
    if (object instanceof GliteJob) {
      GliteJob job = (GliteJob) object;
      
      if (job.submit()) {
        logger.info("| Submission success");
      }
      else {
        logger.error("| Submission failure");
      }

    // Local Job
    } else if (object instanceof LocalJob) {
      LocalJob job = (LocalJob) object;
      
      if (job.submit()) {
        logger.info("| Submission success");
      }
      else {
        logger.error("| Submission failure");
      }

    // KSC
    } else if (object instanceof LLJob) {
      LLJob job = (LLJob) object;
      
      // 작업 제출
      if (job.submit()) {
        logger.info("| Submission success");
      }
      else {
        logger.error("| Submission failure");
      }

    // cloud
    } else if (object instanceof CloudJob) {
      CloudJob job = (CloudJob) object;
      
      if (job.submit()) {
        logger.info("| Submission success");
      }
      else {
        logger.error("| Submission failure");
      }
    } else if (object instanceof ClusterJob) {
        ClusterJob job = (ClusterJob) object;
        
        if (job.submit()) {
          logger.info("| Submission success");
        }
        else {
          logger.error("| Submission failure");
        }
    } else if (object instanceof CondorJob) {
      CondorJob job = (CondorJob) object;
      
      if (job.submit()) {
        logger.info("| Submission success");
      }
      else {
        logger.error("| Submission failure");
      }
    } else {
      logger.error("| Unknown Job Type - add Job Type in SubmitWorkQueue");
    }
  }
  
  public static void main(String[] args) throws Exception {
  
    AgentManager am = AgentManager.getInstance();
    SubmitWorkQueue queue = new SubmitWorkQueue(am, "SubmitWorkQueue", 1);
    queue.addJob(new Integer(1));
  }
}

