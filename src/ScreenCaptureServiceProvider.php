<?php

namespace Native\Mobile\Providers;

use Illuminate\Support\ServiceProvider;
use Native\Mobile\ScreenCapture;

class ScreenCaptureServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->app->singleton(ScreenCapture::class, function () {
            return new ScreenCapture;
        });
    }
}