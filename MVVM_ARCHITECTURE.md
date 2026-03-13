# Cấu trúc MVVM của ứng dụng FlashCard

## Tổng quan

Ứng dụng sử dụng kiến trúc MVVM (Model-View-ViewModel) để tách biệt logic business từ UI.

## Cấu trúc thư mục

```
com/example/flashcarapp/
├── data/                      # Lớp Data (Local Database)
│   ├── database/
│   │   └── FlashCardDatabase.kt    # Room Database
│   ├── entity/                     # Entity cho Room
│   │   ├── DeckEntity.kt
│   │   ├── FlashCardEntity.kt
│   │   └── ChatMessageEntity.kt
│   └── dao/                        # Data Access Objects
│       ├── DeckDao.kt
│       ├── FlashCardDao.kt
│       └── ChatMessageDao.kt
│
├── model/                     # Model (Data classes)
│   ├── FlashCard.kt
│   ├── Deck.kt
│   ├── ChatMessage.kt
│   └── ApiResponse.kt
│
├── network/                   # API Network
│   ├── DeckApiService.kt      # Retrofit interface cho Deck API
│   ├── ChatbotApiService.kt   # Retrofit interface cho Chatbot API
│   └── RetrofitClient.kt      # Retrofit singleton
│
├── repository/                # Repository (quản lý dữ liệu)
│   ├── DeckRepository.kt      # Repository cho Deck (API + Local DB)
│   ├── FlashCardRepository.kt # Repository cho FlashCard (API + Local DB)
│   └── ChatbotRepository.kt   # Repository cho Chatbot (API + Local DB)
│
├── viewmodel/                 # ViewModel
│   ├── DeckViewModel.kt       # ViewModel cho Deck
│   ├── FlashCardViewModel.kt  # ViewModel cho FlashCard
│   ├── ChatbotViewModel.kt    # ViewModel cho Chatbot
│   └── ViewModelFactory.kt    # Factory để tạo ViewModel
│
├── ui/
│   ├── MainActivity.kt         # Activity chính
│   ├── fragments/              # UI Fragments
│   │   ├── DeckListFragment.kt
│   │   ├── FlashCardStudyFragment.kt
│   │   ├── ChatbotFragment.kt
│   │   ├── CreateDeckFragment.kt
│   │   └── AddFlashCardFragment.kt
│   └── adapters/               # RecyclerView Adapters
│       ├── DeckAdapter.kt
│       ├── FlashCardAdapter.kt
│       └── ChatMessageAdapter.kt
│
└── utils/                     # Utilities
    ├── Constants.kt
    └── DateUtils.kt
```

## Quy trình dữ liệu (Data Flow)

```
UI (Fragment/Activity)
    ↓
ViewModel (xử lý logic, quản lý state)
    ↓
Repository (chọn dữ liệu từ Network hoặc Local DB)
    ↓
    ├── Network (API via Retrofit)
    └── Local DB (Room Database)
```

## Chi tiết từng phần

### 1. Model (model/)
- Chứa các data class đại diện cho entities
- `FlashCard`: Biểu diễn một thẻ flashcard
- `Deck`: Biểu diễn một bộ thẻ
- `ChatMessage`: Biểu diễn một tin nhắn chat
- `ApiResponse`: Wrapper cho API response

### 2. Network (network/)
- `DeckApiService`: Retrofit interface định nghĩa các API endpoints cho Deck
- `ChatbotApiService`: Retrofit interface cho Chatbot API
- `RetrofitClient`: Singleton để khởi tạo Retrofit client

### 3. Data Layer (data/)
- **Entity**: Đại diễn dữ liệu trong Room Database
- **DAO**: Interface để truy cập dữ liệu từ database
- **Database**: Room Database instance

### 4. Repository (repository/)
- Là layer trung gian giữa ViewModel và Data Layer
- Quản lý việc lấy dữ liệu từ API hoặc Local Database
- Đồng bộ hóa dữ liệu giữa API và Local DB
- Chuẩn hóa dữ liệu trả về

**Ví dụ:**
```kotlin
suspend fun getAllDecksFromApi(): Result<List<Deck>> {
    // Call API
    // Lưu vào Local DB
    // Return Result
}
```

### 5. ViewModel (viewmodel/)
- Chứa business logic và quản lý state UI
- Sử dụng Coroutine để xử lý async operations
- Cung cấp StateFlow để UI observe thay đổi dữ liệu
- Sống sót khi Activity/Fragment bị recreate

**Ví dụ:**
```kotlin
class DeckViewModel(private val deckRepository: DeckRepository) : ViewModel() {
    private val _deckUiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val deckUiState: StateFlow<DeckUiState> = _deckUiState.asStateFlow()
    
    fun getAllDecks() {
        viewModelScope.launch {
            // Gọi repository
            // Update state
        }
    }
}
```

### 6. UI Layer (ui/)
- **MainActivity**: Activity chính
- **Fragments**: Các màn hình khác nhau
- **Adapters**: RecyclerView adapters

**Quy trình trong Fragment:**
1. Khai báo ViewModel
2. Observe StateFlow từ ViewModel
3. Update UI khi dữ liệu thay đổi
4. Gọi hàm trong ViewModel khi user interact

### 7. Utils (utils/)
- `Constants`: Chứa các constant
- `DateUtils`: Các utility function cho date/time

## Cách sử dụng

### Trong Fragment:
```kotlin
class DeckListFragment : Fragment() {
    private val deckViewModel: DeckViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe data
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                deckViewModel.deckUiState.collect { state ->
                    when (state) {
                        is DeckUiState.Loading -> showLoading()
                        is DeckUiState.Success -> showDecks(state.decks)
                        is DeckUiState.Error -> showError(state.message)
                        DeckUiState.Empty -> showEmpty()
                    }
                }
            }
        }
        
        // Call ViewModel function
        deckViewModel.getAllDecks()
    }
}
```

## Cách thêm feature mới

1. **Tạo Model** (model/)
2. **Tạo API Service** (network/)
3. **Tạo Entity và DAO** (data/)
4. **Tạo Repository** (repository/)
5. **Tạo ViewModel** (viewmodel/)
6. **Tạo Fragment/UI** (ui/)
7. **Tạo Adapter nếu cần** (ui/adapters/)

## Best Practices

✅ Luôn sử dụng ViewModel để quản lý state
✅ Luôn sử dụng Repository để truy cập dữ liệu
✅ Sử dụng Flow/StateFlow để observe dữ liệu
✅ Sử dụng Coroutine để xử lý async operations
✅ Luôn handle errors trong Repository
✅ Luôn cache dữ liệu trong Local DB khi có API call thành công
✅ Chuẩn hóa dữ liệu giữa API response và Local Database

❌ Không call API trực tiếp từ Activity/Fragment
❌ Không lưu trữ dữ liệu trong Activity/Fragment
❌ Không sử dụng global variables cho dữ liệu

