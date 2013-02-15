package jpa.variable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class MyProperties extends Properties {
	private static final long serialVersionUID = 4115280968301218916L;

	public MyProperties() {
		super();
	}
	
	@Override
	public String getProperty(String key) {
		String template = super.getProperty(key);
		if (template == null) return null;
		PropertyRenderer renderer = PropertyRenderer.getInstance();
		try {
			String renderedText = renderer.render(template, this);
			return renderedText;
		}
		catch (Exception e) {
			return super.getProperty(key);
		}
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String template = this.getProperty(key);
		if (template == null) return defaultValue;
		return template;
	}

	private static String fileName = "META-INF/eclipselink.dev.properties";
	public static void main(String[] args) {
		MyProperties props = loadMyProperties(fileName);
		props.list(System.out);
		System.out.println("=================================");
		System.out.println(props.getProperty("dataSource.url"));
		System.out.println(props.getProperty("jndi.url"));
		System.out.println(props.getProperty("jdbc.host"));
		System.out.println(props.getProperty("not.found", "property not found"));
	}
	
	public static MyProperties loadMyProperties(String fileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(fileName);
		if (url == null) {
			throw new RuntimeException("Could not find " + fileName + " file.");
		}
		MyProperties props = new MyProperties();
		try {
			InputStream is = url.openStream();
			props.load(is);
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught", e);
		}
		
		return props;
	}

}
