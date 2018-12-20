package wordcount.model;

import java.util.concurrent.ConcurrentSkipListMap;

public class CountingConcurrentSkipListMap extends ConcurrentSkipListMap<String, Integer> {

	/**
	 * A custom class to count word occurrences in a map.
	 */
	private static final long serialVersionUID = -273732077630607632L;

	@Override
	public synchronized Integer put(String key,  Integer value) {
		if (this.containsKey(key)) {
			return super.put(key, get(key)+1);
		}
		return super.put(key, 1);
	}
}
