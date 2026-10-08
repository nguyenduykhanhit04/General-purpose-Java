package lesson01_architecture_spi;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

/**
 * ==============================================================================
 * BÀI 1.2: BẢN CHẤT CƠ CHẾ SPI (SERVICE PROVIDER INTERFACE) & DRIVERMANAGER
 * ==============================================================================
 *
 * CÂU HỎI LỊCH SỬ KINH ĐIỂN:
 * 1. Tại sao ngày xưa (JDBC 3.0 trở về trước) code luôn bắt đầu bằng:
 *       Class.forName("com.mysql.cj.jdbc.Driver");
 *    - Giải thích: Class.forName() tải class vào JVM, kích hoạt khối `static { ... }` bên trong driver,
 *      trong đó driver gọi: `DriverManager.registerDriver(new Driver())`.
 *
 * 2. Tại sao ngày nay (JDBC 4.0 trở đi) KHÔNG CẦN Class.forName() nữa?
 *    - Giải thích: Nhờ cơ chế SPI (Service Provider Interface) của Java SE!
 *    - Khi mở file JAR của H2 hoặc MySQL Connector, bạn sẽ thấy thư mục:
 *      `META-INF/services/java.sql.Driver`
 *      Nội dung file này chứa tên đầy đủ của class driver (vd: `org.h2.Driver`).
 *    - Khi DriverManager được gọi lần đầu tiên, nó sẽ dùng `ServiceLoader.load(Driver.class)`
 *      để quét toàn bộ file JAR trong ClassPath và tự động nạp Driver vào!
 *
 * 3. DriverManager hoạt động thế nào khi ta gọi getConnection(url)?
 *    - DriverManager lưu một danh sách các Driver đã đăng ký (CopyOnWriteArrayList<DriverInfo>).
 *    - Khi gọi getConnection(url):
 *      For each driver in registeredDrivers:
 *          if (driver.acceptsURL(url)) {
 *              return driver.connect(url, info);
 *          }
 *      Nếu không có driver nào nhận -> ném ra SQLException: "No suitable driver found for ...".
 * ==============================================================================
 */
public class DriverManagerDeepDiveDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 1.2: CƠ CHẾ SPI (SERVICE PROVIDER INTERFACE) TRONG JDBC");
        System.out.println("=======================================================");

        System.out.println("1. Liệt kê toàn bộ các Driver đã được DriverManager tự động nạp qua SPI:");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        int count = 0;
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            count++;
            System.out.printf("   [%d] Driver Class: %s (Phiên bản: %d.%d)%n",
                    count,
                    driver.getClass().getName(),
                    driver.getMajorVersion(),
                    driver.getMinorVersion());
        }

        if (count == 0) {
            System.out.println("   (Chưa có Driver nào được nạp)");
        }

        System.out.println("\n2. Kiểm tra xem các Driver hiện tại có nhận các URL sau không:");
        String[] testUrls = {
                "jdbc:h2:mem:test",
                "jdbc:mysql://localhost:3306/test",
                "jdbc:postgresql://localhost:5432/test",
                "jdbc:oracle:thin:@localhost:1521:xe"
        };

        for (String url : testUrls) {
            System.out.println("\n   Kiểm tra URL: " + url);
            Enumeration<Driver> driverList = DriverManager.getDrivers();
            boolean accepted = false;
            while (driverList.hasMoreElements()) {
                Driver d = driverList.nextElement();
                try {
                    if (d.acceptsURL(url)) {
                        System.out.println("      -> Được chấp nhận bởi Driver: " + d.getClass().getName());
                        accepted = true;
                    }
                } catch (Exception ignored) {}
            }
            if (!accepted) {
                System.out.println("      -> KHÔNG có driver nào trong Classpath nhận URL này.");
            }
        }
    }
}
