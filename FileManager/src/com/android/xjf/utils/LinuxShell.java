package com.android.xjf.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class LinuxShell {
	public static boolean isRoot(Runtime r, long wait)
			throws IOException, InterruptedException {
		boolean root = false;
		Process p = null;
		BufferedReader errReader = null;
		p = Runtime.getRuntime().exec("su");
		Thread.sleep(wait);
		errReader = new BufferedReader(
				new InputStreamReader(p.getErrorStream()));
		if (!errReader.ready()) { 
			root = true;
			p.destroy();
		}
		return root;
	}
}
