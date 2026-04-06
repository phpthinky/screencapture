<?php

namespace Phpthinky\BrowserOpen;

use Illuminate\Support\ServiceProvider;

class BrowserOpenServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->app->singleton(BrowserOpen::class, function () {
            return new BrowserOpen;
        });
    }
}
