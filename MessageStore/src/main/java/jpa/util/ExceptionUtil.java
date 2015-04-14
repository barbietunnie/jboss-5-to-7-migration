package jpa.util;

public class ExceptionUtil {
	public static Exception findException(Exception ex, Class<?> clazz) {
		Exception exception = null;
		if (clazz == null || ex == null) {
			throw new IllegalArgumentException("Input parameters must not be null");
		}
		if (clazz.isInstance(ex)) {
			exception = ex;
		}
		while (ex.getCause()!=null) {
			if (clazz.isInstance(ex.getCause())) {
				exception = (Exception)ex.getCause();
			}
			ex = (Exception)ex.getCause();
		}
		return exception;
	}
}
