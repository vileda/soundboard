package de.fnordeingang.soundboard;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

/**
 * Streaming resource
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 */
@Path("/stream")
@ApplicationScoped
public class MediaResource {
	@Inject
	private SoundfileController soundfileController;

	final int chunk_size = 1024 * 1024; // 1MB chunks

	public MediaResource() {
	}

	//A simple way to verify if the server supports range headers.
	@HEAD
	@Produces("audio/mp3")
	public Response header(@QueryParam("url") String url) {
		if(!soundfileController.isSoundfilePresent(url)) return Response.noContent().build();
		File audio = new File(url);
		return Response.ok().status(206).header(HttpHeaders.CONTENT_LENGTH, audio.length()).build();
	}

	@GET
	@Produces("audio/mp3")
	public Response streamAudio(@HeaderParam("Range") String range, @QueryParam("url") String url) throws Exception {
		if(!soundfileController.isSoundfilePresent(url)) return Response.noContent().build();
		File audio = new File(url);
		return buildStream(audio, range);
	}

	/**
	 * Adapted from http://stackoverflow.com/questions/12768812/video-streaming-to-ipad-does-not-work-with-tapestry5/12829541#12829541
	 *
	 * @param asset Media file
	 * @param range range header
	 * @return Streaming output
	 * @throws Exception IOException if an error occurs in streaming.
	 */
	private Response buildStream(final File asset, final String range) throws Exception {
		// range not requested : Firefox, Opera, IE do not send range headers
		if (range == null) {
			StreamingOutput streamer = output -> {
				try (FileChannel inputChannel = new FileInputStream(asset).getChannel();
						 WritableByteChannel outputChannel = Channels.newChannel(output)) {
					inputChannel.transferTo(0, inputChannel.size(), outputChannel);
				}
			};
			return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
		}

		String[] ranges = range.split("=")[1].split("-");
		final int from = Integer.parseInt(ranges[0]);
		/**
		 * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
		 */
		int to = chunk_size + from;
		if (to >= asset.length()) {
			to = (int) (asset.length() - 1);
		}
		if (ranges.length == 2) {
			to = Integer.parseInt(ranges[1]);
		}

		final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
		final RandomAccessFile raf = new RandomAccessFile(asset, "r");
		raf.seek(from);

		final int len = to - from + 1;
		final MediaStreamer streamer = new MediaStreamer(len, raf);
		Response.ResponseBuilder res = Response.ok(streamer).status(206)
				.header("Accept-Ranges", "bytes")
				.header("Content-Range", responseRange)
				.header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
				.header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
		return res.build();
	}

}
