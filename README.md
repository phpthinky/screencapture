# ScreenCapture Plugin for NativePHP Mobile

Save screenshots and captured elements directly to device storage for NativePHP Mobile applications.

## Overview

The ScreenCapture API provides cross-platform screenshot and element capture functionality, saving images directly to your device's gallery or custom storage locations.

## Installation

```bash
composer require phpthinky/screencapture
Usage
PHP (Livewire/Blade)
php
use Native\Mobile\ScreenCapture\Facades\ScreenCapture;

// Save a base64 image
$result = ScreenCapture::saveBase64(
    data: $base64ImageData,
    filename: 'screenshot.png'
);

if ($result['success']) {
    echo 'Screenshot saved to: ' . $result['path'];
}

// Save with custom path
$result = ScreenCapture::saveBase64(
    data: $base64ImageData,
    filename: 'receipt.png',
    path: 'Documents/Receipts'
);

if ($result['success']) {
    echo 'Receipt saved successfully!';
}
JavaScript (Vue/React/Inertia)
js
import { ScreenCapture } from '#nativephp';

// Save a captured element
const result = await ScreenCapture.saveBase64({
    data: dataUrl,
    filename: 'screenshot.png'
});

if (result.success) {
    console.log('Saved to:', result.path);
}

// Save with custom path
const result = await ScreenCapture.saveBase64({
    data: dataUrl,
    filename: 'card.png',
    path: 'Documents/Cards'
});
React Component Example
jsx
import React, { useRef } from 'react';
import domtoimage from 'dom-to-image';

const CaptureComponent = () => {
    const elementRef = useRef(null);

    const handleCapture = async () => {
        const dataUrl = await domtoimage.toPng(elementRef.current);
        
        const result = await window.NativePHP.call('ScreenCapture.SaveBase64', {
            data: dataUrl,
            filename: `capture_${Date.now()}.png`
        });
        
        if (result.success) {
            alert('Saved to gallery!');
        }
    };

    return (
        <div>
            <div ref={elementRef}>
                <h2>Capture This Content</h2>
            </div>
            <button onClick={handleCapture}>Capture & Save</button>
        </div>
    );
};
Methods
saveBase64(string $data, ?string $filename = null, ?string $path = null): array
Saves a base64 encoded image directly to device storage.

Parameter	Type	Description
data	string	Base64 encoded image data (can include data URL prefix)
filename	string (optional)	Custom filename (auto-generated if not provided)
path	string (optional)	Custom save path (defaults to Pictures/Screenshots)
Returns:

success: bool - Whether the operation succeeded

path: string - The path where the file was saved

filename: string - The actual filename used

error: string - Error message if operation failed (optional)

Behavior
Automatically generates filename with timestamp if not provided

Creates parent directories automatically if they don't exist

Saves to device gallery/photo library by default

Preserves image quality with PNG format

Works with data URLs (e.g., data:image/png;base64,...)

Save Locations
Android
Default: Pictures/Screenshots/ (visible in Gallery)

Custom path: Any valid path in app storage

iOS
Default: Photo Library (visible in Photos app)

Custom path: App Documents directory

Examples
Capture Card Element to Gallery
php
use Native\Mobile\ScreenCapture\Facades\ScreenCapture;

public function saveCard($base64Image)
{
    $result = ScreenCapture::saveBase64(
        data: $base64Image,
        filename: 'business_card.png'
    );

    if ($result['success']) {
        return response()->json([
            'message' => 'Card saved to gallery',
            'path' => $result['path']
        ]);
    }
    
    return response()->json([
        'error' => $result['error']
    ], 500);
}
Save Receipt with Custom Path
php
use Native\Mobile\ScreenCapture\Facades\ScreenCapture;

public function saveReceipt($base64Image)
{
    $result = ScreenCapture::saveBase64(
        data: $base64Image,
        filename: 'receipt_' . date('Ymd_His') . '.png',
        path: 'Documents/Receipts'
    );

    if ($result['success']) {
        return 'Receipt saved to: ' . $result['path'];
    }
}
Batch Save Multiple Images
js
const saveMultipleImages = async (images) => {
    const results = [];
    
    for (const image of images) {
        const result = await ScreenCapture.saveBase64({
            data: image.dataUrl,
            filename: `${image.name}_${Date.now()}.png`
        });
        
        results.push(result);
    }
    
    console.log(`Saved ${results.filter(r => r.success).length} images`);
};
Permissions
Android
Add to your AndroidManifest.xml:

xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
iOS
Add to Info.plist:

xml
<key>NSPhotoLibraryAddUsageDescription</key>
<string>Save screenshots to your photo library</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>Save screenshots to your photo library</string>
Testing
php
// Test with a sample image
$testImage = base64_encode(file_get_contents('test.png'));
$result = ScreenCapture::saveBase64(
    data: $testImage,
    filename: 'test.png'
);

var_dump($result);
Troubleshooting
Image not appearing in gallery
Ensure permissions are granted in device settings

On Android, check if saved to correct directory

On iOS, verify photo library access is allowed

Invalid base64 data error
Ensure the data includes the proper prefix

Verify the image data is not corrupted

Check that base64 string is properly encoded

Permission denied
Request runtime permissions on Android 13+

Check app settings on the device

Verify permissions are declared in config

Debugging
Enable verbose logging in your NativePHP configuration:

php
// config/nativephp.php
'debug' => true,
Check device logs:

Android: adb logcat | grep ScreenCapture

iOS: Xcode console or Console.app

License
MIT

Support
Documentation: https://nativephp.com

Issues: GitHub Issues

text

This README follows the exact same style as your file plugin with:
- Same header structure
- Installation first
- PHP and JavaScript examples
- Methods table
- Behavior description
- Practical examples
- Permissions section
- Troubleshooting

Save this as `README.md` in your plugin root!