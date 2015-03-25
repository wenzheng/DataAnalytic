package com.vincent.bioplatform.ecghandler;

import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;


public class AWSSimpleQueueServiceUtil {
	private BasicAWSCredentials credentials;
	private AmazonSQS sqs;
	private static volatile AWSSimpleQueueServiceUtil awssqsUtil = new AWSSimpleQueueServiceUtil();

	/**
	 * instantiates a AmazonSQSClient
	 * http://docs.aws.amazon.com/AWSJavaSDK/latest
	 * /javadoc/com/amazonaws/services/sqs/AmazonSQSClient.html Currently using
	 * BasicAWSCredentials to pass on the credentials. For SQS you need to set
	 * your regions endpoint for sqs.
	 */
	private AWSSimpleQueueServiceUtil() {
		try {
			this.credentials = new BasicAWSCredentials("REPLACE_ME",
					"REPLACE_ME");

			this.sqs = new AmazonSQSClient(this.credentials);
			/**
			 * My queue is in singapore region which has following endpoint for
			 * sqs https://sqs.ap-southeast-1.amazonaws.com you can find your
			 * endpoints here
			 * http://docs.aws.amazon.com/general/latest/gr/rande.html
			 *
			 * Overrides the default endpoint for this client
			 * ("sqs.us-east-1.amazonaws.com")
			 */
			this.sqs.setEndpoint("https://sqs.ap-southeast-1.amazonaws.com");
			

		} catch (Exception e) {
			System.out.println("exception while creating awss3client : " + e);
		}
	}
	
	 
    public static AWSSimpleQueueServiceUtil getInstance(){
        return awssqsUtil;
    }

    
    
//    /**
//     * send a single message to your sqs queue
//     * @param queueUrl
//     * @param message
//     */
//    public void sendMessageToQueue(String message){
//        SendMessageResult messageResult =  this.sqs.sendMessage(new SendMessageRequest(awssqsUtil.queueUrl, message));
//        System.out.println(messageResult.toString());
//    }
//    
    /**
     * returns the queueurl for for sqs queue if you pass in a name
     * @param queueName
     * @return
     */
    public String getQueueUrl(String queueName){
    	for (String url: this.sqs.listQueues().getQueueUrls()){
    		if (url.contains(queueName)){
    			return url;
    		}
    	}
        return "";
    }
    
    /**
     * gets messages from your queue
     * @param queueUrl
     * @return
     */
    public String getMessagesFromQueue(String queueUrl){
       if (getQueueUrl(queueUrl).equals("")){
    	   return null;
       }
       ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(getQueueUrl(queueUrl));
       List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
       return messages.get(0).getBody();
    }
}
