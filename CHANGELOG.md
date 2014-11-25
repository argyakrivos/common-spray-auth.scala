# Change Log

## 0.7.5 ([#21](https://git.mobcastdev.com/Platform/common-spray-auth/pull/21) 2014-11-25 13:10:48)

Updated dependencies

### Improvements

- Updated dependencies to latest version
- Now supports Scala 2.11 only

## 0.7.4 ([#20](https://git.mobcastdev.com/Platform/common-spray-auth/pull/20) 2014-11-11 16:29:15)

Elevated sessions are now extended

### Bug Fixes

* When checking elevation, sessions are now extended by using `POST`
rather than simply checked using `GET`. This means that expiration is
sliding (as it should be) rather than absolute.

## 0.7.3 ([#19](https://git.mobcastdev.com/Platform/common-spray-auth/pull/19) 2014-11-07 10:12:54)

Ensure headers render correctly

### Bug Fixes

- The `WWW-Authenticate` header now renders correctly, with commas
between the parameters.

## 0.7.2 ([#18](https://git.mobcastdev.com/Platform/common-spray-auth/pull/18) 2014-10-27 13:40:50)

Ensure messages are the same as 'old' ones

The client teams have taken a dependency on the text of the `error_description` field of the `WWW-Authenticate` rather than using the status codes in the `error` and `error_reason` fields. This means we need to change the behaviour of this library to send the text they are expecting :-/

### “Improvements”

- Uses the same error messages as the services currently in production, including different error messages for expired rather than simply invalid tokens.

### Bug Fixes

- The `authToken` and `optionalAuthToken` directives now return `CredentialsMissing` rather than `CredentialsRejected` when credentials are supplied in a scheme other than `Bearer`.

## 0.7.1 ([#17](https://git.mobcastdev.com/Platform/common-spray-auth/pull/17) 2014-10-06 14:05:15)

Introduce an exception to be used when a token has an invalid status

### Improvements

With reference to CP-1926 this patch includes a new exception type to be used by the elevation checker when it wants to signal that a given token has a status that is different from `Valid` on SSO.

## 0.7.0 ([#15](https://git.mobcastdev.com/Platform/common-spray-auth/pull/15) 2014-09-11 10:45:35)

Introduce role-checking directive

### New features

* Introduce a `RoleConstraint` class and a `authenticateAndAuthorize` directive to provide role-checking capabilities

## 0.6.0 ([#14](https://git.mobcastdev.com/Platform/common-spray-auth/pull/14) 2014-09-08 16:03:21)

Make tokens available on the User object

This version has some breaking changes which were necessary to make it
possible to obtain access tokens from a user object, which is necessary
to make checking of elevation in SSO efficient.

### Breaking changes

- The `User` object constructor now has an additional parameter.
- The `TokenElevationChecker` type has been removed, and a new
`ElevationChecker` type added in its place which takes a `User` rather
than a `String`.
- `ZuulTokenAuthenticator` has been renamed to
`BearerTokenAuthenticator` as there’s nothing really zuul-specific
about it.

### New features

- `BearerTokenAuthenticator` implements the new trait `ElevatedContextAuthenticator[T]` which derives from `ContextAuthenticator[T]` making it easier to mock tests that need the `withElevation` method.
- You can now call `accessToken` or `ssoAccessToken` on the `User`
object, making it easier to access either of these things from code.

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

