/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fredboat.api;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

public class OAuth2Client {

    private final String clientId;
    private final String secret;
    private final String tokenUrl;
    private final String redirectUrl;

    OAuth2Client(String clientId, String secret, String tokenUrl, String redirectUrl) {
        this.clientId = clientId;
        this.secret = secret;
        this.tokenUrl = tokenUrl;
        this.redirectUrl = redirectUrl;
    }

    TokenGrant grantToken(String code) throws UnirestException {
        JSONObject json = Unirest.post(tokenUrl)
                .field("code", code)
                .field("client_id", clientId)
                .field("client_secret", secret)
                .field("grant_type", "authorization_code")
                .field("redirect_uri", redirectUrl)
                .asJson().getBody().getObject();

        return new TokenGrant(
                json.getString("access_token"),
                json.getString("refresh_token"),
                json.getString("scope"),
                json.getLong("expires_in")
        );
    }

    public TokenGrant refreshToken(String refresh) throws UnirestException {
        JSONObject json = Unirest.post(tokenUrl)
                .field("refresh_token", refresh)
                .field("client_id", clientId)
                .field("client_secret", secret)
                .field("grant_type", "refresh_token")
                .asJson().getBody().getObject();

        // According to the standard, a new token may optionally be given
        if(json.has("refresh_token")){
            refresh = json.getString("refresh_token");
        }

        return new TokenGrant(
                json.getString("access_token"),
                refresh,
                json.getString("scope"),
                json.getLong("expires_in")
        );
    }
}
