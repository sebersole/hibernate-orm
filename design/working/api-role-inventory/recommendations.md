# Preliminary API role recommendations

These are recommendations, not approved classifications. All other extensible APIs remain unreviewed.

## Strong USE + IMPLEMENT recommendations

### org.hibernate.jdbc.Work

Lifecycle: stable.

[Documented callback supplied to Session.doWork; concrete and lambda test implementations.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/jdbc/Work.java:11)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.dataTypes.BasicOperationsTest$ValidateRowCount](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/dataTypes/BasicOperationsTest.java:160)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.dataTypes.BasicOperationsTest$ValidateSomeEntityColumns](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/dataTypes/BasicOperationsTest.java:104)
- DIRECT_HIERARCHY: [org.hibernate.testing.junit4.BaseCoreFunctionalTestCase$RollbackWork](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-testing/src/main/java/org/hibernate/testing/junit4/BaseCoreFunctionalTestCase.java:467)
### org.hibernate.jdbc.ReturningWork

Lifecycle: stable.

[Documented callback supplied to Session.doReturningWork; concrete and lambda test implementations.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/jdbc/ReturningWork.java:11)

- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.jdbc.GeneralWorkTest](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jdbc/GeneralWorkTest.java:92)
- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.jpa.model.AbstractJPATest](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jpa/model/AbstractJPATest.java:62)
- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.jpa.query.JpaQueryFlushModeTest](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jpa/query/JpaQueryFlushModeTest.java:91)
### org.hibernate.jdbc.AbstractWork

Lifecycle: stable.

[Documented abstract base for user-supplied JDBC work; test subclasses.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/jdbc/AbstractWork.java:10)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.join.JoinTest$1](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/join/JoinTest.java:1)
### org.hibernate.jdbc.AbstractReturningWork

Lifecycle: stable.

[Documented abstract base for returning JDBC work; test subclasses.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/jdbc/AbstractReturningWork.java:10)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.jpa.txn.JtaObtainConnectionExceptionTest$1](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jpa/txn/JtaObtainConnectionExceptionTest.java:1)
- DIRECT_HIERARCHY: [org.hibernate.testing.junit4.BaseCoreFunctionalTestCase$1](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-testing/src/main/java/org/hibernate/testing/junit4/BaseCoreFunctionalTestCase.java:1)
### org.hibernate.Interceptor

Lifecycle: stable.

[Explicit user callback contract and registration points; dedicated interceptor tests.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/Interceptor.java:15)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.bootstrap.BootstrapTest$CustomSessionFactoryInterceptor](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/bootstrap/BootstrapTest.java:562)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.caching.mocked.ReadWriteCacheTest$TransactionInterceptor](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/caching/mocked/ReadWriteCacheTest.java:306)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.dirtiness.CustomDirtinessStrategyTest$OnFlushDirtyInterceptor](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dirtiness/CustomDirtinessStrategyTest.java:188)
### org.hibernate.EntityNameResolver

Lifecycle: stable.

[Documents supplying an application resolver; registration APIs and test implementations.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/EntityNameResolver.java:8)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.dynamicmap.CustomEntityNameResolverTest$HibernateEntityNameResolver](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dynamicmap/CustomEntityNameResolverTest.java:68)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.dynamicmap.CustomEntityNameResolverTest$HibernateEntityNameResolver](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dynamicmap/CustomEntityNameResolverTest.java:1)
- OVERRIDE_OR_IMPLEMENTATION_SIGNATURE: [org.hibernate.orm.test.dynamicmap.CustomEntityNameResolverTest$HibernateEntityNameResolver](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dynamicmap/CustomEntityNameResolverTest.java:1)
### org.hibernate.CustomEntityDirtinessStrategy

Lifecycle: stable.

[Javadoc includes an application-written implementation and nested callback lambda.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/CustomEntityDirtinessStrategy.java:10)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.dirtiness.CustomDirtinessStrategyTest$Strategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dirtiness/CustomDirtinessStrategyTest.java:139)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.dirtiness.HHH11866Test$EntityDirtinessStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dirtiness/HHH11866Test.java:99)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.dirtiness.CustomDirtinessStrategyTest$Strategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dirtiness/CustomDirtinessStrategyTest.java:1)
### org.hibernate.CustomEntityDirtinessStrategy$AttributeChecker

Lifecycle: stable.

[The documented custom dirtiness strategy supplies this callback through doDirtyChecking.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/CustomEntityDirtinessStrategy.java:47)

- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.dirtiness.CustomDirtinessStrategyTest$Strategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dirtiness/CustomDirtinessStrategyTest.java:175)
- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.dirtiness.HHH11866Test$EntityDirtinessStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/dirtiness/HHH11866Test.java:119)
### org.hibernate.SessionFactoryObserver

Lifecycle: stable.

[Documented configured lifecycle observer; source explicitly discusses compatibility for existing implementations.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/SessionFactoryObserver.java:10)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.bootstrap.BootstrapTest$CustomSessionFactoryObserver](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/bootstrap/BootstrapTest.java:564)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.caching.mocked.ReadWriteCacheTest$TransactionInterceptor](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/caching/mocked/ReadWriteCacheTest.java:306)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.events.CallbackTest$TestingObserver](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/events/CallbackTest.java:63)
### org.hibernate.boot.model.naming.ImplicitNamingStrategy

Lifecycle: INCUBATING.

[Javadoc explicitly anticipates custom application implementations; test subclasses of the default implementation provide indirect evidence.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/boot/model/naming/ImplicitNamingStrategy.java:32)

- INDIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.collectionelement.MyImprovedNamingStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/collectionelement/MyImprovedNamingStrategy.java:12)
- INDIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.namingstrategy.LongIdentifierNamingStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/namingstrategy/LongIdentifierNamingStrategy.java:13)
- INDIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.onetomany.orderby.IdClassAndOrderByTest$CustomImplicitNamingStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/onetomany/orderby/IdClassAndOrderByTest.java:86)
### org.hibernate.boot.model.naming.PhysicalNamingStrategy

Lifecycle: INCUBATING.

[Configured custom naming contract, exercised by user-style naming strategy tests.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/boot/model/naming/PhysicalNamingStrategy.java:29)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.boot.database.qualfiedTableNaming.NamespaceTest$TestNamingStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/boot/database/qualfiedTableNaming/NamespaceTest.java:52)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.id.sequence.PooledWithCustomNamingStrategyTest$MyPhysicalNamingStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/id/sequence/PooledWithCustomNamingStrategyTest.java:134)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.namingstrategy.TestNamingStrategy](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/namingstrategy/TestNamingStrategy.java:18)
### org.hibernate.query.TupleTransformer

Lifecycle: INCUBATING.

[Application-supplied result callback with setter and lambda/concrete tests; currently incubating.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/query/TupleTransformer.java:12)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.sql.hand.query.NativeSQLQueriesTest$UpperCasedAliasToEntityMapResultTransformer](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/sql/hand/query/NativeSQLQueriesTest.java:981)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.sql.hand.query.NativeSQLQueriesTest$UpperCasedAliasToEntityMapResultTransformer](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/sql/hand/query/NativeSQLQueriesTest.java:1)
- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.filter.DynamicFilterTest](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/filter/DynamicFilterTest.java:741)
### org.hibernate.query.ResultListTransformer

Lifecycle: INCUBATING.

[Application-supplied list callback with setter and tests; currently incubating.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/query/ResultListTransformer.java:14)

- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.hqlfetchscroll.HQLScrollFetchTest](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/hqlfetchscroll/HQLScrollFetchTest.java:43)
- LAMBDA_OR_METHOD_REFERENCE: [org.hibernate.orm.test.sql.hand.query.NativeSQLQueriesTest](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/sql/hand/query/NativeSQLQueriesTest.java:218)
### org.hibernate.query.criteria.CriteriaDefinition

Lifecycle: INCUBATING.

[Public documentation expressly prescribes anonymous subclassing, not just overriding an abstract callback.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/query/criteria/CriteriaDefinition.java:28)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.jpa.criteria.query.CriteriaDefinitionTest$1](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jpa/criteria/query/CriteriaDefinitionTest.java:1)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.jpa.criteria.query.CriteriaDefinitionTest$10](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jpa/criteria/query/CriteriaDefinitionTest.java:1)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.jpa.criteria.query.CriteriaDefinitionTest$2](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/jpa/criteria/query/CriteriaDefinitionTest.java:1)
### org.hibernate.envers.RevisionListener

Lifecycle: DEPRECATED.

[Javadoc requires an implementation passed to RevisionEntity; integration tests supply one.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/main/java/org/hibernate/envers/RevisionListener.java:9)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.envers.CustomRevisionEntityTest$CustomRevisionEntityListener](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/test/java/org/hibernate/orm/test/envers/CustomRevisionEntityTest.java:142)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.envers.entities.reventity.trackmodifiedentities.ExtendedRevisionListener](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/test/java/org/hibernate/orm/test/envers/entities/reventity/trackmodifiedentities/ExtendedRevisionListener.java:12)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.envers.integration.manytoone.bidirectional.ManyToOneCustomRevisionListenerTest$CustomRevisionListener](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/test/java/org/hibernate/orm/test/envers/integration/manytoone/bidirectional/ManyToOneCustomRevisionListenerTest.java:186)
### org.hibernate.envers.EntityTrackingRevisionListener

Lifecycle: DEPRECATED.

[Extension of the supplied revision callback, exercised by custom revision listener tests.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/main/java/org/hibernate/envers/EntityTrackingRevisionListener.java:8)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.envers.EntityTypeChangeAuditTrackingRevisionListenerTest$CustomTrackingRevisionListener](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/test/java/org/hibernate/orm/test/envers/EntityTypeChangeAuditTrackingRevisionListenerTest.java:218)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.envers.entities.reventity.trackmodifiedentities.CustomTrackingRevisionListener](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/test/java/org/hibernate/orm/test/envers/entities/reventity/trackmodifiedentities/CustomTrackingRevisionListener.java:13)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.envers.integration.manytoone.bidirectional.ManyToOneCustomRevisionListenerTest$CustomRevisionListener](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-envers/src/test/java/org/hibernate/orm/test/envers/integration/manytoone/bidirectional/ManyToOneCustomRevisionListenerTest.java:186)
### org.hibernate.generator.BeforeExecutionGenerator

Lifecycle: stable.

[Explicit custom generator and annotation-registration contract; user-defined generator tests.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/generator/BeforeExecutionGenerator.java:10)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.exceptionhandling.FkIdGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/exceptionhandling/FkIdGenerator.java:24)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.id.GenericGeneratorTest$ExplicitValueGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/id/GenericGeneratorTest.java:35)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.idgen.mixed.MixedTimingGeneratorsTest$IdentityOrRandomGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/idgen/mixed/MixedTimingGeneratorsTest.java:309)
### org.hibernate.generator.OnExecutionGenerator

Lifecycle: stable.

[Documents required overrides and user-written generators; generated-value tests exercise them.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/generator/OnExecutionGenerator.java:33)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.DatabaseCreationTimestampNullableColumnTest$FunctionCreationValueGeneration](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/DatabaseCreationTimestampNullableColumnTest.java:81)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.DatabaseTimestampsColumnTest$TimestampValueGeneration](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/DatabaseTimestampsColumnTest.java:80)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.idgen.mixed.MixedTimingGeneratorsTest$LiteralOrGeneratedStringGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/idgen/mixed/MixedTimingGeneratorsTest.java:335)
### org.hibernate.id.IdentifierGenerator

Lifecycle: stable.

[Explicitly documented extension point; custom generator tests.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/id/IdentifierGenerator.java:25)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.annotations.type.MyOidGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/annotations/type/MyOidGenerator.java:14)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.boot.jaxb.mapping.compositeidgenerator.OrderIdGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/boot/jaxb/mapping/compositeidgenerator/OrderIdGenerator.java:10)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.cid.PurchaseRecordIdGenerator](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/cid/PurchaseRecordIdGenerator.java:15)
### org.hibernate.binder.TypeBinder

Lifecycle: INCUBATING.

[Javadoc demonstrates an application annotation with a custom Binder implementation; currently incubating.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/binder/TypeBinder.java:15)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.mapping.attributebinder.typebinder.NoResultCheck$Binder](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/mapping/attributebinder/typebinder/NoResultCheck.java:24)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.mapping.attributebinder.typebinder.NoResultCheck$Binder](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/mapping/attributebinder/typebinder/NoResultCheck.java:1)
- OVERRIDE_OR_IMPLEMENTATION_SIGNATURE: [org.hibernate.orm.test.mapping.attributebinder.typebinder.NoResultCheck$Binder](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/mapping/attributebinder/typebinder/NoResultCheck.java:1)
### org.hibernate.binder.AttributeBinder

Lifecycle: INCUBATING.

[Explicit user-written annotation binding contract; currently incubating.](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/main/java/org/hibernate/binder/AttributeBinder.java:14)

- DIRECT_HIERARCHY: [org.hibernate.orm.test.mapping.attributebinder.FooBinder](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/mapping/attributebinder/FooBinder.java:20)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.mapping.attributebinder.YesNoBinder](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/mapping/attributebinder/YesNoBinder.java:19)
- DIRECT_HIERARCHY: [org.hibernate.orm.test.mapping.attributebinder.FooBinder](/Users/steveebersole/projects/hibernate/orm/primary/hibernate-orm/hibernate-core/src/test/java/org/hibernate/orm/test/mapping/attributebinder/FooBinder.java:1)

## USE-only candidates requiring a contract decision

- `org.hibernate.Session`: Application-facing session facade; implementations in the inventory are principally Hibernate runtime machinery. Third-party wrappers remain an open question.
- `org.hibernate.StatelessSession`: Application-facing session facade. External wrappers and implementation promises require explicit review.
- `org.hibernate.SessionFactory`: Factory obtained from bootstrap; custom wrappers/delegators must be considered before excluding IMPLEMENT.
- `org.hibernate.query.Query`: Application query facade; framework adapters may implement this outside the repository.
- `org.hibernate.query.SelectionQuery`: Query contract primarily consumed by applications; no inference of USE-only from missing external evidence.
- `org.hibernate.query.MutationQuery`: Query contract primarily consumed by applications; implementation intent remains unreviewed.
- `org.hibernate.boot.Metadata`: Bootstrap result consumed by applications and integrations; review wrappers and existing documentation.
- `org.hibernate.boot.MetadataBuilder`: Bootstrap builder obtained from Hibernate; review extension and delegating-builder use before declaring USE-only.
