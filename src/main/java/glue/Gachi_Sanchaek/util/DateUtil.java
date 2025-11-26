package glue.Gachi_Sanchaek.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtil {
    public static int getTodayYYYYMMW(){
        return getTodayYYYYMMW(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }

    public static int getTodayYYYYMMW(LocalDate date) {
        // 한 주의 시작은 월요일이고, 첫 주에 4일이 포함되어있어야 첫 주 취급 (목/금/토/일)
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);

        int weekOfMonth = date.get(weekFields.weekOfMonth());

        // 첫 주에 해당하지 않는 주의 경우 전 달 마지막 주차로 계산
        if (weekOfMonth == 0) {
            // 전 달의 마지막 날 기준
            LocalDate lastDayOfLastMonth = date.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);
            return getTodayYYYYMMW(lastDayOfLastMonth);
        }

        // 이번 달의 마지막 날 기준
        LocalDate lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        // 마지막 주차의 경우 마지막 날이 월~수 사이이면 다음달 1주차로 계산
        if (weekOfMonth == lastDayOfMonth.get(weekFields.weekOfMonth()) && lastDayOfMonth.getDayOfWeek().compareTo(
                DayOfWeek.THURSDAY) < 0) {
            LocalDate firstDayOfNextMonth = lastDayOfMonth.plusDays(1); // 마지막 날 + 1일 => 다음달 1일
            return getTodayYYYYMMW(firstDayOfNextMonth);
        }

        return date.getYear()*1000 + date.getMonthValue()*10 + weekOfMonth;
    }


}
