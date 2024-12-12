import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请选择运行模式：1手动输入；2文件批量输入。默认手动输入：");
        String mode = scanner.nextLine().trim();
        if (mode.isEmpty()) {
            mode = "1";
        }
        if (mode.equals("1")) {
            // 进入手动输入模式
            ManualLunarBirthdayICSGenerator.main();
        } else if (mode.equals("2")) {
            // 进入批量输入模式
            BatchLunarBirthdayICSGenerator.main();
        } else {
            System.out.println("无效的选择，请选择 1 或 2");
        }
    }
}
