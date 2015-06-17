package org.kisti.htc.dbmanager.beans;

import org.kisti.htc.constant.AgentConstant;
import org.kisti.htc.constant.JobConstant;
import org.kisti.htc.constant.MetaJobConstant;

public class Constant {
	
	public static final String AGENT_STATUS_NEW = AgentConstant.AGENT_STATUS_NEW;
	public static final String AGENT_STATUS_PUSH = AgentConstant.AGENT_STATUS_PUSH;
	public static final String AGENT_STATUS_SUB = AgentConstant.AGENT_STATUS_SUBMIT;
	public static final String AGENT_STATUS_RUN = AgentConstant.AGENT_STATUS_RUN;
	public static final String AGENT_STATUS_DONE = AgentConstant.AGENT_STATUS_DONE;
	public static final String AGENT_STATUS_FAIL = AgentConstant.AGENT_STATUS_FAIL;
	public static final String AGENT_STATUS_CANCEL = AgentConstant.AGENT_STATUS_CANCEL;
	public static final String AGENT_STATUS_NEWZOM = AgentConstant.AGENT_STATUS_NEWZOMBIE;
	public static final String AGENT_STATUS_SUBZOM = AgentConstant.AGENT_STATUS_SUBMITZOMBIE;
	public static final String AGENT_STATUS_RUNZOM = AgentConstant.AGENT_STATUS_RUNZOMBIE;
	public static final String AGENT_STATUS_SUBERR = AgentConstant.AGENT_STATUS_SUBERR;
	public static final String AGENT_QUIT = AgentConstant.AGENT_QUIT;
	
	public static final String JOB_STATUS_WAIT = JobConstant.JOB_STATUS_WAIT;
	public static final String JOB_STATUS_PRE = JobConstant.JOB_STATUS_PRE;
	public static final String JOB_STATUS_RUN = JobConstant.JOB_STATUS_RUN;
	public static final String JOB_STATUS_DONE = JobConstant.JOB_STATUS_DONE;
	public static final String JOB_STATUS_FAIL = JobConstant.JOB_STATUS_FAIL;
	public static final String JOB_STATUS_CANCEL = JobConstant.JOB_STATUS_CANCEL;
	public static final String JOB_STOP = JobConstant.JOB_STOP;
	
	public static final String METAJOB_STATUS_WAIT = MetaJobConstant.METAJOB_STATUS_WAIT;
	public static final String METAJOB_STATUS_SPLITTING = MetaJobConstant.METAJOB_STATUS_SPLITTING;
	public static final String METAJOB_STATUS_SPLIT = MetaJobConstant.METAJOB_STATUS_SPLIT;
	public static final String METAJOB_STATUS_DONE = MetaJobConstant.METAJOB_STATUS_DONE;
	public static final String METAJOB_STATUS_FAIL = MetaJobConstant.METAJOB_STATUS_FAIL;
	public static final String METAJOB_STATUS_CANCEL = MetaJobConstant.METAJOB_STATUS_CANCEL;
	public static final String METAJOB_STATUS_SPLITFAIL = MetaJobConstant.METAJOB_STATUS_SPLITFAIL;
}
