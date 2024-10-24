/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.bind.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.models.bind.spi.BindingContext;
import org.hibernate.boot.models.bind.spi.BindingOptions;
import org.hibernate.boot.models.bind.spi.BindingState;
import org.hibernate.boot.models.categorize.spi.AggregatedKeyMapping;
import org.hibernate.boot.models.categorize.spi.AttributeMetadata;
import org.hibernate.boot.models.categorize.spi.BasicKeyMapping;
import org.hibernate.boot.models.categorize.spi.EntityHierarchy;
import org.hibernate.boot.models.categorize.spi.EntityTypeMetadata;
import org.hibernate.boot.models.categorize.spi.KeyMapping;
import org.hibernate.boot.models.categorize.spi.NonAggregatedKeyMapping;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Table;
import org.hibernate.models.spi.AnnotationUsage;
import org.hibernate.models.spi.MemberDetails;

import jakarta.persistence.Column;

/**
 * Binding for an entity identifier
 *
 * @author Steve Ebersole
 */
public class IdentifierBinding extends Binding implements Observable<KeyValue> {
	private final KeyValue keyValue;

	private boolean resolved;
	private List<ResolutionCallback<KeyValue>> resolutionCallbacks;

	public IdentifierBinding(
			RootEntityBinding rootEntityBinding,
			EntityTypeMetadata entityTypeMetadata,
			BindingOptions options,
			BindingState state,
			BindingContext context) {
		super( options, state, context );

		this.keyValue = prepareMappingValue( rootEntityBinding, entityTypeMetadata, this, options, state, context );
	}

	public KeyValue getValue() {
		return keyValue;
	}

	@Override
	public KeyValue getBinding() {
		return getValue();
	}

	private static KeyValue prepareMappingValue(
			RootEntityBinding rootEntityBinding,
			EntityTypeMetadata entityTypeMetadata,
			IdentifierBinding binding,
			BindingOptions options,
			BindingState state,
			BindingContext context) {
		final RootClass rootClass = rootEntityBinding.getPersistentClass();
		final EntityHierarchy hierarchy = entityTypeMetadata.getHierarchy();
		final KeyMapping idMapping = hierarchy.getIdMapping();
		final Table table = rootClass.getTable();

		if ( idMapping instanceof BasicKeyMapping basicKeyMapping ) {
			return prepareBasicIdentifier( basicKeyMapping, table, rootClass, binding, options, state, context );
		}
		else if ( idMapping instanceof AggregatedKeyMapping aggregatedKeyMapping ) {
			return bindAggregatedIdentifier( aggregatedKeyMapping, table, rootClass, options, state, context );
		}
		else {
			return bindNonAggregatedIdentifier( (NonAggregatedKeyMapping) idMapping, table, rootClass, options, state, context );
		}
	}

	private static KeyValue prepareBasicIdentifier(
			BasicKeyMapping basicKeyMapping,
			Table table,
			RootClass rootClass,
			IdentifierBinding binding,
			BindingOptions options,
			BindingState state,
			BindingContext context) {
		final AttributeMetadata idAttribute = basicKeyMapping.getAttribute();
		final MemberDetails idAttributeMember = idAttribute.getMember();

		final BasicValue idValue = new BasicValue( state.getMetadataBuildingContext(), table );
		rootClass.setIdentifier( idValue );
		idValue.setTable( table );

		final Property idProperty = new Property();
		idProperty.setName( idAttribute.getName() );
		idProperty.setValue( idValue );
		idProperty.setPersistentClass( rootClass );

		rootClass.addProperty( idProperty );
		rootClass.setIdentifierProperty( idProperty );

		final PrimaryKey primaryKey = table.getPrimaryKey();

		final AnnotationUsage<Column> idColumnAnn = idAttributeMember.getAnnotationUsage( Column.class );
		final org.hibernate.mapping.Column column = ColumnHelper.bindColumn(
				idColumnAnn,
				() -> "id",
				true,
				false
		);
		idValue.addColumn( column, true, false );
		primaryKey.addColumn( column );

		BasicValueHelper.bindImplicitJavaType( idAttributeMember, idValue, options, state, context );
		BasicValueHelper.bindJavaType( idAttributeMember, idValue, options, state, context );
		BasicValueHelper.bindJdbcType( idAttributeMember, idValue, options, state, context );
		BasicValueHelper.bindNationalized( idAttributeMember, idValue, options, state, context );

		binding.resolve();

		return idValue;
	}

	private static KeyValue bindAggregatedIdentifier(
			AggregatedKeyMapping aggregatedKeyMapping,
			Table table,
			RootClass rootClass,
			BindingOptions options,
			BindingState state,
			BindingContext context) {
		final Component idValue = new Component( state.getMetadataBuildingContext(), rootClass );
		rootClass.setIdentifier( idValue );
		idValue.setTable( table );

		final Property idProperty = new Property();
		idProperty.setValue( idValue );
		rootClass.setIdentifierProperty( idProperty );

		// todo : need an EmbeddableBinding

		return idValue;
	}

	private static KeyValue bindNonAggregatedIdentifier(
			NonAggregatedKeyMapping idMapping,
			Table table,
			RootClass rootClass,
			BindingOptions options,
			BindingState state,
			BindingContext context) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}


	@Override
	public void whenResolved(ResolutionCallback<KeyValue> callback) {
		if ( resolved ) {
			callback.handleResolution( keyValue );
			return;
		}

		if ( resolutionCallbacks == null ) {
			resolutionCallbacks = new ArrayList<>();
		}
		resolutionCallbacks.add( callback );
	}

	public void resolve() {
		ObservableHelper.processCallbacks( keyValue, resolutionCallbacks );
		resolutionCallbacks = null;
		resolved = true;
	}
}
