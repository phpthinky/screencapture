<?php

namespace Phpthinky\BrowserOpen;

class BrowserOpen
{
    /**
     * Open a URL in the device's default browser
     *
     * @param string $url The URL to open (must include scheme, e.g. https://)
     * @return array Returns ['success' => bool, 'error' => string (optional)]
     */
    public function open(string $url): array
    {
        if (! function_exists('nativephp_call')) {
            return ['success' => false, 'error' => 'NativePHP not available'];
        }

        $params = ['url' => $url];

        $result = nativephp_call('BrowserOpen.Open', json_encode($params));

        if (is_string($result)) {
            $decoded = json_decode($result, true);
            if (isset($decoded['success'])) {
                return $decoded;
            }
        }

        return ['success' => false, 'error' => 'Invalid response from native'];
    }
}
