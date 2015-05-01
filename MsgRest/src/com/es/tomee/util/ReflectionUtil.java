package com.es.tomee.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import jpa.model.EmailAddress;
import jpa.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import com.es.ejb.ws.vo.MailingListVo;

public class ReflectionUtil {
	protected final static Logger logger = Logger.getLogger(ReflectionUtil.class);

	/*
	 * update the object from form parameters by invoking its setters using reflection.
	 */
	public static void updateObject(Object obj, MultivaluedMap<String, String> formParams) {
		Map<String, String> methodMap = new LinkedHashMap<String, String>();
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			List<String> values = formParams.get(key);
			methodMap.put(key, StringUtils.join(values.toArray()));
		}
		updateObject(obj, methodMap);
	}
	
	public static void updateObject(Object obj, Map<String, String> formParams) {
		Map<String, Method> methodMap = new LinkedHashMap<String, Method>();
		Method methods[] = obj.getClass().getMethods();
		
		// retrieve method setters
		for (int i = 0; i< methods.length; i++) {
			String methodName = methods[i].getName();
			if (Modifier.isPublic(methods[i].getModifiers())
					&& !Modifier.isStatic(methods[i].getModifiers())
					&& (methodName.length() > 3 && methodName.startsWith("set"))) {
				Method method = methods[i];
				if (method.getParameterTypes().length == 1) {
					methodMap.put(methodName, method);
					//logger.info("Method setter added: " + methodName);
				}
			}
		}
		
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			String value = formParams.get(key);
			//logger.info("Form Key: " + key + ", Values: " + formParams.get(key));
			String methodName = "set" + WordUtils.capitalize(key);
			//logger.info("Method name constructed: " + methodName);
			if (methodMap.containsKey(methodName)) {
				Method method = methodMap.get(methodName);
				Class<?> paramType = method.getParameterTypes()[0];
				if (paramType.isAssignableFrom(String.class)) {
					String setter =  obj.getClass().getSimpleName() + "." + methodName + "(\"" + value + "\")";
					try {
						logger.info("Executing: " + setter);
						method.invoke(obj, value);
					} catch (Exception e) {
						logger.error("Failed to execute: " + setter, e);
					}
				}
				else if ("int".equals(paramType.getName()) || paramType.isAssignableFrom(Integer.class)) {
					String setter =  obj.getClass().getSimpleName() + "." + methodName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setter);
							method.invoke(obj, Integer.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setter, e);
						}
					}
				}
				else if ("boolean".equals(paramType.getName()) || paramType.isAssignableFrom(Boolean.class)) {
					String setter =  obj.getClass().getSimpleName() + "." + methodName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setter);
							method.invoke(obj, Boolean.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setter, e);
						}
					}
				}
				else if (paramType.isAssignableFrom(java.sql.Timestamp.class)) {
					String setter =  obj.getClass().getSimpleName() + "." + methodName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setter);
							method.invoke(obj, java.sql.Timestamp.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setter, e);
						}
					}
				}
			}
		}
	}

	static void fieldsDiff(Object dest, Object orig) {
		Map<String, Method> method1GetMap = new LinkedHashMap<String, Method>();
		Map<String, Method> method1SetMap = new LinkedHashMap<String, Method>();
		Map<String, Method> method2GetMap = new LinkedHashMap<String, Method>();
		Method methods[] = dest.getClass().getDeclaredMethods();
		
		// retrieve method getters
		for (Method method1 : methods) {
			String methodName = method1.getName();
			if (Modifier.isPublic(method1.getModifiers())
					&& !Modifier.isStatic(method1.getModifiers())) {
				if (methodName.length() > 3 && methodName.startsWith("get")) {
					try {
						Method method2 = orig.getClass().getMethod(methodName, method1.getParameterTypes());
						if (!method1.getReturnType().equals(method2.getReturnType())) {
							continue;
						}
						if (method1.getParameterTypes().length == 0) {
							method1GetMap.put(methodName, method1);
							method2GetMap.put(methodName, method2);
							logger.info("Method getter added: " + methodName);
						}
					}
					catch (Exception e) {
						//logger.info("Method name obj1." + methodName + " not found in obj2, ignored.");
						continue;
					}
				}
				else if (methodName.length() > 3 && methodName.startsWith("set")) {
					try {
						Method method2 = orig.getClass().getMethod(methodName, method1.getParameterTypes());
						if (!method1.getReturnType().equals(method2.getReturnType())) {
							continue;
						}
						if (method1.getParameterTypes().length == 1) {
							method1SetMap.put(methodName, method1);
							logger.info("Method setter added: " + methodName);
						}
					}
					catch (Exception e) {
						//logger.info("Method name obj1." + methodName + " not found in obj2, ignored.");
						continue;
					}
				}
			}
		}
		
		Object [] params = {};
		for (Iterator<String> it=method1GetMap.keySet().iterator(); it.hasNext();) {
			String methodName = it.next();
			String setterName = methodName.replaceFirst("get", "set");
			if (!method1SetMap.containsKey(setterName)) {
				continue;
			}
			Method method1 = method1GetMap.get(methodName);
			Method method2 = method2GetMap.get(methodName);
			Method setter = method1SetMap.get(setterName);
			try {
				Object rst1 = method1.invoke(dest, params);
				Object rst2 = method2.invoke(orig, params);
				String setMethod = setter.getClass().getSimpleName() + "." + setterName + "(" + rst2 + ")";
				if (rst1 == null) {
					if (rst2 != null) {
						logger.info("Invoking.1 " + setMethod);
						setter.invoke(dest, rst2);
					}
				}
				else if (!rst1.equals(rst2)) {
					logger.info("Invoking.2 " + setMethod);
					setter.invoke(dest, rst2);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}
	}
	
	public static void main(String[] args) {
		Map<String, String> formMap = new LinkedHashMap<String, String>();
		formMap.put("address", "mynewaddress@test.com");
		formMap.put("updtUserId", "new user");
		formMap.put("bounceCount", "123");
		formMap.put("acceptHtml", "false");
		formMap.put("lastSentTime", "2015-04-30 13:12:34.123456");
		formMap.put("updtTime", "2015-04-30 13:12:34.123456789");
		EmailAddress obj = new EmailAddress();
		updateObject(obj, formMap);
		logger.info(StringUtil.prettyPrint(obj));
		
		// test fieldsDiff
		jpa.model.MailingList ml = new jpa.model.MailingList();
		MailingListVo vo = new MailingListVo();
		ml.setDisplayName("Display name 1");
		vo.setDisplayName("Display Name 2");
		vo.setListId("SMPLLST1");
		fieldsDiff(ml, vo);
	}
}
