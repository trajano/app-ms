package net.trajano.ms.engine.internal.resteasy;

import java.util.List;
import java.util.TreeMap;

import javax.ws.rs.core.AbstractMultivaluedMap;

public class CaseInsenstiveMultivalueStringMap extends AbstractMultivaluedMap<String, String> {

    public CaseInsenstiveMultivalueStringMap() {

        super(new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER));
    }

}
