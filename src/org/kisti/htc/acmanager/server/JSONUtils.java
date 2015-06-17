package org.kisti.htc.acmanager.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;



/**
 * 어플리케이션 JSON 유틸
 * 
 * @author kocjun
 *
 */
public class JSONUtils {
	
	//private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);
  final static mLogger logger = mLoggerFactory.getLogger("AC");
	
	/**
	 * String 입력을 Map 으로 변환
	 * 
	 * @param input input String
	 * @return Map
	 */
	public static Map toMap(String input) {
		try {
			return new ObjectMapper().readValue(input, HashMap.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 *  InputStream 입력을 Map 으로 변환
	 *  
	 * @param input InputStream
	 * @return Map
	 */
	public static Map toMap(InputStream input) {
		try {
			return new ObjectMapper().readValue(input, HashMap.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *  String을 List 로 변환 
	 * 
	 * @param input input String
	 * @return List
	 */
	public static List toList(String input) {
		try {
			return new ObjectMapper().readValue(input, ArrayList.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * InputStream 입력을 List 로 변환
	 * 
	 * @param input InputStream
	 * @return List
	 */
	public static List toList(InputStream input) {
		try {
			return new ObjectMapper().readValue(input, ArrayList.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
