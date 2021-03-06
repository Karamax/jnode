package org.jnode.pointchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jnode.dto.Filearea;
import jnode.ftn.FtnTools;
import jnode.jscript.IJscriptHelper;

public class PointListHelper extends IJscriptHelper {

	private File createPointList(String seg, String dir, String header,
			String footer) {
		File fdir = new File(dir);
		if (!(fdir.isDirectory() && fdir.canRead())) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (header != null && header.length() > 0) {
			sb.append(getFileContents(new File(header)));
		}

		for (File file : fdir.listFiles()) {
			if (file.isFile()) {
				sb.append("; file " + file.getName() + "\r\n");
				sb.append(getFileContents(file));
				sb.append("\r\n");
			}
		}

		if (footer != null && footer.length() > 0) {
			sb.append(getFileContents(new File(footer)));
		}

		try {
			String str = sb.toString().replaceAll("\r\n", "\n")
					.replaceAll("\r", "").replaceAll("\n{1,}", "\n")
					.replaceAll("\n", "\r\n");
			File ret = File.createTempFile("zip", "pnt");
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(ret));
			zos.putNextEntry(new ZipEntry(seg));
			zos.write(str.toString().getBytes("CP866"));
			zos.closeEntry();
			zos.close();
			return ret;
		} catch (IOException e) {
		}
		return null;
	}

	private String getFileContents(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];
			fis.read(buf);
			fis.close();
			return new String(buf, "CP866");
		} catch (IOException e) {
		}
		return "";
	}

	/**
	 * Отправить файл в арию
	 * 
	 * @param areaName
	 * @param zip
	 * @param seg
	 * @param dir
	 * @param header
	 * @param footer
	 */
	public void hatchToArea(String areaName, String zip, String seg,
			String dir, String header, String footer) {
		Filearea area = FtnTools.getFileareaByName(areaName, null);
		File attachment = createPointList(seg, dir, header, footer);
		FtnTools.hatchFile(area, attachment, zip, "Generated pointlist");

	}

	@Override
	public Version getVersion() {
		return new Version() {

			@Override
			public int getMinor() {
				return 1;
			}

			@Override
			public int getMajor() {
				return 2;
			}
		};
	}

}
