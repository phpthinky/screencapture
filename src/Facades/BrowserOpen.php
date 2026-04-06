<?php

namespace Phpthinky\BrowserOpen\Facades;

use Illuminate\Support\Facades\Facade;

/**
 * @method static array open(string $url)
 */
class BrowserOpen extends Facade
{
    protected static function getFacadeAccessor()
    {
        return \Phpthinky\BrowserOpen\BrowserOpen::class;
    }
}
