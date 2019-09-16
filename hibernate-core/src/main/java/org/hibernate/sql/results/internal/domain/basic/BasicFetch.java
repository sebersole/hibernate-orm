/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.basic;

import java.util.function.Consumer;

import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.internal.BasicValuedSingularAttributeMapping;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.internal.BasicResultAssembler;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.Fetch;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;

/**
 * @author Steve Ebersole
 */
public class BasicFetch implements Fetch {
	private final FetchParent fetchParent;
	private final BasicValuedSingularAttributeMapping attributeMapping;
	private final FetchTiming fetchTiming;


	private final NavigablePath path;

	public BasicFetch(
			FetchParent fetchParent,
			BasicValuedSingularAttributeMapping attributeMapping,
			FetchTiming fetchTiming,
			DomainResultCreationState creationState) {
		this.fetchParent = fetchParent;
		this.attributeMapping = attributeMapping;
		this.fetchTiming = fetchTiming;

		this.path = fetchParent.getNavigablePath().append( attributeMapping.getFetchableName() );
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}


	@Override
	public NavigablePath getNavigablePath() {
		return path;
	}

	@Override
	public String getFetchedNavigableName() {
		return attributeMapping.getFetchableName();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationState creationState) {
		return new BasicResultAssembler(
				attributeMapping.getStateArrayPosition() ,
				attributeMapping.getMappedTypeDescriptor().getMappedJavaTypeDescriptor(),
				attributeMapping.getConverter()
		);
	}

}
