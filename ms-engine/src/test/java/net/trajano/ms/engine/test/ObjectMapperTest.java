package net.trajano.ms.engine.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.trajano.ms.engine.internal.spring.SpringConfiguration;
import net.trajano.ms.engine.jaxrs.CommonObjectMapperProvider;
import net.trajano.ms.engine.sample.Blah;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CommonObjectMapperProvider.class,
    SpringConfiguration.class
})
public class ObjectMapperTest {

    @Autowired
    private CommonObjectMapperProvider objectMapperProvider;

    @Test
    public void testMapper() throws Exception {

        final ObjectMapper objectMapper = objectMapperProvider.getContext(null);
        final Blah value = new Blah();
        value.setHello("abc");
        final ObjectWriter writer = objectMapper.writer();
        final String str = writer.writeValueAsString(value);
        final Blah value2 = objectMapper.readerFor(Blah.class).readValue(str);
        assertEquals(value.getHello(), value2.getHello());
    }
}
