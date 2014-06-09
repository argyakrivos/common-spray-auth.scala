# Change Log

## 0.3.3 ([#8](https://git.mobcastdev.com/Platform/common-spray-auth/pull/8) 2014-06-09 09:56:24)

Updated dependencies

- Patch change to the latest versions of dependencies

## 0.3.2 ([#7](https://git.mobcastdev.com/Platform/common-spray-auth/pull/7) 2014-06-08 14:55:53)

Tiny patch change to force build

Tiny patch change to force build

## 0.3.1 ([#6](https://git.mobcastdev.com/Platform/common-spray-auth/pull/6) 2014-06-05 13:11:11)

Tiny change to force rebuild, should be published to Artifactory

Patch to force version update and rebuild.

## 0.3.0 ([#4](https://git.mobcastdev.com/Platform/common-spray-auth/pull/4) 2014-05-22 18:13:04)

Json4s now uses Jackson rather than Native

#### New features

- Json4s now uses the Jackson library for improved speed instead of the Native one.

## 0.2.0 ([#2](https://git.mobcastdev.com/Platform/common-spray-auth/pull/2) 2014-05-16 17:29:38)

Added optionalAuthToken directive

### New features

- Added ```optionalAuthToken``` directive which extracts the auth token if the request has Authorization header and provides it to the inner route.

