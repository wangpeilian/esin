package com.esin.jdbc.lazy;

import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class LazyList<K, T extends IEntity<K>> implements List<T> {

    private final DaoFactory daoFactory;
    private final Class<T> type;
    private final String name;
    private final K id;

    private List<T> dataList = null;

    public LazyList(DaoFactory daoFactory, Class<T> type, String name, K id) {
        this.daoFactory = daoFactory;
        this.type = type;
        this.name = name;
        this.id = id;
    }

    private synchronized void checkList() {
        if (dataList == null) {
            dataList = daoFactory.getDaoHelper().createQuery(type).getWhere()
                    .addCriterion(name, id).getQuery()
                    .queryLazyEntityList();
        }
    }

    @Override
    public int size() {
        checkList();
        return dataList.size();
    }

    @Override
    public boolean isEmpty() {
        checkList();
        return dataList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        checkList();
        return dataList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        checkList();
        return dataList.iterator();
    }

    @Override
    public Object[] toArray() {
        checkList();
        return dataList.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        checkList();
        return dataList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        checkList();
        return dataList.add(t);
    }

    @Override
    public boolean remove(Object o) {
        checkList();
        return dataList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        checkList();
        return dataList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        checkList();
        return dataList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkList();
        return dataList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkList();
        return dataList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        checkList();
        return dataList.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        checkList();
        dataList.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        checkList();
        dataList.sort(c);
    }

    @Override
    public void clear() {
        checkList();
        dataList.clear();
    }

    @Override
    public T get(int index) {
        checkList();
        return dataList.get(index);
    }

    @Override
    public T set(int index, T element) {
        checkList();
        return dataList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        checkList();
        dataList.add(index, element);
    }

    @Override
    public T remove(int index) {
        checkList();
        return dataList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        checkList();
        return dataList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        checkList();
        return dataList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        checkList();
        return dataList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        checkList();
        return dataList.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        checkList();
        return dataList.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<T> spliterator() {
        checkList();
        return dataList.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        checkList();
        return dataList.removeIf(filter);
    }

    @Override
    public Stream<T> stream() {
        checkList();
        return dataList.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        checkList();
        return dataList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        checkList();
        dataList.forEach(action);
    }
}
