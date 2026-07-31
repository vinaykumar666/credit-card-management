package com.cards.payment.config;

import com.cards.payment.domain.Beneficiary;
import com.cards.payment.domain.BeneficiaryStatus;
import com.cards.payment.domain.BeneficiaryType;
import com.cards.payment.repository.BeneficiaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Seeds demo beneficiaries for Ada and Ben (see docs/USERS.md and docs/BANKING_FEATURES.md).
 */
@Component
@Profile("!prod")
public class BeneficiaryDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BeneficiaryDataInitializer.class);

    public static final UUID ADA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID BEN_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    public static final UUID ADA_PERSON = UUID.fromString("d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1");
    public static final UUID ADA_MERCHANT = UUID.fromString("d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2");
    public static final UUID BEN_PERSON = UUID.fromString("d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3");

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryDataInitializer(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed(ADA_PERSON, ADA_ID, "Mom Home", "Sara Lovelace", "998877665544",
                "First National Bank", "FNBK0001234", BeneficiaryType.PERSON);
        seed(ADA_MERCHANT, ADA_ID, "Electric Co", "Pacific Electric Utility", "UTIL99887766",
                "Utility Clearing Bank", "UTIL0009876", BeneficiaryType.MERCHANT);
        seed(BEN_PERSON, BEN_ID, "Workshop Rent", "Liberty Properties LLC", "112233445566",
                "Liberty Trust", "LBTY0005555", BeneficiaryType.MERCHANT);
        log.info("Beneficiary seed data ready");
    }

    private void seed(UUID id, UUID userId, String nickname, String name, String account,
                      String bank, String ifsc, BeneficiaryType type) {
        if (beneficiaryRepository.existsById(id)
                || beneficiaryRepository.existsByUserIdAndAccountNumberIgnoreCase(userId, account)) {
            return;
        }
        beneficiaryRepository.save(Beneficiary.builder()
                .id(id)
                .userId(userId)
                .nickname(nickname)
                .beneficiaryName(name)
                .accountNumber(account)
                .bankName(bank)
                .ifscOrRouting(ifsc)
                .beneficiaryType(type)
                .status(BeneficiaryStatus.ACTIVE)
                .build());
    }
}
