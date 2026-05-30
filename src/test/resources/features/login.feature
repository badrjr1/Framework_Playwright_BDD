@ui
Feature: Login functionality

  Background:
    * log in to the application

  Scenario Outline: User logs in successfully
    * click on btn_my_account
    * wait for element txt_email to become visible
    * write <email> in field txt_email
    * assert value of that element txt_email equals <email>
    * wait for element txt_password to become visible
    * write <password> in field txt_password
    * assert value of that element txt_password equals <password>
    * save screenshot with name login
    * click on btn_login
    * assert value of that element lnk_current_url contains <url_contains>
    Examples:
      | email                          | password | url_contains          |
      | pranav@testroverautomation.com | Test1234 | route=account/account |
      | fail@test.test                 | Test123  | route=account/account |


