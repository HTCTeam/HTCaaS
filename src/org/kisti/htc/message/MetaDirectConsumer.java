package org.kisti.htc.message;

import java.io.FileInputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaDirectConsumer {
	
	final Logger logger = LoggerFactory.getLogger(MetaDirectConsumer.class);
	
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
    private String MetaJobQueue;
	private String url;

    private boolean transacted;
    private boolean durable;
    private String clientId;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    private long sleepTime = 0;
    private long receiveTimeOut;

    public MetaDirectConsumer(String name) {
    	consumerName = name;
    	try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			url = prop.getProperty("ActiveMQ.url");
			MetaJobQueue = prop.getProperty("ActiveMQ.MetaJobQueue");
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}
    }
    
    public MetaDTO getMessage(long time) throws Exception {
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
            destination = session.createQueue(MetaJobQueue);

//	            replyProducer = session.createProducer(null);
//	            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            MessageConsumer consumer = null;
            
            if (durable && topic) {
                consumer = session.createDurableSubscriber((Topic)destination, consumerName);
            } else {
                consumer = session.createConsumer(destination);
            }

//            Message msg = consumer.receive();
            Message msg = consumer.receive(time);
     
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
    
    public MetaDTO getMessage() throws Exception {
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
            destination = session.createQueue(MetaJobQueue);

//	            replyProducer = session.createProducer(null);
//	            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            MessageConsumer consumer = null;
            
            if (durable && topic) {
                consumer = session.createDurableSubscriber((Topic)destination, consumerName);
            } else {
                consumer = session.createConsumer(destination);
            }

//            Message msg = consumer.receive();
            Message msg = consumer.receive();
     
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

    public MetaDTO handleMsg(Message message) {
        try {
            if (message instanceof ObjectMessage) {
            	ObjectMessage objMsg = (ObjectMessage)message;
                if (verbose) {
                    MetaDTO dto = (MetaDTO) objMsg.getObject();
//                    dto.setMetaJobId(Integer.parseInt(message.getStringProperty("JMSXMetaJobID")));

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

    public void setMetaJobQueue(String queue) {
        this.MetaJobQueue = queue;
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

    public static void main(String[] args) throws Exception {
    	MetaDirectConsumer consumerTool = new MetaDirectConsumer("metaJobQueue");
    	consumerTool.setMetaJobQueue("aaa");
        System.out.println(consumerTool.getMessage(1000));
    }

}

