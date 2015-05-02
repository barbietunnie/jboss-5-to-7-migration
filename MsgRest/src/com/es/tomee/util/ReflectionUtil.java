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
		Map<String, Method> settersMap = new LinkedHashMap<String, Method>();
		Method methods[] = obj.getClass().getMethods();
		
		// retrieve method setters
		for (int i = 0; i< methods.length; i++) {
			String methodName = methods[i].getName();
			if (Modifier.isPublic(methods[i].getModifiers())
					&& !Modifier.isStatic(methods[i].getModifiers())
					&& (methodName.length() > 3 && methodName.startsWith("set"))) {
				Method method = methods[i];
				if (method.getParameterTypes().length == 1) {
					settersMap.put(methodName, method);
					//logger.info("Method setter added: " + methodName);
				}
			}
		}
		
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			String value = formParams.get(key);
			//logger.info("Form Key: " + key + ", Values: " + formParams.get(key));
			String setterName = "set" + WordUtils.capitalize(key);
			//logger.info("Method name constructed: " + methodName);
			if (settersMap.containsKey(setterName)) {
				Method setter = settersMap.get(setterName);
				Class<?> paramType = setter.getParameterTypes()[0];
				if (paramType.isAssignableFrom(String.class)) {
					String setDisp =  obj.getClass().getSimpleName() + "." + setterName + "(\"" + value + "\")";
					try {
						logger.info("Executing: " + setDisp);
						setter.invoke(obj, value);
					} catch (Exception e) {
						logger.error("Failed to execute: " + setDisp, e);
					}
				}
				else if ("int".equals(paramType.getName()) || paramType.isAssignableFrom(Integer.class)) {
					String setDisp =  obj.getClass().getSimpleName() + "." + setterName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setDisp);
							setter.invoke(obj, Integer.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setDisp, e);
						}
					}
				}
				else if ("boolean".equals(paramType.getName()) || paramType.isAssignableFrom(Boolean.class)) {
					String setDisp =  obj.getClass().getSimpleName() + "." + setterName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setDisp);
							setter.invoke(obj, Boolean.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setDisp, e);
						}
					}
				}
				else if (paramType.isAssignableFrom(java.sql.Timestamp.class)) {
					String setDisp =  obj.getClass().getSimpleName() + "." + setterName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setDisp);
							setter.invoke(obj, java.sql.Timestamp.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setDisp, e);
						}
					}
				}
			}
		}
	}

	public static void copyProperties(Object dest, Object orig) {
		Map<String, Method> gettersDestMap = new LinkedHashMap<String, Method>();
		Map<String, Method> settersDestMap = new LinkedHashMap<String, Method>();
		Map<String, Method> gettersOrigMap = new LinkedHashMap<String, Method>();
		Method destMethods[] = dest.getClass().getDeclaredMethods();
		
		// retrieve method getters and setters
		for (Method destMethod : destMethods) {
			String methodName = destMethod.getName();
			if (Modifier.isPublic(destMethod.getModifiers())
					&& !Modifier.isStatic(destMethod.getModifiers())) {
				if (methodName.length() > 3 && methodName.startsWith("get")) {
					try {
						Method origMethod = orig.getClass().getMethod(methodName, destMethod.getParameterTypes());
						if (!destMethod.getReturnType().equals(origMethod.getReturnType())
								|| !Modifier.isPublic(origMethod.getModifiers())) {
							continue;
						}
						if (destMethod.getParameterTypes().length == 0) {
							gettersDestMap.put(methodName, destMethod);
							gettersOrigMap.put(methodName, origMethod);
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
						Method origMethod = orig.getClass().getMethod(methodName, destMethod.getParameterTypes());
						if (!destMethod.getReturnType().equals(origMethod.getReturnType())) {
							continue;
						}
						if (destMethod.getParameterTypes().length == 1) {
							settersDestMap.put(methodName, destMethod);
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
		// loop through getters and copy data from orig to dest if their values are different
		for (Iterator<String> it=gettersDestMap.keySet().iterator(); it.hasNext();) {
			String getterName = it.next();
			String setterName = getterName.replaceFirst("get", "set");
			if (!settersDestMap.containsKey(setterName)) {
				continue;
			}
			Method destMethod = gettersDestMap.get(getterName);
			Method origMethod = gettersOrigMap.get(getterName);
			Method destSetter = settersDestMap.get(setterName);
			try {
				Object destRst = destMethod.invoke(dest, params);
				Object origRst = origMethod.invoke(orig, params);
				String setDisp = destSetter.getClass().getSimpleName() + "." + setterName + "(" + origRst + ")";
				if (destRst == null) {
					if (origRst != null) {
						logger.info("Invoking.1 " + setDisp);
						destSetter.invoke(dest, origRst);
					}
				}
				else if (!destRst.equals(origRst)) {
					logger.info("Invoking.2 " + setDisp);
					destSetter.invoke(dest, origRst);
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
		copyProperties(ml, vo);
	}
}
