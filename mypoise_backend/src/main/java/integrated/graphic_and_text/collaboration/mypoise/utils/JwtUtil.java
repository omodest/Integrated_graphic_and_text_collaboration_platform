//package integrated.graphic_and_text.collaboration.mypoise.utils;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    @Value("${jwt.secret}")
//    private String secret;
//    @Value("${jwt.expiration}")
//    private Long expiration;
//
//    public String generateToken(Long userId, String openid) {
//        return Jwts.builder()
//                .setSubject(userId.toString())
//                .claim("openid", openid)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS512, secret)
//                .compact();
//    }
//
//}
