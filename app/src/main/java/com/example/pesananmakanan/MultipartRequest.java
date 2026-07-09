package com.example.pesananmakanan;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class MultipartRequest extends JsonRequest<JSONObject> {

    private final Map<String, String> stringParams;
    private final String fileFieldName;
    private final File file;
    private final String boundary = "apiClientBoundary" + System.currentTimeMillis();

    public MultipartRequest(String url, Map<String, String> stringParams,
                            String fileFieldName, File file,
                            Response.Listener<JSONObject> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);
        this.stringParams = stringParams;
        this.fileFieldName = fileFieldName;
        this.file = file;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (Map.Entry<String, String> entry : stringParams.entrySet()) {
                bos.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes("UTF-8"));
                bos.write((entry.getValue() + "\r\n").getBytes("UTF-8"));
            }
            if (file != null && file.exists()) {
                bos.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
                bos.write(("Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes("UTF-8"));
                bos.write(("Content-Type: " + getMimeType(file.getName()) + "\r\n\r\n").getBytes("UTF-8"));
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) bos.write(buffer, 0, len);
                fis.close();
                bos.write("\r\n".getBytes("UTF-8"));
            }
            bos.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    private String getMimeType(String fileName) {
        return fileName.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JSONException e) {
            return Response.error(new ParseError(e));
        }
    }
}