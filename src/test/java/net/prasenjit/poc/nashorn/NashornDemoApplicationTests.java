package net.prasenjit.poc.nashorn;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NashornDemoApplication.class)
@WebAppConfiguration
public class NashornDemoApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(NashornDemoApplicationTests.class);

    @Autowired
    private NashornCompileService compileService;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testCacheble() {
        compileService.getCompiledScript("script1.js");
        compileService.getCompiledScript("script1.js");
        compileService.getCompiledScript("script1.js");
        compileService.getCompiledScript("script1.js");
        compileService.getCompiledScript("script2.js");
        compileService.getCompiledScript("script2.js");
        compileService.getCompiledScript("script2.js");
        compileService.getCompiledScript("script2.js");
    }

    @Test
    public void testEval1() throws ScriptException {
        CompiledScript script1 = compileService.getCompiledScript("script1.js");
        Object returnValue = script1.eval(new SimpleBindings());
        LOGGER.info("Found return {}", returnValue);
        Assert.assertEquals(new Double(1.0), returnValue);

        returnValue = script1.eval(new SimpleBindings());
        LOGGER.info("Found return {}", returnValue);
        Assert.assertEquals(new Double(1.0), returnValue);

        returnValue = script1.eval(new SimpleBindings());
        LOGGER.info("Found return {}", returnValue);
        Assert.assertEquals(new Double(1.0), returnValue);
    }

    @Test
    public void testEval2() throws ScriptException {
        CompiledScript script1 = compileService.getCompiledScript("script2.js");
        Object returnValue = script1.eval(new SimpleBindings());
        LOGGER.info("Found return {}", returnValue);
        Assert.assertEquals("return from function", returnValue);

        returnValue = script1.eval(new SimpleBindings());
        LOGGER.info("Found return {}", returnValue);
        Assert.assertEquals("return from function", returnValue);

        returnValue = script1.eval(new SimpleBindings());
        LOGGER.info("Found return {}", returnValue);
        Assert.assertEquals("return from function", returnValue);
    }

}
