package net.trajano.ms.gateway.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.vertx.core.Vertx;
import net.trajano.ms.gateway.GatewayMS;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayMS.class)
public class SpringTest {

    @Autowired
    private Vertx vertx;

    @Test
    public void exampleTest() {

        Assert.assertNotNull(vertx);
    }
}
