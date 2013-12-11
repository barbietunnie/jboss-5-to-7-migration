package com.es.bo;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.es.bo.render.ErrorVariable;
import com.es.bo.render.RenderVariable;
import com.es.bo.render.Renderer;
import com.es.bo.render.TableSection;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.VariableType;
import com.es.data.preload.GlobalVariableEnum;

public final class RendererTest {
	static final Logger logger = Logger.getLogger(RendererTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Test
	public void testRenderer() {
		String template = "BeginTemplate\n"
			+ "Current Date: ${CurrentDate} ${0}\n"
			+ "${name1}, ${name2} Some Text ${name3} More Text\n"
			+ "Some Numberic values: ${numeric1}, ${numeric2}, ${numeric3}\n"
			+ "Some Datetime values: ${datetime1}, ${datetime2}, ${datetime3}, ${datetime4}\n"
			+ "${name4.recurrsive}\n"
			+ "$EndTemplate\n";
		
		java.util.Date currDate = new java.util.Date();
		String dateTimeFormat1 = "yyyy-MM-dd";
		SimpleDateFormat fmt = new SimpleDateFormat(dateTimeFormat1);
		String expected = "BeginTemplate\n"
			+ "Current Date: " + fmt.format(currDate) + " ${0}\n"
			+ "Jack Wang, John Smith Some Text Joe Jones More Text\n"
			+ "Some Numberic values: 12,345.678, (-000,012,345.68), -$99,999.99\n"
			+ "Some Datetime values: 2007-10-01 15:23:12, 12/01/2007, , 2009-07-29 13.04\n"
			+ "Recursive Variable Jack Wang End\n"
			+ "$EndTemplate\n";
		
		Map<String, RenderVariable> map = createVariableMap(currDate, dateTimeFormat1);
		
		Renderer renderer = Renderer.getInstance();
		try {
			Map<String, ErrorVariable> errors = new HashMap<String, ErrorVariable>();
			String renderedText = renderer.render(template, map, errors);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Expected Text++++++++++\n" + expected);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, expected);
			assertTrue(!errors.isEmpty());
			if (!errors.isEmpty()) {
				logger.info("Display Error Variables..........");
				Set<String> set = errors.keySet();
				for (Iterator<String> it=set.iterator(); it.hasNext();) {
					String key = (String) it.next();
					ErrorVariable req = (ErrorVariable) errors.get(key);
					logger.info(req.toString());
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testTableSection() {
		String template="BeginTemplate\n"
				+ "Current Date: ${CurrentDate}\n"
				+ "${name1}${name2} Some Text ${name3}More Text\n"
				+ "Some Numberic values: ${numeric1}   ${numeric2}   ${numeric3}\n"
				+ "Some Datetime values: ${datetime1}  ${datetime2}  ${datetime3}\n"
				+ "Some Email Addresses: ${address1}  ${address2}\n"
				+ "${TABLE_SECTION_BEGIN}TableRowBegin <${name2}> TableRowEnd\n"
				+ "${TABLE_SECTION_END}text\n"
				+ "<<<<< Optional Sections Begin\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 1-1 ${name1}\n"
				+ "${OPTIONAL_SECTION_BEGIN}  Level 2-1 No variable in this section\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}  Level 2-2 ${name1} ${SectionDropped} ${name2}\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}  Level 2-3 ${name2}\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}Level 1-2 ${datetime1}\n${OPTIONAL_SECTION_END}"
				+ ">>>>> Optional Sections End.\n"
				+ "${name4}\n"
				+ "$EndTemplate\n";
		Map<String, RenderVariable> map = createVariableMapWithTableSection();
		Renderer renderer = Renderer.getInstance();
		try {
			Map<String, ErrorVariable> errors = new HashMap<String, ErrorVariable>();
			String renderedText = renderer.render(template, map, errors);
			assertNotNull(renderedText);
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
			String currDateStr = "Current Date: " + sdf.format(new java.util.Date());
			assertTrue(StringUtils.contains(renderedText, currDateStr));
			// verify table section
			assertTrue(StringUtils.contains(renderedText, "Rendered User2 - Row 1"));
			assertTrue(StringUtils.contains(renderedText, "Rendered User2 - Row 2"));
			// verify optional section
			assertTrue(StringUtils.contains(renderedText, "Level 1-1 Jack Wang"));
			assertTrue(StringUtils.contains(renderedText, "Level 2-1"));
			assertTrue(!StringUtils.contains(renderedText, "Level 2-2"));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testBadTemplate() {
		Map<String, RenderVariable> map = createVariableMap(new java.util.Date(), "yyyy-MM-dd");
		Renderer renderer = Renderer.getInstance();
		int exceptionCaught = 0;
		
		String template1 = "Missing first closing delimiter\n"
			+ "Some Numberic values: ${numeric1, ${numeric2}, ${numeric3}\n"
			+ "$EndTemplate\n";
		
		Map<String, ErrorVariable> errors = new HashMap<String, ErrorVariable>();
		try {
			renderer.render(template1, map, errors);
		}
		catch (Exception e) {
			assertTrue(e.getMessage().indexOf("${numeric1,") > 0);
			exceptionCaught++;
		}
		
		String template2 = "Missing middle closing delimiter\n"
			+ "Some Numberic values: ${numeric1}, ${numeric2, ${numeric3}\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template2, map, errors);
		}
		catch (Exception e) {
			assertTrue(e.getMessage().indexOf("${numeric2,") > 0);
			exceptionCaught++;
		}
		
		String template3 = "Missing last closing delimiter\n"
			+ "Some Numberic values: ${numeric1}, ${numeric2}, ${numeric3\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template3, map, errors);
			fail();
		}
		catch (Exception e) {
			assertTrue(e.getMessage().indexOf("${numeric3") > 0);
			exceptionCaught++;
		}
		
		assertTrue(exceptionCaught == 3);
	}

	private Map<String, RenderVariable> createVariableMap(java.util.Date currDate, String dateTimeFormat1) {
		Map<String, RenderVariable> map = new HashMap<String, RenderVariable>();
		
		RenderVariable currentDate = new RenderVariable(
				GlobalVariableEnum.CurrentDate.name(), 
				currDate, 
				VariableType.DATETIME,
				dateTimeFormat1
			);
		map.put(currentDate.getVariableName(), currentDate);
		
		RenderVariable req1 = new RenderVariable(
				"name1", 
				"Jack Wang"
			);
		RenderVariable req2 = new RenderVariable(
				"name2", 
				"John Smith"
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Joe Jones"
			);
		RenderVariable req4 = new RenderVariable(
				"name4.recurrsive", 
				"Recursive Variable ${name1} End", 
				VariableType.TEXT
			);
		RenderVariable req5 = new RenderVariable(
				"name5", 
				"Roger Banner", 
				VariableType.TEXT
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"numeric1", 
				"12345.678", // use default format
				VariableType.NUMERIC
			);
		
		RenderVariable req6_2 = new RenderVariable(
				"numeric2", 
				"-12345.678",
				VariableType.NUMERIC,
				"000,000,000.0#;(-000,000,000.0#)"
			);
		
		RenderVariable req6_3 = new RenderVariable(
				"numeric3", 
				new BigDecimal(-99999.99),
				VariableType.NUMERIC,
				"$###,###,##0.00;-$###,###,##0.00"
			);
		
		RenderVariable req7_1 = new RenderVariable(
				"datetime1", 
				"2007-10-01 15:23:12", // use default format
				VariableType.DATETIME
			);
		
		RenderVariable req7_2 = new RenderVariable(
				"datetime2", 
				"12/01/2007", 
				VariableType.DATETIME,
				"MM/dd/yyyy" // custom format
			);
		
		RenderVariable req7_3 = new RenderVariable(
				"datetime3", 
				null,
				VariableType.DATETIME,
				"yyyy-MM-dd:hh.mm.ss a" // custom format
			);
		
		RenderVariable req7_4 = new RenderVariable(
				"datetime4", 
				new java.util.Date(), // current date time
				VariableType.DATETIME,
				"yyyy-MM-dd HH.mm" // custom format
			);
		
		map.put(req1.getVariableName(), req1);
		map.put(req2.getVariableName(), req2);
		map.put(req3.getVariableName(), req3);
		map.put(req4.getVariableName(), req4);
		map.put(req5.getVariableName(), req5);
		map.put(req6_1.getVariableName(), req6_1);
		map.put(req6_2.getVariableName(), req6_2);
		map.put(req6_3.getVariableName(), req6_3);
		map.put(req7_1.getVariableName(), req7_1);
		map.put(req7_2.getVariableName(), req7_2);
		map.put(req7_3.getVariableName(), req7_3);
		map.put(req7_4.getVariableName(), req7_4);
		
		req7_4 = new RenderVariable(
				"datetime4", 
				"2009-07-29 13.04",
				VariableType.DATETIME,
				"yyyy-MM-dd HH.mm" // custom format
			);
		map.put(req7_4.getVariableName(), req7_4);
		
		return map;
	}
	
	Map<String, RenderVariable> createVariableMapWithTableSection() {
		Map<String, RenderVariable> map = new HashMap<String, RenderVariable>();
		
		RenderVariable currentDate = new RenderVariable(
				GlobalVariableEnum.CurrentDate.name(), 
				null, 
				"yyyy-MM-dd", 
				VariableType.DATETIME, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		map.put(currentDate.getVariableName(), currentDate);
		
		RenderVariable req1 = new RenderVariable(
				"name1", 
				"Jack Wang", 
				null, 
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		RenderVariable req2 = new RenderVariable(
				"name2", 
				"Rendered User2", 
				null, 
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		RenderVariable req3 = new RenderVariable(
				"name3", 
				"Rendered User3", 
				null, 
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		RenderVariable req4 = new RenderVariable(
				"name4", 
				"Recursive Variable ${name1} End", 
				null, 
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		RenderVariable req5 = new RenderVariable(
				"name5", 
				"Rendered User5", 
				null, 
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req6_1 = new RenderVariable(
				"numeric1", 
				"12345.678", 
				null, 
				VariableType.NUMERIC, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req6_2 = new RenderVariable(
				"numeric2", 
				"-12345.678",
				"000,000,000.0#;(-000,000,000.0#)",
				VariableType.NUMERIC, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req6_3 = new RenderVariable(
				"numeric3", 
				Integer.valueOf(122),
				null,
				VariableType.NUMERIC, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req7_1 = new RenderVariable(
				"datetime1", 
				"2007-10-01 15:23:12",
				null,  // default format
				VariableType.DATETIME, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req7_2 = new RenderVariable(
				"datetime2", 
				"12/01/2007", 
				"MM/dd/yyyy", // custom format
				VariableType.DATETIME,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req7_3 = new RenderVariable(
				"datetime3", 
				null, // use current time
				"yyyy-MM-dd:hh.mm.ss a", // custom format
				VariableType.DATETIME,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		
		RenderVariable req8_1 = new RenderVariable(
				"address1", 
				"str.address@legacytojava.com",
				null,
				VariableType.ADDRESS,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		map.put(req8_1.getVariableName(), req8_1);
		
		try {
			RenderVariable req8_2 = new RenderVariable(
					"address2", 
					new InternetAddress("inet.address@legacytojava.com"),
					null,
					VariableType.ADDRESS,
					CodeType.YES_CODE.getValue(),
					Boolean.FALSE
				);
			map.put(req8_2.getVariableName(), req8_2);
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
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		RenderVariable req2_row2 = new RenderVariable(
				"name2", 
				"Rendered User2 - Row 2", 
				null, 
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		TableSection table = new TableSection();
		Map<String, RenderVariable> row1 = table.getEmptyRow(); // a row
		row1.put(req2.getVariableName(), req2_row1);
		row1.put(req3.getVariableName(), req3);
		table.addRow(row1);
		Map<String, RenderVariable> row2 = table.getEmptyRow(); // a row
		row2.put(req2.getVariableName(), req2_row2);
		row2.put(req3.getVariableName(), req3);
		table.addRow(row2);
		RenderVariable array = new RenderVariable(
				Renderer.TableVariableName, 
				table.getCollection(),
				null, 
				VariableType.COLLECTION, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE
			);
		// end of Collection
		
		map.put(req1.getVariableName(), req1);
		map.put(req2.getVariableName(), req2);
		map.put(req3.getVariableName(), req3);
		map.put(req4.getVariableName(), req4);
		map.put(req5.getVariableName(), req5);
		map.put(req6_1.getVariableName(), req6_1);
		map.put(req6_2.getVariableName(), req6_2);
		map.put(req6_3.getVariableName(), req6_3);
		map.put(req7_1.getVariableName(), req7_1);
		map.put(req7_2.getVariableName(), req7_2);
		map.put(req7_3.getVariableName(), req7_3);
		map.put(Renderer.TableVariableName, array);
		
		return map;
	}
}
