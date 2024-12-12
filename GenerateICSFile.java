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
     *
     * @param name              用户姓名
     * @param birthDate         公历生日
     * @param num               生成未来多少年
     * @param state             忙碌状态
     * @param location          事件地址
     * @param remind            是否提醒
     * @param remindTime        提醒时间
     * @param remindDescription 提醒描述
     * @param fileName          输出文件名
     * @throws IOException 文件操作异常
     */
    public static void generateICSFile(String name, LocalDate birthDate, int num, String state, String location, boolean remind, String remindTime, String remindDescription, String fileName) throws IOException {
        // 将公历转为农历
        Solar solarBirth = new Solar(birthDate.getYear(), birthDate.getMonthValue(), birthDate.getDayOfMonth());
        Lunar lunarBirth = solarBirth.getLunar();
        int lunarMonth = lunarBirth.getMonth();
        int lunarDay = lunarBirth.getDay();

        // 判断出生农历日期是否为闰月
        LunarMonth birthLunarMonth = LunarMonth.fromYm(lunarBirth.getYear(), lunarMonth);
        boolean isLeapMonth = birthLunarMonth.isLeap();

        // 构建ICS文件内容
        StringBuilder icsContent = new StringBuilder();
        icsContent.append("BEGIN:VCALENDAR\n");
        icsContent.append("VERSION:2.0\n");
        icsContent.append("CALSCALE:GREGORIAN\n");

        // 首先处理出生年份的农历生日（0岁）
        try {
            Solar zeroYearSolar = lunarBirth.getSolar();
            icsContent.append(generateICSEvent(name, 0, lunarBirth, zeroYearSolar, state, location, remind, remindTime, remindDescription));
        } catch (IllegalArgumentException e) {
            System.err.println("跳过出生年份的无效农历日期: " + e.getMessage());
        }

        // 处理后续年份
        for (int i = 1; i < num; i++) {  // 从下一年开始
            int year = birthDate.getYear() + i;

            // 检查是否为有效闰月日期
            LunarMonth currentLunarMonth = LunarMonth.fromYm(year, lunarMonth);
            if (isLeapMonth && !currentLunarMonth.isLeap()) {
                continue;  // 跳过不符合闰月条件的年份
            }

            try {
                Lunar lunarCurrent = new Lunar(year, lunarMonth, lunarDay);
                Solar solarCurrent = lunarCurrent.getSolar();
                icsContent.append(generateICSEvent(name, i, lunarCurrent, solarCurrent, state, location, remind, remindTime, remindDescription));
            } catch (IllegalArgumentException e) {
                System.err.println("跳过年份 " + year + " 的无效农历日期: " + e.getMessage());
            }
        }

        icsContent.append("END:VCALENDAR\n");
        // 写入文件时强制使用 UTF-8 编码
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            writer.write(icsContent.toString());
        }

        // 写入文件
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
