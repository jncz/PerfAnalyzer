package com.test.instrument.servlet;

import com.test.instrument.persist.FileOp;

public class FileCompactor extends Thread{
	private static final int SLEEP_TIME = 60*1000*2;
	@Override
	public void run() {
		while(true){
			compact();
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void compact() {
		FileOp.archive(false);
	}

}
