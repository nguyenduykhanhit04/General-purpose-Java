# ☕ General-purpose Java

Kho lưu trữ tập hợp các dự án Java được xây dựng với mục đích học tập chuyên sâu — không chỉ dừng lại ở "biết cách dùng" mà còn **thấu hiểu bản chất cơ chế hoạt động bên dưới** của từng công nghệ.

---

## 📂 Danh Sách Dự Án

### 01. [Student Management System](./studentmanagementsystem/)

> **Hệ thống quản lý sinh viên** — thực hành Java Core thuần túy với kiến trúc nhiều tầng.

**Cấu trúc dự án:**
```
studentmanagementsystem/
└── src/
    ├── Main.java                          # Điểm khởi chạy, demo toàn bộ tính năng
    ├── model/
    │   ├── Student.java                   # Entity: id, name, age, email, gender, gpa, createdAt
    │   └── Gender.java                    # Enum: MALE / FEMALE
    ├── repository/
    │   └── StudentRepository.java         # Tầng dữ liệu (in-memory List)
    ├── service/
    │   └── StudentService.java            # Tầng nghiệp vụ: validate, CRUD, search, sort
    ├── data/
    │   └── StudentData.java               # Dữ liệu mẫu (6 sinh viên)
    └── exception/
        ├── DuplicateStudentException.java
        ├── InvalidStudentException.java
        └── StudentNotFoundException.java
```

**Tính năng:**

| Nhóm | Tính năng |
|------|-----------|
| **CRUD** | Thêm, xem, cập nhật, xóa sinh viên |
| **Validation** | Kiểm tra id, tên, tuổi, email (`@`), GPA (0–10), giới tính |
| **Search** | Theo tên (keyword), GPA tối thiểu, tuổi chính xác, giới tính |
| **Sort** | GPA tăng/giảm dần; tên A-Z; tên + GPA đa tiêu chí |
| **Filter phức hợp** | Nữ sinh GPA ≥ ngưỡng **hoặc** nam sinh GPA ≥ 9.0 |
| **Exception handling** | 3 loại custom exception tách biệt rõ từng trường hợp lỗi |

**Điểm nổi bật về thiết kế:**
- **Encapsulation** — `StudentRepository` trả về `List.copyOf()` bảo vệ dữ liệu nội bộ
- **Optional** — `findById()` trả `Optional<Student>` thay vì `null`
- **Stream API** — toàn bộ search/sort dùng `stream().filter().sorted().toList()`
- **Immutable ID** — `Student` không có `setId()`, ngăn thay đổi khóa chính

**Công nghệ:** Java Core · Stream API · Optional · Collections · Custom Exceptions

**Cách chạy:**
Mở project trong IntelliJ IDEA → chạy [`src/Main.java`](./studentmanagementsystem/src/Main.java)

---

## 🛠️ Yêu Cầu

- **JDK 17+**
- **IntelliJ IDEA** (khuyến nghị) hoặc bất kỳ IDE Java nào