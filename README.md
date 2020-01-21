# AccountLockout-IncrementerChecker

An Account Lockout incrementer and checker nodes for ForgeRock's [Identity Platform][forgerock_platform] 6.5 and above. 
These nodes persists failed authentication attempts in JSON format, provides failure messages(warning and account lockout failures) as shared state and checks if account is locked or not. 

## NODE DETAILS
* **AccountLockoutIncrementerNode**: A node which persists failed authentication attempts in user's profile in JSON format `{"invalidCount":2,"lastInvalidAt":1579382470795}`. Also provides failure duration window for failed authentications. 
* **AccountLockoutCheckerDecisionNode**:  This node returns unlocked or locked based on invalid attempts. Also updates shared message state with appropriate failure message such as warning and account lockout failures messages. 

Copy the .jar file from the ../target directory into the ../web-container/webapps/openam/WEB-INF/lib directory where AM is deployed.  Restart the web container to pick up the new node.  The nodes will then appear in the authentication trees components palette.

## USAGE

The code in this repository has binary dependencies that live in the ForgeRock maven repository. Maven can be configured to authenticate to this repository by following the following [ForgeRock Knowledge Base Article](https://backstage.forgerock.com/knowledge/kb/article/a74096897).

**Account Lockout tree**

![ScreenShot](./AccountLockoutTree.png)


**User Profile updates**

![ScreenShot](./UserProfileAccountLockout.png)


**TESTING**
1. First authentication.
```
   curl --location --request POST 'http://am651.example.com:8086/am/json/realms/root/realms/employees/authenticate?authIndexType=service&authIndexValue=AccountLockout' \
    --header 'Content-Type: application/json' \
    --header 'Accept-API-Version: resource=2.0, protocol=1.0' \
    --data-raw '{
        "authId": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9....",
         "callbacks": [
            {
                "type": "NameCallback",
                "output": [
                    {
                        "name": "prompt",
                        "value": "User Name"
                    }
                ],
                "input": [
                    {
                        "name": "IDToken1",
                        "value": "user.99"
                    }
                ],
                "_id": 0
            },
            {
                "type": "PasswordCallback",
                "output": [
                    {
                        "name": "prompt",
                        "value": "Password"
                    }
                ],
                "input": [
                    {
                        "name": "IDToken2",
                        "value": "invalidPassword123131"
                    }
                ],
                "_id": 1
            }
        ]
    }'

    {"code":401,"reason":"Unauthorized","message":"Login failure"}
```

2. Authentication failure when warning counter is reached 
```
   curl --location --request POST 'http://am651.example.com:8086/am/json/realms/root/realms/employees/authenticate?authIndexType=service&authIndexValue=AccountLockout' \
    --header 'Content-Type: application/json' \
    --header 'Accept-API-Version: resource=2.0, protocol=1.0' \
    --data-raw '{
        "authId": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9....",
         "callbacks": [
            ...
        ]
    }'

    {
        "code": 401,
        "reason": "Unauthorized",
        "message": "Login failure",
        "detail": {
            "failureUrl": "Authentication Failed Warning: You will be locked out after 2 more failure(s)."
        }
    }
```

3. Authentication failure when account is locked
```
   curl --location --request POST 'http://am651.example.com:8086/am/json/realms/root/realms/employees/authenticate?authIndexType=service&authIndexValue=AccountLockout' \
    --header 'Content-Type: application/json' \
    --header 'Accept-API-Version: resource=2.0, protocol=1.0' \
    --data-raw '{
        "authId": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9....",
         "callbacks": [
            ...
        ]
    }'

    {
        "code": 401,
        "reason": "Unauthorized",
        "message": "Login failure",
        "detail": {
            "failureUrl": "Your Account has been locked."
        }
    }
```

## DISCLAIMER

Any sample code, scripts,connectors, or other materials (collectively, “Sample Code”) provided by ForgeRock in connection with ForgeRock’s performance 
of the Deployment Support Services may be used by Customer solely for purposes of Customer exercising its license to the ForgeRock Software under this 
Addendum and subject to all restrictions herein (“Purpose”). Unless otherwise specified by ForgeRock, any Sample Code provided by ForgeRock to Customer 
in source form as part of the Deployment Support Services may be further modified by Customer as required for the Purpose. Any Sample Code provided by 
ForgeRock under open source license terms will remain subject to the open source license terms under which it is provided. Customer shall not use or 
combine any open source software with ForgeRock Software in any manner which would subject any ForgeRock Software to any open source license terms. 
For the avoidance of doubt, any Sample Code provided hereunder is expressly excluded from ForgeRock’s indemnity or support obligations.
