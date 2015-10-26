/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitIdentifierColumnNameSource;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.RootEntitySourceImpl;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.SingularAttributeSourceBasic;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;

/**
 * Helper for coordinating the building of relational objects (Table, Column, Formula, etc)
 * from source representations
 *
 * @author Steve Ebersole
 */
public class RelationalObjectBinder {

	public static void bindImplicitSimpleIdColumn(
			RootEntitySourceImpl entitySource,
			SingularAttributeSourceBasic idAttributeSource,
			RootClass entityBinding,
			SimpleValue valueBinding,
			final RootAnnotationBindingContext bindingContext) {
		final Identifier columnName = bindingContext.getBuildingOptions().getImplicitNamingStrategy().determineIdentifierColumnName(
				new ImplicitIdentifierColumnNameSourceImpl( entitySource, idAttributeSource, bindingContext )
		);

		// todo : this is better to do after redesigning Table, Column, Formula etc as proposed on dev list
		final Table table = entityBinding.getRootTable();

		final Column column = new Column();
		column.setName(  );
		column.setValue( valueBinding );

		table.setPrimaryKey( new PrimaryKey() );

		Co
		table.getPrimaryKey().addColumn(  );
		valueBinding.addColumn(  );
	}

	private static Column makeColumn(Identifier columnName) {
		return new Column(  )
	}

	private static class ImplicitIdentifierColumnNameSourceImpl implements ImplicitIdentifierColumnNameSource {
		private final RootEntitySourceImpl root;
		private final SingularAttributeSourceBasic idAttributeSource;
		private final RootAnnotationBindingContext bindingContext;

		public ImplicitIdentifierColumnNameSourceImpl(
				RootEntitySourceImpl root,
				SingularAttributeSourceBasic idAttributeSource,
				RootAnnotationBindingContext bindingContext) {
			this.root = root;
			this.idAttributeSource = idAttributeSource;
			this.bindingContext = bindingContext;
		}

		@Override
		public EntityNaming getEntityNaming() {
			return root.getEntityNamingSource();
		}

		@Override
		public AttributePath getIdentifierAttributePath() {
			return idAttributeSource.getAttributePath();
		}

		@Override
		public MetadataBuildingContext getBuildingContext() {
			return bindingContext;
		}
	}
}
