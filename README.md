# BrowserOpen Plugin for NativePHP Mobile

Open URLs in the device's default browser from your NativePHP Mobile application.

## Installation

```bash
composer require phpthinky/browseropen
```

## Usage

### React (Recommended)

Use `bridgecall` from `#nativephp` to call the native bridge directly:

```jsx
import bridgecall from '#nativephp';

const openLink = async (url) => {
    await bridgecall('BrowserOpen.Open', { url });
};
```

#### React Component Example

```jsx
import React from 'react';
import bridgecall from '#nativephp';

const ExternalLinkButton = ({ href, children }) => {
    const handlePress = async () => {
        const result = await bridgecall('BrowserOpen.Open', { url: href });

        if (!result?.success) {
            console.error('Failed to open browser:', result?.error);
        }
    };

    return (
        <button onClick={handlePress}>
            {children}
        </button>
    );
};

export default ExternalLinkButton;
```

#### Open Link on Mount / useEffect

```jsx
import { useEffect } from 'react';
import bridgecall from '#nativephp';

const SupportPage = () => {
    const openDocs = async () => {
        await bridgecall('BrowserOpen.Open', { url: 'https://nativephp.com' });
    };

    return (
        <div>
            <h1>Support</h1>
            <button onClick={openDocs}>Open Documentation</button>
        </div>
    );
};
```

#### Prevent Default WebView Navigation

If you have anchor tags in your WebView and want them to open in the browser instead of navigating inside the app:

```jsx
import bridgecall from '#nativephp';

const Content = () => {
    const handleLinkClick = async (e) => {
        const href = e.currentTarget.getAttribute('href');
        if (href?.startsWith('http')) {
            e.preventDefault();
            await bridgecall('BrowserOpen.Open', { url: href });
        }
    };

    return (
        <a href="https://example.com" onClick={handleLinkClick}>
            Visit Website
        </a>
    );
};
```

---

### PHP (Livewire / Blade)

```php
use Phpthinky\BrowserOpen\Facades\BrowserOpen;

$result = BrowserOpen::open('https://example.com');

if ($result['success']) {
    // Browser was launched
} else {
    logger()->error('BrowserOpen failed: ' . $result['error']);
}
```

---

## API Reference

### `BrowserOpen.Open`

Opens a URL in the device's default browser.

| Parameter | Type   | Required | Description                          |
|-----------|--------|----------|--------------------------------------|
| `url`     | string | Yes      | The URL to open (must be http/https) |

**Returns:**

| Key       | Type    | Description                              |
|-----------|---------|------------------------------------------|
| `success` | boolean | Whether the browser launched             |
| `error`   | string  | Error message if `success` is `false`    |

---

## Permissions

Permissions are automatically registered via `nativephp.json`. No manual changes needed.

### Android

The plugin declares the following in the app manifest:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<!-- Required for Android 11+ to resolve browser intents -->
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent>
</queries>
```

### iOS

No special permissions required. `UIApplication.shared.open()` handles it natively.

---

## Troubleshooting

**Browser does not open on Android 11+**
Ensure the `<queries>` block is present in your `AndroidManifest.xml`. NativePHP should inject this automatically via `nativephp.json`, but verify after building.

**`bridgecall` returns `success: false`**
- Check the `error` field in the response for details.
- Ensure the URL includes the scheme (`https://`, not just `example.com`).
- Only `http` and `https` URLs are supported.

**URL opens inside the WebView instead of the browser**
Use `e.preventDefault()` on the link's click handler and call `bridgecall('BrowserOpen.Open', ...)` manually (see example above).

**Debugging**

```bash
# Android
adb logcat | grep BrowserOpen

# iOS
# Check Xcode console or Console.app
```

---

## License

MIT
