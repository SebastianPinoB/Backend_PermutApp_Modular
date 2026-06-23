package com.example.PermutApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsConfig {

   @Bean
   public AwsCredentialsProvider awsCredentialsProvider(
         @Value("${AWS_ACCESS_KEY_ID:}") String accessKeyId,
         @Value("${AWS_SECRET_ACCESS_KEY:}") String secretAccessKey) {
      if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
         return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
      }
      return DefaultCredentialsProvider.create();
   }

   @Bean
   public RekognitionClient rekognitionClient(
         @Value("${aws.region:${AWS_REGION:us-east-2}}") String region,
         AwsCredentialsProvider credentialsProvider) {
      return RekognitionClient.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .build();
   }

   @Bean
   public TextractClient textractClient(
         @Value("${aws.region:${AWS_REGION:us-east-2}}") String region,
         AwsCredentialsProvider credentialsProvider) {
      return TextractClient.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .build();
   }
}
