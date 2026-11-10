package com.banking.authenticationservice.states;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.entity.CustomerStatus;

class AuthStatesTest {

    @Test
    void activeCustomerAllowsRegistrationAndAuthentication() {
        Customer customer = new Customer();
        customer.setStatus(CustomerStatus.ACTIVE);

        assertDoesNotThrow(() -> AuthStates.Context.from(customer).validateForRegistration());
        assertDoesNotThrow(() -> AuthStates.Context.from(customer).validateForAuthentication());
    }

    @Test
    void deactivatedAndDeletedCustomersAreBlocked() {
        Customer deactivated = new Customer();
        deactivated.setStatus(CustomerStatus.DEACTIVATED);

        Customer deleted = new Customer();
        deleted.setStatus(CustomerStatus.DELETED);

        assertThrows(ResponseStatusException.class,
                () -> AuthStates.Context.from(deactivated).validateForAuthentication());
        assertThrows(ResponseStatusException.class,
                () -> AuthStates.Context.from(deleted).validateForAuthentication());
    }

    @Test
    void stateTransitionsUpdateCustomerStatusSafely() {
        Customer active = new Customer();
        active.setStatus(CustomerStatus.ACTIVE);

        assertEquals(CustomerStatus.DEACTIVATED,
                AuthStates.Context.from(active).deactivate());
        assertEquals(CustomerStatus.ACTIVE,
                AuthStates.Context.from(active).activate());

        Customer deleted = new Customer();
        deleted.setStatus(CustomerStatus.DELETED);
        assertThrows(ResponseStatusException.class,
                () -> AuthStates.Context.from(deleted).activate());
    }
}
