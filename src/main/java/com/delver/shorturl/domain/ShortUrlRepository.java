package com.delver.shorturl.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    // Compare cached URLs byte for byte even with a case-insensitive MySQL collation.
    @Modifying
    @Query("""
            update ShortUrl link
            set link.accessCount = link.accessCount + 1,
                link.lastAccessedAt = case
                    when link.lastAccessedAt is null or link.lastAccessedAt < :accessedAt
                    then :accessedAt else link.lastAccessedAt end
            where link.shortCode = :shortCode
                and (:expectedUrl is null or cast(link.originalUrl as Binary) = cast(:expectedUrl as Binary))
            """)
    int recordAccess(@Param("shortCode") String shortCode,
                     @Param("expectedUrl") @Nullable String expectedUrl,
                     @Param("accessedAt") LocalDateTime accessedAt);

}
