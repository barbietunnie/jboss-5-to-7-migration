package com.es.bo.render;
	
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.VariableType;
import com.es.data.preload.GlobalVariableEnum;

public class RenderVariable implements Serializable {
	private static final long serialVersionUID = 6995211477252194373L;

	private final String variableName;
	
	// N - Long or String, D - Date of String, A - Address or String
	// L - String or byte[], C - Collection
	private Object variableValue;
	
	// used for Type: N/D/L, for L: Mime Type(include name/content type, etc)
	private final String variableFormat; // TODO: use enum
	
	// T - text, N - numeric, D - Datetime, A - address, X - X header, 
	// L - LOB(Attachment), C - Collection
	private final VariableType variableType;
	private final String allowOverride;
	private final String required;
	private String errorMsg;
    
	/**
	 * Instantiate a RenderVariable with variable name and value.
	 * @param variableName
	 * @param variableValue
	 */
	public RenderVariable(String variableName, Object variableValue) {
		this(variableName, variableValue, null, VariableType.TEXT,
				CodeType.YES_CODE.getValue(), CodeType.NO_CODE.getValue(), null);
	}
	
	/**
	 * Instantiate a RenderVariable with variable name, value, and type.
	 * @param variableName
	 * @param variableValue
	 * @param variableType
	 */
	public RenderVariable(String variableName, Object variableValue,
			VariableType variableType) {
		this(variableName, variableValue, null, variableType,
				CodeType.YES_CODE.getValue(), CodeType.NO_CODE.getValue(), null);
	}
	
	/**
	 * Instantiate a RenderVariable with variable name, value, type, and format.
	 * @param variableName
	 * @param variableValue
	 * @param variableType
	 * @param variableFormat
	 */
	public RenderVariable(String variableName, Object variableValue,
			VariableType variableType, String variableFormat) {
		this(variableName, variableValue, variableFormat, variableType,
				CodeType.YES_CODE.getValue(), CodeType.NO_CODE.getValue(), null);
	}
	
	/**
	 * Instantiate a RenderVariable.
	 * @param variableName
	 * @param variableValue
	 * @param variableFormat
	 * @param variableType
	 * @param allowOverride
	 * @param required
	 * @param errorMsg
	 */
    public RenderVariable(
			String variableName,
			Object variableValue,
			String variableFormat,
			VariableType variableType,
			String allowOverride,
			String required,
			String errorMsg) {
    	
		this.variableName=variableName;
		this.variableValue=variableValue;
		this.variableFormat=variableFormat;
		this.variableType=variableType;
		this.allowOverride=allowOverride;
		this.required=required;
		this.errorMsg=errorMsg;
		
		if (VariableType.NUMERIC.equals(variableType)) {
			if (variableFormat!=null) {
				new DecimalFormat(variableFormat);
			}
			if (variableValue!=null) {
				if (!(variableValue instanceof Long)
						&& !(variableValue instanceof String)
						&& !(variableValue instanceof Integer)
						&& !(variableValue instanceof BigDecimal)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", by " + variableName);
				}
				if (variableValue instanceof String) {
					NumberFormat numberFormat = NumberFormat.getNumberInstance();
					try {
						numberFormat.parse((String)variableValue);
					}
					catch (ParseException e) {
						throw new IllegalArgumentException("Invalid Numeric Value: "
								+ variableValue + ", by " + variableName);
					}
				}
			}
		}
		else if (VariableType.DATETIME.equals(variableType)) {
			SimpleDateFormat fmt = new SimpleDateFormat(Constants.DEFAULT_DATETIME_FORMAT);
			if (variableFormat!=null) {
				fmt.applyPattern(variableFormat); // validate the format
			}
			if (variableValue == null) { // populate "CurrentDateTime"
				if (GlobalVariableEnum.CurrentDateTime.name().equals(variableName)
						|| GlobalVariableEnum.CurrentDate.name().equals(variableName)
						|| GlobalVariableEnum.CurrentTime.name().equals(variableName)) {
					this.variableValue = variableValue = new Date();
				}
			}
			if (variableValue!=null) {
				if (!(variableValue instanceof Date) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
				if (variableValue instanceof String) {
					try {
						fmt.parse((String)variableValue);
					}
					catch (ParseException e) {
						throw new IllegalArgumentException("Invalid DateTime Value: "
								+ variableValue + ", by " + variableName);						
					}
				}
			}
		}
		else if (VariableType.ADDRESS.equals(variableType)) {
			if (variableValue!=null) {
				if (!(variableValue instanceof Address) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
				if (variableValue instanceof String) {
					try {
						InternetAddress.parse((String)variableValue);
					}
					catch (AddressException e) {
						throw new IllegalArgumentException("Invalid Email Address: "
								+ variableValue + ", by " + variableName);
					}
				}
			}
		}
		else if (VariableType.TEXT.equals(variableType) || VariableType.X_HEADER.equals(variableType)) {
			if (variableValue != null) {
				if (!(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
			}
		}
		else if (VariableType.LOB.equals(variableType)) {
			if (variableValue!=null) {
				if (!(variableValue instanceof String) && !(variableValue instanceof byte[])) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
			}
			if (variableFormat==null) {
				throw new IllegalArgumentException(
						"VariableFormat must be provided for LOB variable, by " + variableName);
			}
		}
		else if (VariableType.COLLECTION.equals(variableType)) {
			if (variableValue!=null) {
				if (!(variableValue instanceof Collection)) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
			}
		}
		else {
			throw new IllegalArgumentException("Invalid Variable Type: " + variableType + ", for "
					+ variableName);
		}
   }
	
	public String toString() {
		String LF = System.getProperty("line.separator","\n");
		StringBuffer sb = new StringBuffer();
		sb.append("========== Display RenderVariable Fields =========="+LF);
		sb.append("VariableName:   "+variableName+LF);
		sb.append("VariableValue:  "+(variableValue==null?"null":variableValue.toString())+LF);
		sb.append("VariableFormat: "+variableFormat+LF);
		sb.append("VariableType:   "+variableType.getValue()+LF);
		sb.append("AllowOverride:  "+allowOverride+LF);
		sb.append("Required:       "+required+LF);
		sb.append("ErrorMsg:       "+errorMsg+LF);
		return sb.toString();
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getAllowOverride() {
		return allowOverride;
	}

	public String getRequired() {
		return required;
	}

	public String getVariableFormat() {
		return variableFormat;
	}

	public String getVariableName() {
		return variableName;
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public Object getVariableValue() {
		return variableValue;
	}
}
