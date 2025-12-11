# Security Implementation

## 1. Generating API Keys
Run this Python snippet to generate secure keys for your drones:
```python
import secrets
print(secrets.token_hex(32))
# Example Output: 8f92b...
```

## 2. Backend Validation (FastAPI)
Add this dependency to your FastAPI routes:

```python
from fastapi import Header, HTTPException

API_KEYS = {
    "scout": "scout_secret",
    "delivery": "delivery_secret"
}

async def verify_key(x_api_key: str = Header(...), vehicle_id: str = Path(...)):
    expected = API_KEYS.get(vehicle_id)
    if x_api_key != expected:
        raise HTTPException(status_code=403, detail="Invalid API Key")
```

usage:
```python
@app.post("/api/upload/{vehicle_id}")
async def upload(..., authorized: bool = Depends(verify_key)):
    pass
```

## 3. Operator Authentication (JWT)
For future: Implement `python-jose` to issue JWTs on login.
1. `POST /login` -> returns `access_token`.
2. Frontend sends `Authorization: Bearer <token>`.
3. Backend validates token on sensitive endpoints (e.g. Mission Upload).
