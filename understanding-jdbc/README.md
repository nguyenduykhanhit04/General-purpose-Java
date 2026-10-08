# 🚀 HIỂU RÕ BẢN CHẤT CỐT LÕI CỦA JDBC (JDBC UNDER THE HOOD)

Chào mừng bạn đến với dự án **Understanding JDBC** — được thiết kế đặc biệt nhằm giúp bạn không chỉ biết "cách dùng" JDBC mà còn **thấu hiểu bản chất cơ chế hoạt động bên dưới** (Under the hood) của toàn bộ hệ sinh thái kết nối cơ sở dữ liệu trong Java.

---

## 📑 MỤC LỤC KIẾN THỨC CỐT LÕI

1. [Bản chất kiến trúc JDBC (Specification vs Implementation)](#1-bản-chất-kiến-trúc-jdbc)
2. [Cơ chế SPI & DriverManager (Service Provider Interface)](#2-cơ-chế-spi--drivermanager)
3. [Bản chất Connection & Chi phí khởi tạo tầng mạng (TCP Sockets)](#3-bản-chất-connection--chi-phí-khởi-tạo)
4. [Statement vs PreparedStatement & Lỗ hổng SQL Injection](#4-statement-vs-preparedstatement--lỗ-hổng-sql-injection)
5. [Bản chất ResultSet, Con trỏ (Cursor) & Fetch Size](#5-bản-chất-resultset-con-trỏ-cursor--fetch-size)
6. [Giao dịch (Transaction - ACID) & Điểm lưu (Savepoint)](#6-giao-dịch-transaction---acid--savepoint)
7. [Batch Processing (Xử lý lô) & Giảm thiểu Network Roundtrips](#7-batch-processing-xử-lý-lô)
8. [Tự tay viết Connection Pool (Bí mật Dynamic Proxy của HikariCP)](#8-tự-tay-viết-connection-pool-mini-hikaricp)
9. [Bản chất ORM (Cách Spring JdbcTemplate & Hibernate hoạt động)](#9-bản-chất-orm--dao-pattern)
10. [Hướng dẫn cài đặt & Chạy dự án](#10-hướng-dẫn-chạy-dự-án)

---

## 1. BẢN CHẤT KIẾN TRÚC JDBC

### ❓ JDBC thực chất là gì?
- JDBC (**Java Database Connectivity**) **KHÔNG PHẢI** là một thư viện cụ thể kết nối tới cơ sở dữ liệu nào.
- JDBC thực chất là một **TẬP HỢP CÁC INTERFACE** nằm trong gói `java.sql` và `javax.sql` do Oracle/Sun Microsystems định nghĩa (gọi là **JDBC Specification**).
- Các nhà cung cấp CSDL (Oracle, MySQL, PostgreSQL, Microsoft SQL Server, SQLite, H2...) **phải tự viết mã nguồn (Class implementation)** để hiện thực hóa các interface này. File JAR chứa các class triển khai đó được gọi là **JDBC Driver**.

```
+------------------------------------------------------------------+
|                   ỨNG DỤNG JAVA CỦA BẠN                         |
|         (Chỉ gọi interface: Connection, PreparedStatement...)    |
+------------------------------------------------------------------+
                                 │
                                 ▼
+------------------------------------------------------------------+
|                      JDBC API (java.sql.*)                       |
|           Driver, DriverManager, Connection, Statement...        |
+------------------------------------------------------------------+
                                 │
                 ┌───────────────┼───────────────┐
                 ▼               ▼               ▼
         +---------------+---------------+---------------+
         | MySQL Driver  | Postgres Driver|   H2 Driver   |
         | (MySQL JAR)   | (Postgres JAR)|   (H2 JAR)    |
         +---------------+---------------+---------------+
                 │               │               │
                 ▼ (TCP Socket)  ▼ (TCP Socket)  ▼ (In-Memory/File)
            MySQL Server     PostgreSQL DB     H2 Database
```

> 🎯 **Bài Lab minh họa:** [`CustomDriverDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson01_architecture_spi/CustomDriverDemo.java)  
> Trong bài này, chúng ta tự viết class `ToyCustomDriver implements java.sql.Driver` để chứng minh rằng bất kỳ ai cũng có thể tự tạo một JDBC Driver tuân thủ giao kèo (contract).

---

## 2. CƠ CHẾ SPI & DRIVERMANAGER

### ❓ Vì sao ngày xưa phải viết `Class.forName(...)`, còn ngày nay thì không?
1. **Trước JDBC 4.0 (Java 5 trở về trước):**  
   Bạn bắt buộc phải gọi: `Class.forName("com.mysql.cj.jdbc.Driver");`  
   Mục đích: Nạp class Driver vào bộ nhớ JVM, kích hoạt khối mã tĩnh `static { DriverManager.registerDriver(new Driver()); }`.
2. **Từ JDBC 4.0 (Java 6 trở đi):**  
   Java giới thiệu cơ chế **SPI (Service Provider Interface)** qua `java.util.ServiceLoader`.
   - Trong mỗi file JAR Driver, nhà phát triển đặt một file tại đường dẫn:  
     `META-INF/services/java.sql.Driver`
   - File này chứa tên đầy đủ của class Driver (vd: `org.h2.Driver`, `com.mysql.cj.jdbc.Driver`).
   - Khi `DriverManager` khởi động, nó tự động quét toàn bộ ClassPath, đọc file SPI này và tự đăng ký Driver vào danh sách nội bộ!

### ❓ DriverManager chọn Driver như thế nào?
Khi bạn gọi:
```java
Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
```
`DriverManager` sẽ duyệt qua danh sách các Driver đã đăng ký:
```java
for (DriverInfo aDriver : registeredDrivers) {
    if (aDriver.driver.acceptsURL(url)) {
        return aDriver.driver.connect(url, info);
    }
}
throw new SQLException("No suitable driver found for " + url);
```

> 🎯 **Bài Lab minh họa:** [`DriverManagerDeepDiveDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson01_architecture_spi/DriverManagerDeepDiveDemo.java)

---

## 3. BẢN CHẤT CONNECTION & CHI PHÍ KHỞI TẠO

### ❓ Một Connection ở tầng hệ điều hành là gì?
Một `Connection` trong JDBC thực chất là một **Socket TCP** mở giữa máy Client và Database Server.

Để tạo ra một Connection, hệ thống phải thực hiện:
1. Phân giải DNS / Tìm địa chỉ IP.
2. **Bắt tay 3 bước TCP (TCP 3-way Handshake)**: SYN -> SYN-ACK -> ACK.
3. Bắt tay mã hóa bảo mật SSL/TLS (nếu có).
4. Bắt tay xác thực (Authentication handshake: kiểm tra username, hash mật khẩu).
5. Phía Database Server: Tạo mới một tiến trình con (Process) hoặc luồng (Thread), cấp phát vùng nhớ RAM riêng (Session Memory, Temp Buffers) để phục vụ riêng cho kết nối đó.

⚠️ **Hậu quả nếu mở/đóng Connection liên tục:**  
Thời gian mở kết nối mạng có thể **chậm gấp 10 - 100 lần** thời gian chạy chính câu lệnh SQL!

### ❓ Quản lý tài nguyên & `try-with-resources`
Cả 3 interface: `Connection`, `Statement`, `ResultSet` đều giữ tài nguyên hệ điều hành (File descriptors, Sockets, Cursor handles). Nếu không đóng, ứng dụng sẽ bị **Resource Leak / Socket Exhaustion**.
- Thứ tự đóng chuẩn: `ResultSet` -> `Statement` -> `Connection`.
- Từ Java 7, cả 3 đều kế thừa `java.lang.AutoCloseable`, cho phép dùng cú pháp `try-with-resources` để tự động giải phóng tài nguyên an toàn 100%.

> 🎯 **Bài Lab minh họa:**  
> - [`RawConnectionCostDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson02_connection_lifecycle/RawConnectionCostDemo.java) (Đo chi phí mở 100 raw connections vs tái sử dụng)  
> - [`ResourceLeakDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson02_connection_lifecycle/ResourceLeakDemo.java) (So sánh dọn dẹp kiểu cũ vs try-with-resources)

---

## 4. STATEMENT VS PREPAREDSTATEMENT & LỖ HỔNG SQL INJECTION

| Tiêu chí | `Statement` | `PreparedStatement` |
| :--- | :--- | :--- |
| **Cơ chế xử lý** | Ghép chuỗi thô gửi sang DB Server | Biên dịch câu lệnh mẫu trước với dấu hỏi chấm (`?`) |
| **Phân tích cú pháp (Parsing)** | DB Server phải parse cú pháp **MỖI LẦN GỌI** | DB Server chỉ parse **MỘT LẦN DUY NHẤT** |
| **Tối ưu hóa (Optimizer)** | Sinh lại Execution Plan mỗi lần | Tái sử dụng Kế hoạch thực thi (Execution Plan Cache) |
| **Tham số truyền vào** | Bị lẫn lộn vào cú pháp lệnh SQL | Tách biệt hoàn toàn dưới dạng giá trị Literal |
| **Khả năng chống SQL Injection** | ❌ **RẤT NGUY HIỂM** | ✅ **AN TOÀN TUYỆT ĐỐI 100%** |
| **Hiệu năng khi lặp nhiều lần** | Chậm | Nhanh hơn đáng kể |

### 🛡️ Tại sao PreparedStatement miễn nhiễm với SQL Injection?
Khi hacker nhập `username = "' OR '1'='1"`:
- Với **Statement**: DB nhận chuỗi và xây dựng cây cú pháp AST (Abstract Syntax Tree), trong đó `'1'='1'` được hiểu là **toán tử logic** làm thay đổi ý đồ câu lệnh ban đầu -> Bypass thành công.
- Với **PreparedStatement**: Cây cú pháp AST đã được cố định từ trước. Tham số người dùng nhập được DB Server coi là **dữ liệu thô (string literal)**, dù trong chuỗi có chứa `' OR '1'='1` hay `DROP TABLE` thì DB chỉ đi tìm user có tên đúng bằng chuỗi ký tự đó!

> 🎯 **Bài Lab minh họa:**  
> - [`SqlInjectionExploitDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson03_statement_vs_prepared/SqlInjectionExploitDemo.java) (Demo tấn công bypass đăng nhập thực tế)  
> - [`PreparedStatementDeepDiveDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson03_statement_vs_prepared/PreparedStatementDeepDiveDemo.java) (Tận dụng Plan Cache & Lấy Generated Keys)

---

## 5. BẢN CHẤT RESULTSET, CON TRỎ (CURSOR) & FETCH SIZE

### ❓ ResultSet có phải là một List lưu toàn bộ dữ liệu trong RAM không?
**KHÔNG!** Đây là hiểu lầm cực kỳ tai hại.
1. `ResultSet` thực chất là một **CON TRỎ (CURSOR)** trỏ vào bộ đệm dòng (Row Buffer) nhận qua luồng mạng từ DB Server.
2. Vị trí ban đầu: Đứng ở `Before First` (trước dòng 1).
3. Khi gọi `rs.next()`: Con trỏ dịch chuyển xuống 1 dòng và trả về `true` (nếu còn dòng) hoặc `false` (nếu hết).

### ⚙️ Fetch Size là gì?
- Nếu bảng có **1,000,000 dòng**, bạn gọi `SELECT * FROM big_table`:
  - Nếu Driver tải hết 1 triệu dòng vào Heap RAM -> **`java.lang.OutOfMemoryError` ngay lập tức!**
  - Khi cấu hình `ps.setFetchSize(50)`: Driver chỉ kéo 50 dòng về máy Client mỗi đợt. Khi `rs.next()` duyệt hết 50 dòng đó, Driver mới âm thầm gửi yêu cầu qua Socket để kéo 50 dòng tiếp theo về.

### 🔍 ResultSetMetaData là gì?
`ResultSetMetaData` cho phép chương trình Java "soi thấu" cấu trúc bảng lúc Runtime: số cột, tên cột, kiểu dữ liệu SQL (`Types.VARCHAR`, `Types.INTEGER`...), có cho phép null hay không.  
👉 **Đây chính là nền tảng cốt lõi giúp các ORM (như Hibernate hay MyBatis) tự động map bảng vào Java Object!**

> 🎯 **Bài Lab minh họa:**  
> - [`CursorAndFetchSizeDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson04_resultset_cursor/CursorAndFetchSizeDemo.java)  
> - [`ResultSetMetadataDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson04_resultset_cursor/ResultSetMetadataDemo.java)

---

## 6. GIAO DỊCH (TRANSACTION - ACID) & SAVEPOINT

### ❓ Vì sao mặc định của JDBC lại nguy hiểm?
Mặc định khi mở kết nối: `conn.getAutoCommit() == true`.  
Mỗi câu lệnh `executeUpdate()` đều được commit ngay lập tức xuống đĩa cứng.

Trong nghiệp vụ chuyển tiền ngân hàng giữa 2 tài khoản:
1. Trừ 300,000 VND từ tài khoản A.
2. Cộng 300,000 VND cho tài khoản B.

Nếu dùng `autoCommit = true`, sau bước 1 server bị mất điện hoặc đứt mạng:
- Tiền của A đã bị trừ mất!
- B chưa nhận được tiền!
- **Hệ thống mất tiền, vi phạm nghiêm trọng tính Atomicity (Tính nguyên tử) của ACID!**

### ✅ Giải pháp:
```java
conn.setAutoCommit(false); // Bắt đầu giao dịch tường minh
try {
    // Bước 1: Trừ A
    // Bước 2: Cộng B
    conn.commit();         // Chốt giao dịch thành công
} catch (Exception e) {
    conn.rollback();       // Hoàn tác dữ liệu về nguyên vẹn nếu có lỗi
}
```

### 📍 Savepoint là gì?
`Savepoint` cho phép bạn tạo ra một điểm đánh dấu trong giao dịch để **hoàn tác một phần (partial rollback)** mà không cần hủy bỏ toàn bộ giao dịch chính.

> 🎯 **Bài Lab minh họa:**  
> - [`BankTransferTransactionDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson05_transactions_acid/BankTransferTransactionDemo.java)  
> - [`SavepointDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson05_transactions_acid/SavepointDemo.java)

---

## 7. BATCH PROCESSING (XỬ LÝ LÔ)

### ❓ Tại sao chèn nhiều dữ liệu bằng vòng lặp lại cực kỳ chậm?
- Chèn 1,000 dòng bằng loop `executeUpdate()`:
  - Client gửi gói tin câu 1 -> Chờ DB trả lời ACK -> Gửi câu 2 -> Chờ DB trả lời ACK...
  - Tổng cộng mất **1,000 lần di chuyển gói tin qua mạng (Network Roundtrips)**!
- Dùng Batch Processing (`addBatch()` & `executeBatch()`):
  - Client gom 200 câu lệnh vào bộ đệm.
  - Gửi cả 200 câu trong **1 gói tin TCP duy nhất**.
  - Giảm số lượng Network Roundtrips từ 1,000 lần xuống chỉ còn 5 lần!
  - Kết hợp với `conn.setAutoCommit(false)` để DB không phải ghi log đĩa cho từng câu đơn lẻ!

> 🎯 **Bài Lab minh họa:** [`BatchInsertBenchmarkDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson06_batch_processing/BatchInsertBenchmarkDemo.java) (Đo tốc độ chèn 1,000 dòng thực tế)

---

## 8. TỰ TAY VIẾT CONNECTION POOL (MINI HIKARICP)

### ❓ Bí mật lớn nhất của HikariCP / Connection Pool là gì?
Lập trình viên khi viết code đều dùng `try-with-resources`:
```java
try (Connection conn = dataSource.getConnection()) {
    // truy vấn dữ liệu...
} // HẾT KHỐI TRY NÀY THÌ conn.close() BẮT BUỘC ĐƯỢC GỌI!
```
Nếu `conn.close()` thực sự ngắt socket TCP, vậy Connection Pool có tác dụng gì?

### 💡 BẬT MÍ: Cơ chế Dynamic Proxy
Connection Pool **không bao giờ trả về Connection thật của Driver!**  
Nó bọc Connection thật trong một **Java Dynamic Proxy** (`java.lang.reflect.Proxy`).
- Khi bạn gọi `conn.close()`: Proxy sẽ **đánh chặn (intercept)** hàm này lại! Thay vì ngắt kết nối thật, nó **trả Connection thật về hàng đợi (Queue)** của Pool!
- Mọi hàm khác (`prepareStatement`, `commit`...) thì Proxy ủy quyền gọi trực tiếp trên Connection thật.

```
       Ứng dụng gọi conn.close()
                  │
                  ▼
       [ Dynamic Proxy (Wrapper) ]
                  │
        Bị đánh chặn lại!
                  │
                  ▼
       pool.offer(realConnection) ───► Trả về BlockingQueue
       (Không hề ngắt socket TCP!)
```

> 🎯 **Bài Lab minh họa:**  
> - [`SimpleConnectionPool.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson07_diy_connection_pool/SimpleConnectionPool.java) (Code Connection Pool hoàn chỉnh ~80 dòng)  
> - [`ConnectionPoolBenchmarkDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson07_diy_connection_pool/ConnectionPoolBenchmarkDemo.java) (Mô phỏng 100 requests đồng thời)

---

## 9. BẢN CHẤT ORM & DAO PATTERN

### ❓ Spring JdbcTemplate & Hibernate map dữ liệu như thế nào?
- **Manual RowMapper**: Lập trình viên tự viết logic trích xuất từng trường từ `ResultSet`.
- **Mini Generic ORM**: Kết hợp `ResultSetMetaData` (lấy danh sách tên cột) với `Java Reflection API` (tìm Field tương ứng trong Entity và gán giá trị vào).
- **DAO Pattern**: Chuẩn hóa các thao tác CRUD và nghiệp vụ truy vấn trong interface tách biệt, giúp tầng Service không bị phụ thuộc vào công nghệ cơ sở dữ liệu bên dưới.

> 🎯 **Bài Lab minh họa:**  
> - [`MiniRowMapper.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson08_mini_orm_and_dao/MiniRowMapper.java)  
> - [`StudentDaoJdbcImpl.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson08_mini_orm_and_dao/StudentDaoJdbcImpl.java)  
> - [`MiniOrmDemo.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/lesson08_mini_orm_and_dao/MiniOrmDemo.java)

---

## 10. HƯỚNG DẪN CHẠY DỰ ÁN

Dự án đã tích hợp sẵn thư viện **H2 Database (In-Memory)** và **MySQL Connector/J** trong thư mục `lib/`. Bạn **không cần cài đặt bất kỳ cơ sở dữ liệu nào** trên máy tính, chạy được ngay lập tức!

### Cách 1: Chạy bằng IntelliJ IDEA
1. Mở dự án trong IntelliJ IDEA. Thư mục `understanding-jdbc` đã được cấu hình thành module chuẩn.
2. Tìm đến file [`src/Main.java`](file:///d:/Project/General-purpose%20Java/understanding-jdbc/src/Main.java), nhấn nút **Run** (mũi tên xanh).
3. Màn hình Console tương tác sẽ xuất hiện cho phép bạn nhập số (từ 1 đến 10) để khám phá từng bài lab.

### Cách 2: Chạy bằng Terminal / PowerShell
Chỉ cần chạy file script có sẵn:
```powershell
cd "d:\Project\General-purpose Java\understanding-jdbc"
.\compile_and_run.ps1
```
Hoặc chạy trực tiếp bài thực hành bất kỳ bằng tham số dòng lệnh:
```powershell
.\compile_and_run.ps1 1    # Chạy Lab 1: Bản chất Driver
.\compile_and_run.ps1 4    # Chạy Lab 4: SQL Injection
.\compile_and_run.ps1 8    # Chạy Lab 8: DIY Connection Pool
.\compile_and_run.ps1 all  # Chạy toàn bộ 10 bài thực hành liên tiếp
```

### Cách 3: Chạy bằng Windows Command Prompt (.bat)
Double-click vào file `compile_and_run.bat` hoặc gõ trong cmd:
```cmd
compile_and_run.bat
```
