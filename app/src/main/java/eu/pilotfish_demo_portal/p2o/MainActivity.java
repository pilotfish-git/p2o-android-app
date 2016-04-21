package eu.pilotfish_demo_portal.p2o;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private String buttonId = "test";
    private TextView counterView;
    private TextView messageView;
    private Integer counter;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        counterView = (TextView) findViewById(R.id.counter);
        messageView = (TextView) findViewById(R.id.message);

        try {
            socket = IO.socket("http://pilotfish-demo-portal.eu:3001");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        socket.on("connection-response", onConnectionResponse);
        socket.on("register-response", onRegisterResponse);
        socket.on("button-press", onButtonPress);
        socket.connect();
    }

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

            Log.v("connection status: ", status);
        }
    };

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

                    Log.v("button count: ", counter.toString());
                }
            });
        }
    };

    private Emitter.Listener onButtonPress = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    String id;
//
//                    try {
//                        id = data.getString("id");
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }

                    counter = counter + 1;
                    message = "times pressed";
                    counterView.setText(counter.toString());
                    messageView.setText(message);
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
