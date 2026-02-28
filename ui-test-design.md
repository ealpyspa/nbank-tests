## Iteration - 1

### Login Page 

#### 1 - Positive Scenario: admin can login with correct data 
1) Enter correct username and password.
2) Click on "Login" button.

Expectations:
3) Admin Panel is opened.
4) Token is generated in local storage.

### 2 - Positive Scenario: user can login with correct data
1) Enter correct username and password.
2) Click on "Login" button.

Expectations:
3) User Dashboard is opened.
4) Token is generated in local storage.

#### 3 - Negative Scenario: user cannot login with incorrect data

My hint: if error is not parsed from API response directly, then all error message should be checked on UI.
1) Enter incorrect username/password.
2) Click on "Login" button. 

Expectations:
3) Error message is displayed.
4) No token in the local storage.
5) Same page is still opened. 

### Admin Panel Page 

#### 4 - Positive Scenario: Admin can create user 

Preconditions: 
1) Admin login

Steps: 
2) Enter correct username and password
3) Click on "Add User" button

Expectations:
4) User is displayed in the "All Users" list
5) User is created on API level
6) Alert about user successful creation is displayed 

#### 5 - Negative case: Admin cannot create user with invalid data

Preconditions:
1) Admin login

Steps:
2) Enter incorrect username/password
3) Click on "Add User" button

Expectations:
4) User is not displayed in the "All Users" list
5) User is not created on API level
6) Alert about user is not created is displayed + explanation why 

### User Dashboard 

#### 6 - User can create account

Preconditions:
1) Admin login
2) Admin creates user
3) User login
4) "User Dashboard" is opened

Steps:
5) User click on "Create New Account"

Expectations: 
6) Alert that account is created
7) Account is created on API level 

## Iteration - 2 

### Deposit Money

#### 7 - Positive Scenario: User can make a deposit to their account with valid sum
Preconditions: 
1) Admin login (via API)
2) Admin creates user (via API)
3) User creates account (via API)
4) User login (via token) 

Steps:
5) User make deposit to their account with valid sum
   - User selects their account, select/enter valid amount of money and click on "Deposit" button.

Expectations:
6) Alert about successful deposit is displayed 
7) Check that account balance is changed (via API)

#### 8 - Negative Scenario: User cannot make a deposit to their account with invalid sum
Preconditions:
1) Admin login (via API)
2) Admin creates user (via API)
3) User creates account (via API)
4) User login (via token)

Steps:
5) User makes deposit to their account with invalid sum
    - User selects their account, select/enter invalid amount of money and click on "Deposit" button.

Expectations:
6) Alert is displayed that deposit is not done
7) Check that account balance is not changed (via API)

### Transfer Money 

#### 9 - Positive Scenario: User can make a transfer to another user's account
Preconditions:
1) Admin login
2) Admin creates user1
3) User1 login
4) User1 creates account 
5) Admin creates user2 
6) User2 login 
7) User2 creates account
8) User1 make a deposit to their account

Steps:
5) User1 make transfer to user2's account
   - User2 selects their account, enters user2 name, enters user2 account, enter valid amount of money, check "Confirm details are correct" checkbox and click on "Send Transfer" button. 

Expectations:
6) Alert about successful transfer is displayed
7) Check that user1's account balance is decreased (via API)
8) Check that user2's account balance is increased (via API)

### 10 - Negative Scenario: User cannot transfer money to not existing account
Preconditions:
1) Admin login
2) Admin creates user1
3) User1 login
4) User1 creates account
5) User1 makes a deposit

Steps:
5) User1 make transfer to random user and random account
   - User1 selects their account, enters random name, enters random account, enter valid amount of money, check "Confirm details are correct" checkbox and click on "Send Transfer" button.

Expectations:
6) Alert about not successful transfer is displayed 
7) Check that user1's account balance is not changed (via API)

### Change Username 

### 11 - Positive Scenario: User can change their name to a valid one
Preconditions:
1) Admin login
2) Admin creates user
3) User login

Steps:
5) User clicks on username -> "Edit Profile" is opened
6) User enters valid name and click on "Save Changes" button

Expectations:
7) Alert about successful name change is displayed 
8) Refresh page and see the new name is diaplyed 
9) Check that user's name is changed (via API)

### 12 - Negative Scenario: User cannot change their name to invalid one
Preconditions:
1) Admin login
2) Admin creates user
3) User login

Steps:
5) User clicks on username -> "Edit Profile" is opened
6) User enters invalid name and click on "Save Changes" button

Expectations:
7) Alert about not successful name change is displayed
8) Check that user's name is not changed (via API)