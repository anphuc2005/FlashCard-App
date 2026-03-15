# Tóm tắt Cấu trúc MVVM FlashCard App

## 📁 Danh sách tất cả các file đã được tạo

### Model Layer (4 files)
```
model/
├── FlashCard.kt           - Data class cho flashcard
├── Deck.kt                - Data class cho bộ thẻ
├── ChatMessage.kt         - Data class cho tin nhắn chat
└── ApiResponse.kt         - Wrapper cho API response
```

### Network Layer (3 files)
```
network/
├── DeckApiService.kt      - Retrofit interface cho Deck endpoints
├── ChatbotApiService.kt   - Retrofit interface cho Chatbot endpoints
└── RetrofitClient.kt      - Retrofit singleton client
```

### Data Layer (7 files)
```
data/
├── entity/
│   ├── DeckEntity.kt      - Room entity cho Deck
│   ├── FlashCardEntity.kt - Room entity cho FlashCard
│   └── ChatMessageEntity.kt - Room entity cho ChatMessage
├── dao/
│   ├── DeckDao.kt         - DAO operations cho Deck
│   ├── FlashCardDao.kt    - DAO operations cho FlashCard
│   └── ChatMessageDao.kt  - DAO operations cho ChatMessage
└── database/
    └── FlashCardDatabase.kt - Room database instance
```

### Repository Layer (3 files)
```
repository/
├── DeckRepository.kt      - Repository cho Deck (API + DB)
├── FlashCardRepository.kt - Repository cho FlashCard (API + DB)
└── ChatbotRepository.kt   - Repository cho Chatbot (API + DB)
```

### ViewModel Layer (4 files)
```
viewmodel/
├── DeckViewModel.kt       - ViewModel cho Deck
├── FlashCardViewModel.kt  - ViewModel cho FlashCard
├── ChatbotViewModel.kt    - ViewModel cho Chatbot
└── ViewModelFactory.kt    - Factory để tạo ViewModels
```

### UI Layer (8 files)
```
ui/
├── MainActivity.kt        - Activity chính
├── fragments/
│   ├── DeckListFragment.kt        - Danh sách bộ thẻ
│   ├── FlashCardStudyFragment.kt  - Học flashcard
│   ├── ChatbotFragment.kt         - Chatbot AI
│   ├── CreateDeckFragment.kt      - Tạo bộ thẻ mới
│   └── AddFlashCardFragment.kt    - Thêm flashcard
└── adapters/
    ├── DeckAdapter.kt             - Adapter cho danh sách deck
    ├── FlashCardAdapter.kt        - Adapter cho danh sách flashcard
    └── ChatMessageAdapter.kt      - Adapter cho danh sách chat message
```

### Utilities (3 files)
```
utils/
├── Constants.kt           - Constants
├── DateUtils.kt           - Utility functions cho date/time
└── NetworkErrorHandler.kt - Xử lý lỗi network
```

### Dependency Injection (1 file)
```
di/
└── DIContainer.kt         - DI container cho dependencies
```

---

## 🔄 Data Flow Architecture

```
UI Layer (Activity/Fragment)
    ↓ (gọi hàm, quan sát state)
ViewModel Layer
    ↓ (gọi repository)
Repository Layer
    ↓ (chọn nguồn dữ liệu)
    ├─→ Network Layer (Retrofit) → API Backend
    └─→ Data Layer (Room DB) → Local Database
```

---

## 📊 Tổng cộng

- **Total Files**: 34 files
- **Model Classes**: 4
- **API Services**: 2
- **Entities**: 3
- **DAOs**: 3
- **Repositories**: 3
- **ViewModels**: 3
- **Fragments**: 5
- **Adapters**: 3
- **Utilities**: 3
- **Support Files**: 2 (MainActivity, DIContainer)

---

## 🔑 Key Features

✅ **MVVM Architecture** - Tách biệt concerns
✅ **Repository Pattern** - Quản lý dữ liệu
✅ **Offline Support** - Room Database caching
✅ **Coroutines** - Async operations
✅ **Flow/StateFlow** - Reactive UI updates
✅ **DI Container** - Dependency injection
✅ **Error Handling** - Network error handling
✅ **Type-Safe** - Kotlin data classes

---

## 🚀 Cách sử dụng DIContainer

```kotlin
// Trong Activity hoặc Application
val viewModelFactory = DIContainer.getViewModelFactory(context)

// Trong Fragment
private val deckViewModel: DeckViewModel by viewModels { viewModelFactory }
```

---

## 📝 Lưu ý

1. **BASE_URL**: Cần cập nhật `BASE_URL` trong `RetrofitClient.kt` 
2. **API Endpoints**: Adjust các endpoints theo backend API của bạn
3. **Database Version**: Tăng version khi có schema changes
4. **Error Handling**: Có thể customize error messages trong `NetworkErrorHandler.kt`

---

## 📚 Danh sách API Endpoints cần implement

### DeckApiService
- `GET /decks` - Lấy tất cả bộ thẻ
- `GET /decks/{id}` - Lấy bộ thẻ theo ID
- `POST /decks` - Tạo bộ thẻ mới
- `PUT /decks/{id}` - Cập nhật bộ thẻ
- `DELETE /decks/{id}` - Xóa bộ thẻ
- `GET /decks/{deckId}/cards` - Lấy các flashcard của 1 bộ thẻ

### ChatbotApiService
- `POST /chatbot/ask` - Gửi câu hỏi và nhận trả lời từ AI

---

## 🎯 Next Steps

1. Thiết kế layout XML cho các Fragment
2. Implement ViewHolder binding trong Adapter classes
3. Tạo các API endpoints trên backend
4. Test API connections
5. Implement UI logic trong Fragment
6. Add image loading với Glide
7. Add animations cho transitions


