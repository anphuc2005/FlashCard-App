## Ứng dụng Flash Card hỗ trợ học tập (Android)

Ứng dụng Flash Card là công cụ hỗ trợ ghi nhớ kiến thức (từ vựng, khái niệm, công thức, sự kiện, v.v.) trên nền tảng Android. Người dùng có thể tạo các bộ thẻ theo chủ đề và ôn tập mọi lúc mọi nơi một cách trực quan, dễ sử dụng.

---

### Mục tiêu

- Hỗ trợ người dùng học tập hiệu quả hơn thông qua phương pháp flash card.
- Đơn giản hóa việc tạo, quản lý và ôn tập các bộ thẻ.
- Tối ưu cho việc tự học trên điện thoại Android.

---

### Tính năng chính

- Quản lý bộ thẻ
	- Tạo, sửa, xóa các bộ thẻ theo từng môn học hoặc chủ đề.
	- Mỗi thẻ gồm: **Mặt trước** (câu hỏi/thuật ngữ) và **Mặt sau** (đáp án/giải thích).

- Ôn tập thẻ
	- Vuốt để chuyển thẻ, chạm để lật thẻ xem đáp án.
	- Tùy chọn ôn lần lượt hoặc ngẫu nhiên.
	- Đánh dấu thẻ "Đã thuộc" hoặc "Cần ôn lại".

- Theo dõi tiến độ
	- Thống kê số lượng thẻ trong bộ.
	- Ghi nhận số thẻ đã thuộc / chưa thuộc.

- Trải nghiệm người dùng
	- Giao diện trực quan, thân thiện cho người mới.
	- Thiết kế tối ưu cho màn hình điện thoại.

---

### Công nghệ sử dụng

- Nền tảng: Android.
- Ngôn ngữ: (Điền Kotlin hoặc Java theo project của bạn.)
- Kiến trúc: (Ví dụ: MVVM.)
- Cơ sở dữ liệu: (Ví dụ: Room / SQLite / Realm .)
- Thư viện chính:
	- AndroidX, Material Components.
	- Retrofit, Glide,

Hãy chỉnh lại phần này cho khớp hoàn toàn với cấu trúc và công nghệ trong project.

---

### Cài đặt và chạy ứng dụng

#### Cài đặt trên thiết bị Android (từ APK)

- Tải file APK build từ Android Studio hoặc từ link phát hành.
- Chép file APK vào điện thoại và mở để cài đặt.
- Cho phép cài đặt ứng dụng từ nguồn không xác định nếu hệ thống yêu cầu.

#### Build từ mã nguồn (dành cho lập trình viên)

1. Clone project:
	 ```bash
	 git clone https://github.com/<ten-tai-khoan>/<FlashCard-App>.git
	 ```
2. Mở project bằng Android Studio.
3. Đồng bộ Gradle và chọn thiết bị chạy (máy thật hoặc AVD).
4. Nhấn Run để cài đặt và chạy app.

---

### Hướng dẫn sử dụng

1. Mở ứng dụng và tạo một **bộ thẻ mới** theo chủ đề.
2. Thêm thẻ:
	 - Nhập nội dung mặt trước (câu hỏi, từ vựng, khái niệm,...).
	 - Nhập nội dung mặt sau (nghĩa, đáp án, giải thích,...).
3. Chọn bộ thẻ để bắt đầu **ôn tập**:
	 - Vuốt sang trái/phải để chuyển giữa các thẻ.
	 - Chạm vào thẻ để lật xem mặt sau.
4. Đánh dấu những thẻ đã thuộc hoặc cần ôn lại để việc học hiệu quả hơn.

---

### 🔮 Định hướng phát triển

- Đồng bộ dữ liệu trên nhiều thiết bị (qua tài khoản/Cloud).
- Chia sẻ bộ thẻ với người dùng khác.
- Thêm chế độ ôn tập theo thuật toán lặp lại ngắt quãng (Spaced Repetition).
- Hỗ trợ đa ngôn ngữ giao diện.


---

### 📄 Giấy phép

Mã nguồn và ứng dụng chỉ sử dụng cho mục đích học tập và nghiên cứu trong khuôn khổ môn học/đồ án (có thể chỉnh sửa mô tả này hoặc thay bằng loại license cụ thể như MIT, Apache 2.0, v.v. nếu cần).
