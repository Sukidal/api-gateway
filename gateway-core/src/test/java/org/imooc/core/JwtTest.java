package org.imooc.core;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

public class JwtTest {

    @Test
    public void jwt() {
        String secretKey = "nvjkdnvsjfkdnvsfjv";

        String token = Jwts.builder()
                .setSubject("10000")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        System.out.println(token);

        Jwt jwt = Jwts.parser().setSigningKey(secretKey).parse(token);
        System.out.println(jwt);
        String subject = ((DefaultClaims) jwt.getBody()).getSubject();
        System.out.println(subject);
    }
}
