package com.ngdata.jajc;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;


public class Util {
	
	static Logger log = Logger.getLogger(Util.class.getName()); 
	
	public static void createTgz(String pathToDir, String moduleTgzLocation) throws IOException {
		FileOutputStream fos = new FileOutputStream(moduleTgzLocation);	
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos);
		TarArchiveOutputStream taos = new TarArchiveOutputStream(gcos);
		
		recAddFile(taos, pathToDir, "");
		
		taos.finish();
		taos.close();
		gcos.close();
		bos.close();
		fos.close();
	}
	
	public static void recAddFile(TarArchiveOutputStream taos, String absPath, String parentPath) throws IOException {
		File f = new File(absPath);
		String name = parentPath + f.getName();
		taos.putArchiveEntry(taos.createArchiveEntry(f, name));
		
		if (f.isFile()) {
			//log.debug("Copying " + f.getName() + " to tar");
			IOUtils.copy(new FileInputStream(f), taos);
			taos.closeArchiveEntry();
		}
		else {
			taos.closeArchiveEntry();
			File[] children = f.listFiles();
			
			if (children != null)
				for (File tmp : children)
					recAddFile(taos, tmp.getAbsolutePath(), name + "/" );
		}
	}
}
