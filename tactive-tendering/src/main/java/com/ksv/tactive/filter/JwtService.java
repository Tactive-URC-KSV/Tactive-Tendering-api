package com.ksv.tactive.filter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;

import com.ksv.tactive.config.CustomUserDetailsService;
import com.ksv.tactive.config.UserPrincipal;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
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
	private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
	Map<String,Object> claims=new HashMap<>();
	public String generateToken(UserPrincipal userDetails) {
		claims.put("role", userDetails.getRole());
		claims.put(
				"authorities",
				userDetails.getAuthorities()
						.stream()
						.map(GrantedAuthority::getAuthority)
						.toList()
		);
		claims.put("name", userDetails.getUserName());
		return Jwts.builder()
				.claims()
				.add(claims)
				.subject(userDetails.getUsername())
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
	 public Claims extractAllClaims(String token) {
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
	public void blacklistToken(String token) {
		blacklist.add(token);
	}
	public boolean isBlacklisted(String token) {
		return blacklist.contains(token);
	}

	public String generatePasswordResetToken(String username, String otpHash) {
		return Jwts.builder()
				.setSubject(username)
				.claim("otpHash", otpHash)
				.claim("tokenType", "PASSWORD_RESET")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 minutes
				.signWith(getKey())
				.compact();
	}

	public String generateResetAuthToken(String username) {
		return Jwts.builder()
				.setSubject(username)
				.claim("tokenType", "RESET_AUTH")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 2 * 60 * 1000))
				.signWith(getKey(), SignatureAlgorithm.HS256)
				.compact();
	}
}
