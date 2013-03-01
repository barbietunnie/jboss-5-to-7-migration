package com.legacytojava.jbatch.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class provides a simple implementation for creating Oracle connection
 * pool(s).
 * <p>
 * Methods are provided to request and release connections from pool.
 */
public class SimpleConnPool {
	private Hashtable<Connection, Boolean> connections;
	private int increment;
	private String dbURL, user, password;

	/**
	 * @param dbURL:
	 *            database location in URL format
	 * @param user:
	 *            user name
	 * @param password:
	 *            user password
	 * @param driverClassName:
	 *            Oracle Driver class name
	 * @param initialConnections:
	 *            number of connections to be created initially
	 * @param increment:
	 *            number of connections to be created if no free connection is
	 *            available
	 */
	public SimpleConnPool(String dbURL,
			String user,
			String password,
			String driverClassName,
			int initialConnections,
			int increment) throws SQLException, ClassNotFoundException {

		// Load the specified driver class
		Class.forName(driverClassName);

		this.dbURL = dbURL;
		this.user = user;
		this.password = password;
		this.increment = increment;

		connections = new Hashtable<Connection, Boolean>();

		// Put our pool of Connections in the Hashtable (as name/value pair)
		// The FALSE value indicates they're unused
		for (int i = 0; i < initialConnections; i++) {
			connections.put(DriverManager.getConnection(dbURL, user, password), Boolean.FALSE);
		}
	}

	/**
	 * request a connection from the connection pool
	 * 
	 * @return a JDBC connection
	 */
	public synchronized Connection getConnection() throws SQLException {
		Connection con = null;

		Enumeration<?> cons = connections.keys();

		while (cons.hasMoreElements()) {
			con = (Connection) cons.nextElement();

			Boolean b = (Boolean) connections.get(con);
			if (Boolean.FALSE.equals(b)) {
				// So we found an unused connection.
				// Test its integrity with a quick setAutoCommit(true) call.
				// For production use, more testing should be performed,
				// such as executing a simple query.
				try {
					con.setAutoCommit(true);
				}
				catch (SQLException e) {
					connections.remove(con); // remove the dead connection
					// Problem with the connection, replace it.
					con = DriverManager.getConnection(dbURL, user, password);
				}
				// Update the Hashtable to show this one's taken
				connections.put(con, Boolean.TRUE);
				// Return the connection
				return con;
			}
		}

		// If we get here, there were no free connections.
		// We've got to make more.
		for (int i = 0; i < increment; i++) {
			connections.put(DriverManager.getConnection(dbURL, user, password), Boolean.FALSE);
		}

		// Recurse to get one of the new connections.
		return getConnection();
	}

	/**
	 * release a connection to the connection pool
	 * 
	 * @param returned:
	 *            connection to be returned to the connection pool
	 */
	public synchronized void returnConnection(Connection returned) {
		Connection con;
		Enumeration<?> cons = connections.keys();
		while (cons.hasMoreElements()) {
			con = (Connection) cons.nextElement();
			if (con == returned) {
				connections.put(con, Boolean.FALSE);
				break;
			}
		}
	}
}
