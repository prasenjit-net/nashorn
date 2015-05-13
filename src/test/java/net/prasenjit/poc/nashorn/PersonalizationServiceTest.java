package net.prasenjit.poc.nashorn;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NashornDemoApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersonalizationServiceTest {

	@Autowired
	private PersonalizationService personalizationService;

	@Autowired
	private CountingService asyncCountingService;

	@Test
	public void testGetContentId() {
		String contentId = personalizationService.getContentId("script2", "Prasenjit");
		assertEquals("return from function Prasenjit", contentId);

		contentId = personalizationService.getContentId("script3", "Prasenjit");
		assertEquals("return from script3 Prasenjit", contentId);

		contentId = personalizationService.getContentId("script4", "Prasenjit");
		assertEquals("return from script4 Prasenjit", contentId);
		
		contentId = personalizationService.getContentId("script5", null);
		assertEquals("1.0", contentId);
		
		contentId = personalizationService.getContentId("script5", null);
		assertEquals("1.0", contentId);
	}

	@Test
	public void loadTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 500; i++) {
			personalizationService.getContentId("script2", "Prasenjit");
		}
		long totalTime = System.currentTimeMillis() - startTime;
		System.err.println("Time taken to execute 500 times is " + totalTime + " miliseconds");
		System.err.println("Time per request " + (totalTime / 500L) + " miliseconds");
	}

	@Test
	public void asyncLoadTest() throws InterruptedException, ExecutionException {
		List<Future<Long>> results = new ArrayList<>();
		Future<Long> result = null;
		long totalTime = 0;
		for (int i = 0; i < 500; i++) {
			result = asyncCountingService.countingAsync();
			results.add(result);
		}

		Thread.sleep(2000);
		for (Future<Long> future : results) {
			totalTime += future.get();
		}
		System.err.println("Async Time taken to execute 500 times is " + totalTime + " miliseconds");
		System.err.println("Async Time per request " + (totalTime / 500L) + " miliseconds");
	}

}
