package org.redkalex.test.rest.base;

import java.io.IOException;

import org.redkale.net.http.HttpRequest;
import org.redkale.net.http.HttpResponse;
import org.redkalex.rest.RestHttpServlet;

public class SimpleRestServlet extends RestHttpServlet<UserInfo> {

    @Override
    protected UserInfo currentUser(HttpRequest req) throws IOException {

        return null;
    }

    @Override
    public boolean authenticate(int module, int actionid, HttpRequest request, HttpResponse response) throws IOException {

        return true;
    }

}
