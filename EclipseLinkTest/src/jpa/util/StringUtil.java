/*
 * com/legacytojava/message/util/StringUtil.java
 * 
 * Copyright (C) 2008 Jack W.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package jpa.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public final class StringUtil {
	static final Logger logger = Logger.getLogger(StringUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");

    private StringUtil() {
        // static only
    }

	public static boolean isEmpty(String str) {
		return StringUtils.isEmpty(str);
	}

	/**
	 * trim the input string from the right to the provided length.
	 * 
	 * @param str -
	 *            original string
	 * @param len -
	 *            string size
	 * @return string with maximum size of "len" plus three dots.
	 */
	public static String cutWithDots(String str, int len) {
		if (str == null || str.length() <= len || len < 0)
			return str;
		else if (str.length() > len)
			return str.substring(0, len) + "...";
		else
			return str;
	}

	/**
	 * remove double and single quotes from input string
	 * 
	 * @param data -
	 *            input string
	 * @return string with quotes removed, or null if input is null
	 */
	public static String removeQuotes(String data) {
		if (data == null) return data;
		StringTokenizer st = new StringTokenizer(data, "\"\'");
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens())
			sb.append(st.nextToken());

		return sb.toString();
	}

	/**
	 * Strip off leading and trailing spaces for all String objects in the list
	 * 
	 * @param list -
	 *            a list objects
	 */
	public static void stripAll(ArrayList<Object> list) {
		if (list==null) return;
		for (int i=0; i<list.size(); i++) {
			Object obj = list.get(i);
			if (obj!=null && obj instanceof String)
				list.set(i,((String)obj).trim());
		}
	}

	/**
	 * Strip off leading and trailing spaces for all String objects in an array
	 * 
	 * @param list -
	 *            a list of objects
	 */
	public static void stripAll(Object[] list) {
		if (list==null) return;
		for (int i=0; i<list.length; i++) {
			Object obj = list[i];
			if (obj!=null && obj instanceof String)
				list[i]=((String)obj).trim();
		}
	}

	/**
	 * For String fields defined in the bean class with a getter and a setter,
	 * this method will strip off those fields' leading and trailing spaces.
	 * 
	 * @param obj -
	 *            a bean object
	 */
	public static void stripAll(Object obj) {
		if (obj == null) {
			return;
		}
		Method methods[] = obj.getClass().getDeclaredMethods();
		try {
			Class<?> setParms[] = { Class.forName("java.lang.String") };
			for (int i = 0; i < methods.length; i++) {
				Method method = (Method) methods[i];
				Class<?> parmTypes[] = method.getParameterTypes();
				int mod = method.getModifiers();
				if (Modifier.isPublic(mod) && !Modifier.isAbstract(mod) && !Modifier.isStatic(mod)) {
					if (method.getName().startsWith("get") && parmTypes.length == 0
							&& method.getReturnType().getName().equals("java.lang.String")) {
						// invoke the get method
						String str = (String) method.invoke(obj, (Object[])parmTypes);
						if (str != null) { // trim the string
							String setMethodName = "set" + method.getName().substring(3);
							try {
								Method setMethod = obj.getClass()
										.getMethod(setMethodName, setParms);
								if (setMethod != null) {
									String strParms[] = { str.trim() };
									setMethod.invoke(obj, (Object[])strParms);
								}
							}
							catch (Exception e) {
								// no corresponding set method, ignore.
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.err.println("ERROR: Exception caught during reflection - " + e);
			e.printStackTrace();
		}
	}

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static String prettyPrint(Object obj) {
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
		return prettyPrint(obj, stack, 0, pkgName);
	}

	private static String prettyPrint(Object obj, Stack<Object> stack, int level, String pkgName) {
		if (obj == null) {
		   return ("-null");
		}
		boolean sortByMethodName = true;
		if (level > 10) {
			System.err.println("StringUtil.prettyPrint - has reached nested level of " + level + ", exit.");
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
		stack.push(obj);
		Object [] params = {};
		StringBuffer sb = new StringBuffer();
		HashMap<String, Object> methodMap = new HashMap<String, Object>();
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
							|| (method.getReturnType().equals(Class.forName("java.lang.Integer")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Long")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Short")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Float")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Double")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Boolean")))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))
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
					else if (method.getReturnType().equals(Class.forName("java.util.Date"))
							|| method.getReturnType().equals(Class.forName("java.sql.Date"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(sdf.format(((java.util.Date) rtnObj)));
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Calendar"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(sdf.format(((java.util.Calendar)rtnObj).getTime()));
						}						
					}
					else if (method.getReturnType().equals(Class.forName("java.sql.Timestamp"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							java.sql.Timestamp tms = (java.sql.Timestamp) rtnObj;
							sb.append(sdf.format(tms) + ", Nanos: " + tms.getNanos());
						}						
					}
					else if (method.getReturnType().equals(Class.forName("java.math.BigDecimal"))) {
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
						if (rtnObj.getClass().getName().startsWith(pkgName)) {
							sb.append(prettyPrint(rtnObj, stack, level + 1, pkgName));
						}
						else {
							sb.append((((Class<?>) rtnObj)).getName());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.ArrayList"))
							|| method.getReturnType().equals(Class.forName("java.util.List"))) {
						sb.append("a List");
						List<?> lst = (List<?>) method.invoke(obj, params);
						if (lst != null) {
							for (Iterator<?> it = lst.iterator(); it.hasNext();) {
								Object _obj = it.next();
								if (_obj.getClass().getName().startsWith(pkgName)) {
									sb.append(prettyPrint(_obj, stack, level + 1, pkgName));
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
					else if (method.getName().indexOf("Bean") > 0) {
						// don't try to call the getMapOfBean method - this is
						// used only for printing
						if (method.getName().equals("getMapOfBean")) {
							continue;
						}
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else if (rtnObj.getClass().getName().startsWith(pkgName)) {
							sb.append(prettyPrint(rtnObj, stack, level + 1, pkgName));
						}
					}
					else {
						Class<?> cls = method.getReturnType();
						if (cls.getName().startsWith("[L")) {
							String nm = cls.getComponentType().getCanonicalName();
							if (nm.startsWith(pkgName)) {
								Object[] objs = (Object[])method.invoke(obj, params);
								for (int j=0; objs!=null && j<objs.length; j++) {
									sb.append(prettyPrint(objs[j], stack, level+1, pkgName));
								}
							}
						}
						else {
							String nm = cls.getCanonicalName();
							if (nm.startsWith(pkgName)) {
								sb.append(prettyPrint(method.invoke(obj, params), stack, level + 1, pkgName));
							}
						}
					}
					//sb.append(LF + " ");
				}
				catch (Exception e) {
					//e.printStackTrace();
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
					if (method.getReturnType().equals(Class.forName("java.lang.Boolean"))
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
					//e.printStackTrace();
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

	/**
	 * replace all occurrences of replFrom with replWith in the body string
	 * 
	 * @param body -
	 *            message text
	 * @param replFrom -
	 *            from string
	 * @param replWith -
	 *            with string
	 * @return new string
	 */
	public static String replaceAll(String body, String replFrom, String replWith) {
		return StringUtils.replace(body, replFrom, replWith);
	}

	/**
	 * remove the first occurrence of the given string from body text.
	 * 
	 * @param body -
	 *            original body
	 * @param removeStr -
	 *            string to be removed
	 * @return new body
	 */
	public static String removeStringFirst(String body, String removeStr) {
		return removeString(body, removeStr, false);
	}

	/**
	 * remove the last occurrence of the given string from body text.
	 * 
	 * @param body -
	 *            original body
	 * @param removeStr -
	 *            string to be removed
	 * @return new body
	 */
	public static String removeStringLast(String body, String removeStr) {
		return removeString(body, removeStr, true);
	}

	private static String removeString(String body, String removeStr, boolean removeLast) {
		if (StringUtils.isEmpty(body) || StringUtils.isEmpty(removeStr)) {
			return body;
		}
		int pos = -1;
		if (removeLast) {
			pos = body.lastIndexOf(removeStr);
		}
		else {
			pos = body.indexOf(removeStr);
		}
		if (pos >= 0) {
			body = body.substring(0, pos) + body.substring(pos + removeStr.length());
		}
		return body;
	}

	/**
	 * returns a string of dots with given number of dots.
	 * 
	 * @param level -
	 *            specify number dots to be returned
	 * @return string of dots
	 */
	public static String getDots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++) {
			sb.append(".");
		}
		return sb.toString();
	}

	/**
	 * trim white spaces from the RIGHT side of a string.
	 * 
	 * @param text
	 *            to be trimmed
	 * @return trimmed string
	 */
	public static String trimRight(String text) {
		/*
		 * We could also do this: ("A" + text).trim().substring(1)
		 * but the performance is poor.
		 */
		if (StringUtils.isEmpty(text)) return text; 
		int idx = text.length() - 1;
		while (idx >= 0 && Character.isWhitespace(text.charAt(idx))) {
			idx--;
		}
		if (idx < 0) {
			return "";
		}
		else {
			return text.substring(0, idx + 1);
		}
	}

	/**
	 * Trim the given string with the given trim value.
	 * 
	 * @param string
	 *            The string to be trimmed.
	 * @param trim
	 *            The value to trim the given string off.
	 * @return The trimmed string.
	 */
    public static String trim(String string, String trim) {
        if (StringUtils.isEmpty(trim)) {
            return string;
        }
        if (string == null) {
        	return null;
        }
        int start = 0;
        int end = string.length();
        int length = trim.length();
        while ((start + length) <= end && string.substring(start, start + length).equals(trim)) {
			start += length;
		}
		while ((start + length) <= end && string.substring(end - length, end).equals(trim)) {
			end -= length;
		}
        return string.substring(start, end);
    }

    private static final String ssnRegex = "^(?!000)(?:[0-6]\\d{2}|7(?:[0-6]\\d|7[0-2]))([ -]?)(?!00)\\d{2}\\1(?!0000)\\d{4}$";
    private final static Pattern ssnPattern = Pattern.compile(ssnRegex);
    
    /*
     * Valid SSN: 	078-05-1120 | 078 05 1120 | 078051120
     * Invalid SSN: 987-65-4320 | 000-00-0000 | (555) 555-5555
     */
    public static boolean isValidSSN(String ssn) {
    	if (ssn == null) return false;
    	Matcher matcher = ssnPattern.matcher(ssn);
    	return matcher.matches();
    }
    
    private static final String phoneRegex = "^(?:1[ -]?)?(?:\\(\\d{3}\\)|\\d{3})[ -]?(?:\\d{3}|[a-z]{3})[ -]?(?:\\d{4}|[a-z]{4})$";
    private final static Pattern phonePattern = Pattern.compile(phoneRegex, Pattern.CASE_INSENSITIVE);
    
    /*
     * Matches: 2405525009 | (240)552-5009 | 1(240) 552-5009 | 240 JOE-CELL
     */
    public static boolean isValidPhoneNumber(String phone) {
    	if (phone == null) return false;
    	Matcher matcher = phonePattern.matcher(phone);
    	return matcher.matches();
    }

	public static void main(String[] args) {
		System.out.println(removeStringFirst("<pre>12345abcdefklqhdkh</pre>", "<pre>"));
	}
}
