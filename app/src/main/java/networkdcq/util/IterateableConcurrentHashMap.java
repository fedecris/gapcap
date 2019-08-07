package networkdcq.util;

/**
 * A ConcurrentHashMap which mantains a separate keyList and valueList in order to avoid
 * creating new instances when using tratidional methods like keySet() or values(), hence
 * this type is useful for cases in which iterations over a hashmap is frequently called.
 */

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IterateableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

	/** Class version */
	private static final long serialVersionUID = 1L;
	/** A list containing the keys of the map */
	protected ArrayList<K> keyList = new ArrayList<K>();
	/** A list containing the values of the map */
	protected ArrayList<V> valueList =  new ArrayList<V>();
	
	@Override
	public synchronized V put(K key, V value) {
		super.put(key, value);
		keyList.remove(key);
		valueList.remove(value);
		keyList.add(key);
		valueList.add(value);
		return value;
	}
	
	@Override
	public synchronized V replace(K key, V value) {
		if (containsKey(key)) {
			valueList.set(keyList.indexOf(key), value);
			return put(key, value);
		}
		return null;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new RuntimeException("Method putAll() for type IterateableConcurrentHashMap not implemented yet");
	}
	
	@Override
	public synchronized boolean replace(K key, V oldValue, V newValue) {
		if (containsKey(key) && get(key).equals(oldValue)) {
			replace(key, newValue);
			return true;
		}
		return false;
	}
	
	@Override
	public synchronized V remove(Object key) {
		V value = super.remove(key);
		keyList.remove(key);
		valueList.remove(value);
		return value;
	}

	@Override
	public synchronized boolean remove(Object key, Object value) {
		if (containsKey(key) && get(key).equals(value)) {
			remove(key);
			return true;
		}
		return false;
	}
	
	@Override
	public synchronized void clear() {
		super.clear();
		keyList.clear();
		valueList.clear();
	}

	@Override	
	public V putIfAbsent(K key, V value) {
		if (!containsKey(key)) {
			return put(key, value);
		}
		return get(key);
	};
	
	
	public ArrayList<K> getKeyList() {
		return keyList;
	}

	public ArrayList<V> getValueList() {
		return valueList;
	}

	public K getKeyAt(int pos) {
		return keyList.get(pos);
	}
	
	public V getValueAt(int pos) {
		return valueList.get(pos);
	}

}
