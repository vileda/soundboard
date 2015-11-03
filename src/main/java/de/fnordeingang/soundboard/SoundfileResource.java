package de.fnordeingang.soundboard;

import javax.ejb.Timer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

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
		if(!controller.isSoundfilePresent(request.getFilePath())) {
			return Response.status(FORBIDDEN).build();
		}
		String timerId = cronJobController.createTimer(request);
		return Response.ok(timerId).build();
	}

	@GET
	@Path("/timer")
	public Map<String, Timer> getTimers() {
		return cronJobController.getTimers();
	}

	@POST
	@Path("/timer/clear")
	public Response clearTimer(String timerId) {
		cronJobController.clearTimer(timerId);
		return Response.ok().build();
	}
}
