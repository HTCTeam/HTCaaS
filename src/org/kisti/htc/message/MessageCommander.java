package org.kisti.htc.message;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageCommander {
	private static final Logger logger = LoggerFactory.getLogger(MessageCommander.class);
	
	private static QueueViewMBean iqueue = null;
	private static QueueViewMBean uqueue = null;
	private static String MetaJobQueue = "MetaJobQueue.ActiveMQ";
	private static String UserJobQueue = null;
	private static String JMXServiceURL;
	private static String JMXObjectName;

	public MessageCommander() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));

			JMXServiceURL = prop.getProperty("ActiveMQ.Broker.JMXServiceURL");
			logger.info("JMXServiceURL: " + JMXServiceURL);

			JMXObjectName = prop.getProperty("ActiveMQ.Broker.ObjectName");
			logger.info("JMXObjectName: " + JMXObjectName);

			MetaJobQueue = prop.getProperty("ActiveMQ.MetaJobQueue");
			logger.info("MetaJobQueueName: " + MetaJobQueue);

		} catch (Exception e) {
			System.exit(1);
		}
	}
	
	public void removeQueue(String userName){
	  try {
      JMXServiceURL url;
      url = new JMXServiceURL(JMXServiceURL);

      JMXConnector connector;
      connector = JMXConnectorFactory.connect(url, null);

      connector.connect();
      MBeanServerConnection connection;
      connection = connector.getMBeanServerConnection();
      ObjectName name;
      name = new ObjectName(JMXObjectName);

      BrokerViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name, BrokerViewMBean.class, true);

      System.out.println("Statistics for broker " + mbean.getBrokerId() + " - " + mbean.getBrokerName());

      
      mbean.removeQueue("p258rsw");
      
	  } catch(Exception e){}
	  
	  
	}

	public int removeMessage(int metaJobId, String userName) {
		int num = 0;

		try {
			JMXServiceURL url;
			url = new JMXServiceURL(JMXServiceURL);

			JMXConnector connector;
			connector = JMXConnectorFactory.connect(url, null);

			connector.connect();
			MBeanServerConnection connection;
			connection = connector.getMBeanServerConnection();
			ObjectName name;
			name = new ObjectName(JMXObjectName);

			BrokerViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name, BrokerViewMBean.class, true);

			System.out.println("Statistics for broker " + mbean.getBrokerId() + " - " + mbean.getBrokerName());

			boolean metaQueue = false;
			boolean userQueue = false;
			for (ObjectName queueName : mbean.getQueues()) {
				QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
				if (queueMbean.getName().equals(MetaJobQueue)) {
					iqueue = queueMbean;
					metaQueue = true;
				} else if (queueMbean.getName().equals(userName)) {
					uqueue = queueMbean;
					userQueue = true;
				}
				if (metaQueue == true && userQueue == true)
					break;
			}

			try {

				iqueue.removeMatchingMessages("JMSCorrelationID='" + metaJobId + "'");
				num = uqueue.removeMatchingMessages("JMSCorrelationID='" + metaJobId + "'");
			} catch (Exception e) {
				logger.error("Cannot remove MatchingMessages. Not existing messages " + e);
			}
			// iqueue.removeMessage("ID:diamond.kisti.re.kr-34383-1337836943739-0:0:1:1:1");

			if (iqueue == null && uqueue == null) {
				throw new Exception("Queue not exist: " + MetaJobQueue + "or" + UserJobQueue);
			}

		} catch (MalformedURLException e) {
			logger.error("Cannot access ActiveMQ Broker for Statistics" + e);
			System.exit(1);
		} catch (IOException e) {
			logger.error("Cannot access ActiveMQ Broker for Statistics" + e);
			System.exit(1);
		} catch (MalformedObjectNameException e) {
			logger.error("Cannot access ActiveMQ Broker for Statistics" + e);
			System.exit(1);
		} catch (NullPointerException e) {
			logger.error("Cannot access ActiveMQ Broker for Statistics" + e);
			System.exit(1);
		} catch (Exception e){
			logger.error("Exception Error" + e);
		}

		return num;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	  
	  MessageCommander mc = new MessageCommander();
	  mc.removeQueue("p258rsw");

	}

}
