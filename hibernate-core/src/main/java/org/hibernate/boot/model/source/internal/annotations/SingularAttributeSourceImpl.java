/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.SingularAttribute;
import org.hibernate.boot.model.source.spi.HibernateTypeSource;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.model.source.spi.SingularAttributeSource;

/**
 * @author Hardy Ferentschik
 */
public abstract class SingularAttributeSourceImpl implements SingularAttributeSource {
	private final SingularAttribute attribute;
	private final HibernateTypeSourceImpl type;

	public SingularAttributeSourceImpl(SingularAttribute attribute) {
		this.attribute = attribute;
		this.type = new HibernateTypeSourceImpl( attribute );
	}

	@Override
	public PersistentAttribute getAnnotatedAttribute() {
		return attribute;
	}

	@Override
	public HibernateTypeSource getTypeInformation() {
		return type;
	}

	@Override
	public String getPropertyAccessorName() {
		return attribute.getAccessorStrategy();
	}

	@Override
	public NaturalIdMutability getNaturalIdMutability() {
		return attribute.getNaturalIdMutability();
	}

	@Override
	public boolean isIncludedInOptimisticLocking() {
		return attribute.isIncludeInOptimisticLocking();
	}

	@Override
	public String getName() {
		return attribute.getName();
	}


//	/**
//	 * very ugly, can we just return the columnSourceImpl anyway?
//	 */
//	@Override
//	public List<RelationalValueSource> relationalValueSources() {
//		if ( relationalValueSources == null ) {
//			relationalValueSources = buildRelationalValueSources();
//		}
//		return relationalValueSources;
//	}
//
//	private List<RelationalValueSource> buildRelationalValueSources() {
//		List<RelationalValueSource> results = new ArrayList<RelationalValueSource>();
//
////		if ( attributeOverride != null ) {
////			attributeOverride.apply( attribute );
////		}
//
//		boolean hasDefinedColumnSource = !attribute.getColumnValues().isEmpty();
//		if ( hasDefinedColumnSource ) {
//			for ( Column columnValues : attribute.getColumnValues() ) {
//				results.add( new ColumnSourceImpl( attribute, columnValues ) );
//			}
//		}
//		else if ( attribute.getFormulaValue() != null ) {
//			results.add( new DerivedValueSourceImpl( attribute.getFormulaValue() ) );
//		}
//		else if ( attribute instanceof BasicAttribute ) {
//			//for column transformer
//			BasicAttribute simpleAttribute = BasicAttribute.class.cast( attribute );
//			if ( simpleAttribute.getCustomReadFragment() != null && simpleAttribute.getCustomWriteFragment() != null ) {
//				results.add( new ColumnSourceImpl( attribute, null ) );
//			}
//		}
//		return results;
//	}

	@Override
	public boolean isVirtualAttribute() {
		return false;
	}

	@Override
	public boolean isSingular() {
		return true;
	}
}


