package com.suse.salt.netapi.calls.modules;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.client.impl.HttpAsyncClientImpl;
import com.suse.salt.netapi.datatypes.AuthMethod;
import com.suse.salt.netapi.datatypes.Token;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.CmdExecCodeAll;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.ClientUtils;
import com.suse.salt.netapi.utils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

/**
 * Cmd unit tests.
 */
public class CmdTest {
    private static final int MOCK_HTTP_PORT = 8888;

    static final String JSON_RUN_RESPONSE = ClientUtils.streamToString(
            SaltUtilTest.class.getResourceAsStream("/modules/cmd/uptime.json"));

    static final String JSON_HAS_EXEC_RESPONSE = ClientUtils.streamToString(
            SaltUtilTest.class.getResourceAsStream("/modules/cmd/has_exec.json"));

    static final String JSON_EXEC_CODE_RESPONSE = ClientUtils.streamToString(
            SaltUtilTest.class.getResourceAsStream("/modules/cmd/exec_code.json"));

    static final String JSON_EXEC_CODE_ALL_RESPONSE = ClientUtils.streamToString(
            SaltUtilTest.class.getResourceAsStream("/modules/cmd/exec_code_all.json"));

    static final AuthMethod AUTH = new AuthMethod(new Token());

    private SaltClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(MOCK_HTTP_PORT);

    @Before
    public void init() {
        URI uri = URI.create("http://localhost:" + Integer.toString(MOCK_HTTP_PORT));
        client = new SaltClient(uri, new HttpAsyncClientImpl(TestUtils.defaultClient()));
    }

    @Test
    public void testCmdUptime() {
        // First we get the call to use in the tests
        LocalCall<String> call = Cmd.run("uptime");
        assertEquals("cmd.run", call.getPayload().get("fun"));

        // Test with an successful response
        stubFor(any(urlMatching("/"))
                .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(JSON_RUN_RESPONSE)));

        Map<String, Result<String>> response =
                call.callSync(client, new MinionList("minion"), AUTH)
                        .toCompletableFuture().join();

        assertNotNull(response.get("minion"));

        String output = response.get("minion").result().get();
        assertEquals(" 16:51:23 up 22 min,  0 users,  load average: 0.00, 0.02, 0.09",
                output);
    }

    @Test
    public void testCmdHasExec() {
        // First we get the call to use in the tests
        LocalCall<Boolean> call = Cmd.hasExec("uptime");
        assertEquals("cmd.has_exec", call.getPayload().get("fun"));

        // Test with an successful response
        stubFor(any(urlMatching("/"))
                .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(JSON_HAS_EXEC_RESPONSE)));

        Map<String, Result<Boolean>> response =
                call.callSync(client, new MinionList("minion"), AUTH)
                        .toCompletableFuture().join();

        assertNotNull(response.get("minion"));

        Boolean output = response.get("minion").result().get();
        assertEquals(true, output);
    }

    @Test
    public void testCmdExecCode() {
        // First we get the call to use in the tests
        LocalCall<String> call =
                Cmd.execCode("python", "import sys; print sys.version");
        assertEquals("cmd.exec_code", call.getPayload().get("fun"));

        // Test with an successful response
        stubFor(any(urlMatching("/"))
                .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(JSON_EXEC_CODE_RESPONSE)));

        Map<String, Result<String>> response =
                call.callSync(client, new MinionList("minion"), AUTH)
                        .toCompletableFuture().join();

        assertNotNull(response.get("minion"));

        String output = response.get("minion").result().get();
        assertEquals("2.6.6 (r266:84292, Jul 23 2015, 15:22:56) "
                + "[GCC 4.4.7 20120313 (Red Hat 4.4.7-11)]", output);
    }

    @Test
    public void testCmdExecCodeAll() {
        // First we get the call to use in the tests
        LocalCall<CmdExecCodeAll> call =
                Cmd.execCodeAll("python", "import sys; print sys.version");
        assertEquals("cmd.exec_code_all", call.getPayload().get("fun"));

        // Test with an successful response
        stubFor(any(urlMatching("/"))
                .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(JSON_EXEC_CODE_ALL_RESPONSE)));

        Map<String, Result<CmdExecCodeAll>> response =
                call.callSync(client, new MinionList("minion"), AUTH)
                        .toCompletableFuture().join();

        assertNotNull(response.get("minion"));
        CmdExecCodeAll result = response.get("minion").result().get();
        assertEquals(27299, result.getPid());
        assertEquals(0, result.getRetcode());
        assertEquals("", result.getStderr());
        assertEquals("2.6.6 (r266:84292, Jul 23 2015, 15:22:56) "
                + "[GCC 4.4.7 20120313 (Red Hat 4.4.7-11)]", result.getStdout());
    }
}
