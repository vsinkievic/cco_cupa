package lt.creditco.cupa.base.users;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import com.bpmid.vapp.repository.UserRepository;

public interface CupaUserRepository extends UserRepository{
    @Query("SELECT u FROM CupaUser u WHERE u.merchantIds LIKE %:merchantId%")
    List<CupaUser> findByMerchantId(String merchantId);
    
    @Query("SELECT u FROM CupaUser u WHERE u.login = :login")
    Optional<CupaUser> findCupaUserByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE, unless = "#result == null")
    Optional<CupaUser> findOneWithAuthoritiesByLoginAndMerchantIds(String login, String merchantIds);

}
