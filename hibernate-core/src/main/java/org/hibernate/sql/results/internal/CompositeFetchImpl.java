/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.metamodel.model.domain.internal.SingularPersistentAttributeEmbedded;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.results.spi.CompositeFetch;
import org.hibernate.sql.results.spi.CompositeSqlSelectionGroup;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.InitializerCollector;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class CompositeFetchImpl extends AbstractFetchParent implements CompositeFetch, Initializer {
	private final FetchParent fetchParent;
	private final ColumnReferenceQualifier qualifier;
	private final SingularPersistentAttributeEmbedded fetchedNavigable;
	private final FetchStrategy fetchStrategy;

	private final CompositeSqlSelectionGroup sqlSelectionMappings;

	public CompositeFetchImpl(
			FetchParent fetchParent,
			ColumnReferenceQualifier qualifier,
			SingularPersistentAttributeEmbedded fetchedNavigable,
			FetchStrategy fetchStrategy,
			QueryResultCreationContext creationContext) {
		super(
				fetchedNavigable.getContainer(),
				fetchParent.getNavigablePath().append( fetchedNavigable.getNavigableName() )
		);
		this.fetchParent = fetchParent;
		this.qualifier = qualifier;
		this.fetchedNavigable = fetchedNavigable;
		this.fetchStrategy = fetchStrategy;

		this.sqlSelectionMappings = CompositeSqlSelectionGroupImpl.buildSqlSelectionGroup(
				fetchedNavigable.getEmbeddedDescriptor(),
				qualifier,
				creationContext
		);

		fetchParent.addFetch( this );
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public ColumnReferenceQualifier getSqlExpressionQualifier() {
		return qualifier;
	}

	@Override
	public EmbeddedTypeDescriptor getEmbeddedDescriptor() {
		return getFetchedNavigable().getEmbeddedDescriptor();
	}

	@Override
	public SingularPersistentAttributeEmbedded getFetchedNavigable() {
		return fetchedNavigable;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public boolean isNullable() {
		return fetchedNavigable.isOptional();
	}

	@Override
	public void registerInitializers(
			FetchParentAccess parentAccess,
			InitializerCollector collector) {
		collector.addInitializer( this );

		// todo (6.0) : wrong parent-access
		registerFetchInitializers( parentAccess, collector );
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		// nothing to do here... yet
		// todo (6.0) : this is one potential spot to managed resolving the composite's "state array" to the composite instance
	}
}
