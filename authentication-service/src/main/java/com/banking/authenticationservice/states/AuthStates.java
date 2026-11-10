package com.banking.authenticationservice.states;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.banking.authenticationservice.entity.Customer;
import com.banking.authenticationservice.entity.CustomerStatus;

public interface AuthStates {

    void validateForRegistration(Customer customer);

    void validateForAuthentication(Customer customer);

    default CustomerStatus activate(Customer customer) {
        return updateStatus(customer, CustomerStatus.ACTIVE);
    }

    default CustomerStatus deactivate(Customer customer) {
        return updateStatus(customer, CustomerStatus.DEACTIVATED);
    }

    default CustomerStatus delete(Customer customer) {
        return updateStatus(customer, CustomerStatus.DELETED);
    }

    static CustomerStatus updateStatus(Customer customer, CustomerStatus nextStatus) {
        Objects.requireNonNull(customer, "Customer must not be null");
        customer.setStatus(nextStatus);
        return nextStatus;
    }

    final class Context {
        private final Customer customer;
        private final AuthStates state;

        public Context(Customer customer) {
            this.customer = Objects.requireNonNull(customer, "Customer must not be null");
            this.state = resolve(customer.getStatus());
        }

        public static Context from(Customer customer) {
            return new Context(customer);
        }

        public void validateForRegistration() {
            state.validateForRegistration(customer);
        }

        public void validateForAuthentication() {
            state.validateForAuthentication(customer);
        }

        public CustomerStatus activate() {
            return state.activate(customer);
        }

        public CustomerStatus deactivate() {
            return state.deactivate(customer);
        }

        public CustomerStatus delete() {
            return state.delete(customer);
        }

        private static AuthStates resolve(CustomerStatus status) {
            CustomerStatus resolvedStatus = status == null ? CustomerStatus.ACTIVE : status;
            return switch (resolvedStatus) {
                case ACTIVE -> new Active();
                case DEACTIVATED -> new Deactivated();
                case DELETED -> new Deleted();
            };
        }
    }

    final class Active implements AuthStates {
        @Override
        public void validateForRegistration(Customer customer) {
            Objects.requireNonNull(customer, "Customer must not be null");
        }

        @Override
        public void validateForAuthentication(Customer customer) {
            Objects.requireNonNull(customer, "Customer must not be null");
        }

        @Override
        public CustomerStatus deactivate(Customer customer) {
            return AuthStates.updateStatus(customer, CustomerStatus.DEACTIVATED);
        }

        @Override
        public CustomerStatus delete(Customer customer) {
            return AuthStates.updateStatus(customer, CustomerStatus.DELETED);
        }
    }

    final class Deactivated implements AuthStates {
        @Override
        public void validateForRegistration(Customer customer) {
            Objects.requireNonNull(customer, "Customer must not be null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Customer account is deactivated. Please contact support.");
        }

        @Override
        public void validateForAuthentication(Customer customer) {
            Objects.requireNonNull(customer, "Customer must not be null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Customer account is deactivated. Please contact support.");
        }

        @Override
        public CustomerStatus activate(Customer customer) {
            return AuthStates.updateStatus(customer, CustomerStatus.ACTIVE);
        }

        @Override
        public CustomerStatus delete(Customer customer) {
            return AuthStates.updateStatus(customer, CustomerStatus.DELETED);
        }
    }

    final class Deleted implements AuthStates {
        @Override
        public void validateForRegistration(Customer customer) {
            Objects.requireNonNull(customer, "Customer must not be null");
            throw new ResponseStatusException(HttpStatus.GONE,
                    "Customer account was deleted and cannot be re-used.");
        }

        @Override
        public void validateForAuthentication(Customer customer) {
            Objects.requireNonNull(customer, "Customer must not be null");
            throw new ResponseStatusException(HttpStatus.GONE,
                    "Customer account was deleted and cannot be used to sign in.");
        }

        @Override
        public CustomerStatus activate(Customer customer) {
            throw new ResponseStatusException(HttpStatus.GONE,
                    "Deleted customer accounts cannot be re-activated.");
        }

        @Override
        public CustomerStatus deactivate(Customer customer) {
            throw new ResponseStatusException(HttpStatus.GONE,
                    "Deleted customer accounts cannot be deactivated again.");
        }
    }
}
