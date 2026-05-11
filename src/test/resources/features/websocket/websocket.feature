@websocket @ignore
Feature: WebSocket communication

  Scenario Outline: Validate WebSocket message exchange
    * connect to websocket "<ws_url>"
    * send websocket message "<message>"
    * websocket message should contain "<expected_message>"

    Examples:
      | ws_url                      | message         | expected_message |
      | wss://echo.websocket.events | hello websocket | hello websocket  |