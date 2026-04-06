<?php

namespace Phpthinky\ScreenCapture;

class ScreenCapture
{
    /**
     * Save a base64 encoded image directly to device storage.
     *
     * The image is compressed to JPEG before saving to avoid silent out-of-memory
     * failures when dealing with large screenshots.
     *
     * @param string $data Base64 encoded image data (can include data URL prefix)
     * @param array  $options {
     *     @type int         $quality   JPEG compression quality 1–100 (default 85)
     *     @type string|null $filename  Custom filename (auto-generated if omitted)
     *     @type string|null $path      Custom save path (defaults to Pictures/Screenshots)
     * }
     * @return array Returns ['success' => bool, 'path' => string, 'filename' => string, 'error' => string (optional)]
     *
     * @example
     *   ScreenCapture::saveBase64($data);
     *   ScreenCapture::saveBase64($data, ['quality' => 60]);
     *   ScreenCapture::saveBase64($data, ['quality' => 90, 'filename' => 'my-shot.jpg']);
     */
    public function saveBase64(string $data, array $options = []): array
    {
        if (!function_exists('nativephp_call')) {
            return ['success' => false, 'error' => 'NativePHP not available'];
        }

        $quality  = isset($options['quality'])  ? max(1, min(100, (int) $options['quality'])) : 85;
        $filename = $options['filename'] ?? null;
        $path     = $options['path']     ?? null;

        $params = [
            'data'    => $data,
            'quality' => $quality,
        ];

        if ($filename) {
            $params['filename'] = $filename;
        }

        if ($path) {
            $params['path'] = $path;
        }

        $result = nativephp_call('ScreenCapture.SaveBase64', json_encode($params));

        if (is_string($result)) {
            $decoded = json_decode($result, true);
            if (isset($decoded['success'])) {
                return $decoded;
            }
        }

        return ['success' => false, 'error' => 'Invalid response from native'];
    }
}