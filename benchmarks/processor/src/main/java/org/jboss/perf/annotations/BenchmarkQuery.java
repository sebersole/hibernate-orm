package org.jboss.perf.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Inherited
@Target( ElementType.TYPE )
@Repeatable( BenchmarkQueries.class )
public @interface BenchmarkQuery {

    String name();

    String query();
}
