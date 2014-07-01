/*
This sample shows how you would allow an S3 instance the ability to upload to a S3 bucket without having to provide
credentials. The aws sdk will look up the credentials from the role associated with the S3 instance. An example of how
to set up the role so that it has an instance profile and instance policy for a folder in the particular bucket is
shown in instance_policy_role.template
 */
package com.mcmanus.samples.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.*;
import java.net.URL;

public class S3InstanceProfileClient {

    private static String bucketName = "";
    private static String key = "";
    private static String method = "";
    private static String localFilePath = "";

    public static void main(String[] args) throws IOException {
        // Verify inputs
        if (args.length > 0) {
            if (!("Get".equalsIgnoreCase(args[0]) || "Put".equalsIgnoreCase(args[0]))) {
                usage("Unknown method " + args[0]);
            } else {
                method = args[0];
            }
        } else {
            usage("You must pass arguments to this class");
        }

        if (args.length > 1) {
            if (args[1] == null || "".equals(args[1])) {
                usage("Cannot have a empty remote file name to put/get");
            } else {
                key = args[1];
            }
        }

        if (args.length == 3) {
            if (args[1] == null || "".equals(args[1])) {
                usage("Cannot have a empty local file name to put");
            } else {
                localFilePath = args[2];
            }
        }

        if (args.length > 3) {
            usage("Incorrect number of arguments passed");
        }

        try {
            if ("Get".equalsIgnoreCase(method)) {
                getObject();
            } else if ("Put".equalsIgnoreCase(method)) {
                putObject();
            }
        } catch(AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch(AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "an internal error while trying to communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public static void putObject() throws AmazonClientException {
        AmazonS3 s3Client = new AmazonS3Client();

        File file = new File(localFilePath);
        if (file.exists() && !file.isDirectory()) {
            System.out.println("Uploading a new object to S3 from a file\n");
            s3Client.putObject(new PutObjectRequest(bucketName, key, file));
            // Now generate a pre-signed url so that the user can access the object

            System.out.println("Generating pre-signed URL.");
            java.util.Date expiration = new java.util.Date();
            long milliSeconds = expiration.getTime();
            milliSeconds += 1000 * 60 * 60; // Add 1 hour.
            expiration.setTime(milliSeconds);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, key);
            generatePresignedUrlRequest.setMethod(HttpMethod.GET);
            generatePresignedUrlRequest.setExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            System.out.println("Pre-Signed URL = " + url.toString());
        } else {
            throw new AmazonClientException("Local file: " + localFilePath + " does not exist or is a directory");
        }
    }

    public static void getObject() throws AmazonClientException {
        AmazonS3 s3Client = new AmazonS3Client();

        System.out.println("Downloading the object: " + key);
        S3Object s3object = s3Client.getObject(new GetObjectRequest(bucketName, key));
        try {
            displayTextInputStream(s3object.getObjectContent());
        }
        catch(IOException ioe){
            throw new AmazonClientException("Problem with file: " + ioe.getMessage());
        }
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while(true)
        {
            String line = reader.readLine();
            if(line == null) break;
            System.out.println( "    " + line );
        }
        System.out.println();
    }

    public static void usage(String message) {
        System.out.println(message);
        System.out.println("\n Usage: com.auspost.aws.s3.S3InstanceProfileClient Get <remoteFilePath>");
        System.out.println("\n Usage: com.auspost.aws.s3.S3InstanceProfileClient Put <remoteFilePath> <localFilePath>");
        System.exit(1);
    }
}
