# Backend Requirements: Auth Refresh + Offline Sync

## Goal

The Android app now supports offline-first behavior. When the access token expires, the app should not force logout if local cached data exists. Backend support is needed so the app can refresh tokens and resume online sync without requiring the user to log in again.

## Required Endpoint

### Refresh Access Token

`POST /auth/refresh`

Request body:

```json
{
  "refreshToken": "string"
}
```

Success response:

```json
{
  "status": "success",
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "string",
    "refreshToken": "string"
  }
}
```

Notes:

- `accessToken` is required.
- `refreshToken` is recommended. If backend uses refresh-token rotation, return the new refresh token every time.
- If backend does not rotate refresh tokens, returning the same refresh token is acceptable.

## Status Code Rules

- `200`: refresh succeeded.
- `401`: refresh token is expired, revoked, malformed, or invalid.
- `403`: refresh token is valid but blocked by account state or permissions.
- Do not return `500` for normal token expiration.

Access-protected APIs should follow:

- `401`: access token missing, expired, malformed, or invalid.
- `403`: authenticated but not authorized.

## Logout/Revoke Endpoint

Recommended:

`POST /auth/logout`

Request body:

```json
{
  "refreshToken": "string"
}
```

Expected behavior:

- Revoke the refresh token.
- Future `/auth/refresh` calls with that token return `401`.

## Token Lifetime

Recommended defaults:

- Access token: short-lived, for example 15 minutes to 2 hours.
- Refresh token: longer-lived, for example 7 to 30 days.

Backend should revoke refresh tokens when:

- User logs out.
- User changes password.
- Account is disabled.
- Refresh token reuse is detected, if rotation is implemented.

## Android Client Behavior Expected

When an API returns `401`:

1. Android calls `POST /auth/refresh`.
2. If refresh succeeds, Android saves the new token pair and retries the original request.
3. If refresh fails with `401/403`, Android marks auth as expired but keeps local cache and pending offline changes.
4. User can continue studying cached decks offline.
5. User must log in again before pending changes can sync.

## Offline Sync Requirements

The app can store study reviews locally while offline or auth-expired. Backend endpoints that receive synced study data should be idempotent.

Recommended for study review sync:

- Accept a stable client-generated review id.
- If the same review id is submitted more than once, do not create duplicates.
- Return which records were accepted and the server sync timestamp.

Example:

`POST /study/reviews/sync`

Request:

```json
{
  "reviews": [
    {
      "id": "client-generated-id",
      "cardId": "card-id",
      "deckId": "deck-id",
      "studyMode": "SEQUENTIAL",
      "grade": 3,
      "durationSeconds": 12,
      "studiedAt": "2026-05-19T10:15:30Z"
    }
  ]
}
```

Response:

```json
{
  "status": "success",
  "message": "Reviews synced successfully",
  "data": {
    "syncedCount": 1,
    "syncedAt": "2026-05-19T10:16:00Z"
  }
}
```

## Open Questions For Backend

- What is the final refresh endpoint path: `/auth/refresh`, `/auth/refresh-token`, or another path?
- Does backend rotate refresh tokens?
- Does logout revoke refresh tokens?
- Are study review ids accepted from the client and treated as idempotency keys?
- Should `403` ever be used for expired tokens, or only for permission/account-state errors?
