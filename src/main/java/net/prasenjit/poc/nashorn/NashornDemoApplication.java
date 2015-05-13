package net.prasenjit.poc.nashorn;

import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableTransactionManagement
public class NashornDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NashornDemoApplication.class, args);
	}

	@Bean
	public ClientCache gemfireCache() {
		ClientCache cache = new ClientCacheFactory()
//				.setPdxSerializer(new ReflectionBasedAutoSerializer())
				.setPoolSubscriptionEnabled(true)
				.addPoolLocator("localhost", 10334).create();
		cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("personalizationRules");
		cache.createClientRegionFactory(ClientRegionShortcut.LOCAL).create("compiledScripts");
		return cache;
	}

	@Bean
	public GemfireCacheManager cacheManager(ClientCache cache) {
		GemfireCacheManager cacheManager = new GemfireCacheManager();
		Set<Region<?, ?>> regions = cache.rootRegions();
		cacheManager.setRegions(regions);
		return cacheManager;
	}

	@Bean
	public ScriptEngine scriptEngine() {
		return new ScriptEngineManager().getEngineByName("Nashorn");
	}

	@Bean
	public PlatformTransactionManager transactionManager(ClientCache cache) {
		ClientCacheTransactionManager transactionManager = new ClientCacheTransactionManager(cache);
		transactionManager.setCopyOnRead(false);
		return transactionManager;
	}
	
}
