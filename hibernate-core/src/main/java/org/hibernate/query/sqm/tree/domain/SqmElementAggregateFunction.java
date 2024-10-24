/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.metamodel.model.domain.PluralPersistentAttribute;
import org.hibernate.query.hql.spi.SqmCreationState;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;

/**
 * @author Steve Ebersole
 */
public class SqmElementAggregateFunction<T> extends AbstractSqmSpecificPluralPartPath<T> {
	private final String functionName;

	public SqmElementAggregateFunction(SqmPath<?> pluralDomainPath, String functionName) {
		//noinspection unchecked
		super(
				pluralDomainPath.getNavigablePath().getParent().append( pluralDomainPath.getNavigablePath().getLocalName(), "{" + functionName + "-element}" ),
				pluralDomainPath,
				(PluralPersistentAttribute<?, ?, ?>) pluralDomainPath.getReferencedPathSource(),
				( (PluralPersistentAttribute<?, ?, T>) pluralDomainPath.getReferencedPathSource() ).getElementPathSource()
		);
		this.functionName = functionName;
	}

	@Override
	public SqmElementAggregateFunction<T> copy(SqmCopyContext context) {
		final SqmElementAggregateFunction<T> existing = context.getCopy( this );
		if ( existing != null ) {
			return existing;
		}

		final SqmElementAggregateFunction<T> path = context.registerCopy(
				this,
				new SqmElementAggregateFunction<>(
						getLhs().copy( context ),
						functionName
				)
		);
		copyTo( path, context );
		return path;
	}

	public String getFunctionName() {
		return functionName;
	}

	@Override
	public SqmPath<?> resolvePathPart(
			String name,
			boolean isTerminal,
			SqmCreationState creationState) {
		final SqmPath<?> sqmPath = get( name );
		creationState.getProcessingStateStack().getCurrent().getPathRegistry().register( sqmPath );
		return sqmPath;
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitElementAggregateFunction( this );
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		sb.append(functionName).append( "(" );
		getLhs().appendHqlString( sb );
		sb.append( ')' );
	}
}
