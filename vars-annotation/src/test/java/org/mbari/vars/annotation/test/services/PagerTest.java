package org.mbari.vars.annotation.test.services;


import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.etc.rxjava.Pager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PagerTest {

    @Test
    public void testPager() {
        var data = new ArrayList<Integer>();
        for (var i = 0; i < 1000; i++) {
            data.add(i);
        }

        var pager = new Pager<>((limit, offset) -> {
            var start = offset;
            Long end = limit + offset;
            if (start > data.size()) {
                start = (long) data.size() - 1;
            }
            if (end >= data.size()) {
                end = (long) data.size();
            }
            return data.subList(start.intValue(), end.intValue());

        }, (long) data.size(), 10L);

        var n = new AtomicInteger(0);
        pager.getObservable()
                .subscribe(xs -> n.addAndGet(xs.size()),
                        ex -> fail(),
                        () -> assertEquals(data.size(), n.get()));
        pager.run();

    }
}
