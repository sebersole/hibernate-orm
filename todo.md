* Type resolution.  metamodel branch had a AttributeTypeResolver contract, which I was not a fan of so I did not port
	it.  But we need to account for that information
* PropertyGeneration -> [ValueGeneration, GenerationTiming] - this means a Jandex resolution for annotations
	annotated with @ValueGenerationType and then looking for those locally on members.
* org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector
	&& org.hibernate.boot.model.source.internal.annotations.metadata.util.ConverterAndOverridesHelper - Seems to me that the best 
	place to "register" these is with the attributes (the ultimately named target) themselves.  Currently we collect
	them into the container; cannot remember why I decided to do that before - was there a reason?
	Either way, the feature is not complete anyway.
* Entity listeners defined as "defaults" in orm.xml.  This is a more general question/issue of handlng defaults
 	defined in orm.xml.  The metamodel code handled this by adding "made up" annotations into the "augmented Jandex Index"
 	to represent these defaults.  Not a fan of that solution.  I'd prefer something more explicit, maybe via 
 	MappingDefaults or similar annotations-only contract.
* ExcludeDefaultListeners / ExcludeSuperclassListeners

* And of course actually binding to the mapping model