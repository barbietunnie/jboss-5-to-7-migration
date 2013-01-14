/*
 * com/legacytojava/message/util/Printf.java
 * 
 * Copyright (C) 2008 Jack W.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.legacytojava.message.util;

public class Printf {

	private Printf() {}

	public static String sprintf(String format, Object arg) {
		return sprintf(format, new Object[] { arg });
	}

	public static String sprintf(String format, Object[] args) {
		return String.format(format, args);
	}

	public static void main(String[] args) {
		System.out.println(Printf.sprintf("%-20s", "Field Name"));
		Object[] objs = new Object[] {"-------------", new Integer(10)};
		System.out.println(Printf.sprintf("%-20s : %-5d", objs));
	}
}
