package com.es.bo.render;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;

import org.apache.log4j.Logger;

import com.es.data.constant.Constants;
import com.es.data.constant.VariableType;
import com.es.exception.TemplateException;

/**
 * A template is a text string with variables defined inside ${ and } tokens.<br>
 * Variables are replaced by their values during rendering process. A value could
 * in turn contain variables, the rendering process will perform recursively until
 * no variables remain in the rendered text.
 * <p>
 * The render method scans template for variables, and for each variable it finds,
 * it looks for its value from a map supplied by the calling program. If a match is
 * found, it replaces the variable with its value. Otherwise it leaves the variable
 * intact, logs an error and continues the scanning.
 * <p>
 * A template could contain a "Table Section". To render many rows on a table, the
 * variables defined within the table section should be provided by the calling
 * program using a collection of maps. Each map in the collection represents a row
 * on the table and the map is keyed by variable names. The calling program should 
 * use a predefined name to pass the collection.<br>
 * <pre>
 * For example: a table section is defined as following:
 * ${TABLE_SECTION_BEGIN} RowTitle - ${var1} RowValue - ${var2} ${TABLE_SECTION_END}
 * 
 * the calling program provides the following:
 * A RenderVariable with name of "TableRows" and a list of following two maps:
 *   1. Map one contains variables var1, var2 and their values Title1 and Value1
 *   2. Map two contains variables var1, var2 and their values Title2 and Value2
 *   
 * The rendered table would look like:
 *  RowTitle - Title1 RowValue - Value1
 *  RowTitle - Title2 RowValue - Value2
 * </pre>
 * <p>
 * A template could also contain one or more "Optional Sections". In order for an
 * optional section to get rendered, all of the variables within the section must be 
 * supplied by the calling program. If a match could not be found for any variable
 * within an optional section, the entire section is replaced by a blank.
 */
public final class Renderer implements java.io.Serializable {
	private static final long serialVersionUID = -1670472296238983560L;
	static final Logger logger = Logger.getLogger(Renderer.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	final static String OpenDelimiter="${";
	final static String CloseDelimiter="}";
	final static int DelimitersLen=OpenDelimiter.length()+CloseDelimiter.length();
	final static int VARIABLE_NAME_LENGTH = 26;

	// Controls how white spaces in variable names are treated.
	private static boolean RemoveWhiteSpacesFromVariableName = false;
	private static boolean TrimWhiteSpacesFromVariableName = true;
	
	final static int MAX_LOOP_COUNT = 10; // maximum depth of recursive variables
	
	final static String TableTagBgn="TABLE_SECTION_BEGIN";
	final static String TableTagEnd="TABLE_SECTION_END";
	
	public final static String TableVariableName="TableRows";
	
	final static String OptionalTagBgn="OPTIONAL_SECTION_BEGIN";
	final static String OptionalTagEnd="OPTIONAL_SECTION_END";
	
	private static Renderer renderer = null;

	private Renderer() {
		// empty constructor
	}

	/** return a instance of Renderer which is a singleton. */
	public static Renderer getInstance() {
		if (renderer == null) {
			renderer = new Renderer();
		}
		return renderer;
	}

	public String render(String templateText, Map<String, RenderVariable> variables,
			Map<String, ErrorVariable> errors) throws ParseException, TemplateException {
		return renderTemplate(templateText, variables, errors, false);
	}

	private String renderTemplate(String templateText, Map<String, RenderVariable> variables,
			Map<String, ErrorVariable> errors, boolean isOptionalSection)
			throws ParseException, TemplateException {
		return renderTemplate(templateText, variables, errors, isOptionalSection, 0);
	}

	private String renderTemplate(String templateText, Map<String, RenderVariable> variables,
			Map<String, ErrorVariable> errors, boolean isOptionalSection, int loopCount)
			throws ParseException, TemplateException {
		
		if (templateText==null) {
			throw new IllegalArgumentException("Template Text must be provided");
		}
		if (variables==null) {
			throw new IllegalArgumentException("A Map for variables must be provided");
		}
		if (errors==null) {
			throw new IllegalArgumentException("A Map for errors must be provided");
		}
		templateText = convertUrlBraces(templateText);
		int currPos = 0;
		StringBuffer sb = new StringBuffer();
		VarProperties varProps;
		while ((varProps = getNextVariable(templateText, currPos)) != null) {
			logger.info("varname:" + varProps.name + ", bgnPos:" + varProps.bgnPos + ", endPos:"
					+ varProps.endPos);
			sb.append(templateText.substring(currPos, varProps.bgnPos));
			// optional section
			if (OptionalTagBgn.equals(varProps.name)) {
				int optlEndPos = getEndTagPosition(templateText, varProps.endPos);
				if (optlEndPos < varProps.endPos) {
					ErrorVariable err = buildErrorRecord(varProps.name, String.valueOf(varProps.bgnPos),
							OptionalTagEnd + " Missing");
					errors.put(err.getVariableName(), err);
					break;
				}
				String optlTmplt = templateText.substring(varProps.bgnPos + OptionalTagBgn.length()
						+ DelimitersLen, optlEndPos);
				varProps.endPos = optlEndPos + OptionalTagEnd.length() + DelimitersLen;
				logger.info("Optional Section <" + optlTmplt + ">");
				sb.append(renderTemplate(optlTmplt, variables, errors, true, loopCount));
			}
			// table section - experimental
			else if (TableTagBgn.equals(varProps.name)) {
				int tableEndPos = templateText.indexOf(OpenDelimiter + TableTagEnd + CloseDelimiter,
						varProps.endPos);
				if (tableEndPos < varProps.endPos) {
					ErrorVariable err = buildErrorRecord(varProps.name, String.valueOf(varProps.bgnPos),
							TableTagEnd + " Missing");
					errors.put(err.getVariableName(), err);
					break;
				}
				String tableRow = templateText.substring(varProps.bgnPos + TableTagBgn.length()
						+ DelimitersLen, tableEndPos);
				varProps.endPos = tableEndPos + TableTagEnd.length() + DelimitersLen;
				logger.info("Table Row <" + tableRow + ">");
				// only one table variable is supported with predefined name.
				RenderVariable r = variables.get(TableVariableName);
				if (r != null && r.getVariableValue() != null
					&& VariableType.COLLECTION.equals(r.getVariableType())) {
					@SuppressWarnings("unchecked")
					Collection<Map<String, RenderVariable>> c = (Collection<Map<String, RenderVariable>>) r.getVariableValue();
					for (Iterator<Map<String, RenderVariable>> it = c.iterator(); it.hasNext();) {
						Map<String, RenderVariable> row = new HashMap<String, RenderVariable>();
						row.putAll(variables); // add main variables first
						row.putAll(it.next());
						sb.append(renderTemplate(tableRow, row, errors, isOptionalSection));
					}
				}
				else {
					if (r == null) {
						ErrorVariable err = buildErrorRecord(TableVariableName, 
								"Position: " + varProps.bgnPos + ", not rendered",
								"Variable name not on Render Variables list.");
						errors.put(err.getVariableName(), err);
					}
					else if (r.getVariableValue()==null) {
						ErrorVariable err = buildErrorRecord(TableVariableName, 
								"Position: " + varProps.bgnPos + ", not rendered",
								"Variable value is null in the Variable instance.");
						errors.put(err.getVariableName(), err);
					}
					else if (!VariableType.COLLECTION.equals(r.getVariableType())) {
						ErrorVariable err = buildErrorRecord(TableVariableName, 
								r.getVariableType().name(),
								"Variable Type is not a Collection for a Table");
						errors.put(err.getVariableName(), err);
					}
				}
			}
			// main section
			else if (variables.get(varProps.name) != null) {
				RenderVariable r = variables.get(varProps.name);
				if (VariableType.TEXT.equals(r.getVariableType())
						|| VariableType.X_HEADER.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						if (getNextVariable((String) r.getVariableValue(), 0) != null) {
							// recursive variable
							// this variable contains other variable(s), render it
							if (loopCount <= MAX_LOOP_COUNT) { // check infinite loop
								sb.append(renderTemplate((String) r.getVariableValue(), variables,
										errors, false, ++loopCount));
							}
						}
						else {
							sb.append(r.getVariableValue());
						}
					}
				}
				else if (VariableType.NUMERIC.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						DecimalFormat formatter = new DecimalFormat();
						if (r.getVariableFormat()!=null) {
							formatter.applyPattern(r.getVariableFormat()) ;
						}
						if (r.getVariableValue() instanceof BigDecimal) {
							sb.append(formatter.format(((BigDecimal) r.getVariableValue()).doubleValue()));
						}
						else if (r.getVariableValue() instanceof Integer) {
							sb.append(formatter.format(((Integer)r.getVariableValue()).intValue()));
						}
						else if (r.getVariableValue() instanceof Long) {
							sb.append(formatter.format(((Long)r.getVariableValue()).longValue()));
						}
						else if (r.getVariableValue() instanceof String) {
							try {
								NumberFormat parser = NumberFormat.getNumberInstance();
								Number number = parser.parse((String)r.getVariableValue());
								sb.append(formatter.format(number));
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								sb.append((String)r.getVariableValue());
							}
						}
					}
				}
				else if (VariableType.DATETIME.equals(r.getVariableType())) {
					SimpleDateFormat fmt = new SimpleDateFormat(Constants.DEFAULT_DATETIME_FORMAT);
					if (r.getVariableFormat() != null) {
						fmt.applyPattern(r.getVariableFormat());
					}
					if (r.getVariableValue() instanceof java.util.Date) {
						sb.append(fmt.format(r.getVariableValue()));
					}
					else {
						if (r.getVariableValue()!=null) {
							try {
								java.util.Date date = fmt.parse((String) r.getVariableValue());
								sb.append(fmt.format(date));
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								sb.append((String) r.getVariableValue());
							}
						}
					}
				}
				else if (VariableType.ADDRESS.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						if (r.getVariableValue() instanceof String) {
							sb.append((String) r.getVariableValue());
						}
						else if (r.getVariableValue() instanceof Address) {
							sb.append(((Address)r.getVariableValue()).toString());
						}
					}
				}
				else { // other data types
					sb.append(r.getVariableValue());
				}
			}
			else if (isOptionalSection) {
				// Render list must provide values for EVERY variable in Optional Section.
				// Otherwise the whole section is removed from returned text.
				return "";
			}
			else { // variable name not on render variables list
				ErrorVariable err = buildErrorRecord(varProps.name,
						"Position: " + varProps.bgnPos + ", not rendered",
						"Variable name not on Render Variables list.");
				errors.put(err.getVariableName(), err);
				sb.append(OpenDelimiter + varProps.name + CloseDelimiter);
			}
			// advance to next position
			currPos = varProps.endPos;
		}
		sb.append(templateText.substring(currPos));
		return sb.toString();
	}
	
	private ErrorVariable buildErrorRecord(String name,String value, String error) {
		ErrorVariable err = new ErrorVariable(
			name, 
			value,
			error
			);
		return err;
	}
	
	private VarProperties getNextVariable(String text, int pos) throws TemplateException {
		VarProperties varProps = new VarProperties();
		int nextPos;
		if ((varProps.bgnPos = text.indexOf(OpenDelimiter, pos)) >= 0) {
			if ((nextPos = text.indexOf(CloseDelimiter, varProps.bgnPos + OpenDelimiter.length())) > 0
					&& (nextPos + OpenDelimiter.length() - varProps.bgnPos) <= VARIABLE_NAME_LENGTH) {
				varProps.endPos = nextPos + CloseDelimiter.length();
				varProps.name = text.substring(varProps.bgnPos + OpenDelimiter.length(), nextPos);
				if (RemoveWhiteSpacesFromVariableName) {
					varProps.name = varProps.name.replaceAll("\\s+","");
				}
				else if (TrimWhiteSpacesFromVariableName) {
					varProps.name = varProps.name.trim();
				}
				if (varProps.name.indexOf(OpenDelimiter) >= 0) {
					throw new TemplateException("Missing the Closing Delimiter from position "
							+ varProps.bgnPos + ": " + OpenDelimiter
							+ varProps.name.substring(0, varProps.name.indexOf(OpenDelimiter)));
				}
				return varProps;
			}
			else {
				int len = varProps.bgnPos + VARIABLE_NAME_LENGTH + OpenDelimiter.length();
				len = text.length() < len ? text.length() : len;
				throw new TemplateException("Missing the Closing Delimiter from position " + varProps.bgnPos
						+ ": " + text.substring(varProps.bgnPos, len));
			}
		}
		return null;
	}

	private int getEndTagPosition(String text, int pos) {
		int count = 1;
		int pos1, pos2;
		do {
			pos1 = text.indexOf(OpenDelimiter + OptionalTagBgn + CloseDelimiter, pos + DelimitersLen);
			pos2 = text.indexOf(OpenDelimiter + OptionalTagEnd + CloseDelimiter, pos + DelimitersLen);
			if (pos1 > 0 && pos1 < pos2) {
				count++;
				pos = pos1;
			}
			else {
				count--;
				pos = pos2;
			}
			if (count == 0) {
				break;
			}
		} while (pos1 >= 0 || pos2 >= 0);
		return pos2;
	}

	/*
	 * Curly braces are encoded in URL as "%7B" and "%7D". This method convert
	 * them back to "{" and "}".
	 */
	static String convertUrlBraces(String text) {
		//Sample input: "Web Beacon<img src='http://localhost/es/wsmopen.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BListId%7D' width='1' height='1' alt=''>"
		String regex = "\\$\\%7B(.{1," + VARIABLE_NAME_LENGTH + "}?)\\%7D";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "\\$\\{" + m.group(1)+"\\}");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static class VarProperties {
		int bgnPos = 0;
		int endPos = 0;
		String name = null;
	}
}
