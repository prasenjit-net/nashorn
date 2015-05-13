package net.prasenjit.poc.nashorn.controller;

import net.prasenjit.poc.nashorn.PersonalizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScriptController {

	@Autowired
	private PersonalizationService personalizationService;

	@Autowired
	private CacheManager cacheManager;

	@RequestMapping("execute")
	public String executeScript(@RequestParam String scriptName, @RequestParam String input) {
		return personalizationService.getContentId(scriptName, input);
	}

	@RequestMapping("destroy")
	public String destroyScript(@RequestParam String scriptName) {
		ValueWrapper scriptEntry = cacheManager.getCache("personalizationRules").get(scriptName);
		if (scriptEntry != null) {
			cacheManager.getCache("personalizationRules").put(scriptName, scriptEntry.get());
		}
		return "Success";
	}
}
