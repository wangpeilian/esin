package com.esin.base.utility;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class FormatUtil {
    private static final Logger logger = Logger.getLogger(FormatUtil.class);
    public static final String DatePattern = "yyyy-MM-dd";
    public static final String DateTimePattern = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DateFormatter = new SimpleDateFormat(DatePattern);
    private static final SimpleDateFormat DateTimeFormatter = new SimpleDateFormat(DateTimePattern);
    private static final BeanWrapper<String> CommaPattern = new BeanWrapper<>();
    private static final ThreadLocal<DecimalFormat> NumberFormatLocal = new ThreadLocal<>();

    public static void setCommaPattern(String commaPattern) {
        CommaPattern.setValue(commaPattern);
    }

    public static String getCommaPattern() {
        return Utility.defaultIfEmpty(CommaPattern.getValue(), "#,###0");
    }

    public static DecimalFormat getNumberFormat(String pattern) {
        DecimalFormat format = NumberFormatLocal.get();
        if (format == null) {
            format = new DecimalFormat(pattern);
            NumberFormatLocal.set(format);
        } else if (!format.toPattern().equals(pattern)) {
            format.applyPattern(pattern);
        }
        return format;
    }

    public static Calendar parseCalendar(Integer date) {
        Calendar c = Calendar.getInstance();
        if (date != null && date != 0) {
            int year = date / 10000;
            int month = Math.min(Math.max(date / 100 % 100, 1), 12);
            int day = Math.min(Math.max(date % 100, 1), 31);
            c.set(year, month - 1, day, 0, 0, 0);
        }
        return c;
    }

    public static Integer formatCalendar(Calendar c) {
        if (c == null) {
            return null;
        }
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DATE);
    }

    public static String formatDate(Date value) {
        if (value == null) {
            return Utility.EMPTY;
        }
        synchronized (DateFormatter) {
            return DateFormatter.format(value);
        }
    }

    public static Date parseDate(String value) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        synchronized (DateFormatter) {
            try {
                return DateFormatter.parse(value);
            } catch (ParseException e) {
                logger.warn("parseDate error : " + value);
                return null;
            }
        }
    }

    public static String formatDate(Integer date) {
        if (Utility.isZero(date)) {
            return null;
        }
        return new StringBuilder(String.valueOf(date))
                .insert(6, "-")
                .insert(4, "-")
                .toString();
    }

    public static Integer formatDate(String date) {
        if (Utility.isEmpty(date)) {
            return null;
        }
        if (date.length() > 10) {
            date = date.substring(0, 10);
        }
        date = date.replace("-", "");
        return Integer.valueOf(date);
    }

    public static String formatDateTime(Date value) {
        if (value == null) {
            return Utility.EMPTY;
        }
        synchronized (DateTimeFormatter) {
            return DateTimeFormatter.format(value);
        }
    }

    public static Date parseDateTime(String value) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        synchronized (DateTimeFormatter) {
            try {
                return DateTimeFormatter.parse(value);
            } catch (ParseException e) {
                logger.warn("parseDate error : " + value);
                return null;
            }
        }
    }

    public static String formatInteger(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat(getCommaPattern()).format(number);
    }

    public static String formatDouble2Comma(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat(getCommaPattern() + ".00").format(number);
    }

    public static String formatDouble1(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.0").format(number);
    }

    public static String formatDouble2(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.00").format(number);
    }

    public static String formatDouble2trim(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.##").format(number);
    }

    public static String formatDouble3(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.000").format(number);
    }

    public static String formatDouble4(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.0000").format(number);
    }

    public static String formatDouble4trim2(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.00##").format(number);
    }

    public static String formatDouble4trim(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.####").format(number);
    }

    public static String formatDouble8trim(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return getNumberFormat("0.########").format(number);
    }

    public static String formatPercent(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return formatDouble2(number.doubleValue() * 100) + "%";
    }

    public static String formatPercentTrim(Number number) {
        if (number == null) {
            return Utility.EMPTY;
        }
        return formatDouble2trim(number.doubleValue() * 100) + "%";
    }

    public static Double parseDouble2(Double d) {
        return parseDouble(formatDouble2(d));
    }

    public static Double parseDouble3(Double d) {
        return parseDouble(formatDouble3(d));
    }

    public static Double parseDouble4(Double d) {
        return parseDouble(formatDouble4(d));
    }

    public static String formatAutoPercent(Number number, int type) {
        if (number == null) {
            return Utility.EMPTY;
        }
        double d = number.doubleValue();
        String label = "";
        if (type == 0) {
            d *= 10000;
            label = "%%";
        } else if (type == 1) {
            d *= 1000;
            label = "‰";
        } else {
            d *= 100;
            label = "%";
        }
        return formatDouble2(d) + label;
    }

    public static Integer parseInteger(String value) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        value = value.replace(",", "");
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double parseDouble(String value) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        value = value.replace(",", "");
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Float parseFloat(String value) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        value = value.replace(",", "");
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String formatMessage(String pattern, String... values) {
        return new MessageFormat(pattern).format(values);
    }

    public static String formatSeqNumber(int seqNumber) {
        return String.valueOf(100000000 + seqNumber).substring(1);
    }
}
