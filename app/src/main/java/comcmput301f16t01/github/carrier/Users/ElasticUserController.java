package comcmput301f16t01.github.carrier.Users;

import android.os.AsyncTask;
import android.util.Log;

import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;

import java.io.IOException;

import comcmput301f16t01.github.carrier.ElasticController;
import comcmput301f16t01.github.carrier.Users.User;
import comcmput301f16t01.github.carrier.Users.UserController;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;

/**
 * Handles user operations in elastic search.
 * @see ElasticController
 */
public class ElasticUserController extends ElasticController {
    /** Called to add a user to elastic search */
    public static class AddUserTask extends AsyncTask<User, Void, Void> {

        /**
         * Async task to add user to elastic search.
         *
         * @param users these are the users that we will add in elastic search
         */
        @Override
        protected Void doInBackground(User... users) {
            verifySettings();

            for (User user : users) {
                Index index = new Index.Builder(user).index("cmput301f16t01").type("user").build();

                try {
                    DocumentResult result = client.execute(index);

                    if (result.isSucceeded()) {
                        user.setId(result.getId());
                    } else {
                        Log.i("Add User Unsuccessful", "Failed to add user to elastic search?");
                    }
                } catch (IOException e) {
                    Log.i("Add User Failure", "Something went wrong adding a user to elastic search.");
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    /**
     * Async task to search for a user in elastic search and bind it to an object
     */
    public static class FindUserTask extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... search_parameters) {
            verifySettings();
            String search_string = "{\"from\": 0, \"size\": 1, \"query\": {\"match\": {\"username\": \"" + search_parameters[0] + "\"}}}";

            Search search = new Search.Builder(search_string)
                    .addIndex("cmput301f16t01")
                    .addType("user")
                    .build();

            User foundUser = null;

            try {
                SearchResult result = client.execute(search);
                if (result.isSucceeded()) {
                    foundUser = result.getSourceAsObject(User.class);

                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Error", "Something went wrong when we tried to talk to elastic search");
            }
            return foundUser;
        }
    }

    /**
     * Async task to update a user in elastic search
     */
    public static class EditUserTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... update_params) {
             verifySettings();
            // update_params[0] is the id, update_params[1] is email, update params[2] is phone
            // this is the update string
            String script = "{\n" +
                    "  \"doc\": { \"phoneNumber\": " + "\"" + update_params[2] + "\", " +
                    "\"email\": " + "\"" + update_params[1] + "\"}" +
                    "\n}";
            Update update = new Update.Builder(script)
                    .index("cmput301f16t01")
                    .type("user")
                    .id(update_params[0])
                    .build();

            try {
                client.execute(update);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static class DeleteUserTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... search_params) {
            verifySettings();

            String script = "{\"query\": {\"match\": {\"username\": \"" + search_params[0] + "\"}}}";
            DeleteByQuery delete = new DeleteByQuery.Builder(script)
                    .addIndex("cmput301f16t01")
                    .addType("user")
                    .build();

            try {
                client.execute(delete);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException();
            }

            return null;
        }

    }
}
