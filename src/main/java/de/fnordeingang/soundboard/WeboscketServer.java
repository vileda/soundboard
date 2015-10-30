package de.fnordeingang.soundboard;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/socket")
public class WeboscketServer
{
	@Inject
	private WebsocketSessionManager websocketSessionManager;

	@OnOpen
	public void open(Session session) {
		websocketSessionManager.addSession(session);
	}

	@OnClose
	public void close(Session session) {
		websocketSessionManager.removeSession(session);
	}
}
