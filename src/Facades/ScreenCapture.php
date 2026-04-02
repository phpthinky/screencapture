<?php

namespace Phpthinky\ScreenCapture\Facades;

use Illuminate\Support\Facades\Facade;

/**
 * @method static array saveBase64(string $data, ?string $filename = null, ?string $path = null)
 */
class ScreenCapture extends Facade
{
    protected static function getFacadeAccessor()
    {
        return \Phpthinky\ScreenCapture\ScreenCapture::class;
    }
}