package org.kisti.htc.agentmanager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class AmazonCloud {

	private AmazonEC2 ec2;
	private String ReservationId;
	private String instanceId;
	private AWSCredentials credentials;
	private String endpoint;
	private KeyPair keyPair;
	private String insType;
	private String userData;
	public static List<Instance> instances;

	
	
	public AmazonCloud() {
		try {
			credentials = new PropertiesCredentials(
					AmazonCloud.class.getResourceAsStream("AwsCredentials.properties"));
			
			ec2 = new AmazonEC2Client(credentials);
			ec2.setEndpoint("ec2.ap-northeast-1.amazonaws.com");
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		} catch (IOException e){
			e.printStackTrace();
		}
		
	}

	public static void main(String[] arg) throws Exception {

		AmazonCloud ac = new AmazonCloud();
//		ac.setEndpoint("ec2.ap-northeast-1.amazonaws.com");

		// ac.createSecurityGroup("seungwoo", "test");
		// ac.openPort(21, null, "seungwoo");
		// ac.deleteSecurityGroup("aa");
		// ac.describeSecurityGroup();

//		ac.setInsType("t1.micro");
//		ac.setUserData("shell");

//		ac.createKeyPair("seungwoo");
//		ac.setKeyPair("seungwoo");

//		List<Instance> li = null;
	
		//////////////////////////////////////\ufffd\ufffd\ufffd\ufffd \ufffd\ufffd\ufffd\ufffd \ufffd\u03bd\ufffd\ufffd\u03fd\ufffd\ufffd\ufffd\ufffd\ufffd ip \u0239\ufffd\ufffd///////////////////////
//		instances = ac.createAMInstances("ami-1e43f61f", "seungwoo", 1);
//		ac.getRunningState(instances);
//		li = ac.getRefreshInstanceInfo(instances);
//		ac.getPublicDnsName(li);
		
		
		/////////////////////////////////////\ufffd\ufffd\ufffd\ufffd running \ufffd\ufffd\ufffd\ufffd \ufffd\u03bd\ufffd\ufffd\u03fd\ufffd\ufffd\ufffd\ufffd\ufffd ip \u0239\ufffd\ufffd//////////////////
//		li = ac.getRunningInstanceInfo();
//		ac.getPublicDnsName(li);
		
		

		//		ac.getInstanceInfo();
		// ac.listAvailZone();
		// ac.listImage();
		// ac.deleteKeyPair("test2");w
		// ac.deleteKeyPairAll();
		// ac.listKeyPair();
		// ac.startInstance();
		// ac.aa();
		// ac.terminateInstance("i-76fabd77");
//		 ac.terminateAllInstance();

	}
	
	public List<String> getPublicDnsName(List<Instance> ins){
		List<String> dname = new ArrayList<String>();
		for(Instance it : ins){
			dname.add(it.getPublicDnsName());
			System.out.println(it.getPublicDnsName());
		}
		return dname;
	}
	
	public InstanceState getRunningState(List<Instance> ins) {
		InstanceState is = null;
		for(Instance it : ins){
			while(true){
				is = getInstanceStateInfo(it.getInstanceId());
				if(is.getCode()==16)
					break;
				else
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		return is;
	}


	public void createSecurityGroup(String securityGroupName, String description)
			throws Exception {

		CreateSecurityGroupRequest group = new CreateSecurityGroupRequest();
		group.setGroupName(securityGroupName);
		group.setDescription(description);

		ec2.createSecurityGroup(group);
		System.out.println("Created security group: " + securityGroupName);
	}

	public void deleteSecurityGroup(String securityGroupName) throws Exception {

		DeleteSecurityGroupRequest group = new DeleteSecurityGroupRequest();
		group.setGroupName(securityGroupName);

		ec2.deleteSecurityGroup(group);
		System.out.println("Deleted security group: " + securityGroupName);
	}

	public void describeSecurityGroup() {
		DescribeSecurityGroupsResult result = ec2.describeSecurityGroups();
		List<SecurityGroup> sgs = result.getSecurityGroups();
		for (SecurityGroup sg : sgs) {
			System.out.println(sg.toString());
		}
	}

	public void openPort(int port, String ip, String securityGroupName)
			throws Exception {

		AuthorizeSecurityGroupIngressRequest authRequest = new AuthorizeSecurityGroupIngressRequest();

		authRequest.setIpProtocol("tcp");
		authRequest.setGroupName(securityGroupName);
		authRequest.setFromPort(port);
		authRequest.setToPort(port);

		String cidr;
		if (ip == null
				|| !ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			cidr = "0.0.0.0/0";
		} else {
			cidr = ip + "/32";
		}
		authRequest.setCidrIp(cidr);

		ec2.authorizeSecurityGroupIngress(authRequest);
		System.out.println("Security group " + securityGroupName
				+ " now accepting connections from " + cidr + " on port "
				+ port);

	}

	public void startInstance(String InstanceId) {
		StartInstancesRequest sir = new StartInstancesRequest();
		List<String> st = new ArrayList<String>();
		st.add(InstanceId);
		sir.setInstanceIds(st);

		StartInstancesResult sr = ec2.startInstances(sir);
	}

	public void startInstance(List<String> InstanceId) {

		StartInstancesRequest sir = new StartInstancesRequest();
		sir.setInstanceIds(InstanceId);

		StartInstancesResult sr = ec2.startInstances(sir);
	}

	public void stopInstance(String InstanceId) {

		StopInstancesRequest sir = new StopInstancesRequest();
		List<String> st = new ArrayList<String>();
		st.add(InstanceId);
		sir.setInstanceIds(st);

		StopInstancesResult sr = ec2.stopInstances(sir);
	}

	public void stopInstance(List<String> InstanceId) {

		StopInstancesRequest sir = new StopInstancesRequest();
		sir.setInstanceIds(InstanceId);

		StopInstancesResult sr = ec2.stopInstances(sir);
	}

	public void terminateInstance(String instanceId) {

		TerminateInstancesRequest sir = new TerminateInstancesRequest();
		List<String> st = new ArrayList<String>();
		st.add(instanceId);
		sir.setInstanceIds(st);

		TerminateInstancesResult sr = ec2.terminateInstances(sir);
		System.out.println("terminated InstanceID:" + instanceId);
	}

	public void terminateInstance(List<String> instanceId) {

		TerminateInstancesRequest sir = new TerminateInstancesRequest();
		sir.setInstanceIds(instanceId);

		TerminateInstancesResult sr = ec2.terminateInstances(sir);
	}

	public void terminateAllInstance() {

		DescribeInstancesResult describeInstancesRequest = ec2
				.describeInstances();
		List<Reservation> reservations = describeInstancesRequest
				.getReservations();
		List<String> instanceId = new ArrayList<String>();
		List<Instance> lins = new ArrayList<Instance>();

		for (Reservation reservation : reservations) {
			lins.addAll(reservation.getInstances());
		}
		for (Instance ins : lins) {
			if (ins.getState().getCode() != 48) {
				instanceId.add(ins.getInstanceId());
			}
		}

		this.terminateInstance(instanceId);
		System.out.println("terminated all instances.");
	}

	public void rebootInstance(String InstanceId) {

		RebootInstancesRequest rir = new RebootInstancesRequest();
		List<String> st = new ArrayList<String>();
		st.add(InstanceId);
		rir.setInstanceIds(st);

		ec2.rebootInstances(rir);
	}

	public void rebootInstance(List<String> InstanceId) {

		RebootInstancesRequest rir = new RebootInstancesRequest();
		rir.setInstanceIds(InstanceId);

		ec2.rebootInstances(rir);
	}

	public void listImage() {
		DescribeImagesResult dir = ec2.describeImages();
		List<Image> im = dir.getImages();

		System.out.println(im.isEmpty());
		for (Image i : im) {
			System.out.println(i.toString());
		}
	}

	public void listKeyPair() {
		try {
			DescribeKeyPairsResult key = ec2.describeKeyPairs();
			List<KeyPairInfo> ki = key.getKeyPairs();
			// System.out.println(ki.isEmpty());

			for (KeyPairInfo kp : ki) {
				System.out.println(kp.toString());
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}
	}

	public void createKeyPair(String keyPairName) throws Exception {
		CreateKeyPairRequest kpReq = new CreateKeyPairRequest();
		kpReq.setKeyName(keyPairName);
		CreateKeyPairResult kpres = this.ec2.createKeyPair(kpReq);
		keyPair = new KeyPair();
		keyPair.setKeyName(keyPairName);
		keyPair = kpres.getKeyPair();
		System.out.println("You havekeyPair.getKeyName = "
				+ keyPair.getKeyName() + "\nkeyPair.getKeyFingerprint()="
				+ keyPair.getKeyFingerprint() + "\nkeyPair.getKeyMaterial()="
				+ keyPair.getKeyMaterial());
		FileWriter fw = new FileWriter(keyPairName + ".pem");
		BufferedWriter bw = new BufferedWriter(fw, 1024);
		bw.write(keyPair.getKeyMaterial());
		bw.close();
		fw.close();

	}

	public void deleteKeyPair(String keyPairName) {
		DeleteKeyPairRequest kpReq = new DeleteKeyPairRequest();
		kpReq.setKeyName(keyPairName);
		this.ec2.deleteKeyPair(kpReq);
	}

	public void deleteKeyPairAll() {
		DescribeKeyPairsResult key = ec2.describeKeyPairs();
		List<KeyPairInfo> ki = key.getKeyPairs();
		for (KeyPairInfo kp : ki) {
			this.deleteKeyPair(kp.getKeyName());
		}
	}

	public InstanceState getInstanceStateInfo(String insId) {
		
		DescribeInstancesRequest req = new DescribeInstancesRequest();
		List<String> st = new ArrayList<String>(); 
		st.add(insId);
		req.setInstanceIds(st);
		 
		DescribeInstancesResult dir = ec2.describeInstances(req);
		List<Reservation> reservations = dir.getReservations();
		List<Instance> instances = new ArrayList<Instance>();
		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}
		
		return instances.get(0).getState();
	}
	
	public List<Instance> getRefreshInstanceInfo(List<Instance> ins) {
		
		DescribeInstancesRequest req = new DescribeInstancesRequest();
		List<String> st = new ArrayList<String>(); 
		for(Instance i: ins){
			st.add(i.getInstanceId());
		}
		
		req.setInstanceIds(st);
		 
		DescribeInstancesResult dir = ec2.describeInstances(req);
		
		List<Reservation> reservations = dir.getReservations();
		List<Instance> instances = new ArrayList<Instance>();
//		instances.clear();
		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}
		
		return instances;
	}
	
	public List<Instance> getInstanceInfo() {
		/*
		 * DescribeInstancesRequest req = new DescribeInstancesRequest();
		 * List<String> st = new ArrayList<String>(); st.add("i-3ed2973f");
		 * req.setInstanceIds(st);
		 */
		DescribeInstancesResult dir = ec2.describeInstances();

		List<Reservation> reservations = dir.getReservations();
		List<Instance> instances = new ArrayList<Instance>();
		for (Reservation reservation : reservations) {
			System.out.println(reservation.toString());
			instances.addAll(reservation.getInstances());
		}

		System.out.println("You have " + instances.size()
				+ " Amazon EC2 instance(s) running.");

		/*
		 * Iterator<Instance> itr = instances.iterator(); Instance is = null;
		 * while (itr.hasNext()) { is = itr.next();
		 * System.out.println(is.toString()); }
		 */
		return instances;
	}
	

	public List<Instance> getRunningInstanceInfo() {

		DescribeInstancesResult dir = ec2.describeInstances();

		List<Reservation> reservations = dir.getReservations();
		List<Instance> instances = new ArrayList<Instance>();
		List<Instance> instances2 = new ArrayList<Instance>();

		for (Reservation reservation : reservations) {
			instances2 = reservation.getInstances();
			Instance ins = instances2.get(0);
				if(ins.getState().getCode() == 16){
					System.out.println(reservation.toString());
					instances.add(ins);
				}
		}

		System.out.println("You have " + instances.size()
				+ " Amazon EC2 instance(s) running.");

		/*
		 * Iterator<Instance> itr = instances.iterator(); Instance is = null;
		 * while (itr.hasNext()) { is = itr.next();
		 * System.out.println(is.toString()); }
		 */
		return instances;
	}
	
	public void listAvailZone() {
		try {
			DescribeAvailabilityZonesResult daz = ec2
					.describeAvailabilityZones();
			List<AvailabilityZone> av = daz.getAvailabilityZones();

			System.out.println("You have access to " + av.size()
					+ " Availability Zones.");

			Iterator<AvailabilityZone> itr = av.iterator();
			while (itr.hasNext()) {
				AvailabilityZone az = itr.next();
				System.out.println(az.toString());
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}

	}

	public List<Instance> createAMInstances(String amiId, int max) {
		return createAMInstances(amiId, 1, max, "seungwoo", insType,
				endpoint);
	}

	public List<Instance> createAMInstances(String amiId, int max, String insType){
		return createAMInstances(amiId, 1, max, "seungwoo", insType,
				endpoint);
	}

	public List<Instance> createAMInstances(String amiId, String keyPairName, int max){
		return createAMInstances(amiId, 1, max, keyPairName, insType, endpoint);
	}

	public List<Instance> createAMInstances(String amiId, String keyPairName, int max,
			String insType){
		return createAMInstances(amiId, 1, max, keyPairName, insType, endpoint);
	}

	public List<Instance> createAMInstances(String AMId, int min, int max,
			String keyPairName, String insType, String availabilityZone){
		
		RunInstancesRequest request = new RunInstancesRequest();
		
		try {
			if (insType == null)
				request.setInstanceType("t1.micro");
			else
				request.setInstanceType(insType);
	
			request.setMinCount(min);
			request.setMaxCount(max);
			request.setImageId(AMId);
			request.setKeyName(keyPairName);// assign Keypair name for this request
	
			// read UserData File
			if (userData != null) {
				FileReader fr = new FileReader(userData);
				BufferedReader br = new BufferedReader(fr, 1024);
				StringBuffer sb = new StringBuffer();
				String temp = null;
				while ((temp = br.readLine()) != null) {
					sb.append(temp);
					sb.append("\n");
				}
				br.close();
				fr.close();
				request.setUserData(Base64.encodeBase64String((sb.toString()
						.getBytes())));
			}
			List<String> sgr = new ArrayList<String>();
			sgr.add("seungwoo");
			request.setSecurityGroups(sgr);
		} catch (AmazonServiceException ase) {
		} catch (IOException e){
			e.printStackTrace();
		}
		RunInstancesResult runInstancesRes = this.ec2.runInstances(request);
		this.ReservationId = runInstancesRes.getReservation()
		.getReservationId();
		return runInstancesRes.getReservation().getInstances();
	}
	
	public KeyPair getKeyPair() {
		return keyPair;
	}

	public void setKeyPair(String keyName) {
		keyPair = new KeyPair();
		keyPair.setKeyName(keyName);
	}

	/**
	 * @return the reservationId
	 */
	public String getReservationId() {
		return ReservationId;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	public String getAMInstanceName() {
		return instanceId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		ec2.setEndpoint(endpoint);
		this.endpoint = endpoint;
	}

	public String getInsType() {
		return insType;
	}

	public void setInsType(String insType) {
		this.insType = insType;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}
	
}
