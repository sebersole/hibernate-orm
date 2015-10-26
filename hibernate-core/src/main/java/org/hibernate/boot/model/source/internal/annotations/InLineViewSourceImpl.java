/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.spi.InLineViewSource;

import org.jboss.jandex.AnnotationInstance;

/**
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public class InLineViewSourceImpl implements InLineViewSource {
	private final InlineViewInfo inlineViewInfo;

	public InLineViewSourceImpl(EntityTypeMetadata entityTypeMetadata, AnnotationInstance subselectAnnotation) {
		this.inlineViewInfo = new InlineViewInfo( subselectAnnotation.value().asString(), entityTypeMetadata.getEntityName() );
	}

	public String getSelectStatement() {
		return inlineViewInfo.getSelectStatement();
	}

	@Override
	public String getExplicitSchemaName() {
		return null;
	}

	@Override
	public String getExplicitCatalogName() {
		return null;
	}

	public String getLogicalName() {
		return inlineViewInfo.getLogicalName();
	}

	private static class InlineViewInfo {
		private final String selectStatement;
		private final String logicalName;

		private InlineViewInfo(String selectStatement, String logicalName) {
			this.selectStatement = selectStatement;
			this.logicalName = logicalName;
		}

		public String getSelectStatement() {
			return selectStatement;
		}

		public String getLogicalName() {
			return logicalName;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			InlineViewInfo that = ( InlineViewInfo ) o;

			if ( logicalName != null ? !logicalName.equals( that.logicalName ) : that.logicalName != null ) {
				return false;
			}
			if ( selectStatement != null ? !selectStatement.equals( that.selectStatement ) : that.selectStatement != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = selectStatement != null ? selectStatement.hashCode() : 0;
			result = 31 * result + ( logicalName != null ? logicalName.hashCode() : 0 );
			return result;
		}
	}
}
