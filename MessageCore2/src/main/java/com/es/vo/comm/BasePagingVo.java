package com.es.vo.comm;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class BasePagingVo extends BaseVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -1139494427014770362L;
	static final Logger logger = Logger.getLogger(BasePagingVo.class);
	protected final Set<String> searchFields = new HashSet<String>();
	
	protected abstract void setSearchableFields();
	
	public boolean equalsToSearch(BasePagingVo vo) {
		getLogList().clear();
		if (this == vo) return true;
		if (vo == null) return false;
		String className = this.getClass().getName();
		Method thisMethods[] = this.getClass().getMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = (Method) thisMethods[i];
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (methodName.length() > 3 && methodName.startsWith("get") && params.length == 0) {
				String key = StringUtils.uncapitalize(methodName.substring(3));
				if (!searchFields.contains(key)) {
					continue;
				}
				Method voMethod = null;
				try {
					voMethod = vo.getClass().getMethod(methodName, params);
				}
				catch (NoSuchMethodException e) {
					System.err.println(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
				try {
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if ((returnTypeName.endsWith("java.lang.String"))
							|| (returnTypeName.endsWith("java.lang.Integer"))
							|| (returnTypeName.endsWith("java.lang.Long"))
							|| (returnTypeName.endsWith("java.sql.Timestamp"))
							|| (returnTypeName.endsWith("java.sql.Date"))
							|| (returnType.equals(java.lang.Integer.TYPE))
							|| (returnType.equals(java.lang.Long.TYPE))
							|| (returnType.equals(java.lang.Character.TYPE))
							|| (returnTypeName.endsWith("MsgType"))) {
						Object thisValue = method.invoke((Object)this, (Object[])params);
						Object voValue = voMethod.invoke((Object)vo, (Object[])params);
						if (thisValue == null) {
							if (voValue != null) {
								addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
						else {
							if (!thisValue.equals(voValue)) {
								addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
					}
				}
				catch (Exception e) {
					System.err.println(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
			}
		}
		if (getLogList().size() > 0) return false;
		else return true;
	}
	
	public void printMethodNames() {
		Method thisMethods[] = this.getClass().getDeclaredMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = (Method) thisMethods[i];
			String name = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (name.startsWith("get") && params.length == 0) {
				logger.info("Method Name: " + name + ", Return Type: "
						+ method.getReturnType().getName());
			}
		}
	}
}
