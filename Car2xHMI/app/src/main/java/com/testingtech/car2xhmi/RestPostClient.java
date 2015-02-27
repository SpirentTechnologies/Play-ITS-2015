package com.testingtech.car2xhmi;

    import android.os.AsyncTask;
    import android.widget.TextView;

    import com.google.gson.GsonBuilder;

    import org.apache.http.HttpEntity;
    import org.apache.http.HttpResponse;
    import org.apache.http.client.ClientProtocolException;
    import org.apache.http.client.HttpClient;
    import org.apache.http.client.methods.HttpGet;
    import org.apache.http.client.methods.HttpPost;
    import org.apache.http.entity.StringEntity;
    import org.apache.http.impl.client.DefaultHttpClient;
    import org.apache.http.protocol.BasicHttpContext;
    import org.apache.http.protocol.HttpContext;

    import java.io.IOException;
    import java.io.InputStream;
    import java.io.UnsupportedEncodingException;
    import java.util.HashMap;
    import java.util.Map;

    public class RestPostClient extends AsyncTask<Void, Void, String> {

        TextView textview;

        public RestPostClient(TextView tv) {
            textview = tv;
        }

        @Override
        protected String doInBackground(Void... params) {
            Map<String, String> comment = new HashMap<String, String>();    // create data type according to json structure
            comment.put("subject", "Using the GSON library");               // add information to send
            comment.put("message", "Using libraries is convenient.");
            String json = new GsonBuilder().create().toJson(comment, Map.class);    // convert to json
            HttpResponse response = makeRequest("http://192.168.0.1:3000/post/77/comments", json);          // send to URL
            if(response != null)
                return response.toString();
            else
                return "error";
        }

        protected void onPostExecute(String results) {
            //TODO handle response
            textview.setText(results);
        }

        public static HttpResponse makeRequest(String uri, String json) {
            try {
                HttpPost httpPost = new HttpPost(uri);
                httpPost.setEntity(new StringEntity(json));
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                return new DefaultHttpClient().execute(httpPost);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
