package com.esin.base.utility;

public abstract class BinarySearch<T> {
    public T search(final long min, final long max, final Comparable<T> comparable) {
        return search(1, min, max, comparable);
    }

    public T search(final double accuracyValue, final double min, final double max, final Comparable<T> comparable) {
        double mid = (max + min) / 2;
        T value = convert(mid);
        if (max - min < accuracyValue) {
            return value;
        }
        int compare = comparable.compareTo(value);
        if (compare < 0) {
            return search(accuracyValue, mid, max, comparable);
        } else if (compare > 0) {
            return search(accuracyValue, min, mid, comparable);
        } else {
            return value;
        }
    }

    protected abstract T convert(double value);

    public static final BinarySearch<Double> DoubleSearch = new BinarySearch<Double>() {
        @Override
        protected Double convert(double value) {
            return value;
        }
    };
    public static final BinarySearch<Long> LongSearch = new BinarySearch<Long>() {
        @Override
        protected Long convert(double value) {
            return Math.round(value);
        }
    };
    public static final BinarySearch<Integer> IntSearch = new BinarySearch<Integer>() {
        @Override
        protected Integer convert(double value) {
            return (int) Math.round(value);
        }
    };
}
