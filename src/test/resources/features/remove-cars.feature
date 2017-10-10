Feature: Remove cars

  Background:
    Given Database is initialized

  Scenario: remove car successfully
    Given user is logged in as "admin"
    And search car with model "Ferrari"
    When "Ferrari" is removed
    Then there is no more cars with model "Ferrari"

  Scenario: should fail to remove car without permission
    Given user is logged in as "guest"
    And search car with model "Ferrari"
    When "Ferrari" is removed
    Then error message must be "Access denied"
