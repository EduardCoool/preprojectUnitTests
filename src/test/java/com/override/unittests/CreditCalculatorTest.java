package com.override.unittests;

import com.override.unittests.enums.ClientType;
import com.override.unittests.exception.CannotBePayedException;
import com.override.unittests.exception.CentralBankNotRespondingException;
import com.override.unittests.service.CentralBankService;
import com.override.unittests.service.CreditCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCalculatorTest {

    @InjectMocks
    private CreditCalculator creditCalculator;

    @Mock
    private CentralBankService centralBankService;

    @ParameterizedTest
    @CsvSource({
            "100000d, 10000d, GOVERMENT, 10000d",
            "100000d, 10000d, BUSINESS, 11000d",
            "100000d, 10000d, INDIVIDUAL, 12000d",
    })

    public void calculateOverpaymentByClientType
            (double amount, double monthPaymentAmount, ClientType clientType, double expected) {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, clientType);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void calculateOverpaymentOnTooBigAmountTest() {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 1000000000d;
        double monthPaymentAmount = 10000d;
        assertThrows(CannotBePayedException.class, ()
                -> creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.GOVERMENT));
    }

    @Test
    public void calculateOverpaymentOnManyYearCreditTest() {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 1000000d;
        double monthPaymentAmount = 10000;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.INDIVIDUAL);
        Assertions.assertEquals(120000, result);
    }

    @Test
    public void calculateOverpaymentWhenNoConnectionTest() {
        when(centralBankService.getKeyRate()).thenThrow(new CentralBankNotRespondingException());
        double amount = 100000d;
        double monthPaymentAmount = 10000d;
        assertDoesNotThrow(()
                -> creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.GOVERMENT));
    }
}
