# Kịch Bản Kịch Module cho Ứng Dụng Flashcard Android Kotlin

## 1. Danh Sách Module
- **Module 1: Du học Flashcard**
- **Module 2: Tạo bộ thẻ tự tạo**
- **Module 3: Tải bộ thẻ hiện có**
- **Module 4: Chat với chatbot học tập**

## 2. Luồng Hành Trình Người Dùng
### Du học Flashcard
1. Người dùng mở ứng dụng
2. Chọn bộ thẻ để bắt đầu học
3. Duyệt qua thẻ và trả lời
4. Cung cấp phản hồi về câu trả lời

### Tạo Bộ Thẻ Tự Tạo
1. Nhấn nút 'Tạo Bộ Thẻ'
2. Nhập tên bộ thẻ và thêm các thẻ
3. Lưu bộ thẻ

### Tải Bộ Thẻ Hiện Có
1. Chọn bộ thẻ từ danh sách
2. Nhấn 'Tải về'
3. Bộ thẻ được thêm vào trang chính

### Chat với Chatbot Học Tập
1. Chọn 'Chatbot'
2. Nhập câu hỏi
3. Nhận câu trả lời và phản hồi của chatbot

## 3. Kịch Bản Từng Màn Hình
- **Màn Hình Chính:**
    - Hiển thị danh sách bộ thẻ
    - Nút 'Tạo Bộ Thẻ'
    - Nút 'Chatbot'

- **Màn Hình Chi Tiết Bộ Thẻ:**
    - Hiển thị thẻ
    - Nút 'Phản hồi'

- **Màn Hình Tạo Bộ Thẻ:**
    - Form nhập tên và thẻ
    - Nút 'Lưu'

- **Màn Hình Chatbot:**
    - Khung chat
    - Nút 'Gửi'

## 4. Mô Hình Dữ Liệu
- **FlashCard:**
    - id: String
    - question: String
    - answer: String

- **Deck:**
    - id: String
    - name: String
    - cards: List<FlashCard>

- **UserMessage:**
    - id: String
    - message: String
    - sender: String

## 5. Tương Tác API/Lưu Trữ Địa Phương
- **Tải bộ thẻ:** Gửi yêu cầu GET đến API
- **Lưu bộ thẻ:** Gửi yêu cầu POST với dữ liệu bộ thẻ
- **Tải dữ liệu từ local storage:** Sử dụng Room hoặc SharedPreferences

## 6. Trạng Thái Lỗi/Trống/Đang Tải
- **Chờ:** Hiển thị loader
- **Lỗi:** Hiển thị thông báo lỗi
- **Trống:** Thông báo không có dữ liệu

## 7. Cấu Trúc Gói Gợi Ý
- **com.example.flashcardapp**
    - **ui**
    - **viewmodel**
    - **repository**
    - **data**

## 8. Ghi Chú về Coroutine Scopes và Xử Lý Trạng Thái
- Sử dụng **ViewModelScope** cho các coroutine liên quan đến UI
- **LiveData/StateFlow** cho việc theo dõi thay đổi trạng thái  
