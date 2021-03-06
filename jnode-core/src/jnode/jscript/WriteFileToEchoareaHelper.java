package jnode.jscript;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;

public class WriteFileToEchoareaHelper extends IJscriptHelper {
	
	private static final Logger logger = Logger
			.getLogger(WriteFileToEchoareaHelper.class);

	public void writeFileToEchoarea(String echoArea, String subject,
			String filename) {
		Echoarea area = FtnTools.getAreaByName(echoArea, null);
		String content;
		try {
			content = readFile(filename);
		} catch (IOException e) {
			logger.l2(MessageFormat.format("fail read file {0}", filename), e);
			return;
		}

		FtnTools.writeEchomail(area, subject, content);

	}

	private static String readFile(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(new File(path))) {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size());
            return Charset.forName("UTF8").decode(bb).toString();
        }
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
				return 1;
			}
		};
	}
}
