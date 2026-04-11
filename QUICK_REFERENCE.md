# Quick Reference Card - CivicFix

**Print this page or save to your phone for quick lookup!**

---

## 🚀 Quick Start (30 seconds)

```bash
git clone https://github.com/storm309/CivicFix.git
cd SmartWasteManagementApp
./gradlew :app:installDebug
adb shell am start -n com.example.smartwastemanagementapp/.MainActivity
```

---

## 📁 Important Directories

```
app/src/main/java/com/example/smartwastemanagementapp/
├── MainActivity.kt                    # Entry point (NO editing during testing)
├── viewmodel/
│   ├── AuthViewModel.kt               # Auth + validation
│   └── WasteViewModel.kt              # Reports + submission
├── ui/screens/                        # Edit screens here ✅
│   ├── LoginScreen.kt
│   ├── SignupScreen.kt
│   ├── HomeScreen.kt
│   ├── ReportWasteScreen.kt
│   ├── ViewReportsScreen.kt
│   └── MapScreen.kt
└── ui/theme/
    ├── Color.kt                       # Colors
    └── Type.kt                        # Typography
```

---

## 🔧 Essential Commands

```bash
# Build & Run
./gradlew :app:installDebug              # Build + install on device
./gradlew :app:build                     # Build (no install)

# Testing
./gradlew :app:testDebugUnitTest         # Unit tests
./gradlew :app:connectedAndroidTest      # Device tests

# Debugging
adb logcat -s "CivicFix"                 # View logs
adb logcat | grep -i error               # View errors only

# Release
./gradlew :app:bundleRelease             # Bundle for Play Store
./gradlew clean :app:build               # Full rebuild

# Clean
./gradlew clean                          # Remove build artifacts
rm -rf .gradle/                          # Remove cache (if sync fails)
```

---

## 💡 Code Snippets

### Add a New Screen

**Step 1**: `navigation/Screen.kt`
```kotlin
object MyScreen : Screen("my_screen")
```

**Step 2**: `ui/screens/MyScreen.kt`
```kotlin
@Composable
fun MyScreen(onBack: () -> Unit, viewModel: AuthViewModel) {
    Column {
        Text("Hello")
    }
}
```

**Step 3**: `MainActivity.kt`
```kotlin
composable(Screen.MyScreen.route) {
    MyScreen(onBack = { navController.popBackStack() }, viewModel = authViewModel)
}
```

**Step 4**: Navigate from another screen
```kotlin
Button(onClick = { navController.navigate(Screen.MyScreen.route) })
```

---

### Add Validation

**In ViewModel**:
```kotlin
private fun isValidField(value: String): Boolean = value.length in 5..50

fun myFunction(field: String) {
    if (!isValidField(field)) {
        _error.value = "Field must be 5-50 characters"
        return
    }
    // Proceed...
}
```

---

### Add String Resource

**In `strings.xml`**:
```xml
<string name="my_key">My String Value</string>
```

**In Compose**:
```kotlin
Text(stringResource(R.string.my_key))
```

---

### Add Form Validation (Real Example)

```kotlin
val isFormValid = remember(name, email, age) {
    name.isNotBlank() &&
        email.contains("@") &&
        age.toIntOrNull()?.let { it in 13..120 } == true
}

Button(
    onClick = { submitForm() },
    enabled = isFormValid
)
```

---

## 🎨 Design Guidelines

### Colors (Green Civic Palette)
```kotlin
Primary: #2B7A4B (Forest Green)
Secondary: #4E6355 (Sage)
Tertiary: #3F6E57 (Muted Green)
```

### Typography
```
Headline Large (32sp): Page titles
Title Large (22sp): Section headers
Title Medium (16sp): Card titles
Body Large (16sp): Regular text
Body Small (12sp): Captions
Label Large (14sp): Button text
```

### Spacing
```
16dp: Content padding
12dp: Between form fields
56dp: Button height (min touch target)
24dp: Card corner radius
```

---

## 🚨 Common Errors & Fixes

| Error | Fix |
|-------|-----|
| `Gradle sync failed` | `./gradlew clean && ./gradlew sync` |
| `google-services.json not found` | Place file in `app/` directory |
| `App crashes on startup` | Check `adb logcat \| grep -i crash` |
| `Form never enables button` | Check `remember { isFormValid }` logic |
| `Error message won't disappear` | Add `.value = null` on new operations |
| `Emulator won't start` | `emulator -list-avds` then `emulator -avd YourAVD` |

---

## 📊 ViewModel State Pattern

```kotlin
// State declarations
private val _loading = mutableStateOf(false)
val isLoading: State<Boolean> = _loading

private val _error = mutableStateOf<String?>(null)
val error: State<String?> = _error

// Clear error + set loading before operation
fun performAction() {
    _isLoading.value = true
    _error.value = null  // ← Important!
    // ...async operation...
    _isLoading.value = false
}

// In UI: Use conditional display
if (isLoading.value) {
    CircularProgressIndicator()
} else if (!error.value.isNullOrBlank()) {
    Text(error.value, color = MaterialTheme.colorScheme.error)
} else {
    // Success state
}
```

---

## 📱 Responsive Layout Pattern

```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp)  // ← Caps width on tablet
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)  // ← Flexible spacing
) {
    // Content
}
```

---

## 🔐 Input Validation Pattern

```kotlin
// Email
email.contains("@") && email.contains(".")

// Age (13-120)
age.toIntOrNull()?.let { it in 13..120 } == true

// Phone (7+ digits)
phone.filter { it.isDigit() }.length >= 7

// Password (6+ chars)
password.length >= 6

// Non-blank
field.isNotBlank() && field.trim().isNotEmpty()
```

---

## 🎯 Screen Navigation Map

```
┌─────────┐
│ Splash  │ (2s delay, checks auth)
└────┬────┘
     │
     ├──────────────────────────┐
     ↓ (logged out)             ↓ (logged in)
   ┌──────┐                  ┌──────┐
   │Login │←───────────┐     │Home  │
   └───┬──┘ Signup    │     └─┬────┘
       │ ←─────────────┤       │
       │            ┌──┴──┐    │
       │            │Signup│   │
       │            └──────┘   │
       │                       │
       └─── (pop inclusive) ───┘
           (prevents back to auth)

From Home:
├── ReportWaste (pop back)
├── ViewReports (pop back)
├── Map (pop back)
└── Logout (pop inclusive → Login)
```

---

## 🧪 Testing Patterns

```bash
# Unit test a ViewModel
./gradlew :app:testDebugUnitTest --tests *AuthViewModelTest

# Run single test method
./gradlew :app:testDebugUnitTest --tests *AuthViewModelTest#testLogin

# Test on device with logs
./gradlew :app:connectedAndroidTest -Pandroid.serial=emulator-5554
adb logcat -s "CivicFix" | grep "Test"
```

---

## 📚 Quick Reference Links

- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Material 3**: https://m3.material.io/
- **Firebase**: https://firebase.google.com/docs/android
- **MVVM Guide**: https://developer.android.com/jetpack/guide
- **Kotlin Docs**: https://kotlinlang.org/docs/

---

## 📞 Documentation Files

| File | Purpose | Read When |
|------|---------|-----------|
| **README.md** | Project overview + setup | First time setup |
| **AGENTS.md** | Architecture + patterns | Before coding |
| **DEVELOPMENT.md** | Workflow + commands | During development |
| **PRODUCTION_READINESS.md** | Pre-release checklist | Before publishing |
| **UPGRADE_SUMMARY.md** | What changed in v1.0 | Understand improvements |

---

**Last Updated**: April 11, 2026  
**Recommended for**: Print & keep at desk!

