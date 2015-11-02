package de.fnordeingang.soundboard;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF8")
public class SoundfileResource {
	@Inject
	SoundfileController controller;

	@Inject
	private CronJobController cronJobController;

	@GET
	public List<Category> getSoundfiles() {
		return controller.getSoundfiles();
	}

	@POST
	public Response playSoundfile(String path) {
		controller.play(path);
		return Response.ok().build();
	}

	@POST
	@Path("/kill")
	public Response killall() {
		controller.killall();
		return Response.ok().build();
	}

	@GET
	@Path("/search")
	public List<SortedSoundfile> search(@QueryParam("q") String term) {
		return controller.search(term);
	}

	@POST
	@Path("/timer")
	public Response createTimer(CreateTimerRequest request) {
		String timerId = cronJobController.createTimer(request);
		return Response.ok(timerId).build();
	}

	@POST
	@Path("/timer/clear")
	public Response clearTimer(String timerId) {
		cronJobController.clearTimer(timerId);
		return Response.ok().build();
	}
}
