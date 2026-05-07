# FlashCard-App

## 1. Người đóng góp
- [Phan An Phúc](https://github.com/anphuc2005)
- [Nguyễn Danh Hoà](https://github.com/danhhoa20005)
- [Phạm Mai Linh](https://github.com/tolalinhne)

## 2. Giới thiệu
- FlashCard-App (Flashly) là ứng dụng Android hỗ trợ người học tạo, quản lý và ôn tập flashcard một cách hiệu quả.
- Các tính năng nổi bật bao gồm:

    - **Trải nghiệm học tập hiện đại và trực quan**, có màn hình onboarding và điều hướng thân thiện cho người dùng mới.

    - **Luồng xác thực đầy đủ**, bao gồm đăng nhập email/mật khẩu, Google Sign-In, quên mật khẩu, xác thực OTP và đặt lại mật khẩu.

    - **Quản lý bộ thẻ và thẻ linh hoạt**, cho phép người dùng tạo, chỉnh sửa, tìm kiếm và sắp xếp các bộ flashcard.

    - **Nhiều chế độ học tập**, như học theo thứ tự, chế độ ngẫu nhiên, đua thời gian và lặp lại ngắt quãng (SM-2).

    - **Theo dõi tiến độ chi tiết**, với thống kê học tập, hỗ trợ streak/thành tích và tổng kết phiên học.

    - **Tiện ích mở rộng**, gồm khám phá nội dung (Discover), quản lý hồ sơ cá nhân và hỗ trợ AI chat/tài liệu.

## 3. Mô hình làm việc
- Quá trình phát triển theo mô hình lặp (kiểu sprint), trong đó tính năng được triển khai theo từng phần nhỏ và review liên tục.
- Mỗi tuần có phiên tổng kết để đánh giá hạng mục đã hoàn thành, xử lý blocker và lên kế hoạch cho vòng phát triển tiếp theo.

## Chiến lược quản lý phiên bản
- Team sử dụng quy trình Git theo nhánh, kết hợp Pull Request để review code trước khi merge.

- Chiến lược nhánh hiện tại trong repository:
    - `main`: Chứa mã nguồn ổn định, sẵn sàng phát hành.
    - `develop`: Nhánh tích hợp cho quá trình phát triển liên tục.
    - `PR-*`: Các nhánh feature/fix cho từng đầu việc trước khi tạo Pull Request.

- Quy trình làm việc điển hình:
    - Tạo nhánh từ `develop`.
    - Triển khai và kiểm thử tính năng.
    - Tạo Pull Request để review.
    - Merge vào `develop`, sau đó phát hành lên `main`.

## 4. Công nghệ
- Ngôn ngữ: Kotlin
- Nền tảng phát triển: Android (minSdk 33, targetSdk 36)
- Kiến trúc: MVVM với tách lớp `data` - `domain` - `presentation`
- Thư viện và công cụ chính:
    - AndroidX, Material Components, ViewBinding
    - Navigation Component
    - Room Database + KSP
    - Retrofit2, OkHttp, Gson
    - Kotlin Coroutines
    - Lifecycle (ViewModel, Runtime)
    - Google Play Services Auth
    - Glide
    - Flexmark (trình phân tích Markdown)

## 5. Demo ứng dụng
- Link tải APK: Đang cập nhật
- Các màn hình/tính năng chính:
    - Onboarding, Đăng nhập, Đăng ký, Quên mật khẩu, Xác thực OTP
    - Trang chủ, Quản lý bộ thẻ, Phiên học, Kết quả học tập
    - Khám phá, Thống kê, Tài khoản, Chỉnh sửa hồ sơ
    - AI Chat và Trình xem tài liệu

## 6. Tài liệu tham khảo
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developers Documentation](https://developer.android.com/docs)
- [Android Navigation Documentation](https://developer.android.com/guide/navigation)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Coroutines Documentation](https://kotlinlang.org/docs/coroutines-overview.html)
