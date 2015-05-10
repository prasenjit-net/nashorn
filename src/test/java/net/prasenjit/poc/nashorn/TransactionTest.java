package net.prasenjit.poc.nashorn;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by PRASENJIT on 5/10/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NashornDemoApplication.class)
@WebAppConfiguration
public class TransactionTest {

    @Autowired
    private ImportantService importantService;

    @Autowired
    private CacheManager cacheManager;

    @Before
    public void clearRegions(){
        cacheManager.getCache("region1").clear();
        cacheManager.getCache("region2").clear();
    }

    @Test
    public void successTest() {
        try {
            importantService.doSomethingImportant(false);
        } catch (RuntimeException e) {
        }
        Cache.ValueWrapper actual1 = cacheManager.getCache("region1").get("key1");
        Assert.assertEquals("value1", actual1.get());
        Cache.ValueWrapper actual2 = cacheManager.getCache("region2").get("key2");
        Assert.assertEquals("value2", actual2.get());
    }

    @Test
    public void errorTest() {
        try {
            importantService.doSomethingImportant(true);
        } catch (RuntimeException e) {
        }
        Cache.ValueWrapper actual1 = cacheManager.getCache("region1").get("key1");
        Assert.assertEquals("value1", actual1.get());
        Cache.ValueWrapper actual2 = cacheManager.getCache("region2").get("key2");
        Assert.assertEquals(null, actual2);
    }
}
