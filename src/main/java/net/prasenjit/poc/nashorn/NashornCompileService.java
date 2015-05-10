package net.prasenjit.poc.nashorn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by PRASENJIT on 5/10/2015.
 */
@Service
public class NashornCompileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NashornCompileService.class);

    private Compilable compilable;

    @Cacheable(value = "compiledScripts")
    public CompiledScript getCompiledScript(String scriptId) {
        LOGGER.info("Compiling script {}", scriptId);
        ClassPathResource script = new ClassPathResource(scriptId);
        try (InputStream scriptStream = script.getInputStream()) {
            return compilable.compile(new InputStreamReader(scriptStream));
        } catch (ScriptException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public void setScriptEngine(ScriptEngine scriptEngine){
        compilable = (Compilable) scriptEngine;
    }
}
