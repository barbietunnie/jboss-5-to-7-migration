package com.es.tomee.util;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public final class PrintUtil {
	static final Logger logger = Logger.getLogger(PrintUtil.class);
	
	static final String LF = System.getProperty("line.separator", "\n");
	
	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	final static int MAX_LEVELS = 12;
	
	private PrintUtil() {}

	public static String prettyPrint(Object obj) {
		return prettyPrint(obj, 10); // default to maximum 10 levels
	}

	public static String prettyPrint(Object obj, int levels) {
		if (obj == null) {
			return ("-null");
		}
		Pattern pkgPattern = Pattern.compile("(\\w{1,20}\\.\\w{1,20})\\..*");
		Stack<Object> stack = new Stack<Object>();
		String pkgName = obj.getClass().getPackage().getName();
		Matcher m = pkgPattern.matcher(pkgName);
		if (m.matches() && m.groupCount()>=1) {
			pkgName = m.group(1);
		}
		return prettyPrint(obj, stack, 0, pkgName, levels);
	}

	private static String prettyPrint(Object obj, Stack<Object> stack, int level, String pkgName, int levels) {
		if (obj == null) {
		   return ("-null");
		}
		boolean sortByMethodName = true;
		if (level > MAX_LEVELS) {
			System.err.println("PrintUtil.prettyPrint - has reached nested level of " + level + ", exit.");
			return (obj.getClass().getCanonicalName() + " - (more)...");
		}
		if (level > 0) {
			for (Enumeration<Object> enu=stack.elements(); enu.hasMoreElements(); ) {
				Object sobj = enu.nextElement();
				if (obj.getClass().getCanonicalName().equals(sobj.getClass().getCanonicalName())) {
					return (obj.getClass().getCanonicalName() + " - (loop)...");
				}
			}
		}
		if (level >= levels) {
			return "";
		}
		stack.push(obj);
		Object [] params = {};
		StringBuffer sb = new StringBuffer();
		Map<String, Object> methodMap = new HashMap<String, Object>();
		List<String> methodNamelist = new ArrayList<String>();
		Method methods[] = obj.getClass().getMethods();
		
		// sort the attributes by name
		for (int i = 0; i< methods.length; i++) {
			methodMap.put(methods[i].getName(), methods[i]);
			methodNamelist.add(methods[i].getName());
		}
		if (sortByMethodName) {
			Collections.sort(methodNamelist);
		}
		
		if (methodNamelist.size()>0) {
			sb.append(LF + " ");
		}
		for (int i = 0; i < methodNamelist.size(); i++) {
			String methodName = (String) methodNamelist.get(i);
			Method method = (Method) methodMap.get(methodName);
			String paramClassName = obj.getClass().getName();
			paramClassName = paramClassName.substring(paramClassName.lastIndexOf(".") + 1);
		
			//System.err.println(method.getReturnType().getName() + " - " + methodName);
			if ((methodName.length() > 3) && ((methodName.startsWith("get")))) {
				try {
					if (method.getParameterTypes().length > 0) {
						continue;
					}
					sb.append(LF + " ");
					sb.append("     " + dots(level) + paramClassName + "." + methodName.substring(3, 4).toLowerCase() + methodName.substring(4));
					sb.append("=");
					if ((method.getReturnType().equals(Class.forName("java.lang.String")))
							|| (method.getReturnType().isAssignableFrom(Integer.class))
							|| (method.getReturnType().isAssignableFrom(Long.class))
							|| (method.getReturnType().isAssignableFrom(Short.class))
							|| (method.getReturnType().isAssignableFrom(Float.class))
							|| (method.getReturnType().isAssignableFrom(Double.class))
							|| (method.getReturnType().isAssignableFrom(Boolean.class))
							|| (method.getReturnType().equals(java.lang.Integer.TYPE))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
					else if ("int".equals(method.getReturnType().getName())
							|| "short".equals(method.getReturnType().getName())
							|| "long".equals(method.getReturnType().getName())
							|| "double".equals(method.getReturnType().getName())
							|| "boolean".equals(method.getReturnType().getName())) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
					else if (method.getReturnType().isAssignableFrom(java.util.Date.class)
							|| method.getReturnType().isAssignableFrom(java.sql.Date.class)) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(sdf.format(((java.util.Date) rtnObj)));
						}
					}
					else if (method.getReturnType().isAssignableFrom(java.util.Calendar.class)) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(sdf.format(((java.util.Calendar)rtnObj).getTime()));
						}						
					}
					else if (method.getReturnType().isAssignableFrom(java.sql.Timestamp.class)) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							java.sql.Timestamp tms = (java.sql.Timestamp) rtnObj;
							sb.append(sdf.format(tms) + ", Nanos: " + tms.getNanos());
						}						
					}
					else if (method.getReturnType().isAssignableFrom(java.math.BigDecimal.class)) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(((java.math.BigDecimal) rtnObj).floatValue());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.lang.Class"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj!=null) {
							if (rtnObj.getClass().getName().startsWith(pkgName)) {
								sb.append(prettyPrint(rtnObj, stack, level + 1, pkgName, levels));
							}
							else {
								sb.append((((Class<?>) rtnObj)).getName());
							}
						}
					}
					else if (method.getReturnType().isAssignableFrom(java.util.ArrayList.class)
							|| method.getReturnType().isAssignableFrom(java.util.List.class)) {
						sb.append("a List");
						List<?> lst = (List<?>) method.invoke(obj, params);
						if (lst != null) {
							for (Iterator<?> it = lst.iterator(); it.hasNext();) {
								Object _obj = it.next();
								if (_obj.getClass().getName().startsWith(pkgName)) {
									sb.append(prettyPrint(_obj, stack, level + 1, pkgName, levels));
								}
								else {
									if (_obj instanceof java.lang.String) {
										sb.append(LF + " ");
										sb.append("     " + dots(level+1) + _obj.getClass().getCanonicalName());
										sb.append("=" + _obj.toString());
									}
								}
							}
						}
					}
					else if (method.getReturnType()==byte[].class) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj!=null) {
							sb.append(new String((byte[])rtnObj));
						}
						else {
							sb.append("null");
						}
					}
					else {
						Class<?> cls = method.getReturnType();
						if (cls.getName().startsWith("[L")) {
							String nm = cls.getComponentType().getCanonicalName();
							if (nm.startsWith(pkgName)) {
								Object[] objs = (Object[])method.invoke(obj, params);
								for (int j=0; objs!=null && j<objs.length; j++) {
									sb.append(prettyPrint(objs[j], stack, level+1, pkgName, levels));
								}
							}
						}
						else {
							String nm = cls.getCanonicalName();
							if (nm.startsWith(pkgName)) {
								sb.append(prettyPrint(method.invoke(obj, params), stack, level + 1, pkgName, levels));
							}
						}
					}
					//sb.append(LF + " ");
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
					System.err.println("error getting values in toString, method name: " + methodName + ", " + e.getMessage());
				}
			}
			else if ((methodName.length() > 2) && ((methodName.startsWith("is")))) {
				try {
					if (method.getParameterTypes().length > 0) {
						continue;
					}
					sb.append(LF + " ");
					sb.append("     " + dots(level) + paramClassName + "." + methodName);
					sb.append("=");
					if (method.getReturnType().isAssignableFrom(java.lang.Boolean.class)
							|| method.getReturnType().getName().equals("boolean")) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
				}
				catch (Exception e) {
					//logger.error("Exception caught", e);
					System.err.println("error getting values in toString, method name: " + methodName + ", " + e.getMessage());
				}
			}
		}
		stack.pop();
		return sb.toString();
	}

	private static String dots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++)
			sb.append(".");
		return sb.toString();
	}


}
