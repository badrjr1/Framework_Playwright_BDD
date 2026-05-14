@websocket @ignore
Feature: WebSocket communication

  Scenario Outline: Validate WebSocket message exchange
    * connect to websocket
    * send websocket message "<message>"
    * websocket message should contain "<expected_message>"

    Examples:
      | message         | expected_message |
      | hello websocket | hello websocket  |