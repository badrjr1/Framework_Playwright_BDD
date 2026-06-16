Feature: Login functionality

  Background:
    * log in to the application

  @ui
  Scenario Outline: User logs in successfully
    * search locators in the home_page file
    * click on btn_my_account
    * search locators in the login_page file
    * wait for element txt_email to become visible
    * write <email> in field txt_email
    * save value of txt_email for later in variable Email_value
    * assert value of that element txt_email contains ${Email_value}
    * wait for element txt_password to become visible
    * write <password> in field txt_password
    * assert value of that element txt_password equals <password>
    * save screenshot with name screen_login
    * click on btn_login
    * assert value of that link lnk_current_url contains <url_contains>

    Examples:
      | email                                        | password | url_contains          |
      | pranav@testroverautomation.com               | Test1234 | route=account/account |
      | ${string:5,alnum}@test-${string:4,alpha}.com | Test123  | route=account/account |


