<?php

namespace Phpthinky\ScreenCapture;

use Illuminate\Support\ServiceProvider;

class ScreenCaptureServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->app->singleton(ScreenCapture::class, function () {
            return new ScreenCapture;
        });
    }
}