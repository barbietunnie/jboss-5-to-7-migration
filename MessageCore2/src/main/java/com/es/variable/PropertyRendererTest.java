package com.es.variable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public final class PropertyRendererTest {
	static final Logger logger = Logger.getLogger(PropertyRendererTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Test
	public void testRender() throws Exception {
		PropertyRenderer renderer = PropertyRenderer.getInstance();

		Map<Object, Object> map = loadVariableMap();
		String template = (String) map.get("dataSource.url");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, "jdbc:mysql://localhost:3306/message");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
		template = (String) map.get("jndi.url");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, "remote://localhost:4447");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
		template = (String) map.get("jdbc.host");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, "localhost");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}

		template = "Variable ${not_found} doesn't exist.";
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, template);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
	}

	@Test
	public void testBadTemplate() throws Exception {
		PropertyRenderer renderer = PropertyRenderer.getInstance();

		Map<Object, Object> map = loadVariableMap();
		
		String template1 = "Missing first closing delimiter\n"
			+ "Some Numberic values: ${numeric1, ${numeric2}, ${numeric3}\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template1, map);
			fail();
		}
		catch (Exception e) {
			assertTrue(e.getMessage().indexOf("${numeric1,") > 0);
		}
	}

	private Map<Object, Object> loadVariableMap() {
		String fileName = "META-INF/msgcore.mysql.properties";
		return VarReplProperties.loadMyProperties(fileName);
	}
}
