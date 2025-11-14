package lt.creditco.cupa.service;

import lt.creditco.cupa.base.users.CupaUser;
import com.bpmid.vapp.domain.User;
import com.bpmid.vapp.repository.AuthorityRepository;
import com.bpmid.vapp.repository.UserRepository;
import com.bpmid.vapp.service.LocaleService;
import com.bpmid.vapp.service.MailService;

import lt.creditco.cupa.repository.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing users.
 */
@Service
@Transactional
@Primary
public class CupaUserService extends com.bpmid.vapp.service.UserService{

    private static final Logger LOG = LoggerFactory.getLogger(CupaUserService.class);

    private final MerchantRepository merchantRepository;

    public CupaUserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        CacheManager cacheManager,
        MailService mailService,
        LocaleService localeService,
        MerchantRepository merchantRepository
    ) {
        super(userRepository, passwordEncoder, authorityRepository, cacheManager, mailService, localeService);
        this.merchantRepository = merchantRepository;
    }


    @Override
    public User createUser(User user) {

        if (user instanceof CupaUser cupaUser) {
            // Validate merchant IDs before creating user
            validateMerchantIds(cupaUser.getMerchantIds());
        }

        User savedUser = super.createUser(user);

        LOG.debug("Created Information for User: {}", savedUser);
        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        if (user instanceof CupaUser cupaUser) {
            // Validate merchant IDs before updating user
            validateMerchantIds(cupaUser.getMerchantIds());
        }
        return super.updateUser(user);
    }

    /**
     * Validates merchant IDs to ensure they exist in the system.
     * @param merchantIds comma-separated list of merchant IDs
     * @throws InvalidMerchantIdsException if any merchant ID is invalid
     */
    private void validateMerchantIds(String merchantIds) {
        if (merchantIds == null || merchantIds.trim().isEmpty()) {
            return; // null/empty is valid
        }

        String[] merchantIdArray = merchantIds.split(",");
        for (String merchantId : merchantIdArray) {
            String trimmedId = merchantId.trim();
            if (!trimmedId.isEmpty() && !merchantRepository.existsById(trimmedId)) {
                throw new InvalidMerchantIdsException("Merchant ID '" + trimmedId + "' does not exist in the system");
            }
        }
    }

}
