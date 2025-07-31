# RentalInn - Android Mobile Application

Link Backend https://github.com/SekcyAditya22/expressbackendm

## 🚀 Features

### User Features
- **User Authentication**: Login and registration system
- **Vehicle Browsing**: Browse available vehicles with search and filter options
- **Rental Booking**: Book vehicles with location picker and payment integration
- **Chat System**: Real-time chat with admin support
- **Transaction History**: View rental history and transaction details
- **User Profile**: Manage personal information and settings
- **Payment Processing**: Integrated payment system for rentals

### Admin Features
- **Dashboard**: Overview of rental statistics and system metrics
- **User Management**: Add, edit, and delete user accounts
- **Vehicle Management**: Manage vehicle inventory and units
- **Rental Management**: Monitor and manage rental bookings
- **Verification System**: Verify user details and documents
- **Chat Support**: Provide customer support through chat
- **Payment Management**: Handle payment processing and refunds

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit with OkHttp
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **State Management**: ViewModel and StateFlow
- **Database**: MySQL (Express JS Backend)
- **Build System**: Gradle with Kotlin DSL


## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 21 or higher
- Kotlin 1.8.0 or higher

### Installation

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/rentalinn-android.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the application

### Configuration

1. Update the API base URL in `network/RetrofitClient.kt`
2. Configure your backend server endpoints
3. Set up your payment gateway credentials (if applicable)

## 📁 Project Structure

```
app/src/main/java/com/example/rentalinn/
├── api/                    # API service interfaces
├── di/                     # Dependency injection modules
├── model/                  # Data models and entities
├── navigation/             # Navigation components
├── network/                # Network configuration
├── repository/             # Data repositories
├── screens/                # UI screens
│   ├── admin/             # Admin-specific screens
│   ├── user/              # User-specific screens
│   ├── login/             # Authentication screens
│   └── onboarding/        # Onboarding screens
├── ui/                     # UI components and theme
├── utils/                  # Utility classes
└── viewmodel/              # ViewModels
```

## 🔧 Build Configuration

The project uses Gradle with Kotlin DSL. Key configuration files:

- `build.gradle.kts` - Project-level build configuration
- `app/build.gradle.kts` - App-level build configuration
- `gradle/libs.versions.toml` - Dependency version management

## 📦 Dependencies

Key dependencies include:

- **Jetpack Compose**: Modern UI toolkit
- **Retrofit**: HTTP client for API calls
- **Hilt**: Dependency injection
- **Navigation Compose**: Navigation framework
- **ViewModel**: Architecture component for UI state management
- **Coroutines**: Asynchronous programming

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Developer**: Kelompok Rentalin
- **Project**: RentalInn Android Application

## 📞 Support

For support and questions, please contact:
- Email: muhammadadityamadjid@gmail.com
- Project Issues: [GitHub Issues](https://github.com/yourusername/rentalinn-android/issues)

## 🔄 Version History

- **v1.0.0** - Initial release with core rental functionality
- **v1.1.0** - Added chat system and payment integration
- **v1.2.0** - Enhanced admin dashboard and user management

---

**Note**: This is a mobile application for the RentalInn car rental service. Make sure to configure the backend API endpoints and payment gateway settings before deployment. 
