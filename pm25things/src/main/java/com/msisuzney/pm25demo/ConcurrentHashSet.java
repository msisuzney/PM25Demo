package com.msisuzney.pm25demo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenxin.
 * Date: 2017/10/12.
 * Time: 17:11.
 */

public class ConcurrentHashSet<V> {
    private final Object OBJ = new Object();
    private ConcurrentHashMap<V, Object> hashMap;

    public ConcurrentHashSet() {
        hashMap = new ConcurrentHashMap<>();
    }

    public void put(V v) {
        hashMap.put(v, OBJ);
    }

    public boolean contains(V v) {
        return hashMap.containsKey(v);
    }

    public V remove(V v) {
        return (V) hashMap.remove(v);
    }
}
