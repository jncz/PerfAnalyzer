package com.test.instrument.persist;

import java.io.File;

public interface FileProcessor<T> {

	void process(File f);

}
