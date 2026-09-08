package com.me.controller;

import com.me.bean.NativeRepository;
import com.me.bean.OtpBeans;
import com.me.bean.QueryResult;
import com.me.bean.StaticReportBean;
import com.me.modal.*;
import com.me.service.OtpServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import jakarta.xml.bind.DatatypeConverter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MessageContrl {

	@Autowired
	OtpServiceImp otpServiceImp;

	@Autowired
	NativeRepository nativeRepository;

	@RequestMapping(method = RequestMethod.POST, value = "/saveOtpTemplate", consumes = MediaType.TEXT_PLAIN_VALUE)
	public Otp OtpRegistration(@RequestBody String data) {
		ObjectMapper mapperObj = new ObjectMapper();
		Otp mappingId = new Otp();
		try {
			mappingId = mapperObj.readValue(data, new TypeReference<Otp>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Otp otpObj = otpServiceImp.OtpRegistration(mappingId);

		OtpMailHistry obj = new OtpMailHistry();
		obj.setUniqueId(otpObj.getOtpId());
		obj.setCount(1L);
		obj.setType("OTP");
//     	  obj.setApplicationName(otpObj.getApplicationName());
//          obj.set
		updateHistry(obj);

		return otpObj;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getOtpTemplate")
	public List<Otp> getOtpTemplate() {
		return otpServiceImp.getOtpTemplate();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/saveEmailTemplate", consumes = MediaType.TEXT_PLAIN_VALUE)
	public EmailBean saveEmailTemplate(@RequestBody String data) {
		ObjectMapper mapperObj = new ObjectMapper();
		EmailBean mappingId = new EmailBean();
		try {
			mappingId = mapperObj.readValue(data, new TypeReference<EmailBean>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		EmailBean emailObj = otpServiceImp.saveEmailTemplate(mappingId);

		OtpMailHistry obj = new OtpMailHistry();
		obj.setUniqueId(emailObj.getEmailTemplateId());
		obj.setCount(1L);
		obj.setType("MESSAGE");
		obj.setApplicationName(emailObj.getApplicationName());
		updateHistry(obj);
		return emailObj;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getEmailTemplate")
	public List<EmailBean> getEmailTemplate() {
		return otpServiceImp.getEmailTemplate();
	}

//	@RequestMapping(method=RequestMethod.POST, value="/sendOTP",consumes = MediaType.TEXT_PLAIN_VALUE)
	@RequestMapping(method = RequestMethod.POST, value = "/sendOTP")
	public Map<String, String> sendOTP(@RequestBody String data) {
		Map<String, String> resData = new HashMap<String, String>();
		String message = null;
		String otps = null;
		String q = null;
		ApplicationRegistration appData = new ApplicationRegistration();
		AccountRegistration accData = new AccountRegistration();
		ObjectMapper mapperObj = new ObjectMapper();
		OtpBeans otpBeansData = new OtpBeans();
		Otp otpData = null;
		try {
			otpBeansData = mapperObj.readValue(data, new TypeReference<OtpBeans>() {
			});

			if (otpBeansData.getIsDynamic().equalsIgnoreCase("Y")) {
				appData = otpServiceImp.getApplicationDetailsById(otpBeansData.getApplicationId());
				accData = otpServiceImp.getAccountDetailsById(appData.getAccountId());
				otpData = otpServiceImp.getOtpDetailsByOtpId(otpBeansData.getOtpId());
				message = otpData.getMessage();
				for (int i = 0; i < otpBeansData.getDynamicData().size(); i++) {
					if (otpBeansData.getDynamicData().get(i).equalsIgnoreCase("OTP")) {
						otps = getOpt().toString();
						message = message.replace("<V" + (i + 1) + ">", otps);
					} else {
						message = message.replace("<V" + (i + 1) + ">", otpBeansData.getDynamicData().get(i));
					}
				}

				q = "&message=" + message + "&mnumber=91" + otpBeansData.getMobile() + "&signature="
						+ otpData.getSignature() + "&dlt_entity_id=" + otpData.getEntityId() + "&dlt_template_id="
						+ otpData.getTemplateId();
			} else {
				message = otpBeansData.getDynamicData().get(0);
				 message = URLEncoder.encode(message, StandardCharsets.UTF_8);
				q = "&message=" + message + "&mnumber=91" + otpBeansData.getMobile() + "&signature=" + "SELMOE"
						+ "&dlt_entity_id=" + "1101607010000029348" + "&dlt_template_id=" + "1107171385841986951";
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {

//			String https_url = "https://hydgw.sms.gov.in/failsafe/MLink?username=shagun.sms&pin=P%26j6%40tRb";
			String userName = accData.getUserName() == null ? "shagun.otp" : accData.getUserName();

//			String pin = accData.getPin() == null ? "P%26j6@tRb" : accData.getPin();
			String pin = accData.getPin() == null ? "Smpp@123" : accData.getPin();
//			String https_url = "https://smsgw.sms.gov.in/failsafe/HttpLink?username=" + userName + "&pin=" + pin;
//			String https_url = "https://hydgw.sms.gov.in/failsafe/MLink?username=" + accData.getUserName() + "&pin="
//					+ accData.getPin();
			String https_url = "https://api1.sms.gov.in/failsafe/MLink?username=" + userName + "&pin=" + pin;
			

			System.out.println("https_url---->" + https_url);
			URL url;
			String messageval = "";
			try {
				SSLContext ssl_ctx = SSLContext.getInstance("TLS");
				TrustManager[] trust_mgr = get_trust_mgr();
				ssl_ctx.init(null, // key manager
						trust_mgr, // trust manager
						new SecureRandom()); // random number generator
				HttpsURLConnection.setDefaultSSLSocketFactory(ssl_ctx.getSocketFactory());

//                                      String message="Dear "+msObj.getUserId()+" OTP for Approving the data is "+otp+" (valid for 3 min). For any query reach us as per the details available on the website.";

//                                      String q="&message="+message+"&mnumber=91"+otpBeansData.getMobile()+"&signature=UDISEP&dlt_entity_id=1101607010000029348&dlt_template_id=1107161768570242528";
//				String q = "&message=" + message + "&mnumber=91" + otpBeansData.getMobile() + "&signature="
//						+ otpData.getSignature() + "&dlt_entity_id=" + otpData.getEntityId() + "&dlt_template_id="
//						+ otpData.getTemplateId();

				System.out.println("final message----" + q);
				https_url =https_url+q;
				System.out.println("final https_url---"+https_url);

				url = new URL(https_url);
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("GET");
//				con.setDoOutput(true);
//				OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
//				String line2 = wr.toString();
//				wr.write(q);
//				wr.flush();
				System.out.println("URL: " + https_url);
				System.out.println("Connection: " + con);

				System.out.println("con---"+con);
				
				messageval = print_content(con);
				con.disconnect();

			} catch (MalformedURLException e) {
				e.printStackTrace();
				// System.out.println(e);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}

			resData.put("status", "1");

			// System.out.println("messageval--->"+messageval);

//			if (messageval.contains("Message Accepted")) {
//				// System.out.println("Update table");
//				resData.put("status", "1");
//				resData.put("OTP", otps);
//				OtpMailHistry obj = new OtpMailHistry();
//				obj.setUniqueId(otpBeansData.getOtpId());
//				obj.setCount(1L);
//				obj.setType("OTP");
//				obj.setApplicationName(appData.getApplicationName());
//				obj.setApplicationId(String.valueOf(appData.getId()));
//				obj.setAccountName(appData.getAccountName());
//				obj.setAccountId(String.valueOf(appData.getAccountId()));
//				obj.setTemplateName(otpData.getTemplateName());
//				obj.setTemplateId(String.valueOf(otpData.getId()));
//				updateHistry(obj);
//
//			} else {
//				resData.put("status", "1");
//			}

//              messageString=messageval+"sms successfull";

		} catch (NullPointerException e) {
			resData.put("status", "0");
//              messageString=e.toString();
			e.printStackTrace();
			////// System.out.print("Message String in case of Customer Reg not found");
		} catch (Exception e) {
			resData.put("status", "0");
//                  messageString=e.toString();
			e.printStackTrace();
			////// System.out.print("Message String in case of Customer Reg not found");

		}

		return resData;
	}

	private TrustManager[] get_trust_mgr() {
		TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String t) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String t) {
			}
		} };
		return certs;
	}

	public Integer getOpt() {
		List<Object> lst = new ArrayList<Object>();
		int random_int = (int) (Math.random() * (9999 - 1000 + 1) + 1000);
		// System.out.println(random_int);
		return random_int;

	}

	public String print_content(HttpsURLConnection con) {
		String msg = "";
		if (con != null) {

			try {

				////// System.out.print("****** Content of the URL ********");
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String input;

				while ((input = br.readLine()) != null) {
					System.out.print(input);
					msg = msg + input;
					// System.out.println("output Message--->"+msg);
				}
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getOtpId")
	public Long getOtpId() {
		return (otpServiceImp.getOtpId() + 1);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/getMessageId")
	public Long getMessageId() {
		return (otpServiceImp.getMessageId() + 1);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateHistry", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void updateHistry(@RequestBody OtpMailHistry data) {
		otpServiceImp.updateHistry(data);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteOTPTemplete", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void deleteOTPTemplete(@RequestBody String id) {
		otpServiceImp.deleteOTPTemplete(Long.parseLong(id));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getDashboardData")
	public List<OtpMailHistry> getDashboardData() {
		return otpServiceImp.getDashboardData();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sendMessage", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> sendMessage(@RequestBody EmailBean otpBeansData) {
		ObjectMapper mapperObj = new ObjectMapper();
		EmailBean dbData = new EmailBean();
		ApplicationRegistration dbData1 = new ApplicationRegistration();

		if (otpBeansData.getIsDbBased() == 0) {
			dbData.setEmailToType("D");
			dbData.setEmailCcType("D");
			dbData.setContentType("D");
			dbData.setSubjectType("D");
			dbData.setSignatureType("D");
			dbData.setClosingType("D");
		}

		try {
			if (otpBeansData.getIsDbBased() != 0) {
				dbData = otpServiceImp.getEmailByTemplateId(otpBeansData.getEmailTemplateId());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			if (otpBeansData.getIsDbBased() != 0) {
				dbData1 = otpServiceImp.getApplicationDetailsById(Long.parseLong(otpBeansData.getApplicationId()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		System.out.println(otpBeansData.getContent());
		String content1 = otpBeansData.getContent().replaceAll("\n", " \n ");

		HashMap<String, String> responseMap = new LinkedHashMap<String, String>();
		Properties props = new Properties();
		props.put("mail.smtp.auth", "false");
//		props.put("mail.smtp.ssl.enable", "true");
//		props.put("mail.smtp.host", "164.100.14.95");
//		props.put("mail.smtp.port", "25");

//		props.put("mail.smtp.host", "10.194.83.232");
//		props.put("mail.smtp.port", "25");
		
		props.put("mail.smtp.host", "164.100.13.60");
		props.put("mail.smtp.port", "25");
		
		try {
//			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
//				protected PasswordAuthentication getPasswordAuthentication() {
////					return new PasswordAuthentication("nicsupport-edu@gov.in", "E0$eA7@vO2");
//					return new PasswordAuthentication(null, null);
//				}
//			});

			Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
//			      return new PasswordAuthentication("edu@gov.in", "mmm");
					return new PasswordAuthentication(null, null);
				}
			});

			MimeMessage message = new MimeMessage(session);
//				if (mailBeans.getToEmailIds() != null || mailBeans.getToEmailIds().length()!=0) {

			if (dbData.getEmailToType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(dbData.getEmailTo()));
			} else if (dbData.getEmailToType().equalsIgnoreCase("D")) {
				message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(otpBeansData.getEmailTo()));
			}

			if (dbData.getEmailCcType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(dbData.getEmailCc()));
			} else if (dbData.getEmailCcType().equalsIgnoreCase("D")) {
				message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(otpBeansData.getEmailCc()));
			}
//			message.setFrom(new InternetAddress("noreply-kvs@gov.in", "KVS Teacher Credentials"));

			if (otpBeansData.getMailHeading() != null) {
				message.setFrom(new InternetAddress("nicsupport-edu@gov.in", otpBeansData.getMailHeading()));
			} else {
				message.setFrom(new InternetAddress("nicsupport-edu@gov.in", "KVS Teacher Credentials"));
			}

			if (dbData.getSubjectType().equalsIgnoreCase("F")) {
				message.setSubject(dbData.getSubject());
			} else if (dbData.getSubjectType().equalsIgnoreCase("D")) {
				message.setSubject(otpBeansData.getSubject());
			}

			// Message Formater

			String messageContent = "";
			String signature = "";
			String closing = "";

			if (dbData.getSignatureType().equalsIgnoreCase("F")) {
				signature = dbData.getSignature();
			} else if (dbData.getSignatureType().equalsIgnoreCase("D")) {
				signature = otpBeansData.getSignature();
			}

			if (dbData.getClosingType().equalsIgnoreCase("F")) {
				closing = dbData.getClosing();
			} else if (dbData.getClosingType().equalsIgnoreCase("D")) {
				closing = otpBeansData.getClosing();
			}

			MimeBodyPart messageBodyPart = new MimeBodyPart();

			if (dbData.getContentType().equalsIgnoreCase("F")) {
				String content = dbData.getContent().replaceAll("\n", " \n ");
				messageContent = "<pre><p>" + signature + ",</p> <p>" + content + "</p> <br><p>Regards,</p> <p>"
						+ closing + "</p></pre>";
//					message.setContent(messageContent, "text/html; charset=utf-8");

				messageBodyPart.setContent(messageContent, "text/html");
			} else if (dbData.getContentType().equalsIgnoreCase("D")) {
				String content = otpBeansData.getContent().replaceAll("\n", " \n ");
				if (signature == null || (signature.equalsIgnoreCase("") && closing.equalsIgnoreCase(""))) {
					messageContent = "<p>" + content + "</p>";
				} else {
					messageContent = "<pre><p>" + signature + ",</p> <p>" + content + "</p> <br><p>Regards,<br>"
							+ closing + "</p></pre>";
				}

//						message.setContent(messageContent, "text/html; charset=utf-8");
				messageBodyPart.setContent(messageContent, "text/html");
			}

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			MimeBodyPart attachPart = new MimeBodyPart();

			if (otpBeansData.getAttachmentYn() == 1) {
				File file = new File(otpBeansData.getAttachmentPath());

				byte[] fileDecoded = DatatypeConverter.parseBase64Binary(otpBeansData.getPdfbase64Encoded());
				File file1 = null;
				try {

					File f1 = new File("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId());
					if (!f1.exists()) {
						f1.mkdir();
					}

					if (otpBeansData.getReportType() == 1) {
						Path path = Paths.get("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_school.pdf");
						Files.write(path, fileDecoded);
						file1 = new File("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_school.pdf");
					} else if (otpBeansData.getReportType() == 2) {
						Path path = Paths.get("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_teacher.pdf");
						Files.write(path, fileDecoded);
						file1 = new File("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_teacher.pdf");
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

				String absolutePath = file1.getAbsolutePath();
				attachPart.attachFile(absolutePath);
				multipart.addBodyPart(attachPart);
			}
			message.setContent(multipart);

			Transport transport = session.getTransport("smtp");
//			transport.connect("10.194.83.232", null, null);
//			transport.connect("164.100.14.95", null, null);
			transport.connect("192.168.3.3", null, null);
			
			

//					 Transport.send(msg);

			transport.send(message);

			transport.sendMessage(message, message.getAllRecipients());
//			System.out.println("Mail Sent Succesfully");
			responseMap.put("STATUS", "true");
			responseMap.put("MSG", "Email Sent");
			OtpMailHistry obj = new OtpMailHistry();
			obj.setUniqueId(otpBeansData.getEmailTemplateId());
			obj.setCount(1L);
			obj.setType("MESSAGE");
			obj.setApplicationName(dbData1.getApplicationName());
			obj.setApplicationId(String.valueOf(dbData1.getId()));
			obj.setTemplateName(dbData.getTemplateName());

//			updateHistry(obj);

			return responseMap;
		} catch (MessagingException e) {
			e.printStackTrace();
			responseMap.put("STATUS", "false");
			responseMap.put("MSG", "Somthing Went Wrong");
			return responseMap;
		} catch (Exception e) {
			e.printStackTrace();
			responseMap.put("STATUS", "false");
			responseMap.put("MSG", "Somthing Went Wrong");
			return responseMap;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/sendCorrectionMessage", consumes = MediaType.TEXT_PLAIN_VALUE)
	public Map<String, String> sendCorrectionMessage(@RequestBody String data) {
		ObjectMapper mapperObj = new ObjectMapper();
		EmailBean otpBeansData = new EmailBean();
		EmailBean dbData = new EmailBean();

		// System.out.println(data);

		ApplicationRegistration dbData1 = new ApplicationRegistration();

		System.out.println(data);

		try {
			otpBeansData = mapperObj.readValue(data, new TypeReference<EmailBean>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}

//		System.out.println("otpBeansData--->"+otpBeansData);

		try {
			dbData = otpServiceImp.getEmailByTemplateId(otpBeansData.getEmailTemplateId());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			dbData1 = otpServiceImp.getApplicationDetailsById(Long.parseLong(otpBeansData.getApplicationId()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(otpBeansData.getContent());
		String content1 = otpBeansData.getContent().replaceAll("\n", " \n ");
		// System.out.println("ApplicationId---->"+content1);

		// System.out.println("Get Application Id--->"+dbData1.getId());
		// System.out.println("dbData--->"+dbData1.getApplicationName());
		// System.out.println("dbData--->"+dbData.getEmailTemplateId());
		// System.out.println("dbData--->"+dbData.getTemplateName());
		// System.out.println("-----------------Send mail-----------------------");

		HashMap<String, String> responseMap = new LinkedHashMap<String, String>();
		Properties props = new Properties();
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.host", "10.194.83.232");
		props.put("mail.smtp.port", "25");
		try {
//			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication(null, null);
//				}
//			});

			Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
//			      return new PasswordAuthentication("edu@gov.in", "mmm");
					return new PasswordAuthentication(null, null);
				}
			});

			MimeMessage message = new MimeMessage(session);
//				if (mailBeans.getToEmailIds() != null || mailBeans.getToEmailIds().length()!=0) {

			if (dbData.getEmailToType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(dbData.getEmailTo()));
			} else if (dbData.getEmailToType().equalsIgnoreCase("D")) {
				message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(otpBeansData.getEmailTo()));
			}

			if (dbData.getEmailCcType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(dbData.getEmailCc()));
			} else if (dbData.getEmailCcType().equalsIgnoreCase("D")) {
				message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(otpBeansData.getEmailCc()));
			}
			message.setFrom(new InternetAddress("noreply-kvs@gov.in"));

			if (dbData.getSubjectType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				message.setSubject(dbData.getSubject());
			} else if (dbData.getSubjectType().equalsIgnoreCase("D")) {
				message.setSubject(otpBeansData.getSubject());
			}

			// Message Formater

			String messageContent = "";
			String signature = "";
			String closing = "";

			if (dbData.getSignatureType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				signature = dbData.getSignature();
			} else if (dbData.getSignatureType().equalsIgnoreCase("D")) {
				signature = otpBeansData.getSignature();
			}

			if (dbData.getClosingType().equalsIgnoreCase("F")) {
				// System.out.println("In F");
				closing = dbData.getClosing();
			} else if (dbData.getClosingType().equalsIgnoreCase("D")) {
				closing = otpBeansData.getClosing();
			}

			// System.out.println("closing--->"+closing);

			MimeBodyPart messageBodyPart = new MimeBodyPart();

			if (dbData.getContentType().equalsIgnoreCase("F")) {

				// System.out.println("In F");
				String content = dbData.getContent().replaceAll("\n", " \n ");
				messageContent = "<pre><p>" + signature + ",</p> <p>" + content + "</p> <br><p>Regards,</p> <p>"
						+ closing + "</p></pre>";
//					message.setContent(messageContent, "text/html; charset=utf-8");

				messageBodyPart.setContent(messageContent, "text/html");
			} else if (dbData.getContentType().equalsIgnoreCase("D")) {
				String content = otpBeansData.getContent().replaceAll("\n", " \n ");

//						// System.out.println("content-->"+content);

				messageContent = "<pre><p>" + signature + ",</p> <p>" + content + "</p> <br><p>Regards,</p> <p>"
						+ closing + "</p></pre>";
//						message.setContent(messageContent, "text/html; charset=utf-8");
				messageBodyPart.setContent(messageContent, "text/html");
			}

//					// System.out.println("messageContent---->"+messageContent);
//					// System.out.println("");
//					message.setContent("Hello", "text/html;");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			MimeBodyPart attachPart = new MimeBodyPart();

			if (otpBeansData.getAttachmentYn() == 1) {
				File file = new File(otpBeansData.getAttachmentPath());
//				        File file = new File("/tmp/abc.txt");
//				        File file1 = new File("/resources/abc.txt");

				byte[] fileDecoded = DatatypeConverter.parseBase64Binary(otpBeansData.getPdfbase64Encoded());
				File file1 = null;
				try {

					File f1 = new File("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId());
					if (!f1.exists()) {
						f1.mkdir();
					}

					if (otpBeansData.getReportType() == 1) {
						Path path = Paths.get("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_school.pdf");
						Files.write(path, fileDecoded);
						file1 = new File("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_school.pdf");
					} else if (otpBeansData.getReportType() == 2) {
						Path path = Paths.get("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_teacher.pdf");
						Files.write(path, fileDecoded);
						file1 = new File("/home/uploadDoc" + File.separator + otpBeansData.getTeacherId()
								+ File.separator + "profile_by_teacher.pdf");
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
//				ByteArrayDataSource bds = new ByteArrayDataSource(fileDecoded, "test.pdf"); 
//				attachPart.setDataHandler(new DataHandler(bds)); 
//				attachPart.setFileName(bds.getName());
				String absolutePath = file1.getAbsolutePath();
				attachPart.attachFile(absolutePath);
				multipart.addBodyPart(attachPart);
			}
			message.setContent(multipart);

			Transport transport = session.getTransport("smtp");
			transport.connect("10.194.83.232", null, null);

//					 Transport.send(msg);

			transport.send(message);

			transport.sendMessage(message, message.getAllRecipients());

			responseMap.put("STATUS", "true");
			responseMap.put("MSG", "Email Sent");
			// System.out.println("Template Name---."+dbData.getTemplateName());
			OtpMailHistry obj = new OtpMailHistry();
			// System.out.println("before send mail");
			obj.setUniqueId(otpBeansData.getEmailTemplateId());
			obj.setCount(1L);
			obj.setType("MESSAGE");
			obj.setApplicationName(dbData1.getApplicationName());
			obj.setApplicationId(String.valueOf(dbData1.getId()));
			obj.setTemplateName(dbData.getTemplateName());

			updateHistry(obj);

//				} else {
//					responseMap.put("STATUS", "false");
//					responseMap.put("MSG", "Email Address Not Found");
//				}

			return responseMap;
		} catch (MessagingException e) {
			e.printStackTrace();
			responseMap.put("STATUS", "false");
			responseMap.put("MSG", "Somthing Went Wrong");
			return responseMap;
		} catch (Exception e) {
			e.printStackTrace();
			responseMap.put("STATUS", "false");
			responseMap.put("MSG", "Somthing Went Wrong");
			return responseMap;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/saveAccountRegistration", consumes = MediaType.TEXT_PLAIN_VALUE)
	public AccountRegistration saveAccountRegistration(@RequestBody String data) {

		ObjectMapper mapperObj = new ObjectMapper();
		AccountRegistration dbData = new AccountRegistration();
		try {
			dbData = mapperObj.readValue(data, new TypeReference<AccountRegistration>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return otpServiceImp.saveAccountRegistration(dbData);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/saveApplicationRegistration")
	public ApplicationRegistration saveApplicationRegistration(@RequestBody String data) {

		ObjectMapper mapperObj = new ObjectMapper();
		ApplicationRegistration dbData = new ApplicationRegistration();
		try {
			dbData = mapperObj.readValue(data, new TypeReference<ApplicationRegistration>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return otpServiceImp.saveApplicationRegistration(dbData);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/saveApplicationTempMapping")
	public List<ApplicationTempMapping> saveApplicationTempMapping(@RequestBody String data) {

		ObjectMapper mapperObj = new ObjectMapper();
		List<ApplicationTempMapping> dbData = new ArrayList<ApplicationTempMapping>();
		try {
			dbData = mapperObj.readValue(data, new TypeReference<List<ApplicationTempMapping>>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return otpServiceImp.saveApplicationTempMapping(dbData);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getAccountRegistration")
	public List<AccountRegistration> getAccountRegistration() {
		return otpServiceImp.getAccountRegistration();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getApplicationRegistration")
	public List<ApplicationRegistration> getApplicationRegistration() {
		return otpServiceImp.getApplicationRegistration();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getApplicationTempMapping")
	public List<ApplicationTempMapping> getApplicationTempMapping() {
		return otpServiceImp.getApplicationTempMapping();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getApplicationTemplated", consumes = MediaType.TEXT_PLAIN_VALUE)
	public List<ApplicationTempMapping> getApplicationTemplated(@RequestBody String data) {
		return otpServiceImp.getApplicationTemplated(Long.parseLong(data));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/saveApplicationMailMapping")
	public List<ApplicationMailMapping> saveApplicationMailMapping(@RequestBody String data) {

		ObjectMapper mapperObj = new ObjectMapper();
		List<ApplicationMailMapping> dbData = new ArrayList<ApplicationMailMapping>();
		try {
			dbData = mapperObj.readValue(data, new TypeReference<List<ApplicationMailMapping>>() {
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return otpServiceImp.saveApplicationMailMapping(dbData);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getApplicationMailTemplated", consumes = MediaType.TEXT_PLAIN_VALUE)
	public List<ApplicationMailMapping> getApplicationMailTemplated(@RequestBody String data) {
		// System.out.println("data---->"+data);
		return otpServiceImp.getApplicationMailTemplated(data);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteRegistedApplication", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void deleteRegistedApplication(@RequestBody String data) {
		// System.out.println("data---->"+data);
		otpServiceImp.deleteRegistedApplication(Long.parseLong(data));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteRegistdedSmsTemplate", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void deleteRegistdedSmsTemplate(@RequestBody String data) {
		// System.out.println("data---->"+data);
		otpServiceImp.deleteRegistdedSmsTemplate(Long.parseLong(data));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteRegistdedMailTemplate", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void deleteRegistdedMailTemplate(@RequestBody String data) {
		// System.out.println("data---->"+data);
		otpServiceImp.deleteRegistdedMailTemplate(Long.parseLong(data));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteSmsMapping", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void deleteSmsMapping(@RequestBody String data) {
		// System.out.println("data---->"+data);
		otpServiceImp.deleteSmsMapping(Long.parseLong(data));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteMailMapping", consumes = MediaType.TEXT_PLAIN_VALUE)
	public void deleteMailMapping(@RequestBody String data) {
		// System.out.println("data---->"+data);
		otpServiceImp.deleteMailMapping(Long.parseLong(data));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getDashboardCount")
	public StaticReportBean getDashboardCount() {
		// System.out.println("in get state review called");
		StaticReportBean sobj = new StaticReportBean();
		QueryResult qrObj = nativeRepository.executeQueries("select\r\n" + "count(*),\r\n"
				+ "count(no_of_records) filter (where sent_type = 'MESSAGE' ) as no_of_message,\r\n"
				+ "count(no_of_records) filter (where sent_type = 'OTP' ) as no_of_otp,\r\n"
				+ "sum(no_of_records) filter (where sent_type = 'MESSAGE' ) as total_no_of_message,\r\n"
				+ "sum(no_of_records) filter (where sent_type = 'OTP' ) as total_no_of_otp\r\n"
				+ "from me_otpmail_histry moh");
//					+ "order by  psq.domainid , psq.questionid ");
		sobj.setColumnName(qrObj.getColumnName());
		sobj.setRowValue(qrObj.getRowValue());
		sobj.setColumnDataType(qrObj.getColumnDataType());
		sobj.setStatus("1");
		return sobj;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/getApplicationPerCount")
	public Map<String, Map<String, List<Map<String, String>>>> getApplicationPerCount() {

		Map<String, Map<String, List<Map<String, String>>>> fObj = new HashMap<String, Map<String, List<Map<String, String>>>>();

		Map<String, List<Map<String, String>>> finalObj = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> finalObj1 = new HashMap<String, List<Map<String, String>>>();

		List<OtpMailHistry> obj = otpServiceImp.getApplicationPerCount();

		for (int i = 0; i < obj.size(); i++) {
//			 // System.out.println(obj[i].applicationName);

			if (obj.get(i).getType().equalsIgnoreCase("MESSAGE")) {

				if (finalObj.containsKey(obj.get(i).getApplicationName())) {

					List<Map<String, String>> lis = finalObj.get(obj.get(i).getApplicationName());
					Map<String, String> mp = new HashMap<String, String>();
					mp.put("Key", obj.get(i).getTemplateName());
					mp.put("Value", String.valueOf(obj.get(i).getCount()));
					mp.put("type", obj.get(i).getApplicationName());
					lis.add(mp);
					finalObj.put(obj.get(i).getApplicationName(), lis);
//			 applicationName
				} else {
					List<Map<String, String>> lis = new ArrayList<Map<String, String>>();
					Map<String, String> mp = new HashMap<String, String>();
					mp.put("Key", obj.get(i).getTemplateName());
					mp.put("Value", String.valueOf(obj.get(i).getCount()));
					mp.put("type", obj.get(i).getApplicationName());
					lis.add(mp);
					finalObj.put(obj.get(i).getApplicationName(), lis);
				}

			}
		}

		fObj.put("message", finalObj);

		for (int i = 0; i < obj.size(); i++) {
//			 // System.out.println(obj[i].applicationName);

			if (obj.get(i).getType().equalsIgnoreCase("OTP")) {

				if (finalObj1.containsKey(obj.get(i).getApplicationName())) {

					List<Map<String, String>> lis = finalObj1.get(obj.get(i).getApplicationName());
					Map<String, String> mp = new HashMap<String, String>();
					mp.put("Key", obj.get(i).getTemplateName());
					mp.put("Value", String.valueOf(obj.get(i).getCount()));
					mp.put("type", obj.get(i).getApplicationName());
					lis.add(mp);

					finalObj1.put(obj.get(i).getApplicationName(), lis);
//			 applicationName
				} else {
					List<Map<String, String>> lis = new ArrayList<Map<String, String>>();
					Map<String, String> mp = new HashMap<String, String>();
					mp.put("Key", obj.get(i).getTemplateName());
					mp.put("Value", String.valueOf(obj.get(i).getCount()));
					mp.put("type", obj.get(i).getApplicationName());
					lis.add(mp);
					finalObj1.put(obj.get(i).getApplicationName(), lis);
				}

			}
		}

		fObj.put("otp", finalObj1);

		return fObj;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upload")
	public ResponseEntity<Object> upload(@RequestParam("file") String file,
			@RequestParam("procesoId") Integer procesoId, @RequestParam("fuenteId") Integer fuenteId)
			throws IOException {
		System.out.println(file);
		System.out.println(file.getBytes());
		System.out.println("name--->" + fuenteId);
		byte[] fileDecoded = DatatypeConverter.parseBase64Binary(file);
		System.out.println(fileDecoded);
		Path path = Paths.get("E:\\uploadDoc\\1047\\11.pdf");
		Files.write(path, fileDecoded);
		if (file.isEmpty()) {
//	        return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		} else {
			// ...
		}
		return null;
	}

}
