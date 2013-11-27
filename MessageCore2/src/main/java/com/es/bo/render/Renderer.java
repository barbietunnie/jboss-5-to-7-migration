package com.es.bo.render;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

import com.es.data.constant.Constants;
import com.es.data.constant.VariableType;
import com.es.exception.DataValidationException;

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
 * variables defined within the table section should be provided by calling program
 * with many values.<br>
 * <pre>
 * For example: a table section is defined as following:
 * ${TABLE_SECTION_BEGIN} RowTitle - ${var1} RowValue - ${var2} ${TABLE_SECTION_END}
 * 
 * and the calling program provides the following:
 * An array object contains the following two maps:
 *   1. Map one contains variables var1, var2 and their values Title1 and Value1
 *   2. Map two contains variables var1, var2 and their values Title2 and Value2
 *   
 * The rendered table would look like:
 *  RowTitle - Title1 RowValue - Value1
 *  RowTitle - Title2 RowValue - Value2
 * </pre>
 * <p>
 * A template could also contain one or more "Optional Sections". If a match could not
 * be found for any variable within an optional section, the variable is ignored and
 * is replaced by a blank.
 */
public final class Renderer implements java.io.Serializable {
	private static final long serialVersionUID = -583021283225176808L;
	static final Logger logger = Logger.getLogger(Renderer.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	final static String TableTagBgn="TABLE_SECTION_BEGIN";
	final static String TableTagEnd="TABLE_SECTION_END";
	
	public final static String TableVariableName="TableRows";
	
	final static String OptionalTagBgn="OPTIONAL_SECTION_BEGIN";
	final static String OptionalTagEnd="OPTIONAL_SECTION_END";
	
	final static String openDelimiter=VariableDelimiter.OPEN_DELIMITER;
	final static String closeDelimiter=VariableDelimiter.CLOSE_DELIMITER;
	final static int delimitersLen=openDelimiter.length()+closeDelimiter.length();
	
	final static int MAX_LOOP_COUNT = 10; // maximum depth of recursive variables
	
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
			Map<String, RenderVariable> errors) throws DataValidationException, ParseException {
		return renderTemplate(templateText, variables, errors);
	}

	private String renderTemplate(String templateText, Map<String, RenderVariable> variables,
			Map<String, RenderVariable> errors) throws DataValidationException, ParseException {
		return renderTemplate(templateText, variables, errors, false);
	}

	private String renderTemplate(String templateText, Map<String, RenderVariable> variables,
			Map<String, RenderVariable> errors, boolean isOptionalSection)
			throws DataValidationException, ParseException {
		return renderTemplate(templateText, variables, errors, isOptionalSection, 0);
	}

	@SuppressWarnings("unchecked")
	private String renderTemplate(String templateText, Map<String, RenderVariable> variables,
			Map<String, RenderVariable> errors, boolean isOptionalSection, int loopCount)
			throws DataValidationException, ParseException {
		
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
		while ((varProps = getVariableName(templateText, currPos)) != null) {
			logger.info("varname:" + varProps.name + ", bgnPos:" + varProps.bgnPos + ", endPos:"
					+ varProps.endPos);
			sb.append(templateText.substring(currPos, varProps.bgnPos));
			if (OptionalTagBgn.equals(varProps.name)) { // optional section
				int optEndPos = getEndTagPosition(templateText, varProps.endPos);
				if (optEndPos < varProps.endPos) {
					RenderVariable req = buildErrorRecord(varProps.name, "" + varProps.bgnPos,
							OptionalTagEnd + " Missing");
					errors.put(req.getVariableName(), req);
					break;
				}
				String optTmplt = templateText.substring(varProps.bgnPos + OptionalTagBgn.length()
						+ delimitersLen, optEndPos);
				varProps.endPos = optEndPos + OptionalTagEnd.length() + delimitersLen;
				logger.info("Optional Section <" + optTmplt + ">");
				sb.append(renderTemplate(optTmplt, variables, errors, true, loopCount));
			}
			else if (TableTagBgn.equals(varProps.name)) { // table section
				int tableEndPos = templateText.indexOf(openDelimiter + TableTagEnd + closeDelimiter,
						varProps.endPos);
				if (tableEndPos < varProps.endPos) {
					RenderVariable req = buildErrorRecord(varProps.name, "" + varProps.bgnPos,
							TableTagEnd + " Missing");
					errors.put(req.getVariableName(), req);
					break;
				}
				String arrayRow = templateText.substring(varProps.bgnPos + TableTagBgn.length()
						+ delimitersLen, tableEndPos);
				varProps.endPos = tableEndPos + TableTagEnd.length() + delimitersLen;
				logger.info("Table Row <" + arrayRow + ">");
				if (variables != null) {
					RenderVariable r = (RenderVariable) variables.get(TableVariableName);
					if (r != null && r.getVariableValue() != null
							&& VariableType.COLLECTION.equals(r.getVariableType())
							&& r.getVariableValue() instanceof Collection) {
						Collection<Map<String, RenderVariable>> c = (Collection<Map<String, RenderVariable>>) r.getVariableValue();
						for (Iterator<Map<String, RenderVariable>> it = c.iterator(); it.hasNext();) {
							Map<String, RenderVariable> row = new HashMap<String, RenderVariable>();
							row.putAll(variables); // add main variables first
							row.putAll(it.next());
							sb.append(renderTemplate(arrayRow, row, errors, isOptionalSection));
						}
					}
					else {
						RenderVariable req = buildErrorRecord(varProps.name, TableVariableName,
								"VarblValue is not a Collection for a Table");
						errors.put(req.getVariableName(), req);
					}
				}
			}
			else if (variables.get(varProps.name) != null) { // main section
				RenderVariable r = (RenderVariable) variables.get(varProps.name);
				if (VariableType.TEXT.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						if (getVariableName((String) r.getVariableValue(), 0) != null) {
							// recursive variable
							// this variable contains other variable(s), render it
							if (loopCount <= MAX_LOOP_COUNT) // check infinite loop
								sb.append(renderTemplate((String) r.getVariableValue(), variables,
										errors, false, ++loopCount));
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
						if (r.getVariableValue() instanceof Integer) {
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
					if (r.getVariableValue() == null) { // default to now
						sb.append(fmt.format(new java.util.Date()));
					}
					else {
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
				RenderVariable req = buildErrorRecord(varProps.name, "" + varProps.bgnPos,
						"Variable Name could not be resolved.");
				errors.put(req.getVariableName(), req);
				sb.append(openDelimiter + varProps.name + closeDelimiter);
			}
			// advance to next position
			currPos = varProps.endPos;
		}
		sb.append(templateText.substring(currPos));
		return sb.toString();
	}
	
	private RenderVariable buildErrorRecord(String name,String value, String error) {
		RenderVariable req = new RenderVariable(
			name, 
			value, 
			null, 
			VariableType.TEXT, 
			"Y", 
			null, 
			error
			);
		return req;
	}
	
	private VarProperties getVariableName(String text, int pos) throws DataValidationException {
		VarProperties varProps = new VarProperties();
		int nextPos;
		if ((varProps.bgnPos = text.indexOf(openDelimiter, pos)) >= 0) {
			if ((nextPos = text.indexOf(closeDelimiter, varProps.bgnPos + openDelimiter.length())) > 0) {
				varProps.endPos = nextPos + closeDelimiter.length();
				varProps.name = text.substring(varProps.bgnPos + openDelimiter.length(), nextPos);
				return varProps;
			}
			else {
				throw new DataValidationException("Missing the Closing Delimiter from position " + varProps.bgnPos);
			}
		}
		return null;
	}

	private int getEndTagPosition(String text, int pos) {
		int count = 1;
		int pos1, pos2;
		do {
			pos1 = text.indexOf(openDelimiter + OptionalTagBgn + closeDelimiter, pos + delimitersLen);
			pos2 = text.indexOf(openDelimiter + OptionalTagEnd + closeDelimiter, pos + delimitersLen);
			if (pos1 > 0 && pos1 < pos2) {
				count++;
				pos = pos1;
			}
			else {
				count--;
				pos = pos2;
			}
			if (count == 0)
				break;
		} while (pos1 >= 0 || pos2 >= 0);
		return pos2;
	}

	/*
	 * Curly braces are encoded in URL as "%7B" and "%7D". This method convert
	 * them back to "{" and "}".
	 */
	static String convertUrlBraces(String text) {
		//Sample input: "Web Beacon<img src='http://localhost/es/wsmopen.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BListId%7D' width='1' height='1' alt=''>"
		String regex = "\\$\\%7B(.{1," + VariableDelimiter.VARIABLE_NAME_LENGTH + "}?)\\%7D";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "\\$\\{" + m.group(1)+"\\}");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static void main(String[] atgv) {
		//String contentType = "text/plain";
		String tmplt="BeginTemplate\n"
			+ "Current Date: ${CurrentDate}\n"
			+ "${name1}${name2} Some Text ${name3}More Text\n"
			+ "Some Numberic values: ${numeric1}   ${numeric2}\n"
			+ "Some Datetime values: ${datetime1}  ${datetime2}  ${datetime3}\n"
			+ "Some Email Addresses: ${address1}  ${address2}\n"
			+ "${TABLE_SECTION_BEGIN}TableRowBegin <${name2}> TableRowEnd\n"
			+ "${TABLE_SECTION_END}text\n"
			+ "${OPTIONAL_SECTION_BEGIN}Level 1-1 ${name1}\n"
			+ "${OPTIONAL_SECTION_BEGIN}Level 2-1\n${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_BEGIN}Level 2-2${dropped}\n${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_BEGIN}Level 2-3${name2}\n${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_END}"
			+ "${OPTIONAL_SECTION_BEGIN}Level 1-2\n${OPTIONAL_SECTION_END}"
			+ "${name4}\n"
			+ "$EndTemplate\n";
		
		Map<String, RenderVariable> map=new HashMap<String, RenderVariable>();
		
		RenderVariable currentDate = new RenderVariable(
				"CurrentDate", 
				null, 
				"yyyy-MM-dd", 
				VariableType.DATETIME, 
				"Y",
				"N", 
				null
			);
		map.put(currentDate.getVariableName(), currentDate);
			
		RenderVariable req1 = new RenderVariable(
				"name1", 
				"Jack Wang", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req2 = new RenderVariable(
				"name2", 
				"Rendered User2", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Rendered User3", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req4 = new RenderVariable(
				"name4", 
				"Recursive Variable ${name1} End", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req5 = new RenderVariable(
				"name5", 
				"Rendered User5", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"numeric1", 
				"12345.678", 
				null, 
				VariableType.NUMERIC, 
				"Y",
				"N", 
				null
			);
		
		RenderVariable req6_2 = new RenderVariable(
				"numeric2", 
				"-12345.678",
				"000,000,000.0#;(-000,000,000.0#)",
				VariableType.NUMERIC, 
				"Y",
				"N", 
				null
			);
		
		RenderVariable req7_1 = new RenderVariable(
				"datetime1", 
				"2007-10-01 15:23:12",
				null,  // default format
				VariableType.DATETIME, 
				"Y",
				"N", 
				null
			);
		
		RenderVariable req7_2 = new RenderVariable(
				"datetime2", 
				"12/01/2007", 
				"MM/dd/yyyy", // custom format
				VariableType.DATETIME,
				"Y",
				"N", 
				null
			);
		
		RenderVariable req7_3 = new RenderVariable(
				"datetime3", 
				null, // use current time
				"yyyy-MM-dd:hh.mm.ss a", // custom format
				VariableType.DATETIME,
				"Y",
				"N", 
				null
			);
		
		RenderVariable req8_1 = new RenderVariable(
				"address1", 
				"str.address@legacytojava.com",
				null,
				VariableType.ADDRESS,
				"Y",
				"N", 
				null
			);
		map.put("address1", req8_1);
		
		try {
			RenderVariable req8_2 = new RenderVariable(
					"address2", 
					new InternetAddress("inet.address@legacytojava.com"),
					null,
					VariableType.ADDRESS,
					"Y",
					"N", 
					null
				);
			map.put("address2", req8_2);
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		
		// build a Collection for Table
		RenderVariable req2_row1 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 1", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		RenderVariable req2_row2 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 2", 
				null, 
				VariableType.TEXT, 
				"Y",
				"N", 
				null
			);
		ArrayList<Map<String, RenderVariable>> collection = new ArrayList<Map<String, RenderVariable>>();
		Map<String, RenderVariable> row1 = new HashMap<String, RenderVariable>(); // a row
		row1.put(req2.getVariableName(), req2_row1);
		row1.put(req3.getVariableName(), req3);
		collection.add(row1);
		Map<String, RenderVariable> row2 = new HashMap<String, RenderVariable>(); // a row
		row2.put(req2.getVariableName(), req2_row2);
		row2.put(req3.getVariableName(), req3);
		collection.add(row2);
		RenderVariable array = new RenderVariable(
				TableVariableName, 
				collection, 
				null, 
				VariableType.COLLECTION, 
				"Y",
				"N", 
				null
			);
		// end of Collection
		
		map.put("name1", req1);
		map.put("name2", req2);
		map.put("name3", req3);
		map.put("name4", req4);
		map.put("name5", req5);
		map.put("numeric1", req6_1);
		map.put("numeric2", req6_2);
		map.put("datetime1", req7_1);
		map.put("datetime2", req7_2);
		map.put("datetime3", req7_3);
		map.put(TableVariableName, array);

		Renderer tmp=new Renderer();
		try {
			Map<String, RenderVariable> errors = new HashMap<String, RenderVariable>();
			String text = tmp.render(tmplt, map, errors);
			logger.info("++++++++++ Rendered Text++++++++++\n" + text);
			if (!errors.isEmpty()) {
				logger.info("Display Error Variables..........");
				Set<String> set = errors.keySet();
				for (Iterator<String> it=set.iterator(); it.hasNext();) {
					String key = it.next();
					RenderVariable req = (RenderVariable) errors.get(key);
					logger.info(req.toString());
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		System.exit(0);
	}
}
