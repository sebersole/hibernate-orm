package org.jboss.perf.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Inherited
@Target( ElementType.TYPE )
public @interface BenchmarkQueries {

    BenchmarkQuery[] value();
}
