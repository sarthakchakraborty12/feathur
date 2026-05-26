<p align = "center">
<img width="150" height="150" alt="feathur" src="https://github.com/user-attachments/assets/c00f2684-beac-47d1-86dd-ad3f08538bf2" />
</p>

# Feathur
A lightweight, fast, ad-free, and privacy-focused offline Office Files Viewer for Android. Open Word (`.docx`), Excel (`.xlsx`), PowerPoint (`.pptx`), and Plain Text (`.txt`) documents instantly without loading heavy suites.

<br>

[![GitHub release](https://img.shields.io/github/v/release/sarthakchakraborty12/feathur?color=green&logo=github)](https://github.com/sarthakchakraborty12/feathur/releases)
[![Min SDK](https://img.shields.io/badge/minSdk-24-blue?logo=android)](https://developer.android.com/about/dashboards)
[![Target SDK](https://img.shields.io/badge/targetSdk-36-teal?logo=android)](https://developer.android.com/about/versions/15)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![FOSS](https://img.shields.io/badge/FOSS-Free%20%26%20Open%20Source-orange)](https://en.wikipedia.org/wiki/Free_and_open-source_software)

<br>

<h4>Download</h4>
<p align="center">
  <a href="https://github.com/sarthakchakraborty12/feathur/releases"><img src="https://raw.githubusercontent.com/NeoApplications/Neo-Backup/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" width="200"></a> 

</p>

</div>

---

Feathur is designed to solve a simple problem: opening office documents on the go shouldn't require downloading bloated, heavy suites that consume hundreds of megabytes of RAM and storage. Written completely in modern Jetpack Compose, Feathur provides a premium, responsive, and secure experience directly on your device.

---

## Features ✨

### 📝 Microsoft Word (`.docx`, `.doc`)
- **Rich Formatting**: Renders paragraphs and text runs with bold, italic, underline, custom coloring, and text sizes.
- **Table Viewer**: Clean table grids with horizontal scroll bounds for dense data displays.
- **Copy & Select**: Enable text selection handles by long-pressing to copy word passages directly to the clipboard.
- **Fast Search Highlights**: Instantly highlight in-document text occurrences matching search queries.

### 📊 Microsoft Excel (`.xlsx`, `.xls`)
- **Multi-Sheet Workbooks**: Convenient filter-chip tab bar to switch between workbook sheets instantly.
- **Dynamic Grid Layout**: Responsive 2D cell grid showing rows and columns with fixed letter/number headers.
- **Cell Details Action**: Cell selection outline showing cell content details in a bottom sheet with a quick clipboard copy button.
- **Fit Cells to Content**: Toggle auto-scaling column widths to prevent text truncation in dense columns.

### slide Microsoft PowerPoint (`.pptx`, `.ppt`)
- **Pre-rendered Slide Graphics**: Parses slides dynamically to compile text layers, backgrounds, shapes, and images.
- **Smooth Decodes**: Asynchronous background-threaded image decoding (on `Dispatchers.IO`) prevents frame drops while scrolling slides.
- **Auto-Centering Placeholders**: Automatically handles slides using default layout layouts by centering non-positional texts in a dedicated vertical flow.
- **Interactive Slideshow**: Full-screen presentation mode with horizontal tap gesture navigation (left to go back, right to go forward) and progress controls.

### 📝 Plain Text (`.txt`)
- **Line-by-Line Rendering**: Efficiently handles raw text files by converting lines to lazy items.
- **Text Selection**: Long-press to select paragraphs or copy code blocks.
- **Search Jumping**: Up (`^`) and down (`v`) navigation buttons automatically jump-scroll directly to matching text search occurrences.

### 🎨 General & Material You Design
- **Google Sans Flex Branding**: Beautiful typography rendering on the main title bar using Google Sans Flex typeface properties (weight 605, grade 42, slant -5, width 119.7, roundness 37).
- **Themes**: Support for Wallpaper Adaptive Dynamic Color (Material You), Sleek dark mode, and Monochrome themed styling.
- **Launcher adaptive icon**: Monochrome M3 themed launcher icon and Vertical Gradient color icon.
- **URI Persistence**: Retains SAF persistable read permissions on picked documents to re-open files from the "Recent Files" history after device RAM clearing.
- **Storage Management Option**: Fail-safe permission request to let users optionally grant storage management access to resolve permission-denied file blocks.
- **Complete Privacy**: Zero networking features, zero trackers, and zero telemetry. It runs entirely offline.
- **Size**: Small download size (under 15 MB).
- **No worries of accidentally editing the documents** because all the documents here is read-only mode.
---

> [!WARNING]
> Feathur is currently an offline document viewer. It does not support editing or compiling modifications back to the original documents. Complex nested layouts (like custom vector art groupings, overlapping media layers, or mathematical formulas) may have minor rendering discrepancies compared to full office suites.

> [!IMPORTANT]
> **KNOWN BUGS :** sometimes the PPTX texts may not render properly or sometimes can be misaligned, we are trying to fix it as soon as possible. Especially complex animated and video added PPTX may fail to load or may render incorrectly.
---

## Screenshots

<p align="center">
  <img width="200"  alt="Screenshot_2026-05-26-22-18-20-31_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/e4c99abd-c19b-4e7c-832c-ccb8480076c7" />
  <img width="200"  alt="Screenshot_2026-05-26-22-19-26-65_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/b8314444-97b1-44be-9483-1403c856918d" />
  <img width="200" alt="Screenshot_2026-05-26-22-19-43-02_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/082b8c32-f162-46d7-81cc-8aae4c78f11c" />
  <img width="200" alt="Screenshot_2026-05-26-22-20-55-93_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/186ac2ae-2805-4a1f-9495-45a371534d84" />
  <img width="200" alt="Screenshot_2026-05-26-22-21-54-33_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/dc8ba607-1eb8-47bf-9662-57dc71367dc7" />
  <img width="200" alt="Screenshot_2026-05-26-22-22-01-53_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/71b9823b-fa47-4143-9b82-afe3e0707843" />
  <img width="200" alt="Screenshot_2026-05-26-22-24-45-56_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/cd81c3ad-f410-4c01-89e7-5bdab17d5437" />
  <img width="200" alt="Screenshot_2026-05-26-22-25-30-70_3300fd1dcb5deb0e9a274f23fda030b0" src="https://github.com/user-attachments/assets/b5cacb6b-e23e-46d0-b32f-43fcdbad17f5" />
</p>

---

## Developer Notes 🛠️

### Prerequisites
- Android Studio Ladybug (or newer) / VSCode (with JDK and other plugins required installed) / Antigravity IDE or Antigravity 2.0
- Gradle 8.0+
- JDK 17+
- Android SDK 36 API level configured

### Git Clone
```bash
git clone https://github.com/sarthakchakraborty12/feathur.git
cd feathur
```

### Running compilation
To build and verify the project compiles locally:
```bash
./gradlew compileDebugSources
```

### Running unit tests
To execute all local tests and screenshot capture runs:
```bash
./gradlew test
```

### Assembling the App
- **Compile Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Compile Release APK**:
  ```bash
  ./gradlew assembleRelease
  ```
  *Note: A post-build script configured in the Gradle files will automatically copy the finalized release bundle to the root `/releases` directory as `feathur-release.apk`.*

