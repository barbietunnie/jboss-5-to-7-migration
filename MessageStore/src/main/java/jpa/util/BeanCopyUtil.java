package jpa.util;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.beanutils.converters.SqlTimeConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;

public class BeanCopyUtil {
	
	private static DateConverter dateConverter = new DateConverter(null);
	private static SqlTimestampConverter timestampConverter = new SqlTimestampConverter(null);
	private static SqlDateConverter sqlDateConverter = new SqlDateConverter(null);
	private static SqlTimeConverter sqlTimeonverter = new SqlTimeConverter(null);

	public static void registerBeanUtilsConverters() {
		// setup for BeanUtils.copyProperties() to handle null value
		ConvertUtils.register(dateConverter, java.util.Date.class);
		ConvertUtils.register(timestampConverter, java.sql.Timestamp.class);
		ConvertUtils.register(sqlDateConverter, java.sql.Date.class);
		ConvertUtils.register(sqlTimeonverter, java.sql.Time.class);
	}

}
