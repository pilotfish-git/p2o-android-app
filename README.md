# p2o-android-app

This is a very basic Android app that connects to the Pilotfish P2O server, and can be used as a boilerplate to
create your own.

The Pilotfish P2O server uses a web socket connection to be able to notify clients realtime on 'button presses'.
To setup the web socket connection, this project uses the [socket.io-client-java](https://github.com/socketio/socket.io-client-java) library.

The Pilotfish websocket endpoint runs at `http://pilotfish-demo-portal.eu:3001`. You can connect to this endpoint with:

```java
try {
    socket = IO.socket("http://pilotfish-demo-portal.eu:3001");
} catch (URISyntaxException e) {
    throw new RuntimeException(e);
}
```

The Pilotfish websocket API is pretty simple. When having connected you will be able to listen
for the following responses:

```java
socket.on("connection-response", onConnectionResponse);
socket.on("register-response", onRegisterResponse);
socket.on("button-press", onButtonPress);
```

After you connect to the server, the server will respond with `connection response`.
The data from the response contains a `status` property, that can be `success` or `failed`. if the `data.status`
property is `success`, you need to emit a `register` message, with the button id as payload.

```java
private Emitter.Listener onConnectionResponse = new Emitter.Listener() {
    @Override
    public void call(final Object... args) {
        JSONObject data = (JSONObject) args[0];
        String status;
        try {
            status = data.getString("status");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (status.equals("success")) {
            socket.emit("register", buttonId);
        }
    }
};
```

After emitting the `register` message, the server will respond with `register-response` with the following in its payload:
*   `status` (string): will be either `success` or `failed`
*   `button` (object, optional): if a button object is send, it means there is already a button registered on the server
with this id. The button object will contain the following properties:
    *   `id` (string)
    *   `count` (int) The amount of clicks that happened when client was offline.
    *   `history` (array) An array of max 20 ISO dates that represent last clicks of this button.

```java
private Emitter.Listener onRegisterResponse = new Emitter.Listener() {
    @Override
    public void call(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];

                try {
                    JSONObject button;
                    if (data.has("button")) {
                        button = data.getJSONObject("button");
                        counter = button.getInt("count");
                        message = "times pressed";
                    } else {
                        counter = 0;
                        message = "waiting for button '" + buttonId + "' to register";
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                counterView.setText(counter.toString());
                messageView.setText(message);
            }
        });
    }
};
```

When a button is pressed when connected, the server will emit `button-press` with the `id` of the button as payload.

```java
private Emitter.Listener onButtonPress = new Emitter.Listener() {
    @Override
    public void call(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                counter = counter + 1;
                message = "times pressed";
                counterView.setText(counter.toString());
                messageView.setText(message);
            }
        });
    }
};
```

If you want to use a dummy button to test your app go to [http://pilotfish-demo-portal.eu/button](http://pilotfish-demo-portal.eu/button)
