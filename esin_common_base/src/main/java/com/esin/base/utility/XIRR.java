package com.esin.base.utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

public class XIRR {

    public static void main(String[] args) {
        TreeMap<Integer, Double> dateValueMap = new TreeMap<>();
        dateValueMap.put(20180101, 21000d);
        dateValueMap.put(20180110, 22000d);
        dateValueMap.put(20180120, 23000d);
        dateValueMap.put(20180130, 24000d);
        dateValueMap.put(20180210, 25000d);
        dateValueMap.put(20180220, 26000d);
        dateValueMap.put(20180228, 27000d);
        dateValueMap.put(20180310, 28000d);
        dateValueMap.put(20180320, 29000d);
        dateValueMap.put(20180330, -230000d);
        dateValueMap.put(20180501, 11000d);
        dateValueMap.put(20180510, 12000d);
        dateValueMap.put(20180520, 13000d);
        dateValueMap.put(20180530, 14000d);
        dateValueMap.put(20180610, 15000d);
        dateValueMap.put(20180620, 16000d);
        dateValueMap.put(20180628, 17000d);
        dateValueMap.put(20180710, 18000d);
        dateValueMap.put(20180720, 19000d);
        dateValueMap.put(20180730, -140000d);
//        double rateDate = calculateXIRR(dateValueMap, 0);
//        System.out.println(FormatUtil.formatPercent(rateDate * 1000000) + "%");
//        double rateMonth = Math.pow(1 + rateDate, 30.4375) - 1;
//        System.out.println(FormatUtil.formatPercent(rateMonth * 10000));
//        double rateYear = Math.pow(1 + rateDate, 365.25) - 1;
//        System.out.println(FormatUtil.formatPercent(rateYear * 10000));
    }

    public static double calculateXIRR(TreeMap<Integer, Double> dateValueMap, double preRate) {
        if (dateValueMap.size() < 2) {
            return 0d;
        }

        List<Integer> dateList = new ArrayList<>();
        Integer fromDate = dateValueMap.firstKey();
        while (fromDate.compareTo(dateValueMap.lastKey()) <= 0) {
            dateList.add(fromDate);
            Calendar calendar = FormatUtil.parseCalendar(fromDate);
            calendar.add(Calendar.DATE, 1);
            fromDate = FormatUtil.formatCalendar(calendar);
        }

        return BinarySearch.DoubleSearch.search(0.0000000001d, preRate - 1, preRate + 1, rate -> {
            double value = 0d;
            for (Integer date : dateValueMap.keySet()) {
                value += dateValueMap.get(date) * Math.pow(1 + rate, dateList.size() - 1 - dateList.indexOf(date));
            }
            System.out.println(index++ + " : " + rate + " : " + value);
            return Math.abs(value) < 0.01d ? 0 : value > 0 ? 1 : -1;
        });
    }

    private static int index = 1;
}
