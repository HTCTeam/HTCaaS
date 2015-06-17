/* 
 * 
 */
package org.kisti.htc.jobmanager.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kisti.htc.dbmanager.beans.MetaJob;


/**
 * The Interface JobManager.
 * @author seungwoo
 */
public interface JobManager {
  
  /**
   * Submit meta job.
   *
   * @param userId the user id
   * @param metaJobDocument the meta job document(JSDL)
   * @return result Code and Comment. Integer 0-fail, 1-success and String is comment.
   */
  public Map<Integer, String> submitMetaJob(String userId, String metaJobDocument, int aMaxJobTimeMin, String projectName, String scriptName);
  
  public Map<Integer, String> submitMetaJob(String userId, String metaJobDocument, int aMaxJobTimeMin, String projectName, String scriptName, String ceNames);
  
  /**
   * Cancel meta job.
   *
   * @param userId the user id
   * @param metaJobId the meta job id
   * @return the number of canceled sub-jobs.
   */
  public int cancelMetaJob(String userId, int metaJobId);
  
  /**
   * Resubmit sub job.
   *
   * @param userId the user id
   * @param metaJobId the meta job id
   * @return the int
   */
  public int resubmitSubJobByStatus(String userId, int metaJobId, String status);
  
  public int resubmitSubJobSet(String userId, int metaJobId, Set<Integer> subJobSet);
  
  public boolean removeMetaJob(String userId, int metaJobId);
  
  /**
   * Gets the meta job progress.
   *
   * @param userId the user id
   * @param metaJobId the meta job id
   * @return the meta job progress. String is the status of job and Integer is the number of the status.
   */
  public Map<String, Integer> getMetaJobProgress(String userId, int metaJobId);
  
  /**
   * Gets the all meta job objects of the user.
   *
   * @param userId the user id
   * @return all the meta job objects
   */
  public List<MetaJob> getMetaJobObjectList(String userId);
  
  /**
   * Gets the meta job result.
   *
   * @param userId the user id
   * @param metaJobId the meta job id
   * @return the meta job result list. It is the server location of meta-job result.
   */
  public List<String> getMetaJobResult(String userId, int metaJobId);
  
  
  public List<String> getMetaJobResultAutodock(String userId, int metaJobId, int energyLvLow, int energyLvHigh);
  
  /**
   * Gets the job result.
   *
   * @param userId the user id
   * @param jobId the job id
   * @return the job result. It is the server location of sub-job result.
   */
  public List<String> getJobResult(String userId, int jobId);
  
  public List<String> getJobIdListResult(String userId, List<Integer> jobIdList);
}
