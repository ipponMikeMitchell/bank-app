Feature: BankApp testing

  Scenario: Create an account
    When A Person "Ben" "Scott" creates an account
    Then "Scott" has 0.0 balance

  Scenario: Deposit into account
    And A Person "Ben" "Scott" creates an account
    When "Scott" deposits 100.40
    Then "Scott" has 100.40 balance

  Scenario: Deposit into two accounts
    Given A Person "Sam" "LastName" creates an account
    And A Person "Ben" "Scott" creates an account
    When "LastName" deposits 100.40
    Then "LastName" has 100.40 balance
    When "Scott" deposits 30.1
    Then "Scott" has 30.10 balance

  Scenario: Deposits a negative amount
    Given A Person "Ben" "Scott" creates an account
    When "Scott" deposits -200.45
    Then "Scott" has 0.0 balance

  Scenario Outline: Depositing into an account
    Given A Person <first> <last> creates an account
    And <last> has a balance of <initialBalance>
    When <last> deposits <amount>
    Then <last> has <balance> balance

    Examples:
      | first   | last    | initialBalance | amount | balance |
      | "Ben"   | "Scott" | 0.0            | 40.0   | 40.0    |
      | "Scott" | "Scott" | 0.0            | 85.0   | 85.0    |
      | "b"     | "s"     | 40.0           | 40.0   | 80.0    |
      | "Hello" | "World" | 40.0           | -40.0  | 40.0    |



  Scenario Outline: Withdrawing from an account
    Given A Person <first> <last> creates an account
    And <last> has a balance of <initialBalance>
    When <last> withdraws <amount>
    Then <last> has <balance> balance

    Examples:
      | first   | last    | initialBalance | amount | balance |
      | "Ben"   | "Scott" | 100.0          | 40.0   | 60.0    |
      | "Scott" | "Scott" | 10.30          | 10.30  | 0.0     |
      | "b"     | "s"     | 40.0           | 40.0   | 0.0     |
      | "Hello" | "World" | 40.0           | -40.0  | 40.0    |



  Scenario Outline: Transfering from one account to another
    Given A Person <first> <source> creates an account
    Given A Person <first> <destination> creates an account
    And <source> has a balance of <initialBalance>
    And <destination> has a balance of <initialBalance>
    When <source> transfers <destination> <amount>
    Then <destination> has <destinationBalance> balance
    Then <source> has <sourceBalance> balance

    Examples:
      | first   | source  | destination | initialBalance | amount | sourceBalance | destinationBalance |
      | "Ben"   | "Scott" | "World"     | 100.0          | 40.0   | 60.0          | 140.0              |
      | "Scott" | "Scott" | "World"     | 10.30          | 10.30  | 0.0           | 20.60              |
      | "b"     | "s"     | "World"     | 40.0           | 40.0   | 0.0           | 80.0               |
      | "Hello" | "World" | "Hello"     | 40.0           | -40.0  | 40.0          | 40.0               |
