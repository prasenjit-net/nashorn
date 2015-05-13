package net.prasenjit.poc.nashorn;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonalizationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonalizationService.class);
	@Autowired
	private ScriptEngine scriptEngine;

	@Autowired
	private RuleRepository ruleRepository;

	public String getContentId(String scriptId, String input) {
		LOGGER.debug("loading common methods");
		CompiledScript commonScript = ruleRepository.loadRuleScript("commons");
		LOGGER.debug("loading rule");
		CompiledScript rule = ruleRepository.loadRuleScript(scriptId);
		LOGGER.debug("creating new binding");
		ScriptContext context = new SimpleScriptContext();
		context.setBindings(scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE), ScriptContext.ENGINE_SCOPE);
		Bindings bindings = scriptEngine.createBindings();
		bindings.put("input", input);
		context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

		try {
			LOGGER.debug("evaling common methods");
			commonScript.eval(context);
			LOGGER.debug("evaling rule script");
			Object returnObj = rule.eval(context);
			if (returnObj.getClass().isAssignableFrom(String.class)) {
				LOGGER.debug("returning string directly");
				return (String) returnObj;
			} else {
				LOGGER.debug("calling tostring before sending back");
				return returnObj.toString();
			}
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}
}
