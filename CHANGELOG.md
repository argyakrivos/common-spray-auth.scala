# Change Log

## 0.3.0 ([#4](https://git.mobcastdev.com/Platform/common-spray-auth/pull/4) 2014-05-22 18:13:04)

Json4s now uses Jackson rather than Native

#### New features

- Json4s now uses the Jackson library for improved speed instead of the Native one.

## 0.2.0 ([#2](https://git.mobcastdev.com/Platform/common-spray-auth/pull/2) 2014-05-16 17:29:38)

Added optionalAuthToken directive

### New features

- Added ```optionalAuthToken``` directive which extracts the auth token if the request has Authorization header and provides it to the inner route.

