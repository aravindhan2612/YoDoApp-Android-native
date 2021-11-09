package com.example.ytsample.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class Dummy {
   public void  getVideo(String ytUrl) throws IOException {
        URL youtubeUrl = new URL(ytUrl);

        // Open URL
        BufferedReader reader = new BufferedReader(new InputStreamReader(youtubeUrl.openStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String content = builder.toString();

        // Parse URLs
        String[] list = content.split("url=");
        HashSet<String> results = new HashSet<String>();
        for (String hits : list) {
            if (hits.startsWith("https")) {
                String link = hits.split(",")[0];
                results.add(link);
            }
        }

        String url = null;
        String itag;
        String type;

        for (String set : results) {
            set = set.replace("\\", "\\\\");

            for (String sdata : set.split("\\\\")) {
                if (sdata.startsWith("https"))
                    url = sdata;
            }

            if (url != null) {
                if (url.startsWith("https")) {
                    url = url.split(" ")[0];
                    url = URLDecoder.decode(url, "UTF-8");

                    System.out.println("Stream-URL: " + url.toString());

                    if (url.contains("mime=")) {
                        type = (url.split("mime=")[1]).split("&")[0];
                        type = URLDecoder.decode(type, "UTF-8");
                        System.out.println("Mime-Type: " + type);
                    }

                    if (url.contains("itag")) {
                        itag = (url.split("itag=")[1]).split("&")[0];
                        itag = URLDecoder.decode(itag, "UTF-8");
                        System.out.println("itag: " + itag);
                    }

                    Pattern p = Pattern.compile("<title>(.*?)</title>");
                    Matcher m = p.matcher(content);
                    while (m.find()) {
                        String htmlTitle = m.group(1);
                        System.out.println("Title: " + htmlTitle);
                        break;
                    }
                }
            }
            System.out.println();
        }
    }
}
