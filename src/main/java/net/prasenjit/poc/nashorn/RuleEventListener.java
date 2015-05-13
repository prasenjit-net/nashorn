package net.prasenjit.poc.nashorn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.listener.ContinuousQueryListener;
import org.springframework.stereotype.Component;

import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.query.CqEvent;

@Component
public class RuleEventListener implements ContinuousQueryListener {
	
	@Autowired
	private ClientCache clientCache;

	@Override
	public void onEvent(CqEvent event) {
		System.err.println("Query event " + event.getBaseOperation() + " key " + event.getKey());
		Operation operation = event.getBaseOperation();
		if (operation.isDestroy() || operation.isUpdate()) {
			clientCache.getRegion("compiledScripts").remove(event.getKey());
		}else if (operation.isRegionDestroy() || operation.isRegionInvalidate()) {
			clientCache.getRegion("compiledScripts").clear();
		}
	}

}
