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
import java.util.ArrayList;
import java.util.List;
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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import util.DebugMessage; // output debug message _debug, _warn, _error

/**
 * A simple tool for publishing messages
 * 
 * @version $Revision: 1.2 $
 */
public class MessageSender extends DebugMessage {

	//final Logger logger = LoggerFactory.getLogger(MessageSender.class);

	private Destination destination;

	private boolean verbose = true;
	private int messageSize = 255;
	private long timeToLive;

	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	// private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	// private String url = "failover://tcp://wisdom.kisti.re.kr:61616";
	// private String url = "tcp://150.183.250.212:61616";

	private String url;
	private String subJobQueue;

	private boolean transacted;
	private boolean persistent;

	private Connection connection;
	private Session session;
	private MessageProducer producer;

	public MessageSender() {

    set_logger_prefix("[MessageSender] ");  // debug message prefix

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			url = prop.getProperty("ActiveMQ.url");

		} catch (Exception e) {
			_error("Failed to load config file: " + e.getMessage());
		}

		init();
	}

	public MessageSender(String queueName) {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			url = prop.getProperty("ActiveMQ.url");
			subJobQueue = queueName;

		} catch (Exception e) {
			_error("Failed to load config file: " + e.getMessage());
		}

		init();
	}

	public void init() {
		try {
			_debug("Connecting to URL: " + url);

      if (persistent) _debug("Using persistent messages");
      else _debug("Using non-persistent messages");

			if (timeToLive != 0) {
				_debug("Messages time to live " + timeToLive + " ms");
			}

			// Create the connection.
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
			connection = connectionFactory.createConnection();
			connection.start();

			// Create the session
			this.session = connection.createSession(transacted,
					Session.AUTO_ACKNOWLEDGE);

			// Create queue
			if(subJobQueue!=null){
				this.destination = session.createQueue(subJobQueue);
			}

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
			_error("Caught: " + e);
			e.printStackTrace();
		}

	}
	
	private void terminate(){
		
		try{
			producer.close();
			session.close();
			connection.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
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
			_error("Caught: " + e);
			e.printStackTrace();
		}
	}

	public static void main(String arg[]) {

		MessageSender ms = new MessageSender();

//		MetaDTO mdto = new MetaDTO();
//		mdto.setMetaJobId(1);
//		mdto.setUserId("seungwoo");
//		mdto.setApp("tt");
//		mdto.setNumSubJob(10);
		
		try {
			for(int i=0 ; i<100000 ; i++){
				DTO msg = new DTO();
				msg.setMetaJobId(3);
				msg.setAppName("general");
				msg.setJobId(300);
				msg.setUserId("seungwoo");
				List<String> arguments = new ArrayList<String>();
				arguments.add("test1");
				msg.setArguments(arguments);
	//
				String executable = "exe";
				msg.setExecutable(executable);
				List<String> inputFiles = new ArrayList<String>();
				inputFiles.add("test2");
				msg.setInputFiles(inputFiles);
	//
				List<String> outputFiles = new ArrayList<String>();
				outputFiles.add("test3");
				msg.setOutputFiles(outputFiles);
					
	//			ms.changeDestiQueue("aaa");
	//			ms.sendMessage(mdto);
					
					ms.changeDestiQueue("test");
					ms.sendMessage(msg);
					Thread.sleep(50);
			}
		
		ms.producer.close();
		ms.session.close();
		ms.connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendMessage(DTO msg) throws Exception {

		ObjectMessage message;
		message = session.createObjectMessage();
		message.setObject(msg);

		// message.setJMSMessageID("test2");
		message.setJMSCorrelationID(Integer.toString(msg.getMetaJobId()));
		
//		message.setStringProperty("JMSXUserId", msg.getUserId());
//		message.setStringProperty("JMSXAppName", msg.getAppName());
		// // message.setStringProperty("JMSXProjectName",
		// msg.getProjectName());
//		message.setStringProperty("JMSXJobID", "" + msg.getJobId());
//		message.setStringProperty("JMSXMetaJobID", "" + msg.getMetaJobId());

		// Application ID, GroupdSeq
		if (verbose) {
			// String msg = message.getText();
			DTO dto = (DTO) message.getObject();

			_debug("Sending Message");
			_debug(dto.toString());
		}

		producer.send(message);
		if (transacted) {
			session.commit();
		}

	}
	
	public void sendMessage(MetaDTO msg) throws Exception {

		ObjectMessage message;
		message = session.createObjectMessage();
		message.setObject(msg);

		// message.setJMSMessageID("test2");
		message.setJMSCorrelationID(Integer.toString(msg.getMetaJobId()));
		message.setStringProperty("JMSXUserId", msg.getUserId());

		// Application ID, GroupdSeq
		if (verbose) {
			// String msg = message.getText();
			MetaDTO dto = (MetaDTO) message.getObject();

			_debug("Sending Message");
			_debug(dto.toString());
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

	public void setSubJobQueue(String queue) {
		this.subJobQueue = queue;
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
