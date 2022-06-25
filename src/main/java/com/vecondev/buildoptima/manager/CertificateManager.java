package com.vecondev.buildoptima.manager;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.exception.KeypairException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.lang.Assert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.function.Function;

@Data
@Component
@RequiredArgsConstructor
public class CertificateManager {

  private final JwtConfigProperties jwtConfigProperties;

  public PublicKey publicKey() {
    String publicKeyPath = jwtConfigProperties.getPublicKeyPath();
    SignatureAlgorithm signatureAlgorithm = jwtConfigProperties.getSignatureAlgorithm();

    Assert.notNull(publicKeyPath, "Public key path is required");

    try {
      KeyFactory keyFactory = KeyFactory.getInstance(signatureAlgorithm.getFamilyName());
      return readKey(
          publicKeyPath,
          (bytes -> {
            try {
              return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
            } catch (InvalidKeySpecException e) {
              throw new KeypairException("InvalidKeySpecException", e);
            }
          }));

    } catch (Exception e) {
      throw new KeypairException("Exception occurred when retrieving public key", e);
    }
  }

  public PrivateKey privateKey() {
    String privateKeyPath = jwtConfigProperties.getPrivateKeyPath();
    SignatureAlgorithm signatureAlgorithm = jwtConfigProperties.getSignatureAlgorithm();

    org.springframework.util.Assert.notNull(privateKeyPath, "Private key path is required");

    try {
      KeyFactory keyFactory = KeyFactory.getInstance(signatureAlgorithm.getFamilyName());
      return readKey(
          privateKeyPath,
          (bytes -> {
            try {
              return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
            } catch (InvalidKeySpecException ex) {
              throw new KeypairException("InvalidKeySpecException", ex);
            }
          }));
    } catch (Exception ex) {
      throw new KeypairException("Exception occurred when retrieving private key", ex);
    }
  }

  private <T extends Key> T readKey(String publicKeyPath, Function<byte[], T> keyReader)
      throws IOException {
    return keyReader.apply(
        FileCopyUtils.copyToByteArray(new ClassPathResource(publicKeyPath).getInputStream()));
  }
}
