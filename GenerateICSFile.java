import com.nlf.calendar.Lunar;
import com.nlf.calendar.LunarMonth;
import com.nlf.calendar.Solar;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GenerateICSFile {

    /**
     * 生成ICS文件的方法
     */
    public static void generateICSFile(String name, LocalDate birthDate, int num, String state, String location, boolean remind, String remindTime, String remindDescription, String fileName) throws IOException {
        Solar solarBirth = new Solar(birthDate.getYear(), birthDate.getMonthValue(), birthDate.getDayOfMonth());
        Lunar lunarBirth = solarBirth.getLunar();
        int lunarMonth = lunarBirth.getMonth();
        int lunarDay = lunarBirth.getDay();
        boolean isLeapMonth = checkLeapMonth(lunarBirth, lunarMonth);

        StringBuilder icsContent = initializeICS();

        // 处理出生年份的农历生日
        addZeroYearEvent(icsContent, name, lunarBirth, state, location, remind, remindTime, remindDescription);

        // 处理后续年份
        addFutureEvents(icsContent, name, birthDate, num, lunarMonth, lunarDay, isLeapMonth, state, location, remind, remindTime, remindDescription);

        finalizeICS(icsContent, fileName);
    }

    /**
     * 检查农历日期是否为闰月
     */
    private static boolean checkLeapMonth(Lunar lunarBirth, int lunarMonth) {
        LunarMonth birthLunarMonth = LunarMonth.fromYm(lunarBirth.getYear(), lunarMonth);
        return birthLunarMonth.isLeap();
    }

    /**
     * 初始化ICS内容
     */
    private static StringBuilder initializeICS() {
        StringBuilder icsContent = new StringBuilder();
        icsContent.append("BEGIN:VCALENDAR\n");
        icsContent.append("VERSION:2.0\n");
        icsContent.append("CALSCALE:GREGORIAN\n");
        return icsContent;
    }

    /**
     * 添加0岁的农历生日事件
     */
    private static void addZeroYearEvent(StringBuilder icsContent, String name, Lunar lunarBirth, String state, String location, boolean remind, String remindTime, String remindDescription) {
        try {
            Solar zeroYearSolar = lunarBirth.getSolar();
            icsContent.append(generateICSEvent(name, 0, lunarBirth, zeroYearSolar, state, location, remind, remindTime, remindDescription));
        } catch (IllegalArgumentException e) {
            System.err.println("跳过无效的农历日期: " + e.getMessage());
        }
    }

    /**
     * 添加未来年份的农历生日事件
     */
    private static void addFutureEvents(StringBuilder icsContent, String name, LocalDate birthDate, int num, int lunarMonth, int lunarDay, boolean isLeapMonth, String state, String location, boolean remind, String remindTime, String remindDescription) {
        for (int i = 1; i < num; i++) {
            int year = birthDate.getYear() + i;
            if (shouldSkipLeapMonth(year, lunarMonth, isLeapMonth)) {
                continue;
            }
            try {
                Lunar lunarCurrent = new Lunar(year, lunarMonth, lunarDay);
                Solar solarCurrent = lunarCurrent.getSolar();
                icsContent.append(generateICSEvent(name, i, lunarCurrent, solarCurrent, state, location, remind, remindTime, remindDescription));
            } catch (IllegalArgumentException e) {
                System.err.println("跳过年份 " + year + " 的无效农历日期: " + e.getMessage());
            }
        }
    }

    /**
     * 检查是否需要跳过闰月年份
     */
    private static boolean shouldSkipLeapMonth(int year, int lunarMonth, boolean isLeapMonth) {
        if (!isLeapMonth) {
            return false;
        }
        LunarMonth currentLunarMonth = LunarMonth.fromYm(year, lunarMonth);
        return !currentLunarMonth.isLeap();
    }

    /**
     * 完成ICS文件的构建并写入文件
     */
    private static void finalizeICS(StringBuilder icsContent, String fileName) throws IOException {
        icsContent.append("END:VCALENDAR\n");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            writer.write(icsContent.toString());
        }
    }

    /**
     * 生成ICS事件的方法
     */
    private static String generateICSEvent(String name, int age, Lunar lunar, Solar solar, String state, String location, boolean remind, String remindTime, String remindDescription) {
        StringBuilder event = new StringBuilder();
        event.append("BEGIN:VEVENT\n");
        event.append("UID:").append(solar.toYmd()).append("T000000Z@kamihara.com\n");
        event.append("DTSTAMP:").append(getUTCDateTime()).append("\n");
        event.append("DTSTART;VALUE=DATE:").append(solar.toYmd().replace("-", "")).append("\n");
        event.append("DTEND;VALUE=DATE:").append(solar.toYmd().replace("-", "")).append("\n");
        event.append("SUMMARY:").append(name).append("的农历生日\n");

        // 描述信息
        event.append("DESCRIPTION:这是").append(name).append("的").append(age).append("岁农历生日。他的出生日农历是")
                .append(lunar.getYearInGanZhi()).append("年").append(lunar.getMonthInChinese()).append("月").append(lunar.getDayInChinese())
                .append("，公历是").append(lunar.getSolar().toYmd()).append("。今天农历是：")
                .append(lunar.getYearInGanZhi()).append("年").append(lunar.getMonthInChinese()).append("月").append(lunar.getDayInChinese())
                .append("，公历是").append(solar.toYmd()).append("。\n");

        // 事件地址
        if (!location.isEmpty()) {
            event.append("LOCATION:").append(location).append("\n");
        }

        event.append("PRIORITY:5\n");
        event.append("CATEGORIES:生日,农历\n");
        event.append("CLASS:PRIVATE\n");
        event.append("TRANSP:").append(state).append("\n");
        event.append("STATUS:CONFIRMED\n");

        // 提醒设置
        if (remind) {
            event.append("BEGIN:VALARM\n");
            event.append("TRIGGER:").append(remindTime).append("\n");
            event.append("ACTION:DISPLAY\n");
            event.append("DESCRIPTION:").append(remindDescription).append("\n");
            event.append("END:VALARM\n");
        }

        event.append("END:VEVENT\n");
        return event.toString();
    }

    /**
     * 获取当前UTC时间
     */
    public static String getUTCDateTime() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T000000Z";
    }
}
