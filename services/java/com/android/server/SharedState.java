package com.android.server;

import java.util.HashMap;
import java.util.ArrayList;

public class SharedState {
    private ArrayList<String> allSessions;
    private HashMap<String, String> allRequests;
    public SharedState() {
        allSessions = new ArrayList<>();
        allRequests = new HashMap<>();
    }

    public boolean hasSession(String session) {
        return allSessions.contains(session);
    }

    public void addSession(String session) {
        allSessions.add(session);
    }

    public void addRequest(String request) {
        allRequests.put(request, "notfulfilled");
    }

    public void fulfillRequest(String request, String response) {
        allRequests.put(request, response);
    }

    public String hasBeenFulfilled(String request) {
        return allRequests.get(request);
    }
}
