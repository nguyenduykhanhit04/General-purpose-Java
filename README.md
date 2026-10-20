# ☕ General-purpose Java

Kho lưu trữ tập hợp các dự án Java được xây dựng với mục đích học tập chuyên sâu — không chỉ dừng lại ở "biết cách dùng" mà còn **thấu hiểu bản chất cơ chế hoạt động bên dưới** của từng công nghệ.

---

## 📂 Danh Sách Dự Án

### 01. [Student Management System](./studentmanagementsystem/)

> **Hệ thống quản lý sinh viên** — thực hành Java Core thuần túy với kiến trúc nhiều tầng.

**Nội dung:**
- Mô hình hóa đối tượng (`Student`, `Gender`) với đầy đủ validation
- Kiến trúc 3 tầng: **Repository → Service → Main** (tách biệt trách nhiệm)
- Xử lý ngoại lệ tùy chỉnh: `DuplicateStudentException`, `InvalidStudentException`, `StudentNotFoundException`
- Thao tác CRUD đầy đủ: Create, Read, Update, Delete
- **Stream API**: tìm kiếm theo tên, GPA, tuổi, giới tính
- **Comparator & Sorting**: sắp xếp đơn tiêu chí và đa tiêu chí (tên + GPA)
- Lọc kết hợp điều kiện phức tạp (ví dụ: nữ sinh có GPA ≥ 8.0)

**Công nghệ:** Java Core, Stream API, Optional, Custom Exceptions, Collections

---

### 02. [Understanding JDBC](./understanding-jdbc/)

> **Hiểu rõ bản chất cốt lõi của JDBC** — 8 bài thực hành từ kiến trúc Driver đến tự tay viết Connection Pool.

**Nội dung:**
| # | Chủ đề | Bài học cốt lõi |
|---|--------|----------------|
| 1 | Kiến trúc JDBC & SPI | JDBC là tập Interface, Driver là Implementation; cơ chế ServiceLoader |
| 2 | Connection Lifecycle | TCP Socket handshake, chi phí mở kết nối, `try-with-resources` |
| 3 | Statement vs PreparedStatement | SQL Injection exploit, Execution Plan Cache |
| 4 | ResultSet & Cursor | Cursor model, Fetch Size, `ResultSetMetaData` |
| 5 | Transaction & ACID | `autoCommit`, `commit/rollback`, Savepoint |
| 6 | Batch Processing | Giảm Network Roundtrips từ 1,000 → 5 lần |
| 7 | DIY Connection Pool | Dynamic Proxy (`java.lang.reflect.Proxy`), bí mật của HikariCP |
| 8 | Mini ORM & DAO Pattern | Reflection API + `ResultSetMetaData` = cách Hibernate hoạt động |

**Công nghệ:** JDBC, H2 Database (In-Memory), Java Reflection, Dynamic Proxy

---

## 🚀 Cách Chạy

### Student Management System
Mở `studentmanagementsystem/` trong IntelliJ IDEA và chạy [`src/Main.java`](./studentmanagementsystem/src/Main.java).

### Understanding JDBC
```powershell
cd "d:\Project\General-purpose Java\understanding-jdbc"
.\compile_and_run.ps1        # Chạy menu tương tác
.\compile_and_run.ps1 all    # Chạy toàn bộ 8 bài liên tiếp
```
Hoặc xem chi tiết tại [`understanding-jdbc/README.md`](./understanding-jdbc/README.md).

---

## 🛠️ Yêu Cầu

- **JDK 17+**
- **IntelliJ IDEA** (khuyến nghị) hoặc bất kỳ IDE Java nào
- Không cần cài thêm Database — các dự án dùng **H2 In-Memory Database**