package com.esin.base.utility;

import java.util.Date;

public class CalendarConvert {
    public static String getLundar(Date date) {
        return null;
//		String lundar = ChineseCalendar.sCalendarSolarToLundar(1900 + date.getYear(), date.getMonth() + 1, date.getDate());
//		return new LunarDate(
//			Integer.parseInt(lundar.substring(0, 4)),
//			Integer.parseInt(lundar.substring(4, 6)),
//			Integer.parseInt(lundar.substring(6, 8))
//		).toString();
    }

    public static String getCnWeek(Date date) {
        return null;
//		return new CnWeek(date.getDay()).toString();
    }

    // 自定义日历类
    public static class ChineseCalendar {
        // Array lIntLunarDay is stored in the monthly day information in every year from 1901 to 2100 of the lunar calendar,
        // The lunar calendar can only be 29 or 30 days every month, express with 12(or 13) pieces of binary bit in one year,
        // it is 30 days for 1 form in the corresponding location , otherwise it is 29 days
        private static final int[] iLunarMonthDaysTable = {
                0x4ae0, 0xa570, 0x5268, 0xd260, 0xd950, 0x6aa8, 0x56a0, 0x9ad0, 0x4ae8, 0x4ae0, // 1910
                0xa4d8, 0xa4d0, 0xd250, 0xd548, 0xb550, 0x56a0, 0x96d0, 0x95b0, 0x49b8, 0x49b0, // 1920
                0xa4b0, 0xb258, 0x6a50, 0x6d40, 0xada8, 0x2b60, 0x9570, 0x4978, 0x4970, 0x64b0, // 1930
                0xd4a0, 0xea50, 0x6d48, 0x5ad0, 0x2b60, 0x9370, 0x92e0, 0xc968, 0xc950, 0xd4a0, // 1940
                0xda50, 0xb550, 0x56a0, 0xaad8, 0x25d0, 0x92d0, 0xc958, 0xa950, 0xb4a8, 0x6ca0, // 1950
                0xb550, 0x55a8, 0x4da0, 0xa5b0, 0x52b8, 0x52b0, 0xa950, 0xe950, 0x6aa0, 0xad50, // 1960
                0xab50, 0x4b60, 0xa570, 0xa570, 0x5260, 0xe930, 0xd950, 0x5aa8, 0x56a0, 0x96d0, // 1970
                0x4ae8, 0x4ad0, 0xa4d0, 0xd268, 0xd250, 0xd528, 0xb540, 0xb6a0, 0x96d0, 0x95b0, // 1980
                0x49b0, 0xa4b8, 0xa4b0, 0xb258, 0x6a50, 0x6d40, 0xada0, 0xab60, 0x9370, 0x4978, // 1990
                0x4970, 0x64b0, 0x6a50, 0xea50, 0x6b28, 0x5ac0, 0xab60, 0x9368, 0x92e0, 0xc960, // 2000
                0xd4a8, 0xd4a0, 0xda50, 0x5aa8, 0x56a0, 0xaad8, 0x25d0, 0x92d0, 0xc958, 0xa950, // 2010
                0xb4a0, 0xb550, 0xb550, 0x55a8, 0x4ba0, 0xa5b0, 0x52b8, 0x52b0, 0xa930, 0x74a8, // 2020
                0x6aa0, 0xad50, 0x4da8, 0x4b60, 0x9570, 0xa4e0, 0xd260, 0xe930, 0xd530, 0x5aa0, // 2030
                0x6b50, 0x96d0, 0x4ae8, 0x4ad0, 0xa4d0, 0xd258, 0xd250, 0xd520, 0xdaa0, 0xb5a0, // 2040
                0x56d0, 0x4ad8, 0x49b0, 0xa4b8, 0xa4b0, 0xaa50, 0xb528, 0x6d20, 0xada0, 0x55b0 // 2050
        };
        // Array iLunarLeapMonthTable preserves the lunar calendar leap month from 1901 to 2050,
        // if it is 0 express not to have , every byte was stored for two years
        private static final char[] iLunarLeapMonthTable = {0x00, 0x50, 0x04, 0x00, 0x20, // 1910
                0x60, 0x05, 0x00, 0x20, 0x70, // 1920
                0x05, 0x00, 0x40, 0x02, 0x06, // 1930
                0x00, 0x50, 0x03, 0x07, 0x00, // 1940
                0x60, 0x04, 0x00, 0x20, 0x70, // 1950
                0x05, 0x00, 0x30, 0x80, 0x06, // 1960
                0x00, 0x40, 0x03, 0x07, 0x00, // 1970
                0x50, 0x04, 0x08, 0x00, 0x60, // 1980
                0x04, 0x0a, 0x00, 0x60, 0x05, // 1990
                0x00, 0x30, 0x80, 0x05, 0x00, // 2000
                0x40, 0x02, 0x07, 0x00, 0x50, // 2010
                0x04, 0x09, 0x00, 0x60, 0x04, // 2020
                0x00, 0x20, 0x60, 0x05, 0x00, // 2030
                0x30, 0xb0, 0x06, 0x00, 0x50, // 2040
                0x02, 0x07, 0x00, 0x50, 0x03 // 2050
        };
        // Array iSolarLunarTable stored the offset days
        // in New Year of solar calendar and lunar calendar from 1901 to 2050;
        private static final char[] iSolarLunarOffsetTable = {49, 38, 28, 46, 34, 24, 43, 32, 21, 40, // 1910
                29, 48, 36, 25, 44, 34, 22, 41, 31, 50, // 1920
                38, 27, 46, 35, 23, 43, 32, 22, 40, 29, // 1930
                47, 36, 25, 44, 34, 23, 41, 30, 49, 38, // 1940
                26, 45, 35, 24, 43, 32, 21, 40, 28, 47, // 1950
                36, 26, 44, 33, 23, 42, 30, 48, 38, 27, // 1960
                45, 35, 24, 43, 32, 20, 39, 29, 47, 36, // 1970
                26, 45, 33, 22, 41, 30, 48, 37, 27, 46, // 1980
                35, 24, 43, 32, 50, 39, 28, 47, 36, 26, // 1990
                45, 34, 22, 40, 30, 49, 37, 27, 46, 35, // 2000
                23, 42, 31, 21, 39, 28, 48, 37, 25, 44, // 2010
                33, 23, 41, 31, 50, 39, 28, 47, 35, 24, // 2020
                42, 30, 21, 40, 28, 47, 36, 25, 43, 33, // 2030
                22, 41, 30, 49, 37, 26, 44, 33, 23, 42, // 2040
                31, 21, 40, 29, 47, 36, 25, 44, 32, 22, // 2050
        };

        public static boolean bIsSolarLeapYear(int iYear) {
            return ((iYear % 4 == 0) && (iYear % 100 != 0) || iYear % 400 == 0);
        }

        // The days in the month of solar calendar
        static int iGetSYearMonthDays(int iYear, int iMonth) {
            if ((iMonth == 1) || (iMonth == 3) || (iMonth == 5) || (iMonth == 7) || (iMonth == 8) || (iMonth == 10) || (iMonth == 12))
                return 31;
            else if ((iMonth == 4) || (iMonth == 6) || (iMonth == 9) || (iMonth == 11))
                return 30;
            else if (iMonth == 2) {
                if (bIsSolarLeapYear(iYear))
                    return 29;
                else
                    return 28;
            } else
                return 0;
        }

        // The offset days from New Year and the day when point out in solar calendar
        static int iGetSNewYearOffsetDays(int iYear, int iMonth, int iDay) {
            int iOffsetDays = 0;
            for (int i = 1; i < iMonth; i++) {
                iOffsetDays += iGetSYearMonthDays(iYear, i);
            }
            iOffsetDays += iDay - 1;
            return iOffsetDays;
        }

        static int iGetLLeapMonth(int iYear) {
            char iMonth = iLunarLeapMonthTable[(iYear - 1901) / 2];
            if (iYear % 2 == 0)
                return (iMonth & 0x0f);
            else
                return (iMonth & 0xf0) >> 4;
        }

        static int iGetLMonthDays(int iYear, int iMonth) {
            int iLeapMonth = iGetLLeapMonth(iYear);
            if ((iMonth > 12) && (iMonth - 12 != iLeapMonth) || (iMonth < 0)) {
                System.out.println("Wrong month, ^_^ , i think you are want a -1, go to death!");
                return -1;
            }
            if (iMonth - 12 == iLeapMonth) {
                if ((iLunarMonthDaysTable[iYear - 1901] & (0x8000 >> iLeapMonth)) == 0)
                    return 29;
                else
                    return 30;
            }
            if ((iLeapMonth > 0) && (iMonth > iLeapMonth))
                iMonth++;
            if ((iLunarMonthDaysTable[iYear - 1901] & (0x8000 >> (iMonth - 1))) == 0)
                return 29;
            else
                return 30;
        }

        // Days in this year of lunar calendar
        static int iGetLYearDays(int iYear) {
            int iYearDays = 0;
            int iLeapMonth = iGetLLeapMonth(iYear);
            for (int i = 1; i < 13; i++)
                iYearDays += iGetLMonthDays(iYear, i);
            if (iLeapMonth > 0)
                iYearDays += iGetLMonthDays(iYear, iLeapMonth + 12);
            return iYearDays;
        }

        static int iGetLNewYearOffsetDays(int iYear, int iMonth, int iDay) {
            int iOffsetDays = 0;
            int iLeapMonth = iGetLLeapMonth(iYear);
            if ((iLeapMonth > 0) && (iLeapMonth == iMonth - 12)) {
                iMonth = iLeapMonth;
                iOffsetDays += iGetLMonthDays(iYear, iMonth);
            }
            for (int i = 1; i < iMonth; i++) {
                iOffsetDays += iGetLMonthDays(iYear, i);
                if (i == iLeapMonth)
                    iOffsetDays += iGetLMonthDays(iYear, iLeapMonth + 12);
            }
            iOffsetDays += iDay - 1;
            return iOffsetDays;
        }

        // The solar calendar is turned into the lunar calendar
        static String sCalendarSolarToLundar(int iYear, int iMonth, int iDay) {
            int iLDay, iLMonth, iLYear;
            int iOffsetDays = iGetSNewYearOffsetDays(iYear, iMonth, iDay);
            int iLeapMonth = iGetLLeapMonth(iYear);
            if (iOffsetDays < iSolarLunarOffsetTable[iYear - 1901]) {
                iLYear = iYear - 1;
                iOffsetDays = iSolarLunarOffsetTable[iYear - 1901] - iOffsetDays;
                iLDay = iOffsetDays;
                for (iLMonth = 12; iOffsetDays > iGetLMonthDays(iLYear, iLMonth); iLMonth--) {
                    iLDay = iOffsetDays;
                    iOffsetDays -= iGetLMonthDays(iLYear, iLMonth);
                }
                if (0 == iLDay)
                    iLDay = 1;
                else
                    iLDay = iGetLMonthDays(iLYear, iLMonth) - iOffsetDays + 1;
            } else {
                iLYear = iYear;
                iOffsetDays -= iSolarLunarOffsetTable[iYear - 1901];
                iLDay = iOffsetDays + 1;
                for (iLMonth = 1; iOffsetDays >= 0; iLMonth++) {
                    iLDay = iOffsetDays + 1;
                    iOffsetDays -= iGetLMonthDays(iLYear, iLMonth);
                    if ((iLeapMonth == iLMonth) && (iOffsetDays > 0)) {
                        iLDay = iOffsetDays;
                        iOffsetDays -= iGetLMonthDays(iLYear, iLMonth + 12);
                        if (iOffsetDays <= 0) {
                            iLMonth += 12 + 1;
                            break;
                        }
                    }
                }
                iLMonth--;
            }
            return "" + iLYear + (iLMonth > 9 ? "" + iLMonth : "0" + iLMonth) + (iLDay > 9 ? "" + iLDay : "0" + iLDay);
        }

        // The lunar calendar is turned into the Solar calendar
        static String sCalendarLundarToSolar(int iYear, int iMonth, int iDay) {
            int iSYear, iSMonth, iSDay;
            int iOffsetDays = iGetLNewYearOffsetDays(iYear, iMonth, iDay) + iSolarLunarOffsetTable[iYear - 1901];
            int iYearDays = bIsSolarLeapYear(iYear) ? 366 : 365;
            if (iOffsetDays >= iYearDays) {
                iSYear = iYear + 1;
                iOffsetDays -= iYearDays;
            } else {
                iSYear = iYear;
            }
            iSDay = iOffsetDays + 1;
            for (iSMonth = 1; iOffsetDays >= 0; iSMonth++) {
                iSDay = iOffsetDays + 1;
                iOffsetDays -= iGetSYearMonthDays(iSYear, iSMonth);
            }
            iSMonth--;
            return "" + iSYear + (iSMonth > 9 ? iSMonth + "" : "0" + iSMonth) + (iSDay > 9 ? iSDay + "" : "0" + iSDay);
        }
    }

    // 自定义星期类
    public static class Week {
        int iWeek;
        private String sWeek[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        public Week() {
            iWeek = 0;
        }

        public Week(int w) {
            if ((w > 6) || (w < 0)) {
                System.out.println("Week out of range, I think you want Sunday");
                this.iWeek = 0;
            } else
                this.iWeek = w;
        }

        public String toString() {
            return sWeek[iWeek];
        }
    }

    // 自定义日期类
    public static class MyDate {
        public int iYear;
        public int iMonth;
        public int iDay;

        private static int checkYear(int iYear) {
            if ((iYear > 1901) && (iYear < 2050))
                return iYear;
            else {
                System.out.println("The Year out of range, I think you want 1981");
                return 1981;
            }
        }

        public MyDate(int iYear, int iMonth, int iDay) {
            this.iYear = checkYear(iYear);
            this.iMonth = iMonth;
            this.iDay = iDay;
        }

        public MyDate(int iYear, int iMonth) {
            this.iYear = checkYear(iYear);
            this.iMonth = iMonth;
            this.iDay = 1;
        }

        public MyDate(int iYear) {
            this.iYear = checkYear(iYear);
            this.iMonth = 1;
            this.iDay = 1;
        }

        public MyDate() {
            this.iYear = 1981;
            this.iMonth = 1;
            this.iDay = 1;
        }

        public String toString() {
            return "" + this.iYear + (this.iMonth > 9 ? "" + this.iMonth : "0" + this.iMonth)
                    + (this.iDay > 9 ? "" + this.iDay : "0" + this.iDay);
        }

        public boolean equals(MyDate md) {
            return ((md.iDay == this.iDay) && (md.iMonth == this.iMonth) && (md.iYear == this.iYear));
        }
    }

    // 阳历日期类,继承自定义日期
    public static class SolarDate extends MyDate {
        private static int checkMonth(int iMonth) {
            if (iMonth > 12) {
                System.out.println("Month out of range, I think you want 12 ");
                return 12;
            } else if (iMonth < 1) {
                System.out.println("Month out of range, I think you want 1 ");
                return 1;
            } else
                return iMonth;
        }

        private static int checkDay(int iYear, int iMonth, int iDay) {
            int iMonthDays = ChineseCalendar.iGetSYearMonthDays(iYear, iMonth);
            if (iDay > iMonthDays) {
                System.out.println("Day out of range, I think you want " + iMonthDays + " ");
                return iMonthDays;
            } else if (iDay < 1) {
                System.out.println("Day out of range, I think you want 1 ");
                return 1;
            } else
                return iDay;
        }

        public SolarDate(int iYear, int iMonth, int iDay) {
            super(iYear);
            this.iMonth = checkMonth(iMonth);
            this.iDay = checkDay(this.iYear, this.iMonth, iDay);
        }

        public SolarDate(int iYear, int iMonth) {
            super(iYear);
            this.iMonth = checkMonth(iMonth);
        }

        public SolarDate(int iYear) {
            super(iYear);
        }

        public SolarDate() {
            super();
        }

        public String toString() {
            return "" + this.iYear + (this.iMonth > 9 ? "-" + this.iMonth : "-0" + this.iMonth)
                    + (this.iDay > 9 ? "-" + this.iDay : "-0" + this.iDay);
        }

        public Week toWeek() {
            int iOffsetDays = 0;
            for (int i = 1901; i < iYear; i++) {
                if (ChineseCalendar.bIsSolarLeapYear(i))
                    iOffsetDays += 366;
                else
                    iOffsetDays += 365;
            }
            iOffsetDays += ChineseCalendar.iGetSNewYearOffsetDays(iYear, iMonth, iDay);
            return new Week((iOffsetDays + 2) % 7);
        }

        public LunarDate toLunarDate() {
            int iYear, iMonth, iDay, iDate;
            LunarDate ld;
            iDate = Integer.parseInt(ChineseCalendar.sCalendarSolarToLundar(this.iYear, this.iMonth, this.iDay));
            iYear = iDate / 10000;
            iMonth = iDate % 10000 / 100;
            iDay = iDate % 100;
            ld = new LunarDate(iYear, iMonth, iDay);
            return ld;
        }
    }

    // 阴历日期类,继承自定义日期类
    public static class LunarDate extends MyDate {
        private String sChineseNum[] = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

        private static int checkMonth(int iYear, int iMonth) {
            if ((iMonth > 12) && (iMonth == ChineseCalendar.iGetLLeapMonth(iYear) + 12)) {
                return iMonth;
            } else if (iMonth > 12) {
                System.out.println("Month out of range, I think you want 12 ");
                return 12;
            } else if (iMonth < 1) {
                System.out.println("Month out of range, I think you want 1 ");
                return 1;
            } else
                return iMonth;
        }

        private static int checkDay(int iYear, int iMonth, int iDay) {
            int iMonthDays = ChineseCalendar.iGetLMonthDays(iYear, iMonth);
            if (iDay > iMonthDays) {
                System.out.println("Day out of range, I think you want " + iMonthDays + " ");
                return iMonthDays;
            } else if (iDay < 1) {
                System.out.println("Day out of range, I think you want 1 ");
                return 1;
            } else
                return iDay;
        }

        public LunarDate(int iYear, int iMonth, int iDay) {
            super(iYear);
            this.iMonth = checkMonth(this.iYear, iMonth);
            this.iDay = checkDay(this.iYear, this.iMonth, iDay);
        }

        public LunarDate(int iYear, int iMonth) {
            super(iYear);
            this.iMonth = checkMonth(this.iYear, iMonth);
        }

        public LunarDate(int iYear) {
            super(iYear);
        }

        public LunarDate() {
            super();
        }

        public String toString() {
            String sCalendar = "农历";
            sCalendar += sChineseNum[iYear / 1000] + sChineseNum[iYear % 1000 / 100] + sChineseNum[iYear % 100 / 10]
                    + sChineseNum[iYear % 10] + "(" + toChineseEra() + ")年";
            if (iMonth > 12) {
                iMonth -= 12;
                sCalendar += "闰";
            }
            if (iMonth == 12)
                sCalendar += "腊月";
            else if (iMonth == 11)
                sCalendar += "冬月";
            else if (iMonth == 1)
                sCalendar += "正月";
            else
                sCalendar += sChineseNum[iMonth] + "月";
            if (iDay > 29)
                sCalendar += "三十";
            else if (iDay > 20)
                sCalendar += "二十" + sChineseNum[iDay % 20];
            else if (iDay == 20)
                sCalendar += "二十";
            else if (iDay > 10)
                sCalendar += "十" + sChineseNum[iDay % 10];
            else
                sCalendar += "初" + sChineseNum[iDay];
            return sCalendar;
        }

        public CnWeek toWeek() {
            int iOffsetDays = 0;
            for (int i = 1901; i < iYear; i++)
                iOffsetDays += ChineseCalendar.iGetLYearDays(i);
            iOffsetDays += ChineseCalendar.iGetLNewYearOffsetDays(iYear, iMonth, iDay);
            return new CnWeek((iOffsetDays + 2) % 7);
        }

        public ChineseEra toChineseEra() {
            return new ChineseEra(iYear);
        }

        public SolarDate toSolarDate() {
            int iYear, iMonth, iDay, iDate;
            SolarDate sd;
            iDate = Integer.parseInt(ChineseCalendar.sCalendarLundarToSolar(this.iYear, this.iMonth, this.iDay));
            iYear = iDate / 10000;
            iMonth = iDate % 10000 / 100;
            iDay = iDate % 100;
            sd = new SolarDate(iYear, iMonth, iDay);
            return sd;
        }
    }

    public static class CnWeek extends Week {
        private String sCnWeek[] = {"日", "一", "二", "三", "四", "五", "六"};

        public CnWeek() {
            super();
        }

        public CnWeek(int iWeek) {
            super(iWeek);
        }

        public String toString() {
            return "星期" + sCnWeek[this.iWeek];
        }
    }

    public static class ChineseEra {
        int iYear;
        String[] sHeavenlyStems = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        String[] sEarthlyBranches = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

        public ChineseEra() {
            iYear = 1981;
        }

        public ChineseEra(int iYear) {
            if ((iYear < 2050) && (iYear > 1901))
                this.iYear = iYear;
            else
                this.iYear = 1981;
        }

        public String toString() {
            int temp;
            temp = Math.abs(iYear - 1924);
            return sHeavenlyStems[temp % 10] + sEarthlyBranches[temp % 12];
        }
    }
}
