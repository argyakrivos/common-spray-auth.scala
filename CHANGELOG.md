# Change Log

## 0.5.2 ([#13](https://git.mobcastdev.com/Platform/common-spray-auth/pull/13) 2014-09-08 12:57:54)

Update dependencies

Patch to update dependency versions

## 0.5.1 ([#12](https://git.mobcastdev.com/Platform/common-spray-auth/pull/12) 2014-09-04 12:52:34)

Cross compiles to Scala 2.11

### Improvements

* Now cross-compiles to Scala 2.11

## 0.5.0 ([#11](https://git.mobcastdev.com/Platform/common-spray-auth/pull/11) 2014-07-14 10:18:42)

Added claims & roles to the User object

### Breaking changes

- The `User` constructor now takes an additional `claims` parameter
containing the additional claims about the user.

### New features

- You can now access all the claims about a user.
- The roles claim is parsed into a set of `UserRole` values on demand.

### Bug fixes

- `ZuulTokenAuthenticator` doesn’t check elevation when the desired
elevation is `Unelevated`.

## 0.4.0 ([#10](https://git.mobcastdev.com/Platform/common-spray-auth/pull/10) 2014-06-27 11:16:27)

AuthToken directive

### New Feature

- Added `authToken` directive that extracts the auth token from the request

## 0.3.4 ([#9](https://git.mobcastdev.com/Platform/common-spray-auth/pull/9) 2014-06-12 10:38:20)

Excluded unnecessary dependencies

The blinkbox-security-jwt dependency drags in some slf4j dependencies
even though it doesn’t actually use them. This patch excludes them from
being imported into this project as they can conflict with other slf4j
implementations such as logback.

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

