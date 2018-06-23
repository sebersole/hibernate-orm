/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.sql.spi;

import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;

/**
 * Abstract SqlTypeDescriptor that defines templated support for managing
 * JdbcValueMapper instances.  If a mapper for a given {@link BasicJavaDescriptor}
 * does not yet exist for this SqlTypeDescriptor, one is created and cached.
 *
 * between this SqlTypeDescriptor
 *
 *
 * @author Steve Ebersole
 */
public abstract class AbstractTemplateSqlTypeDescriptor extends AbstractSqlTypeDescriptor {
	/**
	 * @implSpec Defined as final since the whole point of this base class is to support
	 * this method via templating.  If sub classes want to re-define how this method works
	 * consider extending from {@link AbstractSqlTypeDescriptor} instead
	 */
	@Override
	public final <X> JdbcValueMapper getJdbcValueMapper(BasicJavaDescriptor<X> javaTypeDescriptor) {
		return determineValueMapper(
				javaTypeDescriptor,
				jtd -> {
					final JdbcValueBinder<X> binder = createBinder( javaTypeDescriptor );
					final JdbcValueExtractor<X> extractor = createExtractor( javaTypeDescriptor );

					return new JdbcValueMapperImpl( javaTypeDescriptor, this, extractor, binder );
				}
		);
	}

	/**
	 * Called from {@link #getJdbcValueMapper} when needing to create the mapper.
	 *
	 * @implNote The value returned from here will be the {@link JdbcValueMapper#getJdbcValueBinder()}
	 * for the mapper returned from {@link #getJdbcValueMapper}
	 */
	protected abstract <X> JdbcValueBinder<X> createBinder(final BasicJavaDescriptor<X> javaTypeDescriptor);

	/**
	 * Called from {@link #getJdbcValueMapper} when needing to create the mapper.
	 *
	 * @implNote The value returned from here will be the {@link JdbcValueMapper#getJdbcValueExtractor}
	 * for the mapper returned from {@link #getJdbcValueMapper}
	 */
	protected abstract <X> JdbcValueExtractor<X> createExtractor(final BasicJavaDescriptor<X> javaTypeDescriptor);
}
