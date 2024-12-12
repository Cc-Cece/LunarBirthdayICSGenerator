import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BatchLunarBirthdayICSGenerator {
    public static void main() {
        // 使用文件选择器选择输入文件
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("请选择输入文件");
        int result = fileChooser.showOpenDialog(null);

        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("未选择文件，程序退出。");
            System.out.println("请参考以下信息，手动创建满足条件的.txt文件\n\n[姓名（任何文字）,公历生日（YYYY.MM.DD),生成未来年数（纯整数）,事件忙碌状态(y忙碌否则空闲),事件地址（任何文字，可选）,是否提醒（y提醒否则不提醒）,提醒时间（参照下文）,提醒描述（任何文字，可选）](本行为表头，以下是示例)\n张三,1990.05.15,50,1,北京市,y,-P1D,请提前一天准备礼物\n李四,1985.12.01,100,0,,n,,\n王五,2000.01.10,30,1,上海市,n,,\n赵六,1995.07.20,80,0,深圳市,y,-P2D,提前两天通知准备\n\nP：表示时间段（Period）；T：分隔日期和时间的标记。D：天、H：小时、M：分钟、S：秒。如-P1D提前1天；-P6H提前6小时；-P1DT6H提前1天6小时）");
            return;
        }

        File inputFile = fileChooser.getSelectedFile();
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("无效的文件选择，请选择正确的文件。");
            return;
        }

        // 读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 2) {
                    System.err.println("跳过无效行: " + line);
                    continue;
                }

                // 获取输入数据
                String name = fields[0].trim();
                String birthDateInput = fields[1].trim();
                LocalDate birthDate = LocalDate.parse(birthDateInput, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                int num = (fields.length > 2 && !fields[2].isEmpty()) ? Integer.parseInt(fields[2].trim()) : 100;
                String state = (fields.length > 3 && "y".equalsIgnoreCase(fields[3].trim())) ? "OPAQUE" : "TRANSPARENT";
                String location = (fields.length > 4) ? fields[4].trim() : "";
                boolean remind = (fields.length > 5 && "y".equalsIgnoreCase(fields[5].trim()));
                String remindTime = (fields.length > 6 && !fields[6].isEmpty()) ? fields[6].trim() : "-P1D";
                String remindDescription = (fields.length > 7) ? fields[7].trim() : "";
                String fileName = name + "的农历生日.ics";
                GenerateICSFile.generateICSFile(name, birthDate, num, state, location, remind, remindTime, remindDescription, fileName);
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }
        System.out.println("批量 ICS 文件生成完成！");
    }
}
