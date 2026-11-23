package com.delver.shorturl.application;

import io.hypersistence.tsid.TSID;
import org.springframework.stereotype.Component;

@Component
public class Base62ShortCodeGenerator implements ShortCodeGenerator {

    @Override
    public String generate() {
        return TSID.Factory.getTsid().encode(62);
    }
}
