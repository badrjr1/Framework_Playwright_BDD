@websocket @ignore
Feature: Test WebSocket connection

  Scenario Outline: Client sends message and validates websocket response
    * connect to websocket <endpoint>
    * send websocket message <message>
    * wait for websocket message within <timeout_seconds> seconds
    * assert websocket message contains <expected_response>
    * close websocket connection

    Examples:
      | endpoint | message | timeout_seconds | expected_response |
      | /ws      | ping    | 5               | pong              |
      | /ws      | hello   | 5               | echo: hello       |