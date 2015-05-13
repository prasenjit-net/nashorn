package net.prasenjit.poc.nashorn;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RuleRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleRepository.class);

	private Compilable compilable;

	@Autowired
	private CacheManager cacheManager;

	@Cacheable("compiledScripts")
	public CompiledScript loadRuleScript(String scriptId) {
		LOGGER.info("loading script {} from gemfire", scriptId);
		try {
			ValueWrapper scriptEntry = cacheManager.getCache("personalizationRules").get(scriptId);
			if (scriptEntry != null) {
				return compilable.compile(scriptEntry.get().toString());
			} else {
				throw new RuntimeException("script with id " + scriptId + " not available");
			}
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	@Autowired
	public void setCompilable(ScriptEngine scriptEngine) {
		this.compilable = (Compilable) scriptEngine;
	}
}
