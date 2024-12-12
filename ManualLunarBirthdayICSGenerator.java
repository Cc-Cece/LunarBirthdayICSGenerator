
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
public class ManualLunarBirthdayICSGenerator {
    public static void main () throws IOException {
        Scanner scanner = new Scanner(System.in);
        // 用户输入姓名
        System.out.print("请输入姓名：");
        String name = scanner.nextLine();
        // 用户输入公历生日
        System.out.print("请输入公历生日（格式：yyyy.MM.dd）：");
        String birthDateInput = scanner.nextLine();
        LocalDate birthDate = LocalDate.parse(birthDateInput, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        int num = 100;  // 默认生成未来100年
        System.out.print("请输入生成未来多少年的农历生日（默认生成100）：");
        String numInput = scanner.nextLine();
        if (!numInput.isEmpty()) {
            num = Integer.parseInt(numInput);
        }
        // 用户输入事件忙碌状态
        System.out.print("请输入事件忙碌状态（输入 y 表示忙碌，直接回车表示空闲）：");
        String state = scanner.nextLine().trim();
        if (state.equals("y")) {
            state = "OPAQUE";
        } else {
            state = "TRANSPARENT";
        }
        // 用户输入事件地址
        System.out.print("请输入事件地址（可选，按回车跳过）：");
        String location = scanner.nextLine();
        // 用户输入是否提醒
        System.out.print("是否提醒（y/n，默认n，按回车跳过）：");
        String remindInput = scanner.nextLine();
        boolean remind = !remindInput.isEmpty() && remindInput.equalsIgnoreCase("y");
        // 用户输入提醒时间
        String remindTime = "-P1D";  // 默认提前一天
        if (remind) {
            System.out.print("请输入提醒时间（默认提前1天）：");
            String remindTimeInput = scanner.nextLine();
            if (!remindTimeInput.isEmpty()) {
                remindTime = remindTimeInput;
            }
        }
        // 用户输入提醒描述
        System.out.print("请输入提醒描述（可选，默认空，按回车跳过）：");
        String remindDescription = scanner.nextLine();

        // 调用生成ICS文件的函数
        String fileName = name + "的农历生日.ics";
        GenerateICSFile.generateICSFile(name, birthDate, num, state, location, remind, remindTime, remindDescription, fileName);
        System.out.println("ICS文件生成成功！文件名：" + fileName);
    }


}
