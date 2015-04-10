package de.isys.wiremock.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class GZipTransformer extends ResponseTransformer {

	public GZipTransformer() {
		super();
	}

	@Override
	public boolean applyGlobally() {
		return false;
	}
	
	public String name() {
		return "gzip";
	}

	@Override
	public ResponseDefinition transform(final Request request,
			final ResponseDefinition responseDefinition, final FileSource files) {
		final byte[] body = responseDefinition.getByteBody();
		ByteArrayOutputStream baos = null;
		GZIPOutputStream os = null;
		try {
			baos = new ByteArrayOutputStream();
			os = new GZIPOutputStream(baos);
			os.write(body);
			os.close();
			responseDefinition.setBody(baos.toByteArray());
			return responseDefinition;
		} catch (final IOException e) {
			throw new RuntimeException("shouldn't happen", e);
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(baos);
		}
	}

}
