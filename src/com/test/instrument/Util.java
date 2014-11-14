package com.test.instrument;

import java.io.Closeable;
import java.io.IOException;

public class Util {

	public static void close(Closeable fw) {
		if(fw != null){
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
