package ap.edu;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleGetCall {

    private static SimpleDateFormat sdfHeader = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static SimpleDateFormat sdfHmac = new SimpleDateFormat("yyyyMMddHHmmss");
    private static String endPoint = "";
    private static String version = "";
    private static String publicKey = "";
    private static String privateKey = "";
    private static String instellingsNr = "";
    private static String requestMethod = "";

    public static void main(String[] args) {

        String baseURL = String.format("https://%s/%s/", endPoint, version);
        String calledURL = "";
        JsonObject jsonObject = Json
                .createObjectBuilder()
                .add("schoolYear", "2015-16")
                .add("startDate", "2015-09-10T00:00:00.000Z")
                .add("endDate", "2015-12-31T00:00:00.000Z")
                .build();
        //String jsonBody = "{\"schoolYear\":\"2015-16\"}";

        Date date = new Date();
        String timeStampHeader = sdfHeader.format(new Timestamp(date.getTime()));
        String timeStampHmac = sdfHmac.format(new Timestamp(date.getTime()));

        //
        System.out.println(timeStampHeader);
        System.out.println(timeStampHmac);
        //
        try {
            URL url = new URL(baseURL + calledURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("InstellingsNr", instellingsNr);
            conn.setRequestProperty("Timestamp", timeStampHeader);

            String message = String.format("verb=%1s&timestamp=%2s&url=%3s&instellingsnr=%4s",
                    requestMethod,
                    timeStampHmac,
                    url.toString(),
                    instellingsNr);
            //
            System.out.println(message);
            //
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(privateKey.getBytes(), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(message.getBytes("UTF-8"));
            String hash = DatatypeConverter.printHexBinary(hashBytes).replace("-", "").toLowerCase();//

            //
            System.out.println(hash);
            System.out.println(String.format("%s:%s", publicKey, hash));
            //

            String basicAuth = String.format("%s:%s", publicKey, hash);
            conn.setRequestProperty("Authentication", basicAuth);

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(jsonObject.size()));
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(jsonObject.toString());
            os.close();


            if (conn.getResponseCode() != 200) {

                //
                //
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getErrorStream())));

                String output;
                System.out.println("Error from Server .... \n");
                while ((output = br.readLine()) != null) {
                    System.out.println(output);
                }
                //
                //

                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode()
                        + " \ndetails: "
                        + conn.getResponseMessage());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();

        } catch (InvalidKeyException e) {

            e.printStackTrace();

        }


    }


}
