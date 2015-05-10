package net.prasenjit.poc.nashorn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Created by PRASENJIT on 5/10/2015.
 */
@Transactional
@Service
public class ImportantService {

    @Autowired
    private CacheManager cacheManager;

    public void doSomethingImportant(boolean error) {
        cacheManager.getCache("region1").put("key1", "value1");
        if (error) throw new RuntimeException("rollback it");
        cacheManager.getCache("region2").put("key2", "value2");
    }
}
