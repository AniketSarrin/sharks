package com.sharks.event.service;

import com.sharks.event.config.SupabaseStorageProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.util.UUID;

/**
 * Uploads JPEG images to Supabase Storage via the S3-compatible API.
 * On startup, applies a public-read bucket policy so stored URLs are accessible without auth.
 */
@Service
public class SupabaseStorageService {

	private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

	private final SupabaseStorageProperties props;
	private final S3Client s3;

	/**
	 * Derived from the S3 endpoint by replacing the trailing {@code /s3} segment with
	 * {@code /object/public}, giving the Supabase Storage REST public-read base URL.
	 */
	private final String publicBaseUrl;

	public SupabaseStorageService(SupabaseStorageProperties props) {
		this.props = props;

		boolean configured = StringUtils.hasText(props.url())
				&& StringUtils.hasText(props.accessKey())
				&& StringUtils.hasText(props.secretKey());

		if (configured) {
			this.s3 = S3Client.builder()
					.endpointOverride(URI.create(props.url()))
					.credentialsProvider(StaticCredentialsProvider.create(
							AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
					.region(Region.of("auto"))
					.serviceConfiguration(S3Configuration.builder()
							.pathStyleAccessEnabled(true)
							.build())
					.httpClient(UrlConnectionHttpClient.builder().build())
					.build();
		}
		else {
			this.s3 = null;
		}

		// Build public REST URL base: strip /s3 suffix then /object/public is appended per-call.
		String base = props.url() == null ? "" : props.url().trim();
		if (base.endsWith("/s3")) {
			base = base.substring(0, base.length() - 3);
		}
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		this.publicBaseUrl = base;
	}

	/**
	 * Applies a bucket policy on startup:
	 * <ul>
	 *   <li><b>PublicRead</b> — {@code s3:GetObject} allowed for everyone (no credentials needed).</li>
	 *   <li><b>DenyAnonymousWrite</b> — {@code s3:PutObject} and {@code s3:DeleteObject} are
	 *       explicitly denied for unauthenticated (anonymous) requests; only the holder of the
	 *       configured access key may upload or delete objects.</li>
	 * </ul>
	 */
	@PostConstruct
	public void configureBucketPolicy() {
		if (s3 == null) {
			log.warn("Supabase S3 credentials not configured; skipping bucket policy setup.");
			return;
		}
		String bucket = props.bucket();
		String resource = "arn:aws:s3:::" + bucket + "/*";
		String policy = """
				{
				  "Version": "2012-10-17",
				  "Statement": [
				    {
				      "Sid": "PublicRead",
				      "Effect": "Allow",
				      "Principal": "*",
				      "Action": "s3:GetObject",
				      "Resource": "%s"
				    },
				    {
				      "Sid": "DenyAnonymousWrite",
				      "Effect": "Deny",
				      "Principal": "*",
				      "Action": ["s3:PutObject", "s3:DeleteObject"],
				      "Resource": "%s",
				      "Condition": {
				        "StringEquals": {
				          "s3:authType": "REST-QUERY-STRING"
				        }
				      }
				    }
				  ]
				}
				""".formatted(resource, resource);
		try {
			s3.putBucketPolicy(PutBucketPolicyRequest.builder()
					.bucket(bucket)
					.policy(policy)
					.build());
			log.info("Bucket policy applied to '{}': public read, authenticated write only.", bucket);
		}
		catch (Exception e) {
			log.warn("Could not apply bucket policy to '{}': {}", bucket, e.getMessage());
		}
	}

	/**
	 * @param eventId persisted event id (used as path prefix in the bucket)
	 * @param imageBytes raw JPEG bytes
	 * @return public Supabase Storage REST URL for the uploaded object
	 */
	public String uploadJpeg(Long eventId, byte[] imageBytes) {
		if (s3 == null) {
			throw new IllegalStateException(
					"Supabase S3 not configured. Set SUPABASE_STORAGE_URL, SUPABASE_STORAGE_ACCESS_KEY and SUPABASE_STORAGE_SECRET_KEY.");
		}
		if (imageBytes == null || imageBytes.length == 0) {
			throw new IllegalArgumentException("Image body is empty");
		}
		if (!isJpeg(imageBytes)) {
			throw new IllegalArgumentException("File must be a JPEG image");
		}

		String objectKey = "events/" + eventId + "/" + UUID.randomUUID() + ".jpg";
		try {
			s3.putObject(
					PutObjectRequest.builder()
							.bucket(props.bucket())
							.key(objectKey)
							.contentType("image/jpeg")
							.build(),
					RequestBody.fromBytes(imageBytes));
		}
		catch (S3Exception e) {
			throw new IllegalStateException("Supabase Storage upload failed: " + e.awsErrorDetails().errorMessage(), e);
		}

		return publicBaseUrl + "/object/public/" + props.bucket() + "/" + objectKey;
	}

	private static boolean isJpeg(byte[] bytes) {
		if (bytes.length < 3) {
			return false;
		}
		return (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
	}
}
