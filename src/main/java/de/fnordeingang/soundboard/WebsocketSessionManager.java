package de.fnordeingang.soundboard;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WebsocketSessionManager
{
	private List<Session> sessions = new ArrayList<>();

	public void addSession(Session session) {
		sessions.add(session);
	}

	public void broadcast(String message) {
		for (Session session : sessions)
		{
			session.getAsyncRemote().sendText(message);
		}
	}

	public void removeSession(Session session)
	{
		sessions.remove(session);
	}
}
