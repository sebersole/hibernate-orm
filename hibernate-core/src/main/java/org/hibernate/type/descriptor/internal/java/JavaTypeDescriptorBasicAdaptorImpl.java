/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.util.Comparator;

import org.hibernate.HibernateException;
import org.hibernate.annotations.Immutable;
import org.hibernate.internal.util.compare.ComparableComparator;
import org.hibernate.type.descriptor.spi.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.MutabilityPlan;
import org.hibernate.type.descriptor.spi.MutableMutabilityPlan;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.spi.java.basic.AbstractTypeDescriptorBasicImpl;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;


/**
 * AbstractTypeDescriptorBasicImpl adaptor for cases where we do not know a
 * proper JavaTypeDescriptor for a given Java type.
 *
 * @author Steve Ebersole
 */
public class JavaTypeDescriptorBasicAdaptorImpl<T> extends AbstractTypeDescriptorBasicImpl<T> {
	public JavaTypeDescriptorBasicAdaptorImpl(Class<T> type) {
		this( type, determineMutabilityPlan( type ) );
	}

	@SuppressWarnings("unchecked")
	private static <T> MutabilityPlan<T> determineMutabilityPlan(final Class<T> type) {
		// really we are interested in the @Immutable annotation on the field/getter, but we
		// do not have access to that here...
		if ( type.isAnnotationPresent( Immutable.class ) ) {
			return ImmutableMutabilityPlan.INSTANCE;
		}
		// MutableMutabilityPlan is the "safest" option, but we do not necessarily know how to deepCopy etc...
		return new MutableMutabilityPlan<T>() {
			@Override
			protected T deepCopyNotNull(T value) {
				throw new HibernateException(
						"Not known how to deep copy value of type [" + type.getName() + "]"
				);
			}
		};
	}

	protected JavaTypeDescriptorBasicAdaptorImpl(Class<T> type, MutabilityPlan<T> mutabilityPlan) {
		this( type, mutabilityPlan, determineComparator( type ) );
	}

	private static <T> Comparator determineComparator(Class<T> type) {
		return Comparable.class.isAssignableFrom( type )
				? ComparableComparator.INSTANCE
				: null;
	}

	public JavaTypeDescriptorBasicAdaptorImpl(Class<T> type, MutabilityPlan<T> mutabilityPlan, Comparator comparator) {
		super( type, mutabilityPlan, comparator );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		throw new UnsupportedOperationException(
				"Recommended SqlTypeDescriptor not known for this Java type : " + getJavaTypeClass().getName()
		);
	}

	@Override
	public String toString(T value) {
		return value == null ? "<null>" : value.toString();
	}

	@Override
	public T fromString(String string) {
		throw new HibernateException(
				"Not known how to convert String to given Java type [" + getJavaTypeClass().getName() + "]"
		);
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		throw new UnsupportedOperationException(
				"Unwrap strategy not known for this Java type : " + getJavaTypeClass().getName()
		);
	}

	@Override
	public <X> T wrap(X value, WrapperOptions options) {
		throw new UnsupportedOperationException(
				"Wrap strategy not known for this Java type : " + getJavaTypeClass().getName()
		);
	}
}
