/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.named.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.procedure.internal.Util;
import org.hibernate.procedure.spi.ParameterStrategy;
import org.hibernate.procedure.spi.ProcedureCallImplementor;
import org.hibernate.query.named.spi.AbstractNamedQueryDescriptor;
import org.hibernate.query.named.spi.NamedCallableQueryDescriptor;
import org.hibernate.query.named.spi.ParameterDescriptor;

/**
 * @author Steve Ebersole
 */
public class NamedCallableQueryDescriptorImpl
		extends AbstractNamedQueryDescriptor
		implements NamedCallableQueryDescriptor {
	private final String callableName;
	private final ParameterStrategy parameterStrategy;
	private final Class[] resultClasses;
	private final String[] resultSetMappingNames;
	private final Collection<String> querySpaces;

	public NamedCallableQueryDescriptorImpl(
			String name,
			String callableName,
			ParameterStrategy parameterStrategy,
			List<ParameterDescriptor> parameterDescriptors,
			Class[] resultClasses,
			String[] resultSetMappingNames,
			Collection<String> querySpaces,
			Boolean cacheable,
			String cacheRegion,
			CacheMode cacheMode,
			FlushMode flushMode,
			Boolean readOnly,
			LockOptions lockOptions,
			Integer timeout,
			Integer fetchSize,
			String comment,
			Map<String, Object> hints) {
		super(
				name,
				parameterDescriptors,
				cacheable,
				cacheRegion,
				cacheMode,
				flushMode,
				readOnly,
				lockOptions,
				timeout,
				fetchSize,
				comment,
				hints
		);
		this.callableName = callableName;
		this.parameterStrategy = parameterStrategy;
		this.resultClasses = resultClasses;
		this.resultSetMappingNames = resultSetMappingNames;
		this.querySpaces = querySpaces;
	}

	@Override
	public String getCallableName() {
		return callableName;
	}

	public ParameterStrategy getParameterStrategy() {
		return parameterStrategy;
	}

	@Override
	public Collection<String> getQuerySpaces() {
		return querySpaces;
	}

	@Override
	public String getQueryString() {
		return callableName;
	}

	@Override
	public NamedCallableQueryDescriptor makeCopy(String name) {
		return new NamedCallableQueryDescriptorImpl(
				name,
				getCallableName(),
				getParameterStrategy(),
				getParameterDescriptors(),
				resultClasses,
				resultSetMappingNames,
				getQuerySpaces(),
				getCacheable(),
				getCacheRegion(),
				getCacheMode(),
				getFlushMode(),
				getReadOnly(),
				getLockOptions(),
				getTimeout(),
				getFetchSize(),
				getComment(),
				getHintsCopy()
		);
	}

	@Override
	public <T> ProcedureCallImplementor<T> toQuery(SharedSessionContractImplementor session, Class<T> resultType) {
		throw new NotYetImplementedFor6Exception();
	}
}
