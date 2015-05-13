package net.prasenjit.poc.nashorn;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NashornDemoApplication.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CQTest {

	@Autowired
	private CacheManager cacheManager;

	@Test
	public void test() {
		cacheManager.getCache("personalizationRules").put("script5", Math.random());
		cacheManager.getCache("personalizationRules").evict("script5");
	}

}
