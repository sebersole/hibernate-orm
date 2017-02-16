/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.stat.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.access.CollectionStorageAccess;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.stat.SecondLevelCacheStatistics;

/**
 * Second level cache statistics of a specific region
 *
 * @author Alex Snaps
 */
public class ConcurrentSecondLevelCacheStatisticsImpl extends CategorizedStatistics implements SecondLevelCacheStatistics {
	private final transient Region region;
	private final transient EntityStorageAccess entityRegionAccess;
	private final transient CollectionStorageAccess collectionRegionAccess;
	private AtomicLong hitCount = new AtomicLong();
	private AtomicLong missCount = new AtomicLong();
	private AtomicLong putCount = new AtomicLong();

	ConcurrentSecondLevelCacheStatisticsImpl(
			Region region,
			EntityStorageAccess entityRegionAccess,
			CollectionStorageAccess collectionRegionAccess) {
		super( region.getName() );
		this.region = region;
		this.entityRegionAccess = entityRegionAccess;
		this.collectionRegionAccess = collectionRegionAccess;
	}

	public long getHitCount() {
		return hitCount.get();
	}

	public long getMissCount() {
		return missCount.get();
	}

	public long getPutCount() {
		return putCount.get();
	}

	public long getElementCountInMemory() {
		return region.getElementCountInMemory();
	}

	public long getElementCountOnDisk() {
		return region.getElementCountOnDisk();
	}

	public long getSizeInMemory() {
		return region.getSizeInMemory();
	}

	public Map getEntries() {
		Map map = new HashMap();
		for ( Object o : region.toMap().entrySet() ) {
			Map.Entry me = (Map.Entry) o;
			Object id;
			if ( entityRegionAccess != null ) {
				id = entityRegionAccess.getCacheKeyId( me.getKey() );
			}
			else if ( collectionRegionAccess != null ) {
				id = collectionRegionAccess.getCacheKeyId( me.getKey() );
			}
			else {
				id = me.getKey();
			}
			map.put( id, me.getValue() );
		}
		return map;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder()
				.append( "SecondLevelCacheStatistics" )
				.append( "[hitCount=").append( this.hitCount )
				.append( ",missCount=").append( this.missCount )
				.append( ",putCount=").append( this.putCount );
		//not sure if this would ever be null but wanted to be careful
		if ( region != null ) {
			buf.append( ",elementCountInMemory=" ).append( this.getElementCountInMemory() )
					.append( ",elementCountOnDisk=" ).append( this.getElementCountOnDisk() )
					.append( ",sizeInMemory=" ).append( this.getSizeInMemory() );
		}
		buf.append( ']' );
		return buf.toString();
	}

	void incrementHitCount() {
		hitCount.getAndIncrement();
	}

	void incrementMissCount() {
		missCount.getAndIncrement();
	}

	void incrementPutCount() {
		putCount.getAndIncrement();
	}
}
