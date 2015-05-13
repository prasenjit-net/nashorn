package net.prasenjit.poc.nashorn;

import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class CountingService {

	@Autowired
	private PersonalizationService personalizationService;

	@Async
	public Future<Long> countingAsync() {
		personalizationService.getContentId("script2", "Prasenjit");
		Long currentTime = System.currentTimeMillis();
		personalizationService.getContentId("script2", "Prasenjit");
		long totalTime = System.currentTimeMillis() - currentTime;
		return new AsyncResult<Long>(totalTime);
	}

	public long countingSync() {
		Long currentTime = System.currentTimeMillis();
		personalizationService.getContentId("script2", "Prasenjit");
		long totalTime = System.currentTimeMillis() - currentTime;
		return totalTime;
	}
}
