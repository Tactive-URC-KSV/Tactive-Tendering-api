package com.ksv.tactive.filter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Service
public class JwtService {
	private static final String SECRET_KEY = "YWVpdGtyb3lhbHNtZmp2bHdlc3ZtZnV3dm9zZ2t4ZXlzd3N6b3h0ag==";
	Map<String,Object> claims=new HashMap<>();
	public String generateToken(String username) {
		return Jwts.builder()
				.claims()
				.add(claims)
				.subject(username)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis()+3*60*60*1000))
				.and()
				.signWith(getKey())
				.compact();
	}
	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
	}
	public String extractUsername(String jwt) {
		return extractClaim(jwt, Claims::getSubject);
	}
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
	 private Claims extractAllClaims(String token) {
        return Jwts.parser()
        		.verifyWith(getKey())
        		.build()
        		.parseSignedClaims(token)
        		.getPayload();
    }
	public boolean validateToken(String jwt, UserDetails userDetails) {
		String userName=extractUsername(jwt);
		return (userName.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
	}
	public boolean isTokenExpired(String jwt) {
		return extractExpiration(jwt).before(new Date());
	}
	public Date extractExpiration(String jwt) {
		return extractClaim(jwt, Claims :: getExpiration);
	}
}
