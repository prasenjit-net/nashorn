package net.prasenjit.poc.nashorn;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableTransactionManagement
public class NashornDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NashornDemoApplication.class, args);
    }

    @Bean
    public Cache gemfireCache() {
        Cache cache = new CacheFactory()
                .set("mcast-port", "0")
                .create();
        cache.createRegionFactory(RegionShortcut.LOCAL).create("compiledScripts");
        cache.createRegionFactory(RegionShortcut.LOCAL).create("region1");
        cache.createRegionFactory(RegionShortcut.LOCAL).create("region2");
        return cache;
    }

    @Bean
    public GemfireCacheManager cacheManager(Cache cache){
        GemfireCacheManager cacheManager = new GemfireCacheManager();
        cacheManager.setCache(cache);
        return cacheManager;
    }

    @Bean
    public ScriptEngine scriptEngine(){
        return new ScriptEngineManager().getEngineByName("Nashorn");
    }
}
