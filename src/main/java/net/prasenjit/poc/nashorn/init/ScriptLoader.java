package net.prasenjit.poc.nashorn.init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class ScriptLoader implements CommandLineRunner {
	private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	@Autowired
	private CacheManager cacheManager;

	@Override
	public void run(String... args) throws Exception {
		Resource[] resources = resolver.getResources("classpath:/scripts/*.txt");
		for (Resource resource : resources) {
			try (InputStream scriptStream = resource.getInputStream()) {
				String scriptString = StreamUtils.copyToString(scriptStream, Charset.defaultCharset());
				System.out.println(resource.getFilename());
				String name = resource.getFilename().split("\\.")[0];
				cacheManager.getCache("personalizationRules").put(name, scriptString);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
