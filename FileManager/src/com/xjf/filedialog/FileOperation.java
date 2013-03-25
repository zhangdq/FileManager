package com.xjf.filedialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileOperation {
	public static void copyFile(String oldPath, String newPath)
			throws IOException {
		int bytesum = 0;
		int byteread = 0;
		File oldfile = new File(oldPath);
		if (oldfile.exists()) {
			InputStream inStream = new FileInputStream(oldPath); // 读入原文件
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[4096];
			//int length;
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread; // 字节数 文件大小
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
		}
	}
	 
	
	/**
	 * 递归获取文件夹里所有文件的总大小
	 * BUG: 不能分辨链接文件
	 * */
	public static long getDirectorySize(File f) throws IOException{
		long size = 0;
		File flist[] = f.listFiles();
		if (flist == null)
			return f.length();
		int length = flist.length;
		for (int i = 0; i < length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getDirectorySize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	/**
	 * BUG: 不能分辨链接文件
	 * */
	public static long getDirectorySize(String fp) throws IOException{
		long size = 0;
		File f = new File(fp);
		File flist[] = f.listFiles();
		if (flist == null)
			return f.length();
		int length = flist.length;
		for (int i = 0; i < length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getDirectorySize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
}
