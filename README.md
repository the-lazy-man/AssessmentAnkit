# Product Catalog & Offline Cart — Android App

An Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM Architecture**, **Room Database**, and **Retrofit**. The application fetches products from the [DummyJSON Products API](https://dummyjson.com/docs/products), allows users to browse and search products, view full details, and provides a **locally persisted shopping cart that remains fully functional offline**.

---

## 🌟 Key Features

### 1. Product Catalog & Listing
- Display products retrieved dynamically from the REST API (`https://dummyjson.com/products`).
- Clean 2-column grid layout showing product image, title, category, rating stars, price, discount tag, and stock status.
- Category filter chips (All Products, Beauty, Fragrances, Furniture, Groceries, etc.).
- UI state handling for **Loading**, **Empty Results**, **API/Network Errors**, and **Retry** capability.

### 2. Product Search
- Search bar with 400ms debouncing to minimize redundant network calls.
- Supports query clearing and updates results dynamically.
- Offline search support querying local Room cache when network connection is unavailable.

### 3. Product Details Screen
- Full product details including high-resolution thumbnail, category, brand, star ratings, price breakdown, discount percentage, stock level, and description.
- Quantity selector (`+` / `-`) for choosing quantities before adding to cart.
- Add to cart button with instant visual feedback via animated snackbars.

### 4. Persisted Offline Shopping Cart
- Persisted locally using **Room Database** (`CartItemEntity`, `CartDao`).
- View cart items with thumbnail, title, unit price, item total price, quantity controls, and single-tap delete buttons.
- Real-time updates for total number of items and grand total price across all screens via Kotlin `Flow`.
- Dynamic checkout confirmation dialog and "Clear All" cart feature.
- **100% Offline Capability**: View cart, modify item quantities, remove items, and view total item count & price with no active internet connection.

### 5. Network State & Offline Banner
- `NetworkMonitor` utility observing real-time network connectivity.
- Top animated notification banner warning users when offline while emphasizing that cart features remain fully available.

---

## 🛠 Tech Stack & Architecture

| Layer | Technology / Library |
| :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) |
| **Architecture** | MVVM + Clean Architecture + Repository Pattern |
| **Local Persistence** | [Room Database](https://developer.android.com/training/data-storage/room) |
| **Networking** | [Retrofit 2](https://square.github.io/retrofit/) + [OkHttp 3](https://square.github.io/okhttp/) + [Gson](https://github.com/google/gson) |
| **Image Loading** | [Coil Compose](https://coil-kt.github.io/coil/compose/) |
| **Asynchronous Programming**| Kotlin [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) + [Flow](https://kotlinlang.org/docs/flow.html) |
| **Navigation** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) |
| **Testing** | JUnit 4 + KotlinX Coroutines Test |

---

## 📁 Project Structure

```
com.rahul.assessmentankit/
├── data/
│   ├── local/
│   │   ├── AppDatabase.java        # Room Database initialization
│   │   ├── CartDao.kt              # DAO for cart persistence
│   │   ├── ProductDao.kt           # DAO for product offline caching
│   │   └── ProductEntity.kt        # Room Entities (ProductEntity & CartItemEntity)
│   ├── remote/
│   │   ├── DummyJsonApi.kt         # Retrofit API interface
│   │   ├── NetworkModels.kt        # DTO models & mappers
│   │   └── NetworkResult.kt        # Sealed class for network states
│   └── repository/
│       ├── CartRepository.kt       # Cart data operations & reactive flows
│       └── ProductRepository.kt    # Product syncing & offline fallback repository
├── domain/
│   └── model/
│       └── Product.kt              # Domain models (Product, CartItem, Category)
├── ui/
│   ├── components/                 # Reusable UI Components
│   │   ├── CartBadgeIconButton.kt  # Animated cart badge
│   │   ├── ConnectionStateBanner.kt# Offline banner
│   │   ├── ProductCard.kt          # Grid item card
│   │   ├── RatingBar.kt            # Star rating component
│   │   ├── SearchBarView.kt        # Search text field
│   │   ├── CategoryChips.kt        # Category filter row
│   │   └── StateViews.kt           # Loading, Empty & Error views
│   ├── screens/
│   │   ├── catalog/                # Product Catalog Screen & ViewModel
│   │   ├── detail/                 # Product Detail Screen & ViewModel
│   │   └── cart/                   # Shopping Cart Screen & ViewModel
│   ├── navigation/
│   │   └── NavGraph.kt             # Navigation Compose router
│   └── theme/                      # Color, Type, and Theme definitions
├── util/
│   ├── AppContainer.kt             # Manual Dependency Container (DI)
│   └── NetworkMonitor.kt           # Connectivity observer
├── AssessmentApplication.kt        # Application class
└── MainActivity.kt                 # Single Activity entry point
```

---

## 🌐 API Endpoints Used

Base URL: `https://dummyjson.com/`

- `GET /products` — Fetch list of products
- `GET /products/search?q={query}` — Search products by query
- `GET /products/{id}` — Fetch product details by ID
- `GET /products/categories` — Fetch product categories
- `GET /products/category/{category}` — Filter products by category

---

## 🚀 How to Build & Run

### Prerequisites
- **Android Studio**: Ladybug / Baklava (2024.2.1+) or newer.
- **JDK**: Java 17 or Java 21 (bundled JetBrains Runtime `jbr`).
- **Android SDK**: `compileSdk = 36`, `targetSdk = 35`, `minSdk = 24`.

### Building via Command Line

1. **Clone the repository**:
   ```bash
   git clone <repository_url>
   cd AssessmentAnkit
   ```

2. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

3. **Assemble Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on connected Device / Emulator**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Unit Tests

Unit tests are included under `app/src/test/java/com/rahul/assessmentankit/`:
- **`CartItemTest.kt`**: Validates `CartItem` total price calculations, formatted strings, and product discount computations.
- **`ProductDtoTest.kt`**: Tests network DTO to domain model transformations and handling of optional fields.

Run tests using:
```bash
./gradlew test
```

---

## 📄 License

This project is created for assessment purposes. All product data is provided by [DummyJSON](https://dummyjson.com/).
