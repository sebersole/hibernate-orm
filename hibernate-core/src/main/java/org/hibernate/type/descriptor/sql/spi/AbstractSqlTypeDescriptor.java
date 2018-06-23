/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.sql.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqlTypeDescriptor implements SqlTypeDescriptor {
	private final Map<JavaTypeDescriptor<?>,JdbcValueMapper<?>> valueMapperCache = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	protected <J> JdbcValueMapper<J> determineValueMapper(
			JavaTypeDescriptor<J> javaTypeDescriptor,
			Function<JavaTypeDescriptor<J>,JdbcValueMapper<J>> creator) {
		return (JdbcValueMapper<J>) valueMapperCache.computeIfAbsent(
				javaTypeDescriptor,
				javaTypeDescriptor1 -> creator.apply( javaTypeDescriptor )
		);
	}

	// todo (6.0) : implement support for creating the JdbcValueMapper here via protected createBinder & createExtractor methods
	//		rather than the Function
}
