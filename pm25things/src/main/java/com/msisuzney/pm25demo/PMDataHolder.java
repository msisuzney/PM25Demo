package com.msisuzney.pm25demo;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by chenxin.
 * Date: 2017/7/12.
 * Time: 16:36.
 */

public class PMDataHolder {
    private static String data = "";
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    public static void setData(String s){
        lock.writeLock().lock();
        data = s;
        lock.writeLock().unlock();
    }
    public static String getData(){
        String s = "";
        lock.readLock().lock();
        s = data;
        lock.readLock().unlock();
        return s;
    }
}
