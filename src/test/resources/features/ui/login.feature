Feature: Login functionality

  Background:
    * navigate to "https://ecommerce-playground.lambdatest.io/"

  Scenario Outline: User logs in successfully
    * click on btn_my_account
    * write "<email>" in field txt_email
    * write "<password>" in field txt_password
    * click on btn_login
    * assert that url contains "<url_contains>" is "<status>"
    Examples:
      | email                          | password | url_contains          | status |
      | pranav@testroverautomation.com | Test1234 | route=account/account | true   |
      | test@test.test                 | Test123  | route=account/account | false  |
