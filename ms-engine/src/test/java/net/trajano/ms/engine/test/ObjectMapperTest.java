package net.trajano.ms.engine.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.trajano.ms.engine.jaxrs.CommonObjectMapperProvider;
import net.trajano.ms.engine.sample.Blah;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectMapperTest {

    @Test
    public void testMapper() throws Exception {

        ObjectMapper objectMapper = new CommonObjectMapperProvider().getContext(null);
        final Blah value = new Blah();
        value.setHello("abc");
        final String str = objectMapper.writer().writeValueAsString(value);
        Blah value2 = objectMapper.readerFor(Blah.class).readValue(str);
        assertEquals(value.getHello(), value2.getHello());
    }
}
