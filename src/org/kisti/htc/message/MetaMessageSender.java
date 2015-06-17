package org.kisti.htc.message;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.FileInputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger; // logger

/**
 * A simple tool for publishing messages
 * 
 * @version $Revision: 1.2 $
 */
public class MetaMessageSender {

  final Logger logger = LoggerFactory.getLogger(MetaMessageSender.class);
  //private static final mLogger logger = new mLogger();
  
  private Destination destination;

  private boolean verbose = true;
  private int messageSize = 255;
  private long timeToLive;

  private String user = ActiveMQConnection.DEFAULT_USER;
  private String password = ActiveMQConnection.DEFAULT_PASSWORD;
//  private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
//  private String url =  "failover://tcp://wisdom.kisti.re.kr:61616";
//  private String url =  "tcp://150.183.250.212:61616";
  
  private String url;
  private static String MetaJobQueue;

  private boolean transacted;
  private boolean persistent;

  private Connection connection;
  private  Session session;
  private  MessageProducer producer;

  public MetaMessageSender() {

    //logger.set_prefix("[MetaMessageSender] ");  // logger message prefix

    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
      url = prop.getProperty("ActiveMQ.url");
      MetaJobQueue = prop.getProperty("ActiveMQ.MetaJobQueue");
    } catch (Exception e) {
      logger.error("Failed to load config file: " + e.getMessage());
      System.exit(1);
    }
    
    try {
      logger.debug("Connecting to URL: " + url);

      if (persistent) logger.debug("Using persistent messages");
      else logger.debug("Using non-persistent messages");

      if (timeToLive != 0) {
        logger.debug("Messages time to live " + timeToLive + " ms");
      }

      // Create the connection
      logger.debug("Messages time to live " + timeToLive + " ms");
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
      connection = connectionFactory.createConnection();
      connection.start();

      // Create the session
      this.session = connection.createSession(transacted,  Session.AUTO_ACKNOWLEDGE);

      // Create queue
      this.destination = session.createQueue(MetaJobQueue);

      // Create the producer.
      this.producer = session.createProducer(destination);

      if (persistent) {
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
      } else {
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      }
      if (timeToLive != 0) {
        producer.setTimeToLive(timeToLive);
      }
      
    } catch (Exception e) {
      logger.error("Caught: " + e);
      e.printStackTrace();
    }

  }
  
  public static void main(String[] arg){
    new MetaMessageSender();
//    mm.changeDestiQueue(MetaJobQueue);
  }
   
  public void changeDestiQueue(String queue) {
    try {
      
      // Create queue
      this.destination = session.createQueue(queue);

      // Create the producer.
      this.producer = session.createProducer(destination);

      if (persistent) {
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
      } else {
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      }
      if (timeToLive != 0) {
        producer.setTimeToLive(timeToLive);
      }

      // producer.setPriority(2);
    } catch (Exception e) {
      logger.error("Caught: " + e);
      e.printStackTrace();
    }
  }
  
  public void sendMessage(MetaDTO msg) throws Exception {
    
    
    ObjectMessage message;
    message = session.createObjectMessage();
    message.setObject(msg);

    message.setJMSCorrelationID(Integer.toString(msg.getMetaJobId()));
    message.setIntProperty("JMSXMetaJobID", msg.getMetaJobId());
    
    //Application ID, GroupdSeq 
    if (verbose) {
      // String msg = message.getText();
      MetaDTO dto = (MetaDTO) message.getObject();
      logger.debug("Sending Message");
      logger.debug(dto.toString());      
    }

    producer.send(message);
    if (transacted) {
      session.commit();
    }

  }

  public void close() throws JMSException {
    connection.close();
  }
  
  /*
   * private String createMessageText(int index) { StringBuffer buffer = new
   * StringBuffer(messageSize); buffer.append("Message: " + index +
   * " sent at: " + new Date()); if (buffer.length() > messageSize) { return
   * buffer.substring(0, messageSize); } for (int i = buffer.length(); i <
   * messageSize; i++) { buffer.append(' '); } return buffer.toString(); }
   */
  public void setPersistent(boolean durable) {
    this.persistent = durable;
  }

  public void setMessageSize(int messageSize) {
    this.messageSize = messageSize;
  }

  public void setPassword(String pwd) {
    this.password = pwd;
  }

  public void setTimeToLive(long timeToLive) {
    this.timeToLive = timeToLive;
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
}
