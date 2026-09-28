import urllib.request
import urllib.parse
import json

def api_call(endpoint, method, token=None, data=None):
    url = "http://localhost:8080/api" + endpoint
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(url, method=method, headers=headers)
    if data:
        req.data = json.dumps(data).encode('utf-8')
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print(f"Error {e.code}: {e.read().decode('utf-8')}")
        return None

# 1. Login Alice
alice_token = api_call("/auth/login", "POST", data={"email":"emp1@company.com", "password":"password123"})["token"]
print("Alice Token:", alice_token[:10])

# 2. Alice applies for leave
alice_leave = api_call("/leaves", "POST", token=alice_token, data={
    "leaveType": "ANNUAL",
    "startDate": "2026-11-15",
    "endDate": "2026-11-15",
    "reason": "Test"
})
print("Alice Leave:", alice_leave)

# 3. Login Bob
bob_token = api_call("/auth/login", "POST", data={"email":"emp2@company.com", "password":"password123"})["token"]

# 4. Bob applies for leave
bob_leave = api_call("/leaves", "POST", token=bob_token, data={
    "leaveType": "ANNUAL",
    "startDate": "2026-11-15",
    "endDate": "2026-11-15",
    "reason": "Test"
})
print("Bob Leave:", bob_leave)

# 5. Check Bob's eligible swaps
swaps = api_call(f"/swaps/eligible/{bob_leave['id']}", "GET", token=bob_token)
print("Eligible Swaps:", swaps)
