Feature: Plugin integrated in default Android Library

Scenario: Single-module default library builds successfully
    And I run the script "features/scripts/build_library.sh" synchronously
    Then I should receive no requests
    And the exit code equals 0
