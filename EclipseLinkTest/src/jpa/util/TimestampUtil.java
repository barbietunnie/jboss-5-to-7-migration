package jpa.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * This class contains some useful time stamp routines. Like converting db2 time
 * stamp to decimal string, generating db2 time stamp from a java Date object,
 * and converting time stamp format between db2 and oracle, etc.
 */
public class TimestampUtil implements java.io.Serializable {
	private static final long serialVersionUID = -6023017890883430172L;
	protected static final Logger logger = Logger.getLogger(TimestampUtil.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	private final SimpleDateFormat sdf;
	static final int RADIX = 36;
	static final Random RANDOM = new Random(System.currentTimeMillis());

	public static void main(String argv[]) {
		String db2tm = "1582-10-23-00.48.04.702003";
		db2tm = "0697-10-13-22.29.59.972003";
		db2tm = getDb2Timestamp();
		String converted = db2ToDecimalString(db2tm);
		// converted = "30023805873165862201";
		String restored = decimalStringToDb2(converted);
		logger.info("Date: " + db2tm + ", converted: " + converted + ", restored: "
				+ restored);
	}

	/** constructor, using provided date pattern */
	public TimestampUtil(String pattern) {
		sdf = new SimpleDateFormat();
		sdf.applyPattern(pattern);
	}

	/** constructor, using default pattern: yyyy-MM-dd HH:mm:ss.SSS */
	public TimestampUtil() {
		this("yyyy-MM-dd HH:mm:ss.SSS");
	}

	/* Instance Methods */

	/** convert the formatted time stamp to a java long */
	public long timestampToLong(String ts) {
		// convert a standard time stamp to a long number
		ParsePosition pos = new ParsePosition(0);
		Date date = sdf.parse(ts, pos);
		return date.getTime();
	}

	/** convert the long to the formatted time stamp */
	public String longToTimestamp(long tm) {
		// convert a long number to a standard time stamp
		Date date = new Date();
		date.setTime(tm);
		return sdf.format(date);
	}

	//
	// ================= Static methods ===================
	//

	/** convert a db2 time stamp to a decimal string */
	public static String db2ToDecimalString(String ts) throws NumberFormatException {
		// convert a db2 time stamp to a decimal string
		return convert(ts);
	}

	/** convert a decimal string to a db2 time stamp */
	public static String decimalStringToDb2(String st) throws NumberFormatException {
		// convert a decimal string to a db2 time stamp
		long tm;
		long millis = 0;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

		// remove possible CR/LF, tabs, and blanks, that are inserted by some
		// Email servers, from bounced e-mails (MS exchange server for one).
		// MS exchange server inserted \r\n\t into the Email_ID string, and it
		// caused "check digit test" error.
		StringTokenizer tokens = new StringTokenizer(st, "\r\n\t ");
		StringBuffer sb = new StringBuffer();
		while (tokens.hasMoreTokens()) {
			sb.append(tokens.nextToken());
		}
		st = sb.toString();

		String db2ts = null;
		int plusPos = st.indexOf("+");
		if (plusPos > 0) { // string contains "+", example: 130da3bfd+75c29
			tm = Long.parseLong(st.substring(0, plusPos), RADIX);
			millis = Long.parseLong(st.substring(plusPos + 1), RADIX);

			String dateString = formatter.format(new Date(tm));

			String ssssss;
			if (Long.toString(millis).length() >= 6) {
				// current year is prepended to the milliseconds, remove it.
				ssssss = fillWithTrailingZeros(Long.toString(millis).substring(4), 6);
			}
			else {
				// make it compatible to old hex string.
				ssssss = fillWithTrailingZeros(Long.toString(millis), 6);
			}
			db2ts = dateString + "." + ssssss;
		}
		else { // new Email_ID format received, example: 1234567890123456789
			db2ts = restore(st);
		}

		if (!isValidDb2Timestamp(db2ts)) {
			throw new NumberFormatException("Converted <" + st + "> to an invalid DB2 Timestamp <"
					+ db2ts + ">");
		}
		return db2ts;
	}

	/** swap the first and the last four bytes to show the real year */
	public static String swapFirst4AndLast4(String db2ts) throws NumberFormatException {
		if (!isValidDb2Timestamp(db2ts)) {
			throw new NumberFormatException("Received an invalid DB2 Timestamp: <" + db2ts + ">");
		}
		int len = db2ts.length();
		return db2ts.substring(len - 4) + db2ts.substring(4, len - 4) + db2ts.substring(0, 4);
	}

	/** convert db2 time stamp to oracle time stamp: MM/dd/yyyy HH:mm:ss */
	public static String db2ToOracle(String ts) throws NumberFormatException {
		/*
		 * convert a db2 time stamp to a oracle date string for example:
		 * yyyy-MM-dd-HH.mm.ss.ssssss => MM/dd/yyyy HH:mm:ss
		 */
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = db2ToDate(ts);
		String dateString = formatter.format(date);

		return dateString;
	}

	/** convert db2 time stamp to java Date object */
	public static Date db2ToDate(String ts) throws NumberFormatException {
		/*
		 * convert a db2 time stamp to a java date object for example:
		 * yyyy-MM-dd-hh.mi.ss.ssssss => MM/dd/yyyy hh:mi:ss:SSS
		 */
		Date date = null;
		int years, months, days, hours, minutes, seconds, millis;
		StringTokenizer st = new StringTokenizer(ts, " -.:");
		if (st.countTokens() != 7) {
			throw new NumberFormatException("Time Stamp format error! " + ts);
		}
		else {
			years = Integer.parseInt(st.nextToken());
			months = Integer.parseInt(st.nextToken());
			days = Integer.parseInt(st.nextToken());
			hours = Integer.parseInt(st.nextToken());
			minutes = Integer.parseInt(st.nextToken());
			seconds = Integer.parseInt(st.nextToken());

			String millis_str = st.nextToken();
			if (millis_str.length() > 3)
				millis_str = millis_str.substring(0, 3);
			millis = Integer.parseInt(millis_str);

			Calendar cal = Calendar.getInstance();
			cal.set(years, --months, days, hours, minutes, seconds);
			cal.set(Calendar.MILLISECOND, millis);
			date = cal.getTime();
		}
		return date;
	}

	/**
	 * create a db2 time stamp using current system time, the last three digits
	 * will be filled with a random number.
	 */
	public static String getDb2Timestamp() {
		return getDb2Timestamp(new Date(), true);
	}

	/**
	 * create a db2 time stamp from a java Date, the last three digits will be
	 * filled with a random number.
	 */
	public static String getDb2Timestamp(Date date) {
		return getDb2Timestamp(date, true);
	}

	/**
	 * create a db2 time stamp from a java Date. And if "random_ms" is true,
	 * fill the last three digits with a random number, otherwise filled with
	 * zeros.
	 */
	public static String getDb2Timestamp(Date date, boolean random_ms) {
		if (date == null) {
			date = new Date();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String dateString = formatter.format(date);

		String millis;
		if (random_ms) {
			millis = Integer.toString(RANDOM.nextInt(1000));
		}
		else {
			millis = "000";
		}
		return dateString + fillWithTrailingZeros(millis, 3);
	}

	/**
	 * convert a string in "MM/dd/yyyy HH:mm:ss" format to a java Date object.<br>
	 * A null will be returned if the date represented by the string is invalid.
	 */
	public static Date oracleToDate(String dateStr) {
		return stringToDate(dateStr, "MM/dd/yyyy HH:mm:ss");
	}

	/**
	 * convert a string to a java Date object using the supplied "pattern".<br>
	 * A null will be returned if the date represented by the string is invalid.
	 */
	public static Date stringToDate(String dateStr, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		ParsePosition pos = new ParsePosition(0);
		return formatter.parse(dateStr, pos);
	}

	private static String fillWithTrailingZeros(String str, int len) {
		String zeros = "00000000";
		if (str.length() > len) // this shouldn't happen
		{
			return str.substring(0, len);
		}
		else if (str.length() < len) {
			return zeros.substring(0, len - str.length()) + str;
		}
		else {
			return str;
		}
	}

	private static String fillWithTrailingZeros(int num, int len) {
		String str = Integer.valueOf(num).toString();
		return fillWithTrailingZeros(str, len);
	}

	/** return true if db2ts is a valid db2 time stamp, false otherwise. */
	public static boolean isValidDb2Timestamp(String db2ts) {
		if (db2ts == null || db2ts.length() != 26) {
			return false;
		}
		StringTokenizer st = new StringTokenizer(db2ts, "-.");
		if (st.countTokens() != 7) {
			return false;
		}
		String token;
		for (int i = 0; i < 7; i++) {
			token = st.nextToken();
			if (i == 0 && token.length() != 4) {
				return false;
			}
			if ((i >= 1 && i <= 5) && token.length() != 2) {
				return false;
			}
			if (i == 6 && token.length() != 6) {
				return false;
			}
			try {
				Integer.parseInt(token);
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	/* methods added to generate Message Reference Id */

	// convert db2 time stamp to an Email_ID
	private static String convert(String db2ts) throws NumberFormatException {
		StringTokenizer st = new StringTokenizer(db2ts, " -.:");
		int years, months, days, hours, minutes, seconds;
		String nanosStr;

		if (st.countTokens() != 7) {
			throw new NumberFormatException("Invalid Time Stamp: " + db2ts);
		}

		years = Integer.parseInt(st.nextToken());
		months = Integer.parseInt(st.nextToken());
		days = Integer.parseInt(st.nextToken());
		hours = Integer.parseInt(st.nextToken());
		minutes = Integer.parseInt(st.nextToken());
		seconds = Integer.parseInt(st.nextToken());
		nanosStr = st.nextToken();

		myGregCal cal = new myGregCal();
		int[] daysArray = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		int maxDays;
		if (months == 2 && cal.isLeapYear(years)) {
			maxDays = 29;
		}
		else {
			maxDays = daysArray[months - 1];
		}
		if (months < 1 || months > 12 || days < 1 || days > maxDays || hours > 23 || minutes > 59
				|| seconds > 59 || nanosStr.length() != 6) {
			throw new NumberFormatException("Invalid timestamp: " + db2ts);
		}

		String first3 = nanosStr.substring(0, 3);
		String last3 = nanosStr.substring(3);

		String Millis = fillWithTrailingZeros(years, 4) + fillWithTrailingZeros(months, 2)
				+ fillWithTrailingZeros(days, 2) + fillWithTrailingZeros(hours, 2)
				+ fillWithTrailingZeros(minutes, 2) + fillWithTrailingZeros(seconds, 2)
				+ fillWithTrailingZeros(first3, 3);

		int checkdigit = getCheckDigit(Millis + last3);

		String shuffled = shuffle(Millis + last3, checkdigit, true);

		return shuffled + checkdigit;
	}

	// restore db2 time stamp from an Email_ID
	private static String restore(String refid) throws NumberFormatException {
		//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		try {
			String oldCheckDigit = refid.substring(refid.length() - 1);
			String tmpstr = refid.substring(0, refid.length() - 1);
			String newRefid = shuffle(tmpstr, Integer.parseInt(oldCheckDigit), false);

			int split = newRefid.length() - 3;

			String Millis = newRefid.substring(0, split);
			String last3 = newRefid.substring(split);

			String newCheckDigit = Integer.valueOf(getCheckDigit(Millis + last3)).toString();
			if (!oldCheckDigit.equals(newCheckDigit)) {
				throw new NumberFormatException("checkdigit inconsistant.");
			}

			if (Millis.length() >= 17) {
				String year = Millis.substring(0, 4);
				String month = Millis.substring(4, 2 + 4);
				String date = Millis.substring(6, 2 + 6);
				String hour = Millis.substring(8, 2 + 8);
				String min = Millis.substring(10, 2 + 10);
				String sec = Millis.substring(12, 2 + 12);
				String mil = Millis.substring(14, 3 + 14);

				String dateStr = year + "-" + month + "-" + date + "-" + hour + "." + min + "."
						+ sec + "." + mil;

				return dateStr + last3;
			}
			else {
				return restore_old(refid);
			}
		}
		catch (IndexOutOfBoundsException e) {
			logger.error("IndexOutOfBoundsException caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
	}

	static String convert_old(String db2ts) throws NumberFormatException {
		StringTokenizer st = new StringTokenizer(db2ts, " -.:");

		long totalSeconds, totalMillis;
		int years, months, days, hours, minutes, seconds;
		String nanosStr;

		if (st.countTokens() != 7) {
			throw new NumberFormatException("Time Stamp format error! " + db2ts);
		}

		years = Integer.parseInt(st.nextToken());
		months = Integer.parseInt(st.nextToken());
		days = Integer.parseInt(st.nextToken());
		hours = Integer.parseInt(st.nextToken());
		minutes = Integer.parseInt(st.nextToken());
		seconds = Integer.parseInt(st.nextToken());
		nanosStr = st.nextToken();

		myGregCal cal = new myGregCal();

		int[] daysArray = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		int maxDays;
		if (months == 2 && cal.isLeapYear(years)) {
			maxDays = 29;
		}
		else {
			maxDays = daysArray[months - 1];
		}

		if (months < 1 || months > 12 || days < 1 || days > maxDays || hours > 23 || minutes > 59
				|| seconds > 59) {
			throw new NumberFormatException("Invalid timestamp: " + db2ts);
		}
		cal.set(years, --months, days, hours, minutes, seconds);
		cal.set(Calendar.MILLISECOND, 0);

		totalMillis = cal.getTimeInMillis();
		totalSeconds = totalMillis / 1000L;

		String prefix = "0";
		if (totalSeconds < 0) {
			prefix = "1";
		}
		String newSeconds = prefix + Long.valueOf(Math.abs(totalSeconds)).toString();

		int checkdigit = getCheckDigit(newSeconds + nanosStr);

		// logger.info("unshuffled:"+newSeconds+nanosStr+checkdigit);
		String shuffled = shuffle(newSeconds + nanosStr, checkdigit, true);

		return shuffled + checkdigit;
	}

	static String restore_old(String refid) throws NumberFormatException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

		try {
			String oldCheckDigit = refid.substring(refid.length() - 1);
			String tmpstr = refid.substring(0, refid.length() - 1);
			String newRefid = shuffle(tmpstr, Integer.parseInt(oldCheckDigit), false);

			int split = newRefid.length() - 6;

			String Seconds = newRefid.substring(0, split);
			String Nanos = newRefid.substring(split);

			String newCheckDigit = Integer.valueOf(getCheckDigit(Seconds + Nanos)).toString();
			if (!oldCheckDigit.equals(newCheckDigit)) {
				throw new NumberFormatException("checkdigit inconsistant.");
			}
			long totalSeconds = Long.parseLong(Seconds.substring(1));
			long totalMillis = totalSeconds * 1000L;
			if (Seconds.startsWith("1")) {
				totalMillis *= -1;
			}
			myGregCal cal = new myGregCal();
			cal.setTimeInMillis(totalMillis);

			String dateString = formatter.format(cal.getTime());

			return dateString + "." + Nanos;
		}
		catch (IndexOutOfBoundsException e) {
			logger.error("IndexOutOfBoundsException caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
	}

	/**
	 * returns the check digit number of the given string.
	 */
	public static int getCheckDigit(String nbrstr) {
		// Calculate the check digit
		char multiplier[] = { 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1,
				3, 7, 1, 3, 7, 1, 3 };
		char charstr[] = nbrstr.toCharArray();
		long sum = 0L;
		for (short i = 0; i < charstr.length; i++) {
			sum += multiplier[i] * (charstr[i] & 0x0f);
		}
		sum = (10 - (sum % 10)) % 10;
		return (int) sum;
	}

	// shuffle the string
	private static String shuffle(String str, int span, boolean forward) {
		if (span == 0 || span >= str.length()) {
			return reverse(str);
		}
		String str1, str2;
		if (forward) {
			str1 = str.substring(0, span);
			str2 = str.substring(span);
		}
		else {
			str1 = str.substring(0, str.length() - span);
			str2 = str.substring(str.length() - span);
		}

		return reverse(str2) + reverse(str1);
	}

	// reverse the string, use method from StringBuffer
	private static String reverse(String str) {
		StringBuffer sb = new StringBuffer(str);
		return sb.reverse().toString();
	}

	/**
	 * This method will create a random java Date, and use it to create a db2
	 * time stamp.
	 * 
	 * @return a db2 time stamp
	 */
	public static String getRandomDb2Ts() {
		int years, months, days, hours, minutes, seconds, micros;

		years = RANDOM.nextInt(9999) + 1;
		// years = random.nextInt(86)+1970; // from 1970 to 2056
		months = RANDOM.nextInt(12) + 1;

		GregorianCalendar gcal = new GregorianCalendar();

		int[] daysArray = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		int maxDays;
		if (months == 2 && gcal.isLeapYear(years)) {
			maxDays = 29;
		}
		else {
			maxDays = daysArray[months - 1];
		}
		days = RANDOM.nextInt(maxDays) + 1;
		hours = RANDOM.nextInt(24);
		minutes = RANDOM.nextInt(60);
		seconds = RANDOM.nextInt(60);
		micros = RANDOM.nextInt(999999 + 1);

		return fillWithTrailingZeros("" + years, 4) + "-" + fillWithTrailingZeros("" + months, 2)
				+ "-" + fillWithTrailingZeros("" + days, 2) + "-"
				+ fillWithTrailingZeros("" + hours, 2) + "."
				+ fillWithTrailingZeros("" + minutes, 2) + "."
				+ fillWithTrailingZeros("" + seconds, 2) + "."
				+ fillWithTrailingZeros("" + micros, 6);
	}

	private static class myGregCal extends GregorianCalendar {
		private static final long serialVersionUID = 4219232260493472518L;

		public myGregCal() {
			super();
		}

		public long getTimeInMillis() {
			return super.getTimeInMillis();
		}

		public void setTimeInMillis(long millis) {
			super.setTimeInMillis(millis);
		}

		public void computeFields() {
			super.computeFields();
		}

		public void computeTime() {
			super.computeTime();
		}
	}
}
