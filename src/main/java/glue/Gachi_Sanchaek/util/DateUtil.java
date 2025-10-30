package glue.Gachi_Sanchaek.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtil {
    public static int getTodayYYYYMMW(){
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Seoul"));
        int yy = date.getYear();
        int mm = date.getMonthValue();
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int weekOfMonth = date.get(weekFields.weekOfMonth());

        return yy*1000 + mm*10 + weekOfMonth;
    }
}
