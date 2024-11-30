package org.example;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Month;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        final String API_KEY = "60efbfc96eb64a47a3daa04c5c8ed120";

        Scanner input = new Scanner(System.in);
        final String fileUrl;
        final String outputTextFile;
        final String baseUrl = "https://github.com/Froboz67/AudioFiles/raw/refs/heads/main/";



        System.out.println("");
        LocalDate today = LocalDate.now();
        Month month = today.getMonth();
        int day = today.getDayOfMonth();
        int year = today.getYear();
        System.out.println("Today is " + month + " " + day + ", " + year);
        System.out.println("-----------------------------------------");
        System.out.println();
        System.out.print("please paste filename including the extension you wish to convert to text: " + baseUrl);
        fileUrl = input.nextLine();
        System.out.print("please enter the name for your text file ");
        outputTextFile = input.nextLine();


        Transcript transcript = new Transcript();
//        transcript.setAudio_url("https://github.com/Froboz67/AudioFiles/raw/refs/heads/main/spotify-project-audio.mp3");
        transcript.setAudio_url("https://github.com/Froboz67/AudioFiles/raw/refs/heads/main/" + fileUrl);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);

        System.out.println(jsonRequest);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println(postResponse.body());

        transcript = gson.fromJson(postResponse.body(), Transcript.class);
        transcript.getId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId()))
                .header("Authorization", API_KEY)
                .GET()
                .build();

        while(true) {
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            transcript = gson.fromJson(getResponse.body(), Transcript.class);

            System.out.println(transcript.getStatus());

            if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
                break;
            }

            Thread.sleep(1000);
        }
        System.out.println("Transcription Completed!");

//        System.out.println(transcript.getText());

        String audioTranscription = transcript.getText();

        try (BufferedWriter convertedAudio = new BufferedWriter(new FileWriter(outputTextFile + ".txt"))) {
            convertedAudio.write(audioTranscription);
            System.out.println("text file created: " + outputTextFile + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}