package com.legacytojava.message.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang.StringUtils;

import com.legacytojava.message.util.StringUtil;

public class TraverseFolder {
	static final String LF = System.getProperty("line.separator", "\n");
	private final String rootPath;
	
	public TraverseFolder() {
		rootPath = System.getProperty("user.dir") + File.separator + "JavaSource" + File.separator;
		System.out.println("RootPath: " + rootPath);
	}
	
	void visitAllDirsAndFiles(File dir) {
		if (dir == null) return;
		if (dir.isFile()) {
			String fileName = dir.getName();
			if (fileName.endsWith(".java")) {
				System.out.println("File Name: " + fileName);
				processFile(dir);
			}
		}
		else if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirsAndFiles(new File(dir, children[i]));
	        }
	    }
	}

	void processFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line=reader.readLine())!=null) {
				sb.append(line + LF);
			}
			String javaFile = sb.toString();
			if (javaFile.startsWith("/*") && javaFile.indexOf("Copyright (C)")>0) {
				//System.out.println(javaFile);
			}
			else {
				String pkgPath = StringUtil.trim(file.getAbsolutePath(), rootPath);
				pkgPath = StringUtils.replace(pkgPath, "\\", "/");
				String license = 
				"/*" + LF +
				" * " + pkgPath + LF +
				" * " + LF +
				" * Copyright (C) 2008 Jack W." + LF +
				" * " + LF +
				" * This program is free software: you can redistribute it and/or modify it under the terms of the" + LF +
				" * GNU Lesser General Public License as published by the Free Software Foundation, either version 3" + LF +
				" * of the License, or (at your option) any later version." + LF +
				" * " + LF +
				" * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without" + LF +
				" * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU" + LF +
				" * Lesser General Public License for more details." + LF +
				" * " + LF +
				" * You should have received a copy of the GNU Lesser General Public License along with this library." + LF +
				" * If not, see <http://www.gnu.org/licenses/>." + LF +
				" */" + LF;
				writeToFile(file, license + javaFile);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) { 
					// ignore
				}
			}
		}
	}

	private void writeToFile(File file, String javaFile) throws IOException {
		// write to disk
		if (file.canWrite()) {
			BufferedReader br = new BufferedReader(new StringReader(javaFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String line;
			while ((line=br.readLine())!=null) {
				bw.write(line);
				bw.newLine();
			}
			br.close();
			bw.close();
		}
	}

	void process() {
		String startPath = rootPath + "com/legacytojava/message/util";
		File dir = new File(startPath);
		visitAllDirsAndFiles(dir);
	}

	public static void main(String[] args) {
		TraverseFolder folder = new TraverseFolder();
		folder.process();
	}
}
