Feature: Plugin integrated in default Android Library

@skip_agp3_4_0
Scenario: Single-module default library builds successfully
    When I build the library module fixture
    Then I should receive no requests
