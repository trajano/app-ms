package net.trajano.ms.example.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.example.SampleMS;

/**
 * This tests that all the autowired components and configuration are set up
 * correctly so that the application would actually start up.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleMSTest {

    @Autowired
    private SampleMS sampleMs;

    @Test
    public void test() {

        assertNotNull(sampleMs);
    }
}
