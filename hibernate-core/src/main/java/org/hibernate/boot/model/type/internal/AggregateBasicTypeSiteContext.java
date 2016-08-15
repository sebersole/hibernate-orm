/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.type.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.persistence.EnumType;
import javax.persistence.TemporalType;

import org.hibernate.boot.model.type.spi.BasicTypeSiteContext;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.mapper.spi.basic.AttributeConverterDefinition;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.type.descriptor.spi.MutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class AggregateBasicTypeSiteContext implements BasicTypeSiteContext {
	private List<BasicTypeSiteContext> delegates = new ArrayList<>();

	public AggregateBasicTypeSiteContext() {
	}

	public AggregateBasicTypeSiteContext(BasicTypeSiteContext... delegates) {
		if ( delegates != null ) {
			Collections.addAll( this.delegates, delegates );
		}
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return findFirstNonNull( BasicTypeSiteContext::getJavaTypeDescriptor );
	}

	private <T> T findFirstNonNull(Function<BasicTypeSiteContext,T> extractor) {
		for ( BasicTypeSiteContext delegate : delegates ) {
			final T value = extractor.apply( delegate );
			if ( value != null ) {
				return value;
			}
		}

		return null;
	}

	@Override
	public SqlTypeDescriptor getSqlTypeDescriptor() {
		return findFirstNonNull( BasicTypeSiteContext::getSqlTypeDescriptor );
	}

	@Override
	public AttributeConverterDefinition getAttributeConverterDefinition() {
		return findFirstNonNull( BasicTypeSiteContext::getAttributeConverterDefinition );
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return findFirstNonNull( BasicTypeSiteContext::getMutabilityPlan );
	}

	@Override
	public Comparator getComparator() {
		return findFirstNonNull( BasicTypeSiteContext::getComparator );
	}

	@Override
	public TemporalType getTemporalPrecision() {
		return findFirstNonNull( BasicTypeSiteContext::getTemporalPrecision );
	}

	@Override
	public Map getLocalTypeParameters() {
		return findFirstNonNull( BasicTypeSiteContext::getLocalTypeParameters );
	}

	@Override
	public boolean isId() {
		return hasAnyTrue( BasicTypeSiteContext::isId );
	}

	private boolean hasAnyTrue(Predicate<BasicTypeSiteContext> predicate) {
		for ( BasicTypeSiteContext delegate : delegates ) {
			final boolean value = predicate.test( delegate );
			if ( value ) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isVersion() {
		return hasAnyTrue( BasicTypeSiteContext::isVersion );
	}

	@Override
	public boolean isNationalized() {
		return hasAnyTrue( BasicTypeSiteContext::isNationalized );
	}

	@Override
	public boolean isLob() {
		return hasAnyTrue( BasicTypeSiteContext::isLob );
	}

	@Override
	public EnumType getEnumeratedType() {
		return findFirstNonNull( BasicTypeSiteContext::getEnumeratedType );
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return findFirstNonNull( BasicTypeSiteContext::getTypeConfiguration );
	}
}
