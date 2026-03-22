# Tailwind Eye
A WebStorm extension that provides a visual fading or folding effect for code. It can toggle between fading non-styling code (to focus on Tailwind classes) and folding the content of `className` strings.

## Motivation
To reduce visual noise and focus on what matters, whether it's the structure of your HTML/JSX or the Tailwind styling itself.

## How to Build and Install

### 1. Build the Plugin
This project uses Gradle. To compile the plugin and create an installable distribution, run:
```bash
gradle buildPlugin
```
The resulting ZIP file will be located in:
`build/distributions/tailwind-eye-1.0-SNAPSHOT.zip`

### 2. Install in your IDE
To install the plugin in your personal IDE (not the sandbox):
1. Open your IDE (WebStorm, IntelliJ IDEA, etc.).
2. Go to **Settings** (or **Preferences** on macOS) > **Plugins**.
3. Click the **cog icon** (⚙️) and select **Install Plugin from Disk...**.
4. Navigate to the `build/distributions/` folder and select the ZIP file.
5. Restart the IDE if prompted.

## Usage
- Use the shortcut `Shift + Alt + F` to toggle between the two modes:
    - **Fade non-styling code**: Fades everything except `className` attributes.
    - **Fold className content**: Folds the actual Tailwind utility strings.

## Development
To run a development instance of the IDE with the plugin pre-installed:
```bash
gradle runIde
```
