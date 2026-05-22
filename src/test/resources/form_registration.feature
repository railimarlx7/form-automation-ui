@automation
Feature: Form Registration
  As a user, I want to register entries in the form so they are stored and retrievable.

  @validation
  Scenario Outline: Submitting incomplete data shows a validation message
    Given I submit the registration form with username "<username>", password "<password>" and name "<name>"
    Then I should see the message "<message>"

    Examples:
      | username | password | name | message                   |
      | jrlima   | 123      |      | This message will not match. |

  @registration
  Scenario Outline: Submitting complete data stores the entry in the table
    Given I submit the registration form with username "<username>", password "<password>" and name "<name>"
    Then the table should display username "<username>", password "<password>" and name "<name>"

    Examples:
      | username | password | name     |
      | jrlima   | 123      | Junior   |
      | jrlima2  | 1234     | Raimundo |

  @delete
  Scenario Outline: Deleting an entry removes it from the table
    Given I submit the registration form with username "<username>", password "<password>" and name "<name>"
    When I delete the entry
    Then the table should no longer display username "<username>", password "<password>" and name "<name>"

    Examples:
      | username | password | name     |
      | jrlima2  | 1234     | Raimundo |

  @refresh
  Scenario Outline: Registered entries remain visible after a page refresh
    Given I submit the registration form with username "<username>", password "<password>" and name "<name>"
    When I refresh the page
    Then the table should display username "<username>", password "<password>" and name "<name>"

    Examples:
      | username | password | name   |
      | jrlima   | 123      | Junior |
