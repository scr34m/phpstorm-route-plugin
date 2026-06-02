# Phpstorm plugin route
Easy navigate by route name to controllers, searching for `routes.php` to build routes.

## Support
- Runtime 27.0.7.
- Phpstorm 2025.*

## Build
- ``./gradlew buildPlugin``
- ``ls -la build/distributions/``
- You should see something like route-navigator-1.0.0.zip

## Install
- Open phpstorm
- Settings / Plugins / Install from Disk and select zip file.
- or use release build [route-navigator-1.0.0.zip](route-navigator-1.0.0.zip)

## Route example

```php
Route::group(['as' => 'user'], function () {
    Route::get(
        'belepes',
        [Site\Http\Controllers\Users\LoginController::class, 'index'],
        ['hu' => 'belepes', 'en' => 'login']
    );
    Route::get(
        'kilepes',
        [Site\Http\Controllers\Users\LoginController::class, 'logout'],
        ['hu' => 'kilepes', 'en' => 'logout']
    );
});
```

Code is based on https://github.com/metasyncSite/phpstorm-laravel-route-plugin