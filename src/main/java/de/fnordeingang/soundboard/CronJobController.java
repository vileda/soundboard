package de.fnordeingang.soundboard;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class CronJobController {
	@Resource
	private TimerService timerService;

	@Inject
	private SoundfileQueue soundfileQueue;

	private final Map<String, Timer> timerMap = new HashMap<>();

	private static Logger logger = Logger.getLogger(CronJobController.class.getName());

	public String createTimer(CreateTimerRequest request) {
		logger.info("cronjob " + request);
		ScheduleExpression schedule = new ScheduleExpression();

		schedule.year(getCronValue(request.getYear()));
		schedule.month(getCronValue(request.getMonth()));
		schedule.dayOfMonth(getCronValue(request.getDay()));
		schedule.hour(getCronValue(request.getHour()));
		schedule.minute(getCronValue(request.getMinute()));
		schedule.second(getCronValue(request.getSecond()));

		String timerId = UUID.randomUUID().toString();
		timerMap.put(
				timerId,
				timerService.createCalendarTimer(schedule, new TimerConfig(request, false))
		);

		return timerId;
	}

	public void clearTimer(String timerId) {
		Timer timer = timerMap.get(timerId);
		timer.cancel();
		timerMap.remove(timerId);
	}

	public Map<String, Timer> getTimers() {
		return timerMap;
	}

	private String getCronValue(String value) {
		return !isEmpty(value) ? value : "*";
	}

	@Timeout
	public void cronJob(Timer timer) {
		CreateTimerRequest timerRequest = (CreateTimerRequest) timer.getInfo();

		if(isEmpty(timerRequest.getFilePath())) return;

		soundfileQueue.add(timerRequest.getFilePath());
		logger.info("cronjob " + timerRequest);
	}
}
