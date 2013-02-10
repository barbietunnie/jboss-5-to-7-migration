package jpa.variable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RenderVariable implements Serializable {
	private static final long serialVersionUID = -1784984311808865823L;
	public final static String TEXT = "T";
	public final static String NUMERIC = "N";
	public final static String DATETIME = "D";
	final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	final static String LF = System.getProperty("line.separator", "\n");

	private final String variableName;
	private final Object variableValue;
	// Data Type: T - Text, N - Numeric, D - Date time.
	private final String variableType;
	// Data Format, used by Numeric and Date time data types.
	private final String variableFormat;

	/**
	 * Define a text variable.
	 * @param variableName - name
	 * @param variableValue - value
	 */
	public RenderVariable(String variableName, String variableValue) {
		this(variableName, variableValue, TEXT);
	}

	/**
	 * Define a variable with specified type and default format.
	 * <ul>Variable Type vs. Variable Value:
	 * <li>Text - String</li>
	 * <li>Numeric - String or BigDecimal</li>
	 * <li>DateTime - String or java.util.Date</li>
	 * </ul>
	 * @param variableName - name
	 * @param variableValue - value
	 * @param variableType - type
	 */
	public RenderVariable(String variableName, Object variableValue, String variableType) {
		this(variableName, variableValue, variableType, null);
	}

	/**
	 * Define a variable with specified type and format.
	 * <ul>Variable Type vs. Variable Value:
	 * <li>Text - String</li>
	 * <li>Numeric - String or BigDecimal</li>
	 * <li>DateTime - String or java.util.Date</li>
	 * </ul>
	 * @param variableName - name
	 * @param variableValue - value
	 * @param variableType - type
	 * @param variableFormat - ignored for Text type.
	 */
	public RenderVariable(String variableName, Object variableValue, String variableType,
			String variableFormat) {

		this.variableName = variableName;
		this.variableValue = variableValue;
		this.variableType = variableType;
		this.variableFormat = variableFormat;

		if (NUMERIC.equals(variableType)) {
			if (variableFormat != null) {
				new DecimalFormat(variableFormat); // validate the format
			}
			if (variableValue != null) {
				if (!(variableValue instanceof BigDecimal) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
				if (variableValue instanceof String) {
					NumberFormat numberFormat = NumberFormat.getNumberInstance();
					try {
						numberFormat.parse((String) variableValue);
					}
					catch (ParseException e) {
						throw new IllegalArgumentException("Invalid Numeric Value: " + variableValue
								+ ", for " + variableName);
					}
				}
			}
		}
		else if (DATETIME.equals(variableType)) {
			SimpleDateFormat fmt = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
			if (variableFormat != null) {
				fmt.applyPattern(variableFormat); // validate the format
			}
			if (variableValue != null) {
				if (!(variableValue instanceof Date) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
				if (variableValue instanceof String) {
					try {
						fmt.parse((String) variableValue);
					}
					catch (ParseException e) {
						throw new IllegalArgumentException("Invalid DateTime Value: " + variableValue
								+ ", for " + variableName);
					}
				}
			}
		}
		else if (TEXT.equals(variableType)) {
			if (variableValue != null) {
				if (!(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
			}
		}
		else {
			throw new IllegalArgumentException("Invalid Variable Type: " + variableType + ", for "
					+ variableName);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("========== RenderVariable Fields ==========" + LF);
		sb.append("VariableName:   " + variableName + LF);
		sb.append("VariableValue:  " + (variableValue == null ? "null" : variableValue) + LF);
		sb.append("VariableType:   " + variableType + LF);
		sb.append("VariableFormat: " + variableFormat + LF);
		return sb.toString();
	}

	public String getVariableType() {
		return variableType;
	}

	public Object getVariableValue() {
		return variableValue;
	}

	public String getVariableFormat() {
		return variableFormat;
	}

	public String getVariableName() {
		return variableName;
	}
}
