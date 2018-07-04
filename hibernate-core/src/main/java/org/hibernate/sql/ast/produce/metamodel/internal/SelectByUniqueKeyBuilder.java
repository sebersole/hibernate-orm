package org.hibernate.sql.ast.produce.metamodel.internal;

import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;

/**
 * @author Andrea Boriero
 */
public class SelectByUniqueKeyBuilder extends AbstractMetamodelSelectBuilder {

	public SelectByUniqueKeyBuilder(
			SessionFactoryImplementor sessionFactory,
			NavigableContainer rootNavigable,
			Navigable restrictedNavigable) {
		super( sessionFactory, rootNavigable, restrictedNavigable );
	}

//	@Override
//	public SqlAstSelectDescriptor generateSelectStatement(
//			int numberOfKeysToLoad,
//			LoadQueryInfluencers loadQueryInfluencers,
//			LockOptions lockOptions) {
//		return generateSelectStatement( numberOfKeysToLoad, navigablesToBeJoined, loadQueryInfluencers, lockOptions );
//	}

//	protected SqlAstSelectDescriptor generateSelectStatement(
//			int numberOfKeysToLoad,
//			List<Navigable<?>> navigablesToBeSelected,
//			LoadQueryInfluencers loadQueryInfluencers,
//			LockOptions lockOptions) {
//		return MetamodelSelectBuilderProcess.createSelect(
//				sessionFactory,
//				rootNavigable,
//				navigablesToBeSelected,
////				navigablesToBeJoined,
//				restrictedNavigable,
//				// allow passing in a QueryResult (readers) already built
//				null,
//				numberOfKeysToLoad,
//				loadQueryInfluencers,
//				lockOptions
//		);
//	}


}

