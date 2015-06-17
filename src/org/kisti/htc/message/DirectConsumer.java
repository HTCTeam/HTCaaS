package org.kisti.htc.message;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectConsumer implements Runnable {
	
	final Logger logger = LoggerFactory.getLogger(DirectConsumer.class);
	
    private String consumerName;    
	private boolean running;

	private Connection connection;
	private Session session;
	private Destination destination;
	private MessageProducer replyProducer;

    private boolean pauseBeforeShutdown;
    private boolean verbose = true;
    private int maxiumMessages;
    private boolean topic;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String url;
	private String SubJobQueue;

    private boolean transacted;
    private boolean durable;
    private String clientId;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    private long sleepTime = 0;
    private long receiveTimeOut;

    public DirectConsumer(String name, long sleepTime) {
    	consumerName = name;
    	this.sleepTime = sleepTime;
    	try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			url = prop.getProperty("ActiveMQ.url");
			SubJobQueue = prop.getProperty("ActiveMQ.SubJobQueue");
			
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}
    }
    
    public DirectConsumer(String name) {
    	consumerName = name;
    	try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			url = prop.getProperty("ActiveMQ.url");
			SubJobQueue = prop.getProperty("ActiveMQ.SubJobQueue");
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}
    }
    
    public DirectConsumer(String name, String queueName) {
    	consumerName = name;
    	try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			url = prop.getProperty("ActiveMQ.url");
			SubJobQueue = prop.getProperty(queueName);
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}
    }
    
    public DTO getMessage(String queueName, int waitingTime) throws Exception {
        try {
            running = true;

            logger.debug("Connecting to URL: " + url);
            logger.debug("Using a " + (durable ? "durable" : "non-durable") + " subscription");

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
            connection = connectionFactory.createConnection();
            if (durable && clientId != null && clientId.length() > 0 && !"null".equals(clientId)) {
                connection.setClientID(clientId);
            }
            connection.start();

            
            session = connection.createSession(transacted, ackMode);
            destination = session.createQueue(queueName);

//	            replyProducer = session.createProducer(null);
//	            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            MessageConsumer consumer = null;
            
            if (durable && topic) {
                consumer = session.createDurableSubscriber((Topic)destination, consumerName);
            } else {
                consumer = session.createConsumer(destination);
            }

//            System.out.println("wating");
//            Message msg = consumer.receive();
            Message msg = consumer.receive(waitingTime);
//            System.out.println("end");
            if (msg == null) {
            	throw new Exception("No messages in Queue");
            }
            
            return handleMsg(msg);
            
        } catch (Exception e) {
        	logger.error("Caught: " + e);
        	throw e;
        }
        finally {
            connection.close();
        }
    }
    
//    public DTO getMessage() throws Exception {
//        try {
//            running = true;
//
//            logger.debug("Connecting to URL: " + url);
//            logger.debug("Using a " + (durable ? "durable" : "non-durable") + " subscription");
//
//            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
//            connection = connectionFactory.createConnection();
//            if (durable && clientId != null && clientId.length() > 0 && !"null".equals(clientId)) {
//                connection.setClientID(clientId);
//            }
//            connection.start();
//
//            
//            session = connection.createSession(transacted, ackMode);
//            destination = session.createQueue(SubJobQueue);
//
////	            replyProducer = session.createProducer(null);
////	            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//
//            MessageConsumer consumer = null;
//            
//            if (durable && topic) {
//                consumer = session.createDurableSubscriber((Topic)destination, consumerName);
//            } else {
//                consumer = session.createConsumer(destination);
//            }
//
////            System.out.println("wating");
////            Message msg = consumer.receive();
//            Message msg = consumer.receive(1000);
////            System.out.println("end");
//            if (msg == null) {
//            	throw new Exception("No messages in Queue");
//            }
//            
//            return handleMsg(msg);
//            
//        } catch (Exception e) {
//        	logger.error("Caught: " + e);
//        	throw e;
//        }
//        finally {
//            connection.close();
//        }
//    }

    public DTO handleMsg(Message message) {
        try {
            if (message instanceof ObjectMessage) {
            	ObjectMessage objMsg = (ObjectMessage)message;
                if (verbose) {
                    DTO dto = (DTO) objMsg.getObject();
//                    dto.setJobId(Integer.parseInt(message.getStringProperty("JMSXJobID")));
//                    dto.setUserId(message.getStringProperty("JMSXUserId"));
//                    dto.setAppName(message.getStringProperty("JMSXAppName"));
//                    dto.setProjectName(message.getStringProperty("JMSXProjectName"));

                    logger.debug(dto.toString());
                    return dto;
                }
                
            } else {
                if (verbose) {
                    logger.debug("Received: " + message);
                }
            }

            if (message.getJMSReplyTo() != null) {
                replyProducer.send(message.getJMSReplyTo(), session.createTextMessage("Reply: " + message.getJMSMessageID()));
            }

            if (transacted) {
                session.commit();
            } else if (ackMode == Session.CLIENT_ACKNOWLEDGE) {
                message.acknowledge();
            }

        } catch (JMSException e) {
            logger.debug("Caught: " + e);
            e.printStackTrace();
        } finally {
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
        }
        
        return null;
    }

    public synchronized void onException(JMSException ex) {
        logger.debug("JMS Exception occured.  Shutting down client.");
        running = false;
    }

    synchronized boolean isRunning() {
        return running;
    }

   
    public void setAckMode(String ackMode) {
        if ("CLIENT_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.CLIENT_ACKNOWLEDGE;
        }
        if ("AUTO_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.AUTO_ACKNOWLEDGE;
        }
        if ("DUPS_OK_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.DUPS_OK_ACKNOWLEDGE;
        }
        if ("SESSION_TRANSACTED".equals(ackMode)) {
            this.ackMode = Session.SESSION_TRANSACTED;
        }
    }

    public void setClientId(String clientID) {
        this.clientId = clientID;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

   
    public void setPauseBeforeShutdown(boolean pauseBeforeShutdown) {
        this.pauseBeforeShutdown = pauseBeforeShutdown;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public void setReceiveTimeOut(long receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

//    public void setSubject(String subject) {
//        this.subject = subject;
//    }
    
    public void setTopic(boolean topic) {
        this.topic = topic;
    }

    public void setQueue(boolean queue) {
        this.topic = !queue;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
	public void run(){
//    	DirectConsumer consumerTool = new DirectConsumer(Thread.currentThread().getName());
    	System.out.println(Thread.currentThread().getName());
    	DTO dd;
		try {
//			dd = getMessage(SubJobQueue);
//			System.out.println(dd.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public static void main(String[] args) throws Exception {
    	
    	
    	for(int i =0; i<50 ; i++){
    		DirectConsumer consumerTool = new DirectConsumer("test");
    		DTO dto = consumerTool.getMessage("test", 1);
    		System.out.println(dto.toString());
    	}
////    	System.out.println(consumerTool.consumerName);
//    	DTO dd;
//    	dd = consumerTool.getMessage("bbb", 5000);
//		System.out.println(dd.toString());
		
//		String JMXServiceURL = "service:jmx:rmi:///jndi/rmi://test.kisti.re.kr:2011/jmxrmi";
//		MBeanServerConnection connection;
//		String JMXObjectName = "my-broker:BrokerName=localhost,Type=Broker";
//		BrokerViewMBean mbean;
//		QueueViewMBean iqueue = null;
//		JMXServiceURL url = new JMXServiceURL(JMXServiceURL);
//		System.out.println("1");
//    JMXConnector connector = JMXConnectorFactory.connect(url);
//    System.out.println("2");
//    connector.connect();
//    connection = connector.getMBeanServerConnection();
//    ObjectName name = new ObjectName(JMXObjectName);
//    mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name, BrokerViewMBean.class, true);
//	String metaJobQueue = "MetaJobQueue.ActiveMQ";	
//		QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, name, QueueViewMBean.class, true);
//		System.out.println("ActiveMQ broker id="   + mbean.getBrokerId());
//	    System.out.println("ActiveMQ broker name=" + mbean.getBrokerName());
//	      for (ObjectName queueName : mbean.getQueues()) {
//	        queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
//	        if (queueMbean.getName().equals(metaJobQueue)) {
//	          iqueue = queueMbean;
//	          break;
//	        }
//	      }
//	      if (iqueue == null) {
//	        System.out.println("ActiveMQ Queue does not exist");
//	        throw new Exception("Queue not exist: " + metaJobQueue);
//	      }
//    	int num = 2000;
//    	ExecutorService executorService = Executors.newFixedThreadPool(num);
//    	for(int i=0;i<num;i++){
//    		executorService.execute(new DirectConsumer(Thread.currentThread().getName()));
//    		
//    	}
//    	executorService.shutdown();
    }
}

