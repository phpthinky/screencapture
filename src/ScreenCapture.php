<?php

namespace Native\Mobile;

class ScreenCapture
{
    /**
     * Save a base64 encoded image directly to device storage
     *
     * @param string $data Base64 encoded image data (can include data URL prefix)
     * @param string|null $filename Custom filename (optional, auto-generated if not provided)
     * @param string|null $path Custom path (optional, defaults to Pictures/Screenshots)
     * @return array Returns ['success' => bool, 'path' => string, 'error' => string (optional)]
     */
    public function saveBase64(string $data, ?string $filename = null, ?string $path = null): array
    {
        if (!function_exists('nativephp_call')) {
            return ['success' => false, 'error' => 'NativePHP not available'];
        }

        $params = ['data' => $data];
        
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