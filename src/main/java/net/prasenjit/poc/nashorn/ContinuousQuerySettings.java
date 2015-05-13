package net.prasenjit.poc.nashorn;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.listener.ContinuousQueryDefinition;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;

import com.gemstone.gemfire.cache.RegionService;

@Configuration
public class ContinuousQuerySettings {

	private static final String RULE_LISTENER_QUERY = "select * from /personalizationRules";

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public ContinuousQueryListenerContainer container(RegionService cache) {
		ContinuousQueryListenerContainer container = new ContinuousQueryListenerContainer();
		container.setCache(cache);
		container.setAutoStartup(true);
		Map<String, ContinuousQueryDefinition> queryMaps = applicationContext.getBeansOfType(ContinuousQueryDefinition.class);
		Set<ContinuousQueryDefinition> queries = new HashSet<>();
		for (Entry<String, ContinuousQueryDefinition> definitionEntry : queryMaps.entrySet()) {
			ContinuousQueryDefinition definition = definitionEntry.getValue();
			queries.add(definition);
		}
		container.setQueryListeners(queries);
		return container;
	}

	@Bean
	public ContinuousQueryDefinition createRuleListener(RuleEventListener listener) {
		ContinuousQueryDefinition definition = new ContinuousQueryDefinition(RULE_LISTENER_QUERY, listener);
		return definition;
	}
}
