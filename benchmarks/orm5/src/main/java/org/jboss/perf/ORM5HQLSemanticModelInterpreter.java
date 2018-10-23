package org.jboss.perf;

import java.lang.invoke.MethodHandle;
import java.util.Collections;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.ast.HqlParser;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;

public class ORM5HQLSemanticModelInterpreter extends AbstractBenchmarkHQLSemanticModelInterpreter {

	// using a ***STATIC FINAL*** method handle to invoke a private method without a performance penalty
	private static final MethodHandle PARSE = getMethodHandle( QueryTranslatorImpl.class, "parse", boolean.class );

	@Override
	public Object getSemanticModel(String hqlString) {
		try {
			QueryTranslatorImpl queryTranslator = new QueryTranslatorImpl( hqlString, hqlString, Collections.EMPTY_MAP,
																		   (SessionFactoryImplementor) entityManagerFactory
			);
			return ( (HqlParser) PARSE.invokeExact( queryTranslator, false ) ).getAST();
		}
		catch (Throwable throwable) {
			throwable.printStackTrace();
			return null;
		}
	}

}
